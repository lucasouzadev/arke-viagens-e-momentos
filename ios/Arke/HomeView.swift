import SwiftUI
import MapKit
import CoreLocation

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @SceneStorage("home.context") private var contextValue = TravelContext.planning.rawValue
    @SceneStorage("home.detent") private var detentValue = PanelDetent.balanced.rawValue
    @StateObject private var location = ForegroundLocation()
    // D4: sem Pins nem localização, o mapa abre no Brasil; com Pins salvos, enquadra todos (ver `frameSavedPins`).
    @State private var camera: MapCameraPosition = .region(MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: -14.2, longitude: -51.9), span: MKCoordinateSpan(latitudeDelta: 38, longitudeDelta: 38)))
    @State private var showingTrips = false
    @State private var pinPlan: LocalPinPlan?
    @State private var pinIdea: PlaceIdea?
    @State private var mapDetailIdea: PlaceIdea?
    @State private var selectedMapPin: String?
    @State private var tripPath: [String] = []
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
    @State private var keyboardVisible = false
    @ScaledMetric(relativeTo: .headline) private var minimumContentHeight: CGFloat = 160

    private var state: HomeState {
        if let capture = journalCapture { return capture }
        return HomeState(
            context: TravelContext(rawValue: contextValue) ?? .planning,
            detent: PanelDetent(rawValue: detentValue) ?? .balanced
        )
    }

    private var journalCapture: HomeState? {
        #if DEBUG
        for argument in ProcessInfo.processInfo.arguments where argument.hasPrefix("--capture-journal=") {
            let fields = argument.dropFirst("--capture-journal=".count).split(separator: "/")
            if fields.count == 2, let context = TravelContext(rawValue: String(fields[0])),
               let detent = PanelDetent(rawValue: String(fields[1])) {
                return HomeState(context: context, detent: detent)
            }
        }
        #endif
        return nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            GeometryReader { geometry in
                let restingHeight = panelHeight ?? mapHeight(in: geometry.size.height)
                let expandedHeight = min(geometry.size.height, max(restingHeight, geometry.size.height - 52))
                let compactMap = !mapExpanded && restingHeight < 150
                VStack(spacing: 8) {
                    Color.clear.frame(height: restingHeight).accessibilityHidden(true)

                    panelMenu
                        .highPriorityGesture(panelDrag(in: geometry.size.height), including: keyboardVisible ? .none : .all)
                        .opacity(mapExpanded ? 0 : 1)
                        .allowsHitTesting(!mapExpanded)
                        .accessibilityHidden(mapExpanded)
                        .zIndex(1)

                    VStack(spacing: 12) {
                        ScrollView {
                            VStack(alignment: .leading, spacing: 12) {
                            if state.context == .planning {
                                HStack {
                                    Button { tripPath = []; showingTrips = true } label: {
                                        HStack(spacing: 6) {
                                            Image(systemName: "suitcase").font(.system(size: 15))
                                            Text("Viagens").arkeText(.label)
                                            Image(systemName: "chevron.down").font(.system(size: 12, weight: .semibold))
                                        }
                                        .padding(.horizontal, 14).frame(minHeight: 44)
                                        .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
                                        .contentShape(Capsule())
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityIdentifier("home.trips")
                                    Spacer()
                                    if places.ready {
                                        Text(places.ideas.count == 1 ? "1 lugar" : "\(places.ideas.count) lugares")
                                            .arkeText(.body).foregroundStyle(.secondary)
                                    }
                                }
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
                                        }, onPin: { pinIdea = $0 }, onDetailsVisibilityChanged: { showingSavedDetails = $0 }, focusedID: $focusedIdeaID, detent: state.detent)
                                    } else {
                                        if state.context == .expenses || state.context == .savings {
                                            if state.detent == .explore {
                                                Button { settlePanel(.details) } label: {
                                                    Label(state.context.title, systemImage: state.context == .expenses ? "receipt" : "chart.bar.fill")
                                                }
                                                .frame(minHeight: 44)
                                            } else {
                                                LocalFinanceView(trips: pinPlan?.trips ?? [], savings: state.context == .savings,
                                                                 detailed: state.detent == .details)
                                                    .id(state.context.rawValue)
                                            }
                                        } else {
                                            if state.detent == .explore {
                                                Button { settlePanel(.details) } label: {
                                                    Label(state.context.title, systemImage: state.context == .reviews ? "star" : "photo.on.rectangle")
                                                }.frame(minHeight: 44)
                                            } else {
                                                TripJournalView(plan: pinPlan, ideas: places.ideas, reviews: state.context == .reviews,
                                                                detailed: state.detent == .details, preview: journalCapture != nil)
                                                    .tint(Color(uiColor: .label)).id(state.context.rawValue)
                                                if state.detent == .balanced {
                                                    Button("Ver detalhes") { settlePanel(.details) }
                                                }
                                            }
                                        }
                                        Button("Ver meus lugares") { contextValue = TravelContext.planning.rawValue }
                                    }
                                } else if places.busy { ProgressView("Abrindo seus lugares…") }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .accessibilityIdentifier("home.contentBody")
                        .safeAreaInset(edge: .top, spacing: 0) {
                            VStack(alignment: .leading, spacing: 6) {
                                cardHeader
                                if state.context == .planning, state.detent != .explore,
                                   let idea = places.ideas.first(where: { $0.id == focusedIdeaID }) {
                                    Text(idea.name).arkeText(.caption).foregroundStyle(.secondary)
                                        .lineLimit(1).frame(maxWidth: .infinity, alignment: .leading)
                                        .accessibilityLabel("Lugar selecionado: " + idea.name)
                                }
                            }
                            .padding(.bottom, 12)
                            // Fundo sólido igual ao do cartão: o material cinza aparecia como uma faixa estranha sobre o cartão branco.
                            .background(ArkeUI.sheetSurface(colorScheme))
                        }
                        .frame(maxHeight: .infinity)
                        .arkeScrollEdges()
                        .scrollDismissesKeyboard(.interactively)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20))
                    .clipShape(.rect(cornerRadius: 20))
                    // Com o mapa expandido, só a borda do cartão fica à mostra: sem texto sob a alça.
                    .overlay { if mapExpanded { Color(uiColor: .secondarySystemGroupedBackground) } }
                    .offset(y: mapExpanded ? 12 : 0)
                    .allowsHitTesting(!mapExpanded)
                    .accessibilityHidden(mapExpanded)
                }
                .overlay(alignment: .top) {
                    Map(position: $camera, selection: $selectedMapPin) {
                        UserAnnotation()
                        ForEach(pinPlan?.pins ?? [], id: \.ideaID) { pin in
                            if let idea = places.ideas.first(where: { $0.id == pin.ideaID }) {
                                Marker(idea.name, coordinate: CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude)).tag(pin.ideaID)
                            }
                        }
                        if let searchPin { Marker(item: searchPin) }
                    }
                        .mapStyle(.standard)
                        .accessibilityLabel("Mapa Apple. Explore o mundo ou use Minha localização.")
                        .frame(height: mapExpanded ? expandedHeight : restingHeight)
                        .overlay {
                            RoundedRectangle(cornerRadius: 30)
                                .strokeBorder(.white.opacity(0.25), lineWidth: 9)
                                .blur(radius: 7)
                                .allowsHitTesting(false)
                        }
                        .overlay(alignment: .top) {
                            mapControls(compact: compactMap)
                        }
                        .overlay(alignment: .bottomLeading) {
                            if !compactMap, state.context == .planning,
                               let idea = places.ideas.first(where: { $0.id == focusedIdeaID }) {
                                Label(idea.name, systemImage: "bookmark")
                                    .font(.subheadline.weight(.semibold)).lineLimit(1)
                                    .padding(10).background(.regularMaterial, in: Capsule())
                                    .padding(.leading, 12).padding(.trailing, 68).padding(.bottom, 34)
                                    .accessibilityLabel("Ideia selecionada: \(idea.name)")
                                    .allowsHitTesting(false)
                            }
                        }
                        .clipShape(.rect(cornerRadius: 30))
                        .onReceive(location.$coordinate) { coordinate in
                            if let coordinate { camera = .region(MKCoordinateRegion(center: coordinate, span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04))) }
                        }
                }
                .overlay(alignment: .bottom) {
                    if mapExpanded {
                        Capsule().fill(.secondary).frame(width: 36, height: 4)
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .contentShape(Rectangle())
                            .gesture(DragGesture(minimumDistance: 10).onEnded { value in
                                // Mesma direção da alça normal: arrastar para cima devolve espaço ao cartão.
                                if value.translation.height < -16 { setMapExpanded(false) }
                            })
                            .onTapGesture { setMapExpanded(false) }
                            .accessibilityLabel("Recolher mapa")
                            .accessibilityAddTraits(.isButton)
                            .accessibilityAction { setMapExpanded(false) }
                    }
                }
                .clipped()
            }
        }
        .onChange(of: selectedMapPin) { _, id in
            guard let id, let pin = pinPlan?.pins.first(where: { $0.ideaID == id }) else { return }
            focusedIdeaID = id
            if let tripID = pin.tripID {
                tripPath = [tripID]
                showingTrips = true
            } else if let idea = places.ideas.first(where: { $0.id == id }) {
                mapDetailIdea = idea
            }
            selectedMapPin = nil
        }
        .alert("Minha localização", isPresented: Binding(get: { location.message != nil }, set: { if !$0 { location.message = nil } })) {
            Button("Entendi", role: .cancel) { location.message = nil }
        } message: { Text(location.message ?? "") }
        .arkeText(.body)
        .scrollIndicators(.hidden)
        .padding(16)
        .background(Color(uiColor: .systemGroupedBackground))
        .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardWillShowNotification)) { _ in keyboardVisible = true }
        .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardWillHideNotification)) { _ in keyboardVisible = false }
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
            .presentationDragIndicator(.hidden)
        }
        .task {
            pinPlan = try? await places.loadPinPlan()
            frameSavedPins()
        }
        .sheet(isPresented: $showingTrips) {
            NavigationStack(path: $tripPath) {
                // Sheet contínuo: o título rola com o conteúdo; fechar é arrastar para baixo.
                ScrollView {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Viagens").font(.title.bold()).padding(.bottom, 6).accessibilityAddTraits(.isHeader)
                        if let plan = pinPlan {
                            if plan.trips.isEmpty {
                                Text("Suas viagens aparecem aqui quando você vincula um Pin.").foregroundStyle(.secondary)
                            }
                            ForEach(plan.trips, id: \.id) { trip in
                                let count = plan.pins.filter { $0.tripID == trip.id }.count
                                NavigationLink(value: trip.id) {
                                    tripRow(icon: "suitcase", title: trip.title,
                                            detail: count == 1 ? "1 lugar" : "\(count) lugares", chevron: true)
                                }.buttonStyle(.plain)
                            }
                            Text("Fora de viagens").font(.footnote.weight(.semibold)).foregroundStyle(.secondary)
                                .padding(.top, 10).padding(.leading, 4)
                            let loose = places.ideas.filter { idea in
                                !plan.pins.contains { $0.ideaID == idea.id && $0.tripID != nil }
                            }.count
                            tripRow(icon: "mappin", title: "Ideias soltas",
                                    detail: loose == 1 ? "1 lugar sem viagem" : "\(loose) lugares sem viagem", chevron: false)
                        } else { Text("Não foi possível abrir as viagens.") }
                    }
                    .padding(.horizontal, 20).padding(.top, 28).padding(.bottom, 32)
                }
                .scrollIndicators(.hidden)
                .toolbar(.hidden, for: .navigationBar)

                .navigationDestination(for: String.self) { id in
                    if let plan = pinPlan, let trip = plan.trips.first(where: { $0.id == id }) {
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
            }
            .presentationDragIndicator(.hidden)
        }
        .sheet(item: $mapDetailIdea) { idea in
            PlaceDetails(ideas: places.ideas, initialID: idea.id) { mapDetailIdea = nil }
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

    /// Controles sobre o mapa: busca em pílula à esquerda; localização e expandir empilhados à direita.
    @ViewBuilder
    private func mapControls(compact: Bool) -> some View {
        let searchButton = Button { showingMapSearch = true } label: {
            if compact {
                Image(systemName: "magnifyingglass").font(.system(size: 18, weight: .semibold)).frame(width: 26, height: 26)
            } else {
                Label("Buscar lugares", systemImage: "magnifyingglass")
                    .font(.subheadline).frame(maxWidth: .infinity, minHeight: 24, alignment: .leading)
            }
        }
        .modifier(MapLocationButtonStyle(isPill: !compact))
        .accessibilityLabel("Buscar lugares")
        .modifier(MapSearchMotion(namespace: mapSearchTransition, isSource: true))
        let locateButton = Button { location.request() } label: {
            Image(systemName: "location.fill").font(.system(size: 18, weight: .semibold)).frame(width: 26, height: 26)
        }
        .modifier(MapLocationButtonStyle())
        .accessibilityLabel("Minha localização")
        .accessibilityIdentifier("home.location")
        .accessibilityValue(location.coordinate == nil ? "" : "Localização disponível")
        let expandButton = Button { setMapExpanded(!mapExpanded) } label: {
            Image(systemName: mapExpanded ? "arrow.down.right.and.arrow.up.left" : "arrow.up.left.and.arrow.down.right")
                .font(.system(size: 18, weight: .semibold)).frame(width: 26, height: 26)
        }
        .modifier(MapLocationButtonStyle())
        .accessibilityLabel(mapExpanded ? "Recolher mapa" : "Expandir mapa")
        .accessibilityIdentifier("home.expandMap")
        .disabled(keyboardVisible)
        if compact {
            HStack(spacing: 6) { searchButton; locateButton; expandButton }
                .padding(.horizontal, 12).padding(.top, 8)
        } else {
            HStack(alignment: .top, spacing: 8) {
                searchButton
                VStack(spacing: 8) { locateButton; expandButton }
            }
            .padding(.horizontal, 12).padding(.top, 12)
        }
    }

    private func tripRow(icon: String, title: String, detail: String, chevron: Bool) -> some View {
        HStack(spacing: 14) {
            Image(systemName: icon).font(.system(size: 20)).foregroundStyle(ArkeUI.ink)
                .frame(width: 48, height: 48).background(ArkeUI.sheetSurface(colorScheme), in: Circle())
            VStack(alignment: .leading, spacing: 2) {
                Text(title).arkeText(.contextTitle).lineLimit(1)
                Text(detail).arkeText(.body).foregroundStyle(.secondary)
            }
            Spacer(minLength: 8)
            if chevron { Image(systemName: "chevron.right").font(.system(size: 14, weight: .semibold)).foregroundStyle(.tertiary) }
        }
        .padding(.horizontal, 16).padding(.vertical, 12).frame(maxWidth: .infinity, alignment: .leading)
        .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 26))
        .contentShape(Rectangle())
    }

    /// D4: com Pins salvos, a câmera inicial enquadra todos; nunca pede permissão de localização sozinha.
    private func frameSavedPins() {
        guard let pins = pinPlan?.pins, !pins.isEmpty else { return }
        let latitudes = pins.map(\.latitude), longitudes = pins.map(\.longitude)
        guard let minLat = latitudes.min(), let maxLat = latitudes.max(),
              let minLon = longitudes.min(), let maxLon = longitudes.max() else { return }
        let center = CLLocationCoordinate2D(latitude: (minLat + maxLat) / 2, longitude: (minLon + maxLon) / 2)
        let span = MKCoordinateSpan(latitudeDelta: max((maxLat - minLat) * 1.6, 0.05), longitudeDelta: max((maxLon - minLon) * 1.6, 0.05))
        camera = .region(MKCoordinateRegion(center: center, span: span))
    }

    private var cardHeader: some View {
        HStack(spacing: 8) {
            let options = TravelContext.allCases
            let index = options.firstIndex(of: state.context) ?? 0
            HStack(spacing: 0) {
                contextNeighbor(index - 1, forward: false)
                VStack(spacing: 6) {
                    HStack(spacing: 8) {
                        Image(systemName: state.context.symbol).font(.system(size: 17, weight: .medium))
                            .foregroundStyle(ArkeUI.ink).accessibilityHidden(true)
                        Text(state.context.title)
                            .arkeText(.contextTitle)
                            .multilineTextAlignment(.center)
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)
                            .accessibilityAddTraits(.isHeader)
                            .accessibilityIdentifier("home.contextTitle")
                    }
                    .frame(maxWidth: .infinity, minHeight: 28)
                    contextDots(index: index, count: options.count)
                }
                .frame(maxWidth: .infinity, minHeight: 48)
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
                contextNeighbor(index + 1, forward: true)
            }
            Button { showingAddPlace = true } label: {
                Image(systemName: "plus")
                    .font(.system(size: 22, weight: .semibold)).foregroundStyle(.white)
                    .frame(width: 44, height: 44)
                    .background(ArkeUI.blue, in: Circle())
                    .contentShape(Circle())
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

    /// Vizinho do contexto atual: botão tocável (nunca texto cortado). O nome completo vai para a acessibilidade.
    @ViewBuilder
    private func contextNeighbor(_ index: Int, forward: Bool) -> some View {
        let options = TravelContext.allCases
        if options.indices.contains(index) {
            Button { selectContext(index) } label: {
                Image(systemName: options[index].symbol)
                    .font(.system(size: 18, weight: .medium))
                    .frame(width: 44, height: 48)
                    .contentShape(Rectangle())
            }
            .foregroundStyle(.secondary)
            .accessibilityLabel((forward ? "Próximo contexto: " : "Contexto anterior: ") + options[index].title)
            .accessibilityIdentifier(forward ? "home.contextNext" : "home.contextPrevious")
        } else {
            Color.clear.frame(width: 44, height: 48).accessibilityHidden(true)
        }
    }

    /// Posição entre os cinco contextos, só como apoio visual (a posição é anunciada pelo seletor ajustável).
    private func contextDots(index: Int, count: Int) -> some View {
        HStack(spacing: 5) {
            ForEach(0..<count, id: \.self) { position in
                Capsule()
                    .fill(position == index ? AnyShapeStyle(ArkeUI.ink) : AnyShapeStyle(Color.secondary.opacity(0.45)))
                    .frame(width: position == index ? 16 : 6, height: 6)
            }
        }
        .animation(reduceMotion ? nil : .smooth(duration: 0.2), value: index)
        .accessibilityHidden(true)
    }

    private func settlePanel(_ detent: PanelDetent) {
        withAnimation(reduceMotion ? nil : .smooth(duration: 0.28)) {
            detentValue = detent.rawValue
            panelHeight = nil
            panelOrigin = nil
        }
    }

    private var panelMenu: some View {
        Capsule().fill(.secondary).frame(width: 36, height: 4)
            .frame(maxWidth: .infinity, minHeight: 44)
            .contentShape(Rectangle())
            .accessibilityLabel("Ajustar proporção")
            .accessibilityValue(state.detent.title)
            .accessibilityIdentifier("home.panelMenu")
            .accessibilityAddTraits(.isButton)
        .accessibilityAdjustableAction { direction in
            switch direction {
            case .increment: movePanel(towardMap: true)
            case .decrement: movePanel(towardMap: false)
            @unknown default: break
            }
        }
        .accessibilityAction(named: "Mostrar mais mapa") { movePanel(towardMap: true) }
        .accessibilityAction(named: "Expandir conteúdo") { movePanel(towardMap: false) }
    }

    private func setMapExpanded(_ expanded: Bool) {
        withAnimation(reduceMotion ? nil : .smooth(duration: 0.28)) { mapExpanded = expanded }
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
        max(min(84, height), min(value, height - minimumContentHeight - 60))
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
    /// Ícone por contexto (D6).
    var symbol: String {
        switch self {
        case .planning: "mappin.and.ellipse"
        case .expenses: "receipt"
        case .savings: "target"
        case .reviews: "star"
        case .memories: "photo"
        }
    }
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
    @Environment(\.colorScheme) private var colorScheme
    var isPill = false
    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content.foregroundStyle(colorScheme == .dark ? Color.white : Color.primary).tint(colorScheme == .dark ? .white : .primary).buttonStyle(.glass).buttonBorderShape(isPill ? .capsule : .circle)
        } else {
            content.foregroundStyle(colorScheme == .dark ? Color.white : Color.primary).background(.regularMaterial, in: Capsule())
        }
    }
}

private struct MapPlaceSearch: View {
    let onSelect: (MKMapItem) -> Void
    @State private var query: String
    @State private var results: [MKMapItem] = []
    @State private var message: String?
    @State private var searching = false

    init(initialQuery: String = "", onSelect: @escaping (MKMapItem) -> Void) {
        _query = State(initialValue: initialQuery)
        self.onSelect = onSelect
    }

    var body: some View {
        NavigationStack {
            List {
                if searching {
                    HStack(spacing: 12) { ProgressView(); Text("Buscando…").foregroundStyle(.secondary) }
                        .accessibilityElement(children: .combine).accessibilityIdentifier("search.loading")
                }
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
            guard phrase.count >= 2 else { searching = false; return }
            searching = true
            do {
                try await Task.sleep(for: .milliseconds(350))
                let request = MKLocalSearch.Request()
                request.naturalLanguageQuery = phrase
                let response = try await MKLocalSearch(request: request).start()
                try Task.checkCancellation()
                results = response.mapItems
                searching = false
                if results.isEmpty { message = "Nenhum lugar encontrado. Confira o nome ou mova o mapa para marcar o ponto." }
            } catch {
                if !Task.isCancelled { message = "Não foi possível buscar. Tente novamente."; searching = false }
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





private struct TripJournalView: View {
    struct Entry: Codable, Identifiable {
        var id = UUID()
        let tripID: String
        let text: String
        let rating: Int?
        let placeID: String?
    }

    let plan: LocalPinPlan?
    let ideas: [PlaceIdea]
    let reviews: Bool
    let detailed: Bool
    var preview = false
    @AppStorage("arke.trip-journal.v1") private var data = Data()
    @State private var selectedTrip = ""
    @State private var placeID: String?
    @State private var rating = 5
    @State private var draft = ""
    @State private var error = false

    var body: some View {
        let activePlan = preview ? try? JSONDecoder().decode(LocalPinPlan.self, from: Data(
            #"{"pins":[],"trips":[{"id":"preview","title":"Milano"}]}"#.utf8)) : plan
        if let plan = activePlan, let trip = plan.trips.first(where: { $0.id == selectedTrip }) ?? plan.trips.first {
            Picker("Viagem", selection: Binding(get: { trip.id }, set: {
                selectedTrip = $0
                draft = ""
                placeID = nil
                error = false
            })) {
                ForEach(plan.trips, id: \.id) { Text($0.title).tag($0.id) }
            }
            let source = preview ? [
                Entry(tripID: "preview", text: reviews ? "Chegamos cedo e encontramos a praça tranquila." : "A primeira manhã na praça do Duomo.", rating: reviews ? 4 : nil, placeID: nil)
            ] : ((try? JSONDecoder().decode([Entry].self, from: data)) ?? [])
            let entries = source
                .filter { $0.tripID == trip.id && ($0.rating != nil) == reviews }
            if entries.isEmpty { Text(reviews ? "Nenhuma avaliação nesta viagem." : "Nenhuma memória nesta viagem.").foregroundStyle(.secondary) }
            ForEach(detailed ? entries.reversed().map { $0 } : Array(entries.suffix(1))) { entry in
                let layout = detailed || reviews
                    ? AnyLayout(VStackLayout(alignment: .leading, spacing: 6))
                    : AnyLayout(HStackLayout(alignment: .center, spacing: 10))
                layout {
                    if let rating = entry.rating {
                        Label("\(rating) de 5", systemImage: "star.fill").arkeText(.contextTitle)
                    } else if let image = journalCover(entry) {
                        Image(uiImage: image).resizable().scaledToFit().frame(maxWidth: detailed ? .infinity : 64, maxHeight: detailed ? 190 : 44)
                            .padding(10)
                            .background {
                                Color(uiColor: .systemBackground)
                                    .mask { StampEdge().fill(style: FillStyle(eoFill: true)) }
                            }
                            .accessibilityHidden(true)
                    } else {
                        Label("Memória", systemImage: "photo.on.rectangle").arkeText(.contextTitle)
                    }
                    Text(entry.text).lineLimit(detailed ? nil : 2)
                }.padding(.vertical, 6)
            }
            if detailed {
                if reviews {
                    Stepper("Nota: \(rating) de 5", value: $rating, in: 1...5)
                } else {
                    Picker("Capa do lugar", selection: $placeID) {
                        Text("Sem capa").tag(nil as String?)
                        ForEach(ideas.filter { idea in
                            idea.cover != nil && plan.pins.contains { $0.ideaID == idea.id && $0.tripID == trip.id }
                        }) { idea in Text(idea.name).tag(Optional(idea.id)) }
                    }
                }
                TextField(reviews ? "Escreva sua avaliação" : "Escreva sua memória", text: $draft, axis: .vertical)
                Button(reviews ? "Salvar avaliação" : "Salvar memória") {
                    let text = draft.trimmingCharacters(in: .whitespacesAndNewlines)
                    guard !text.isEmpty, text.count <= 1000 else { return }
                    do {
                        var all = data.isEmpty ? [] : try JSONDecoder().decode([Entry].self, from: data)
                        all.append(Entry(tripID: trip.id, text: text, rating: reviews ? rating : nil, placeID: reviews ? nil : placeID))
                        data = try JSONEncoder().encode(all)
                        draft = ""
                        placeID = nil
                        error = false
                    } catch { self.error = true }
                }.disabled(preview || draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || draft.count > 1000)
                if error { Text("Não foi possível salvar.").foregroundStyle(.red) }
            }
        } else {
            Text("Crie uma viagem local para começar.").foregroundStyle(.secondary)
        }
    }
    private func journalCover(_ entry: Entry) -> UIImage? {
        #if DEBUG
        if preview {
            return UIGraphicsImageRenderer(size: CGSize(width: 360, height: 210)).image { context in
                UIColor(red: 0.23, green: 0.50, blue: 0.62, alpha: 1).setFill()
                context.fill(CGRect(x: 0, y: 0, width: 360, height: 210))
                UIImage(systemName: "building.columns.fill")?.withTintColor(.white, renderingMode: .alwaysOriginal)
                    .draw(in: CGRect(x: 125, y: 48, width: 110, height: 110))
            }
        }
        #endif
        guard let cover = ideas.first(where: { $0.id == entry.placeID })?.cover else { return nil }
        return UIImage(data: cover)
    }
}
private struct LocalFinanceView: View {
    struct Entry: Codable, Identifiable {
        var id = UUID()
        var trip: String
        var name: String
        var currency: String
        var amount: Int64
        var kind: String
    }

    let trips: [LocalPinTrip]
    let savings: Bool
    let detailed: Bool
    @AppStorage("arke.local-finance.v1") private var stored = Data()
    @State private var selectedTrip = ""
    @State private var name = ""
    @State private var amount = ""
    @State private var currency = "BRL"
    @State private var kind = ""
    @FocusState private var financeFieldFocused: Bool
    @State private var editing: UUID?
    @State private var error: String?

    private var records: [Entry] {
        (try? JSONDecoder().decode([Entry].self, from: stored)) ?? []
    }
    private var trip: String? {
        trips.first(where: { $0.id == selectedTrip })?.id ?? trips.first?.id
    }

    var body: some View {
        if let trip {
            Picker("Viagem", selection: Binding(get: { trip }, set: { selectedTrip = $0; clearDraft() })) {
                ForEach(trips, id: \.id) { Text($0.title).tag($0.id) }
            }
            let entries = records.filter { $0.trip == trip && (savings ? ["goal", "saving"].contains($0.kind) : ["planned", "incurred"].contains($0.kind)) }
            let goal = entries.first { $0.kind == "goal" }
            if savings, let goal {
                let total = entries.filter { $0.kind == "saving" && $0.currency == goal.currency }.reduce(Int64(0)) { $0 + $1.amount }
                Text("Reservado: \(formatted(total, goal.currency)) / \(formatted(goal.amount, goal.currency))")
                ProgressView(value: min(Double(total), Double(goal.amount)), total: Double(goal.amount))
                    .accessibilityLabel("Progresso da meta")
                Text("Falta: \(formatted(max(0, goal.amount - total), goal.currency))")
            }
            if !savings {
                ForEach(Set(entries.map(\.currency)).sorted(), id: \.self) { code in
                    let subtotal = entries.filter { $0.currency == code }
                    Text("Previsto: \(formatted(subtotal.filter { $0.kind == "planned" }.reduce(0) { $0 + $1.amount }, code))")
                    Text("Realizado: \(formatted(subtotal.filter { $0.kind == "incurred" }.reduce(0) { $0 + $1.amount }, code))")
                }
            }
            ForEach(detailed ? entries : Array(entries.suffix(3))) { entry in
                Button {
                    editing = entry.id; name = entry.name; currency = entry.currency; kind = entry.kind
                    amount = displayAmount(entry.amount, code: entry.currency)
                } label: { Text("\(entry.name) · \(formatted(entry.amount, entry.currency))") }
            }
            Picker("Tipo", selection: $kind) {
                Text("Escolha o tipo").tag("")
                if savings {
                    Text("Meta").tag("goal")
                    if goal != nil { Text("Valor reservado").tag("saving") }
                } else {
                    Text("Prevista").tag("planned")
                    Text("Realizada").tag("incurred")
                }
            }.disabled(editing != nil && savings)
            if !savings { TextField("Descrição", text: $name) }
            Picker("Moeda", selection: Binding(get: { savings && kind == "saving" ? (goal?.currency ?? currency) : currency }, set: { currency = $0 })) {
                ForEach(["BRL", "USD", "EUR", "JPY", "KWD"], id: \.self) { Text($0).tag($0) }
            }.disabled(savings && (kind == "saving" || entries.contains { $0.kind == "saving" }))
            TextField("Valor", text: $amount).keyboardType(.decimalPad).focused($financeFieldFocused)
            if financeFieldFocused { Button("Fechar teclado") { financeFieldFocused = false } }
            if let error { Text(error).foregroundStyle(.red) }
            Button(editing == nil ? "Salvar registro" : "Salvar alterações") { save(trip: trip, goal: goal) }
            if editing != nil { Button("Cancelar edição") { clearDraft() } }
        } else {
            Text("Crie uma viagem local para começar.")
        }
    }

    private func displayAmount(_ amount: Int64, code: String) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.maximumFractionDigits = digits(code)
        return formatter.string(from: NSNumber(value: Double(amount) / pow(10, Double(digits(code))))) ?? ""
    }
    private func digits(_ code: String) -> Int { code == "JPY" ? 0 : code == "KWD" ? 3 : 2 }
    private func formatted(_ amount: Int64, _ code: String) -> String { "\(code) \(displayAmount(amount, code: code))" }

    private func clearDraft() {
        editing = nil; name = ""; amount = ""; kind = ""; error = nil
    }

    private func save(trip: String, goal: Entry?) {
        let code = savings && kind == "saving" ? (goal?.currency ?? currency) : currency
        let input = amount.trimmingCharacters(in: .whitespacesAndNewlines)
        let separator = Locale.current.decimalSeparator ?? "."
        let pieces = input.components(separatedBy: separator)
        guard !kind.isEmpty, savings || !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              (kind != "saving" || goal != nil),
              pieces.count <= 2, pieces.allSatisfy({ !$0.isEmpty && $0.utf8.allSatisfy { (48...57).contains($0) } }),
              (pieces.dropFirst().first?.count ?? 0) <= digits(code),
              let value = Decimal(string: pieces.joined(separator: "."), locale: Locale(identifier: "en_US_POSIX")),
              value > 0, value <= 1_000_000_000 else {
            error = "Informe um tipo e um valor válido para a moeda."
            return
        }
        do {
            var entries = stored.isEmpty ? [] : try JSONDecoder().decode([Entry].self, from: stored)
            guard entries.count < 10_000 || editing != nil || (kind == "goal" && goal != nil) else {
                error = "Limite de registros atingido."
                return
            }
            let id = editing ?? (kind == "goal" ? goal?.id : nil) ?? UUID()
            let scale = Decimal(Int(pow(10, Double(digits(code)))))
            let label = savings ? (kind == "goal" ? "Meta" : "Valor reservado") : name.trimmingCharacters(in: .whitespacesAndNewlines)
            let entry = Entry(id: id, trip: trip, name: label, currency: code, amount: NSDecimalNumber(decimal: value * scale).int64Value, kind: kind)
            if let index = entries.firstIndex(where: { $0.id == id }) { entries[index] = entry }
            else { entries.append(entry) }
            stored = try JSONEncoder().encode(entries)
            financeFieldFocused = false
            clearDraft()
        } catch { self.error = "Não foi possível salvar os registros." }
    }
}
/// Compartilhar exige conta (D5 pendente, GROUP-001). Sem credencial, URL de servidor ou formulário de login no código:
/// nada é enviado e o lugar continua só neste aparelho.
private struct SharePinForm: View {
    let name: String
    let pin: LocalMapPin

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Compartilhar exige uma conta Arke").font(.title2.bold()).accessibilityAddTraits(.isHeader)
            Text("O compartilhamento de viagens ainda não está disponível nesta versão. \(name) continua salvo só neste aparelho e nada é enviado.")
                .accessibilityIdentifier("share.unavailable")
            Label("O Cofrinho continua só seu.", systemImage: "lock").foregroundStyle(.secondary)
            Spacer()
        }
        .padding(20).frame(maxWidth: .infinity, alignment: .leading)
        .navigationTitle("Compartilhar Pin")
    }
}
/// Marcar no mapa: o mapa é o conteúdo; o painel inferior concentra a decisão (lugar, ponto, viagem opcional e confirmação).
private struct PinEditor: View {
    let idea: PlaceIdea
    let places: PlaceIdeasStore
    let onSaved: (LocalPinPlan) -> Void
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme
    @State private var camera: MapCameraPosition = .automatic
    @StateObject private var pinLocation = ForegroundLocation()
    @State private var searchingAddress = false
    @State private var point: CLLocationCoordinate2D?
    @State private var plan: LocalPinPlan?
    @State private var tripID: String?
    @State private var title = ""
    @State private var newTripID = UUID().uuidString
    @State private var creating = false
    @State private var busy = false
    @State private var error: String?

    private var trimmedTitle: String { title.trimmingCharacters(in: .whitespacesAndNewlines) }
    private var canConfirm: Bool { point != nil && plan != nil && !busy && !(creating && trimmedTitle.isEmpty) }

    /// Explica por que a confirmação está indisponível; nunca deixa o botão apagado sem motivo.
    private var hint: String? {
        if plan == nil { return error == nil ? "Abrindo seus Pins…" : nil }
        if point == nil { return "Mova o mapa ou busque um endereço para marcar o ponto." }
        if creating && trimmedTitle.isEmpty { return "Dê um nome à nova viagem para continuar." }
        return nil
    }

    private var pointDescription: String {
        guard let point else { return "Nenhum ponto marcado ainda" }
        return String(format: "Ponto marcado: %.4f, %.4f", point.latitude, point.longitude)
    }

    var body: some View {
        ZStack {
            Map(position: $camera)
                .onMapCameraChange(frequency: .continuous) { update in
                    if camera.positionedByUser { point = update.region.center }
                }
                .ignoresSafeArea()
                .accessibilityLabel("Mapa. Mova para posicionar a marca no centro.")
            // O ponto salvo é o centro do mapa: o pontinho marca o ponto exato e o pino fica logo acima dele.
            ZStack {
                Circle().fill(.white).frame(width: 8, height: 8)
                    .overlay(Circle().stroke(ArkeUI.blue, lineWidth: 2))
                Image(systemName: "mappin").font(.system(size: 34, weight: .semibold))
                    .foregroundStyle(ArkeUI.blue).shadow(radius: 2, y: 1).offset(y: -22)
            }
            .allowsHitTesting(false).accessibilityHidden(true)
        }
        .overlay(alignment: .top) { topControls }
        .overlay(alignment: .bottom) { panel }
        .interactiveDismissDisabled(busy)
        .sheet(isPresented: $searchingAddress) {
            MapPlaceSearch(initialQuery: idea.address) { item in
                centerMap(on: item.placemark.coordinate)
                searchingAddress = false
            }
        }
        .alert("Minha localização", isPresented: Binding(get: { pinLocation.message != nil }, set: { if !$0 { pinLocation.message = nil } })) {
            Button("Entendi", role: .cancel) { pinLocation.message = nil }
        } message: { Text(pinLocation.message ?? "") }
        .onReceive(pinLocation.$coordinate) { if let coordinate = $0 { centerMap(on: coordinate) } }
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
                if point == nil || !loaded.pins.contains(where: { $0.ideaID == idea.id }) {
                    let request = MKLocalSearch.Request()
                    request.naturalLanguageQuery = idea.address
                    if let results = try? await MKLocalSearch(request: request).start(),
                       results.mapItems.count == 1, let match = results.mapItems.first,
                       !Task.isCancelled {
                        centerMap(on: match.placemark.coordinate)
                    }
                }
            } catch { self.error = "Não foi possível abrir os Pins." }
        }
    }

    private var topControls: some View {
        VStack(spacing: 10) {
            HStack {
                Button { dismiss() } label: {
                    Image(systemName: "xmark").font(.system(size: 18, weight: .semibold)).frame(width: 26, height: 26)
                }
                .modifier(MapLocationButtonStyle()).disabled(busy)
                .accessibilityLabel("Cancelar").accessibilityIdentifier("pin.cancel")
                Spacer()
                Text("Marcar no mapa").arkeText(.contextTitle)
                    .padding(.horizontal, 18).padding(.vertical, 7)
                    .background(.regularMaterial, in: Capsule())
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                Button { pinLocation.request() } label: {
                    Image(systemName: "location.fill").font(.system(size: 18, weight: .semibold)).frame(width: 26, height: 26)
                }
                .modifier(MapLocationButtonStyle())
                .accessibilityLabel("Minha localização").accessibilityIdentifier("pin.location")
            }
            Button { searchingAddress = true } label: {
                Label("Buscar endereço", systemImage: "magnifyingglass")
                    .font(.subheadline).frame(maxWidth: .infinity, minHeight: 24, alignment: .leading).padding(.horizontal, 4)
            }
            .modifier(MapLocationButtonStyle(isPill: true))
            .accessibilityIdentifier("pin.search")
        }
        .padding(.horizontal, 16).padding(.top, 8)
    }

    private var panel: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(idea.name).arkeText(.cardTitle).lineLimit(1)
                Text(idea.address).arkeText(.caption).foregroundStyle(.secondary).lineLimit(1)
            }
            Label(pointDescription, systemImage: "mappin.and.ellipse")
                .arkeText(.caption).foregroundStyle(point == nil ? .secondary : .primary)
                .accessibilityIdentifier("pin.point")
            VStack(alignment: .leading, spacing: 6) {
                Text("Vincular a uma viagem (opcional)").arkeText(.label).foregroundStyle(.secondary)
                ScrollView(.horizontal) {
                    HStack(spacing: 8) {
                        chip("Sem viagem", selected: !creating && tripID == nil) { creating = false; tripID = nil }
                        ForEach(plan?.trips ?? [], id: \.id) { trip in
                            chip(trip.title, selected: !creating && tripID == trip.id) { creating = false; tripID = trip.id }
                        }
                        chip("Nova viagem", icon: "plus", selected: creating) { creating = true; tripID = nil }
                    }
                }
                .scrollIndicators(.hidden)
                if creating {
                    TextField("Nome da viagem", text: $title)
                        .padding(.horizontal, 16).frame(minHeight: 48)
                        .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 16))
                        .accessibilityIdentifier("pin.tripTitle")
                }
            }
            if let error { Text(error).arkeText(.caption).foregroundStyle(Color(uiColor: .systemRed)) }
            if let hint { Text(hint).arkeText(.caption).foregroundStyle(.secondary).accessibilityIdentifier("pin.hint") }
            Button { save() } label: {
                Text(busy ? "Salvando…" : "Confirmar ponto").arkeText(.button)
                    .foregroundStyle(canConfirm ? Color.white : Color.secondary)
                    .frame(maxWidth: .infinity, minHeight: ArkeUI.controlHeight)
                    .background(canConfirm ? AnyShapeStyle(ArkeUI.blue) : AnyShapeStyle(Color(uiColor: .tertiarySystemFill)), in: Capsule())
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain).disabled(!canConfirm)
            .accessibilityIdentifier("pin.confirm")
        }
        .padding(16)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 32))
        .padding(.horizontal, 12).padding(.bottom, 8)
    }

    private func chip(_ title: String, icon: String? = nil, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let icon { Image(systemName: icon).font(.system(size: 13, weight: .bold)) }
                else if selected { Image(systemName: "checkmark").font(.system(size: 13, weight: .bold)) }
                Text(title).arkeText(.label).lineLimit(1)
            }
            .padding(.horizontal, 14).frame(minHeight: 44)
            .foregroundStyle(selected ? Color.white : Color(uiColor: .label))
            .background(selected ? AnyShapeStyle(ArkeUI.blue) : AnyShapeStyle(Color(uiColor: .tertiarySystemFill)), in: Capsule())
        }
        .buttonStyle(.plain).disabled(busy)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }

    private func centerMap(on coordinate: CLLocationCoordinate2D) {
        point = coordinate
        camera = .region(MKCoordinateRegion(center: coordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)))
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
