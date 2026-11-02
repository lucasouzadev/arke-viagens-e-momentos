import SwiftUI
import MapKit
import CoreLocation

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @SceneStorage("home.context") private var contextValue = TravelContext.planning.rawValue
    @SceneStorage("home.detent") private var detentValue = PanelDetent.balanced.rawValue
    @StateObject private var location = ForegroundLocation()
    @State private var camera: MapCameraPosition = .region(MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 0, longitude: 0), span: MKCoordinateSpan(latitudeDelta: 120, longitudeDelta: 160)))
    @State private var panelHeight: CGFloat?
    @State private var panelOrigin: CGFloat?
    @State private var showingAddPlace = false
    @State private var showingDetailPreview = false
    @State private var showingEditPreview = false
    @State private var showingSavedDetails = false
    @State private var savedNotice = false
    @StateObject private var places = PlaceIdeasStore()
    @State private var showingContextPicker = false
    @State private var showingPanelPicker = false
    @ScaledMetric(relativeTo: .headline) private var minimumContentHeight: CGFloat = 160

    private var state: HomeState {
        HomeState(
            context: TravelContext(rawValue: contextValue) ?? .planning,
            detent: PanelDetent(rawValue: detentValue) ?? .balanced
        )
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Arke").arkeText(.appTitle)
            Text("Onde vamos?").arkeText(.screenTitle)
            GeometryReader { geometry in
                VStack(spacing: 8) {
                    Map(position: $camera) { UserAnnotation() }
                        .mapStyle(.standard)
                        .accessibilityLabel("Mapa Apple. Explore o mundo ou use Minha localização.")
                        .frame(height: geometry.size.height)
                        .frame(height: panelHeight ?? mapHeight(in: geometry.size.height), alignment: .center).clipped()
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .contentShape(RoundedRectangle(cornerRadius: 20))
                        .overlay(alignment: .bottomTrailing) {
                            Button { location.request() } label: {
                                Image(systemName: "location.fill").frame(width: 48, height: 48)
                                    .background(.regularMaterial, in: Circle())
                            }.padding(12).accessibilityLabel("Minha localização")
                        }
                        .onReceive(location.$coordinate) { coordinate in
                            if let coordinate { camera = .region(MKCoordinateRegion(center: coordinate, span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04))) }
                        }

                    panelMenu
                        .highPriorityGesture(panelDrag(in: geometry.size.height))
                        .zIndex(1)

                    VStack(spacing: 12) {
                        cardHeader
                        Divider()
                        ScrollView {
                            VStack(alignment: .leading, spacing: 12) {
                                if savedNotice { Text("Lugar salvo neste aparelho.").arkeText(.caption).accessibilityIdentifier("place.savedNotice") }
                                if let error = places.error, !showingAddPlace {
                                    Text(error).foregroundStyle(Color(uiColor: .systemRed))
                                    Button("Tentar novamente") { Task { await places.load() } }
                                }
                                if places.ready {
                                    if state.context == .planning {
                                        PlaceIdeasContent(store: places, onDelete: { id in
                                            savedNotice = false
                                            Task { await places.remove(id: id) }
                                        }, onDetailsVisibilityChanged: { showingSavedDetails = $0 })
                                    } else {
                                        Text(state.context.title).arkeText(.cardTitle)
                                        Text("Este espaço está em desenvolvimento.")
                                        Text("Seus lugares estão em Planejamento.")
                                        Button("Ver meus lugares") { contextValue = TravelContext.planning.rawValue }
                                    }
                                } else if places.busy { ProgressView("Abrindo seus lugares…") }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .frame(maxHeight: .infinity).arkeScrollEdges()
                        .accessibilityIdentifier("home.contentBody")
                    }
                    .padding(20)
                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20))
                }
            }
        }
        .alert("Minha localização", isPresented: Binding(get: { location.message != nil }, set: { if !$0 { location.message = nil } })) {
            Button("Entendi", role: .cancel) { location.message = nil }
        } message: { Text(location.message ?? "") }
        .arkeText(.body)
        .padding(16)
        .background(Color(uiColor: .systemGroupedBackground))
        .arkeReceding(presented: showingAddPlace || showingDetailPreview || showingSavedDetails || showingEditPreview)
        .task {
            await places.load()
            #if DEBUG
            if places.ready && ProcessInfo.processInfo.arguments.contains("--capture-place-editor") { showingAddPlace = true }
            if ProcessInfo.processInfo.arguments.contains("--capture-place-details") { showingDetailPreview = true }
            if ProcessInfo.processInfo.arguments.contains("--capture-place-edit") { showingEditPreview = true }
            #endif
        }
        #if DEBUG
        .sheet(isPresented: $showingDetailPreview) {
            PlaceDetails(ideas: DetailCapture.ideas, initialID: DetailCapture.ideas[ProcessInfo.processInfo.arguments.contains("--capture-short-detail") ? 1 : 0].id, onClose: { showingDetailPreview = false })
        }
        .sheet(isPresented: $showingEditPreview) {
            PlaceEditor(initialIdea: DetailCapture.ideas[0], saving: false, saveError: nil,
                onSave: { _ in }, onCancel: { showingEditPreview = false })
        }
        #endif
        .sheet(isPresented: $showingAddPlace) {
            PlaceEditor(saving: places.busy, saveError: places.error, onSave: { draft in
                Task {
                    if await places.add(draft) { showingAddPlace = false; savedNotice = true }
                }
            }, onCancel: { showingAddPlace = false })
        }

    }

    private var cardHeader: some View {
        HStack(spacing: 8) {
            Button { showingAddPlace = true } label: {
                Image(systemName: "plus")
                    .font(.system(size: 24, weight: .medium))
                    .frame(width: 48, height: 48)
                    .contentShape(Rectangle())
            }
            .disabled(!places.ready || places.busy)
            .accessibilityLabel("Adicionar lugar")
            .accessibilityIdentifier("home.addPlace")

            Text(state.context.title)
                .arkeText(.contextTitle)
                .foregroundStyle(Color(uiColor: .label))
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity)
                .accessibilityAddTraits(.isHeader)
                .accessibilityIdentifier("home.contextTitle")

            Button { showingContextPicker = true } label: {
                Image(systemName: "clock")
                    .font(.system(size: 24))
                    .frame(width: 48, height: 48)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("Trocar contexto")
            .accessibilityValue(state.context.title)
            .accessibilityIdentifier("home.contextMenu")
            .sheet(isPresented: $showingContextPicker) {
                VStack(spacing: 12) {
                    Text("Escolha o contexto").arkeText(.contextTitle).padding(.top, 20)
                    Picker("Contexto", selection: $contextValue) {
                        ForEach(TravelContext.allCases, id: \.self) { context in
                            Text(context.title).tag(context.rawValue).accessibilityIdentifier("context." + context.rawValue)
                        }
                    }.pickerStyle(.wheel).accessibilityIdentifier("home.contextWheel")
                    Button("Concluído") { showingContextPicker = false }.padding(.bottom, 20)
                }.presentationDetents([.height(320)]).presentationDragIndicator(.visible)
            }
        }
        .buttonStyle(.plain)
        .foregroundStyle(controlStyle)
    }

    private var panelMenu: some View {
        Button { showingPanelPicker = true } label: {
            VStack(spacing: 4) {
                Capsule().fill(.secondary).frame(width: 36, height: 4)
                    .accessibilityHidden(true)
                Text(state.detent.title).arkeText(.label)
            }
                .frame(maxWidth: .infinity, minHeight: 44)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .foregroundStyle(controlStyle)
        .accessibilityLabel("Ajustar proporção")
        .accessibilityValue(state.detent.title)
        .accessibilityIdentifier("home.panelMenu")
        .accessibilityHint("Arraste para ajustar ou toque para escolher a proporção.")
        .accessibilityAdjustableAction { direction in
            switch direction {
            case .increment: movePanel(towardMap: true)
            case .decrement: movePanel(towardMap: false)
            @unknown default: break
            }
        }
        .accessibilityAction(named: "Mostrar mais mapa") { movePanel(towardMap: true) }
        .accessibilityAction(named: "Expandir conteúdo") { movePanel(towardMap: false) }
        .confirmationDialog("Proporção atual: \(state.detent.title)", isPresented: $showingPanelPicker, titleVisibility: .visible) {
            ForEach(PanelDetent.allCases, id: \.self) { detent in
                Button(detent.title + (detent == state.detent ? " ✓" : "")) {
                    var next = state
                    next.setDetent(detent)
                    detentValue = next.detent.rawValue
                }
            }
            Button("Cancelar", role: .cancel) {}
        }
    }

    private var controlStyle: AnyShapeStyle {
        colorScheme == .dark ? AnyShapeStyle(Color(uiColor: .label)) : AnyShapeStyle(.tint)
    }

    private func panelDrag(in height: CGFloat) -> some Gesture {
        DragGesture(minimumDistance: 10, coordinateSpace: .global)
            .onChanged { value in
                if panelOrigin == nil { panelOrigin = mapHeight(in: height) }
                panelHeight = clampedMapHeight((panelOrigin ?? mapHeight(in: height)) + value.translation.height, in: height)
            }
            .onEnded { value in
                let target = clampedMapHeight((panelOrigin ?? mapHeight(in: height)) + value.translation.height, in: height)
                let closest = PanelDetent.allCases.min {
                    abs(mapHeight(for: $0, in: height) - target) < abs(mapHeight(for: $1, in: height) - target)
                }
                var transaction = Transaction(); transaction.disablesAnimations = true
                withTransaction(transaction) {
                    if let closest { detentValue = closest.rawValue }
                    panelHeight = nil; panelOrigin = nil
                }
            }
    }

    private func movePanel(towardMap: Bool) {
        let ordered: [PanelDetent] = [.details, .balanced, .explore]
        guard let index = ordered.firstIndex(of: state.detent) else { return }
        detentValue = ordered[min(2, max(0, index + (towardMap ? 1 : -1)))].rawValue
    }

    private func clampedMapHeight(_ value: CGFloat, in height: CGFloat) -> CGFloat {
        max(64, min(value, height - minimumContentHeight - 60))
    }

    private func mapHeight(in height: CGFloat) -> CGFloat {
        mapHeight(for: state.detent, in: height)
    }

    private func mapHeight(for detent: PanelDetent, in height: CGFloat) -> CGFloat {
        let fraction: CGFloat
        switch detent {
        case .explore: fraction = 0.65
        case .balanced: fraction = 0.45
        case .details: fraction = 0.25
        }
        return clampedMapHeight(height * fraction, in: height)
    }

}

private extension TravelContext {
    var title: String {
        switch self {
        case .planning: "Planejamento"
        case .expenses: "Gastos"
        case .savings: "Cofrinho"
        case .reviews: "Avaliações"
        case .memories: "Memórias"
        }
    }
}

private extension PanelDetent {
    var title: String {
        switch self {
        case .explore: "Explorar"
        case .balanced: "Equilibrado"
        case .details: "Detalhes"
        }
    }
}

@MainActor
private final class ForegroundLocation: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var coordinate: CLLocationCoordinate2D?
    @Published var message: String?
    private let manager = CLLocationManager()
    private var requested = false
    override init() { super.init(); manager.delegate = self; manager.desiredAccuracy = kCLLocationAccuracyHundredMeters }
    func request() {
        requested = true
        switch manager.authorizationStatus {
        case .notDetermined: manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways: manager.requestLocation()
        case .denied, .restricted: message = "Permita a localização nos Ajustes para mostrar sua posição. Você pode continuar explorando o mapa."
        @unknown default: message = "Localização indisponível. Você pode continuar explorando o mapa."
        }
    }
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard requested else { return }
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways { manager.requestLocation() }
    }
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        coordinate = locations.last?.coordinate; requested = false
    }
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        message = "Não foi possível obter sua posição agora. Tente novamente."; requested = false
    }
}
