import SwiftUI
import MapKit
import CoreLocation

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @SceneStorage("home.context") private var contextValue = TravelContext.planning.rawValue
    @SceneStorage("home.detent") private var detentValue = PanelDetent.balanced.rawValue
    @StateObject private var location = ForegroundLocation()
    @State private var camera: MapCameraPosition = .region(MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 0, longitude: 0), span: MKCoordinateSpan(latitudeDelta: 120, longitudeDelta: 160)))
    @State private var showingTrips = false
    @State private var pinPlan: LocalPinPlan?
    @State private var pinIdea: PlaceIdea?
    @State private var focusedIdeaID: String?
    @State private var mapExpanded = false
    @State private var panelHeight: CGFloat?
    @State private var panelOrigin: CGFloat?
    @Namespace private var mapSearchTransition
    @State private var showingMapSearch = false
    @State private var searchPin: MKMapItem?
    @State private var showingAddPlace = false
    @State private var showingDetailPreview = false
    @State private var showingEditPreview = false
    @State private var showingSavedDetails = false
    @State private var savedNotice = false
    @StateObject private var places = PlaceIdeasStore()
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
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
                let restingHeight = panelHeight ?? mapHeight(in: geometry.size.height)
                let expandedHeight = min(geometry.size.height, max(restingHeight, geometry.size.height - 52))
                VStack(spacing: 8) {
                    Color.clear.frame(height: restingHeight).accessibilityHidden(true)

                    panelMenu
                        .highPriorityGesture(panelDrag(in: geometry.size.height))
                        .opacity(mapExpanded ? 0 : 1)
                        .allowsHitTesting(!mapExpanded)
                        .accessibilityHidden(mapExpanded)
                        .zIndex(1)

                    VStack(spacing: 12) {
                        cardHeader
                        Divider()
                        ScrollView {
                            VStack(alignment: .leading, spacing: 12) {
                            if state.context == .planning {
                                Button { showingTrips = true } label: { Label("Viagens", systemImage: "suitcase") }
                            }
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
                                        }, onPin: { pinIdea = $0 }, onDetailsVisibilityChanged: { showingSavedDetails = $0 }, focusedID: $focusedIdeaID)
                                    } else {
                                        Text("Em breve.")
                                        Button("Ver meus lugares") { contextValue = TravelContext.planning.rawValue }
                                    }
                                } else if places.busy { ProgressView("Abrindo seus lugares…") }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .frame(maxHeight: .infinity)
                        .accessibilityIdentifier("home.contentBody")
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20))
                    .clipShape(.rect(cornerRadius: 20))
                    .offset(y: mapExpanded ? 12 : 0)
                    .allowsHitTesting(!mapExpanded)
                    .accessibilityHidden(mapExpanded)
                }
                .overlay(alignment: .top) {
                    Map(position: $camera) {
                        UserAnnotation()
                        ForEach(pinPlan?.pins ?? [], id: \.ideaID) { pin in
                            if let idea = places.ideas.first(where: { $0.id == pin.ideaID }) {
                                Marker(idea.name, coordinate: CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude))
                            }
                        }
                        if let searchPin { Marker(item: searchPin) }
                    }
                        .mapStyle(.standard)
                        .accessibilityLabel("Mapa Apple. Explore o mundo ou use Minha localização.")
                        .frame(height: mapExpanded ? expandedHeight : restingHeight)
                        .clipShape(.rect(cornerRadius: 30))
                        .overlay {
                            RoundedRectangle(cornerRadius: 30)
                                .strokeBorder(.white.opacity(0.44), lineWidth: 1)
                                .shadow(color: .white.opacity(0.75), radius: 7)
                                .allowsHitTesting(false)
                        }
                        .overlay(alignment: .bottom) {
                            HStack(spacing: 6) {
                                Button { showingMapSearch = true } label: {
                                    Label("Buscar lugares", systemImage: "magnifyingglass")
                                        .font(.subheadline).frame(maxWidth: .infinity, minHeight: 34)
                                }
                                .modifier(MapLocationButtonStyle(isPill: true))
                                .modifier(MapSearchMotion(namespace: mapSearchTransition, isSource: true))
                                Button { location.request() } label: {
                                    Image(systemName: "location.fill").frame(width: 36, height: 36)
                                }
                                .modifier(MapLocationButtonStyle())
                                .accessibilityLabel("Minha localização")
                                .accessibilityIdentifier("home.location")
                                .accessibilityValue(location.coordinate == nil ? "" : "Localização disponível")
                            }
                            .padding(.horizontal, 12).padding(.bottom, 28)
                        }
                        .overlay(alignment: .topLeading) {
                            if state.context == .planning,
                               let idea = places.ideas.first(where: { $0.id == focusedIdeaID }) {
                                Label(idea.name, systemImage: "bookmark")
                                    .font(.subheadline.weight(.semibold)).lineLimit(1)
                                    .padding(10).background(.regularMaterial, in: Capsule())
                                    .padding(.leading, 12).padding(.trailing, 68).padding(.top, 18)
                                    .accessibilityLabel("Ideia selecionada: \(idea.name)")
                                    .allowsHitTesting(false)
                            }
                        }
                        .overlay(alignment: .topTrailing) {
                            Button {
                                withAnimation(reduceMotion ? nil : .smooth(duration: 0.28)) {
                                    mapExpanded.toggle()
                                }
                            } label: {
                                Image(systemName: mapExpanded ? "arrow.down.right.and.arrow.up.left" : "arrow.up.left.and.arrow.down.right")
                                    .frame(width: 44, height: 44)
                            }
                            .modifier(MapLocationButtonStyle())
                            .accessibilityLabel(mapExpanded ? "Recolher mapa" : "Expandir mapa")
                            .accessibilityIdentifier("home.expandMap")
                            .padding(12)
                        }
                        .onReceive(location.$coordinate) { coordinate in
                            if let coordinate { camera = .region(MKCoordinateRegion(center: coordinate, span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04))) }
                        }
                }
                .clipped()
            }
        }
        .alert("Minha localização", isPresented: Binding(get: { location.message != nil }, set: { if !$0 { location.message = nil } })) {
            Button("Entendi", role: .cancel) { location.message = nil }
        } message: { Text(location.message ?? "") }
        .arkeText(.body)
        .padding(16)
        .background(Color(uiColor: .systemGroupedBackground))
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .arkeReceding(presented: showingAddPlace || showingDetailPreview || showingSavedDetails || showingEditPreview)
        .task(id: savedNotice) {
            if savedNotice {
                do { try await Task.sleep(nanoseconds: 4_000_000_000); savedNotice = false } catch { }
            }
        }
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
        .sheet(isPresented: $showingMapSearch) {
            MapPlaceSearch { item in
                searchPin = item
                camera = .region(MKCoordinateRegion(center: item.placemark.coordinate,
                    span: MKCoordinateSpan(latitudeDelta: 0.035, longitudeDelta: 0.035)))
                showingMapSearch = false
            }
            .modifier(MapSearchMotion(namespace: mapSearchTransition, isSource: false))
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
        .task { pinPlan = try? await places.loadPinPlan() }
        .sheet(isPresented: $showingTrips) {
            NavigationStack {
                List {
                    if let plan = pinPlan {
                        if plan.trips.isEmpty { Text("Suas viagens aparecem aqui quando você vincula um Pin.") }
                        ForEach(plan.trips, id: \.id) { trip in
                            NavigationLink(trip.title) {
                                List {
                                    let ideas = places.ideas.filter { idea in
                                        plan.pins.contains { $0.ideaID == idea.id && $0.tripID == trip.id }
                                    }
                                    if ideas.isEmpty { Text("Nenhum Pin nesta viagem.") }
                                    ForEach(ideas) { idea in
                                        VStack(alignment: .leading) {
                                            Text(idea.name).font(.headline)
                                            Text(idea.address).foregroundStyle(.secondary)
                                            if let pin = plan.pins.first(where: { $0.ideaID == idea.id }) {
                                                NavigationLink("Compartilhar Pin") { SharePinForm(name: idea.name, pin: pin) }
                                            }
                                        }
                                    }
                                }.navigationTitle(trip.title)
                            }
                        }
                    } else { Text("Não foi possível abrir as viagens.") }
                }
                .navigationTitle("Viagens")
                .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Fechar") { showingTrips = false } } }
            }
        }
        .sheet(item: $pinIdea) { idea in
            PinEditor(idea: idea, places: places) { plan in
                pinPlan = plan
                if let pin = plan.pins.first(where: { $0.ideaID == idea.id }) {
                    camera = .region(MKCoordinateRegion(
                        center: CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude),
                        span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)))
                }
                pinIdea = nil
            }
        }
        .sheet(isPresented: $showingAddPlace) {
            PlaceEditor(saving: places.busy, saveError: places.error, onSave: { draft in
                Task {
                    if await places.add(draft) { showingAddPlace = false; savedNotice = true }
                }
            }, onCancel: { showingAddPlace = false })
        }

    }

    private var cardHeader: some View {
        ZStack(alignment: .leading) {
            let options = TravelContext.allCases
            let index = options.firstIndex(of: state.context) ?? 0
            HStack(spacing: 4) {
                contextPreview(index - 1)
                Text(state.context.title)
                    .arkeText(.contextTitle)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity, minHeight: 48)
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("home.contextTitle")
                contextPreview(index + 1)
            }
            .contentShape(Rectangle())
            .simultaneousGesture(DragGesture(minimumDistance: 24).onEnded { value in
                guard abs(value.translation.width) > abs(value.translation.height) else { return }
                selectContext(index + (value.translation.width < 0 ? 1 : -1))
            })
            .accessibilityAdjustableAction { direction in
                switch direction {
                case .increment: selectContext(index + 1)
                case .decrement: selectContext(index - 1)
                @unknown default: break
                }
            }
            Button { showingAddPlace = true } label: {
                Image(systemName: "plus")
                    .font(.system(size: 24, weight: .medium))
                    .frame(width: 48, height: 48)
                    .background(ArkeUI.insetSurface(colorScheme), in: Circle())
            }
            .disabled(!places.ready || places.busy)
            .accessibilityLabel("Adicionar lugar")
            .accessibilityIdentifier("home.addPlace")

        }
        .buttonStyle(.plain)
        .foregroundStyle(controlStyle)
        .sensoryFeedback(.selection, trigger: contextValue)
    }

    private func selectContext(_ index: Int) {
        guard TravelContext.allCases.indices.contains(index) else { return }
        contextValue = TravelContext.allCases[index].rawValue
    }

    private func contextPreview(_ index: Int) -> some View {
        let options = TravelContext.allCases
        return Text(options.indices.contains(index) ? options[index].title : "")
            .font(.caption.weight(.semibold)).lineLimit(1)
            .frame(width: 56).opacity(0.34).accessibilityHidden(true)
    }

    private func settlePanel(_ detent: PanelDetent) {
        withAnimation(reduceMotion ? nil : .smooth(duration: 0.28)) {
            detentValue = detent.rawValue
            panelHeight = nil
            panelOrigin = nil
        }
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
                    settlePanel(detent)
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
                settlePanel(closest ?? state.detent)
            }
    }

    private func movePanel(towardMap: Bool) {
        let ordered: [PanelDetent] = [.details, .balanced, .explore]
        guard let index = ordered.firstIndex(of: state.detent) else { return }
        settlePanel(ordered[min(2, max(0, index + (towardMap ? 1 : -1)))])
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
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor [weak self] in
            guard let self, self.requested else { return }
            if self.manager.authorizationStatus == .authorizedWhenInUse || self.manager.authorizationStatus == .authorizedAlways { self.manager.requestLocation() }
        }
    }
    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let coordinate = locations.last?.coordinate
        Task { @MainActor [weak self] in self?.coordinate = coordinate; self?.requested = false }
    }
    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor [weak self] in
            self?.message = "Não foi possível obter sua posição agora. Tente novamente."; self?.requested = false
        }
    }
}

private struct MapLocationButtonStyle: ViewModifier {
    var isPill = false
    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content.buttonStyle(.glass).buttonBorderShape(isPill ? .capsule : .circle)
        } else {
            content.background(.regularMaterial, in: Capsule())
        }
    }
}

private struct MapPlaceSearch: View {
    let onSelect: (MKMapItem) -> Void
    @State private var query = ""
    @State private var results: [MKMapItem] = []
    @State private var message: String?

    var body: some View {
        NavigationStack {
            List {
                if let message { Text(message).foregroundStyle(.secondary) }
                ForEach(results.indices, id: \.self) { index in
                    let item = results[index]
                    Button { onSelect(item) } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.name ?? "Lugar")
                            if let address = item.placemark.title {
                                Text(address).font(.caption).foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Buscar lugares")
            .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: "Nome ou endereço")
            .arkeScrollEdges()
        }
        .task(id: query) {
            results = []
            message = nil
            let phrase = query.trimmingCharacters(in: .whitespacesAndNewlines)
            guard phrase.count >= 2 else { return }
            do {
                try await Task.sleep(for: .milliseconds(350))
                let request = MKLocalSearch.Request()
                request.naturalLanguageQuery = phrase
                let response = try await MKLocalSearch(request: request).start()
                try Task.checkCancellation()
                results = response.mapItems
                if results.isEmpty { message = "Nenhum lugar encontrado." }
            } catch {
                if !Task.isCancelled { message = "Não foi possível buscar. Tente novamente." }
            }
        }
    }
}

private struct MapSearchMotion: ViewModifier {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let namespace: Namespace.ID
    let isSource: Bool

    @ViewBuilder
    func body(content: Content) -> some View {
        if #available(iOS 18, *), !reduceMotion {
            if isSource {
                content.matchedTransitionSource(id: "map.search", in: namespace)
            } else {
                content.navigationTransition(.zoom(sourceID: "map.search", in: namespace))
            }
        } else {
            content
        }
    }
}



private struct SharePinForm: View {
    struct Trip: Decodable, Identifiable { let id: UUID; let title: String }
    struct Session: Decodable { let access_token: String }
    let name: String
    let pin: LocalMapPin
    @State private var email = ""
    @State private var password = ""
    @State private var token: String?
    @State private var trips: [Trip] = []
    @State private var destination: UUID?
    @State private var newTitle = ""
    @State private var busy = false
    @State private var message: String?
    @State private var published = false

    var body: some View {
        Form {
            Text("Compartilhará o nome e as coordenadas de \(name) com os membros da viagem escolhida.")
            if let token {
                Picker("Viagem compartilhada", selection: $destination) {
                    Text("Escolha uma viagem").tag(nil as UUID?)
                    ForEach(trips) { Text($0.title).tag(Optional($0.id)) }
                }
                Button("Confirmar compartilhamento") {
                    guard let destination else { return }
                    perform {
                        _ = try await request("/rest/v1/shared_trip_pins", token: token, body: [
                            "trip_id": destination.uuidString, "title": name,
                            "latitude": pin.latitude, "longitude": pin.longitude
                        ])
                        published = true
                        message = "Pin compartilhado."
                    }
                }.disabled(destination == nil || published)
                if !published {
                    TextField("Nome da nova viagem compartilhada", text: $newTitle)
                    Button("Criar viagem compartilhada") {
                        perform {
                            let data = try await request("/rest/v1/shared_trips", token: token, body: ["title": newTitle], representation: true)
                            let created = try JSONDecoder().decode([Trip].self, from: data)
                            trips.append(contentsOf: created)
                            destination = created.first?.id
                            newTitle = ""
                        }
                    }.disabled(newTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            } else {
                TextField("E-mail", text: $email).keyboardType(.emailAddress).textInputAutocapitalization(.never)
                SecureField("Senha", text: $password)
                Button("Entrar") {
                    perform {
                        defer { password = "" }
                        let auth = try await request("/auth/v1/token?grant_type=password", body: ["email": email, "password": password])
                        let session = try JSONDecoder().decode(Session.self, from: auth)
                        let data = try await request("/rest/v1/shared_trips?select=id,title&order=title", token: session.access_token)
                        trips = try JSONDecoder().decode([Trip].self, from: data)
                        token = session.access_token
                    }
                }.disabled(email.isEmpty || password.isEmpty)
            }
            if let message { Text(message) }
        }.disabled(busy)
            .interactiveDismissDisabled(busy)
            .navigationTitle("Compartilhar Pin")
    }

    private func perform(_ action: @escaping () async throws -> Void) {
        busy = true
        message = nil
        Task {
            defer { busy = false }
            do { try await action() }
            catch { message = "Não foi possível concluir. Confira a conexão e suas permissões." }
        }
    }

    private func request(_ path: String, token: String? = nil, body: [String: Any]? = nil, representation: Bool = false) async throws -> Data {
        var request = URLRequest(url: URL(string: "https://nbfvflfjegiufbtyjuch.supabase.co" + path)!)
        request.setValue("sb_publishable_KN5XnD4anPn4_vANwr_-Fg_4_UZIsGB", forHTTPHeaderField: "apikey")
        if let token { request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        if representation { request.setValue("return=representation", forHTTPHeaderField: "Prefer") }
        if let body {
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
        }
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let response = response as? HTTPURLResponse, (200..<300).contains(response.statusCode) else {
            throw URLError(.badServerResponse)
        }
        return data
    }
}
private struct PinEditor: View {
    let idea: PlaceIdea
    let places: PlaceIdeasStore
    let onSaved: (LocalPinPlan) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var camera: MapCameraPosition = .automatic
    @State private var point: CLLocationCoordinate2D?
    @State private var plan: LocalPinPlan?
    @State private var tripID: String?
    @State private var title = ""
    @State private var newTripID = UUID().uuidString
    @State private var creating = false
    @State private var busy = false
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Form {
                Section(idea.name) {
                    Text(idea.address)
                    Text("Mova o mapa para posicionar a marca no centro.")
                    Map(position: $camera)
                        .onMapCameraChange(frequency: .continuous) { point = $0.region.center }
                        .overlay { Image(systemName: "mappin").font(.title).allowsHitTesting(false) }
                        .frame(height: 230)
                }
                Section("Viagem") {
                    Picker("Vínculo", selection: $tripID) {
                        Text("Sem viagem").tag(nil as String?)
                        ForEach(plan?.trips ?? [], id: \.id) { trip in
                            Text(trip.title).tag(Optional(trip.id))
                        }
                    }.disabled(creating)
                    Toggle("Criar viagem", isOn: $creating)
                    if creating { TextField("Nome da viagem", text: $title) }
                }
                if let error { Text(error).foregroundStyle(.red) }
                Button("Confirmar ponto e salvar Pin") { save() }
                    .disabled(point == nil || plan == nil || busy || (creating && title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty))
            }
            .navigationTitle("Marcar no mapa")
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Cancelar") { dismiss() }.disabled(busy) } }
            .interactiveDismissDisabled(busy)
            .task {
                do {
                    let loaded = try await places.loadPinPlan()
                    if let pin = loaded.pins.first(where: { $0.ideaID == idea.id }) {
                        let center = CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude)
                        point = center
                        tripID = pin.tripID
                        camera = .region(MKCoordinateRegion(center: center, span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)))
                    }
                    plan = loaded
                } catch { self.error = "Não foi possível abrir os Pins." }
            }
        }
    }

    private func save() {
        guard let point, !busy else { return }
        let choice: LocalPinTripChoice = creating ? .create(id: newTripID, title: title) : tripID.map { .existing($0) } ?? .none
        busy = true
        error = nil
        Task {
            defer { busy = false }
            do {
                onSaved(try await places.savePin(id: idea.id, latitude: point.latitude, longitude: point.longitude, trip: choice))
            } catch { self.error = "Não foi possível salvar o Pin. Tente novamente." }
        }
    }
}
