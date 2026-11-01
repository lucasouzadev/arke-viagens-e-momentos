import SwiftUI
import MapKit

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @SceneStorage("home.context") private var contextValue = TravelContext.planning.rawValue
    @SceneStorage("home.detent") private var detentValue = PanelDetent.balanced.rawValue
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
                        .frame(height: mapHeight(in: geometry.size.height))
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .contentShape(RoundedRectangle(cornerRadius: 20))

                    panelMenu.zIndex(1)

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
                    }
                    .padding(20)
                    .background(.background, in: RoundedRectangle(cornerRadius: 20))
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
            Label(state.detent.title, systemImage: "chevron.up.chevron.down")
                .font(.caption)
                .frame(maxWidth: .infinity, minHeight: 44)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .foregroundStyle(controlStyle)
        .accessibilityLabel("Ajustar proporção")
        .accessibilityValue(state.detent.title)
        .accessibilityIdentifier("home.panelMenu")
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

    private func mapHeight(in height: CGFloat) -> CGFloat {
        max(64, min(height * mapFraction, height - minimumContentHeight - 60))
    }

    private var mapFraction: CGFloat {
        switch state.detent {
        case .explore: 0.65
        case .balanced: 0.45
        case .details: 0.25
        }
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
