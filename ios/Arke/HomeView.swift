import SwiftUI
import MapKit

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @SceneStorage("home.context") private var contextValue = TravelContext.planning.rawValue
    @SceneStorage("home.detent") private var detentValue = PanelDetent.balanced.rawValue

    private var state: HomeState {
        HomeState(
            context: TravelContext(rawValue: contextValue) ?? .planning,
            detent: PanelDetent(rawValue: detentValue) ?? .balanced
        )
    }

    var body: some View {
        GeometryReader { geometry in
            VStack(alignment: .leading, spacing: 12) {
                Text("Arke").font(.title2.bold())
                Text("Onde vamos?").font(.largeTitle.bold())
                Map(initialPosition: .region(MKCoordinateRegion(
                    center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
                    span: MKCoordinateSpan(latitudeDelta: 120, longitudeDelta: 160)
                ))) {
                }
                .mapStyle(.standard)
                .accessibilityLabel("Mapa Apple. Explore o mundo; localização pessoal desativada.")
                .frame(height: max(72, (geometry.size.height - 120) * mapFraction))
                .clipShape(RoundedRectangle(cornerRadius: 20))

                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Menu {
                            ForEach(TravelContext.allCases, id: \.self) { context in
                                Button(context.title) {
                                    var next = state
                                    next.selectContext(context)
                                    contextValue = next.context.rawValue
                                }
                            }
                        } label: {
                            Label(state.context.title, systemImage: "clock")
                                .foregroundStyle(colorScheme == .dark ? AnyShapeStyle(Color(uiColor: .label)) : AnyShapeStyle(.tint))
                                .frame(minHeight: 44)
                        }
                        .accessibilityLabel("Trocar contexto. Atual: \(state.context.title)")

                        Menu {
                            ForEach(PanelDetent.allCases, id: \.self) { detent in
                                Button(detent.title) {
                                    var next = state
                                    next.setDetent(detent)
                                    detentValue = next.detent.rawValue
                                }
                            }
                        } label: {
                            Label(state.detent.title, systemImage: "arrow.up.arrow.down")
                                .foregroundStyle(colorScheme == .dark ? AnyShapeStyle(Color(uiColor: .label)) : AnyShapeStyle(.tint))
                                .frame(minHeight: 44)
                        }
                        .accessibilityLabel("Ajustar painel. Atual: \(state.detent.title)")
                        Divider()
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
                    .padding(20)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(.background, in: RoundedRectangle(cornerRadius: 20))
            }
            .padding(16)
            .background(Color(uiColor: .systemGroupedBackground))
        }
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
