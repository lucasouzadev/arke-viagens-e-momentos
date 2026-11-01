import SwiftUI
import MapKit

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @SceneStorage("home.context") private var contextValue = TravelContext.planning.rawValue
    @SceneStorage("home.detent") private var detentValue = PanelDetent.balanced.rawValue
    @GestureState private var panelTranslation: CGFloat = 0
    @State private var showingAddPlace = false
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
            Text("Arke").font(.title2.bold())
            Text("Onde vamos?").font(.largeTitle.bold())
            GeometryReader { geometry in
                VStack(spacing: 8) {
                    Map(initialPosition: .region(MKCoordinateRegion(
                        center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
                        span: MKCoordinateSpan(latitudeDelta: 120, longitudeDelta: 160)
                    ))) {}
                        .mapStyle(.standard)
                        .accessibilityLabel("Mapa Apple. Explore o mundo; localização pessoal desativada.")
                        .frame(height: clampedMapHeight(mapHeight(in: geometry.size.height) + panelTranslation, in: geometry.size.height))
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .contentShape(RoundedRectangle(cornerRadius: 20))

                    panelMenu
                        .highPriorityGesture(panelDrag(in: geometry.size.height))
                        .zIndex(1)

                    VStack(spacing: 12) {
                        cardHeader
                        Divider()
                        ScrollView {
                            VStack(alignment: .leading, spacing: 12) {
                                Text(state.context == .planning ? "Sua próxima viagem começa com uma ideia" : state.context.title)
                                    .font(.title2.bold())
                                    .accessibilityAddTraits(.isHeader)
                                Text(state.context == .planning
                                     ? "Em breve, você poderá guardar lugares antes mesmo de criar uma viagem."
                                     : "Este espaço está em desenvolvimento.")
                                Text("Versão de desenvolvimento · nenhum dado salvo")
                                    .font(.footnote)
                                    .foregroundStyle(.secondary)
                                    .padding(.top, 12)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .frame(maxHeight: .infinity)
                        .accessibilityIdentifier("home.contentBody")
                    }
                    .padding(20)
                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20))
                }
            }
        }
        .padding(16)
        .background(Color(uiColor: .systemGroupedBackground))
        .alert("Adicionar lugar", isPresented: $showingAddPlace) {
            Button("Entendi", role: .cancel) {}
        } message: {
            Text("O salvamento de lugares ainda está em desenvolvimento. Em breve, você poderá marcar um ponto no mapa e guardar sua ideia de viagem.")
        }
    }

    private var cardHeader: some View {
        HStack(spacing: 8) {
            Button { showingAddPlace = true } label: {
                Image(systemName: "plus")
                    .font(.title3.weight(.medium))
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("Adicionar lugar")
            .accessibilityIdentifier("home.addPlace")

            Text(state.context.title)
                .font(.headline)
                .foregroundStyle(Color(uiColor: .label))
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity)
                .accessibilityAddTraits(.isHeader)
                .accessibilityIdentifier("home.contextTitle")

            Button { showingContextPicker = true } label: {
                Image(systemName: "clock")
                    .font(.title3)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("Trocar contexto")
            .accessibilityValue(state.context.title)
            .accessibilityIdentifier("home.contextMenu")
            .confirmationDialog("Contexto atual: \(state.context.title)", isPresented: $showingContextPicker, titleVisibility: .visible) {
                ForEach(TravelContext.allCases, id: \.self) { context in
                    Button(context.title + (context == state.context ? " ✓" : "")) {
                        var next = state
                        next.selectContext(context)
                        contextValue = next.context.rawValue
                    }
                }
                Button("Cancelar", role: .cancel) {}
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
                Text(state.detent.title).font(.caption)
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
            .updating($panelTranslation) { value, translation, _ in
                translation = value.translation.height
            }
            .onEnded { value in
                let target = clampedMapHeight(mapHeight(in: height) + value.translation.height, in: height)
                let closest = PanelDetent.allCases.min {
                    abs(mapHeight(for: $0, in: height) - target) < abs(mapHeight(for: $1, in: height) - target)
                }
                if let closest { detentValue = closest.rawValue }
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
