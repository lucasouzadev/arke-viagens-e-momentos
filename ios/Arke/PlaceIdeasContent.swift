import SwiftUI

struct PlaceIdeasContent: View {
    @ObservedObject var store: PlaceIdeasStore
    let onDelete: (String) -> Void
    @State private var deleting: PlaceIdea?
    @State private var selected: PlaceIdea?

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if store.ideas.isEmpty {
                Text("Sua próxima viagem começa com uma ideia").arkeText(.cardTitle).accessibilityAddTraits(.isHeader)
                Text("Guarde os lugares que você quer conhecer, mesmo antes de criar uma viagem.")
            } else {
                Text("Ideias de lugares").arkeText(.cardTitle).accessibilityAddTraits(.isHeader)
                ForEach(store.ideas) { idea in
                    VStack(alignment: .leading, spacing: 8) {
                        Text(idea.name).arkeText(.contextTitle)
                        Text(idea.address).foregroundStyle(.secondary)
                        if !idea.note.isEmpty { Text(idea.note) }
                        Button("Ver detalhes") { selected = idea }
                            .frame(minHeight: 48)
                            .accessibilityLabel("Ver detalhes de \(idea.name)")
                            .accessibilityIdentifier("place.open.\(idea.id)")
                        Button("Excluir lugar", role: .destructive) { deleting = idea }
                            .disabled(store.busy)
                            .accessibilityLabel("Excluir \(idea.name)")
                            .accessibilityIdentifier("place.delete.\(idea.id)")
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    Divider()
                }
            }
            Text("Em desenvolvimento · ideias salvas apenas neste aparelho")
                .arkeText(.caption).foregroundStyle(.secondary)
        }
        .sheet(item: $selected) { idea in
            PlaceDetails(ideas: store.ideas, initialID: idea.id, onClose: { selected = nil })
        }
        .alert("Excluir ideia?", isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } })) {
            Button("Excluir", role: .destructive) {
                if let id = deleting?.id { onDelete(id) }
                deleting = nil
            }
            Button("Cancelar", role: .cancel) { deleting = nil }
        } message: {
            Text("Esta ideia será removida deste aparelho.")
        }
    }
}

struct PlaceDetails: View {
    @Environment(\.colorScheme) private var colorScheme
    let ideas: [PlaceIdea]
    let onClose: () -> Void
    @State private var selectedID: String

    init(ideas: [PlaceIdea], initialID: String, onClose: @escaping () -> Void) {
        self.ideas = ideas
        self.onClose = onClose
        _selectedID = State(initialValue: initialID)
    }

    private var index: Int? { ideas.firstIndex { $0.id == selectedID } }
    private var actionInk: Color { colorScheme == .dark ? Color(uiColor: .label) : ArkeUI.blue }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                Button(action: onClose) {
                    Text("Fechar").arkeText(.button)
                        .frame(width: 88, height: 48, alignment: .leading).contentShape(Rectangle())
                }.accessibilityIdentifier("detail.close")
                Text("Detalhes do lugar").arkeText(.contextTitle)
                    .foregroundStyle(Color(uiColor: .label))
                    .multilineTextAlignment(.center).fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity).accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("detail.title")
                Color.clear.frame(width: 88, height: 1).accessibilityHidden(true)
            }.padding(.top, 24)
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    if let index {
                        let idea = ideas[index]
                        section("Nome do lugar", value: idea.name, title: true, identifier: "detail.name")
                        section("Endereço ou referência", value: idea.address, identifier: "detail.address")
                        section("Nota opcional", value: idea.note.isEmpty ? "Sem nota" : idea.note, identifier: "detail.note")
                    } else {
                        Text("Este lugar não está mais disponível.").arkeText(.body)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading).padding(.top, 24).padding(.bottom, 48)
            }
            .id(selectedID).accessibilityIdentifier("detail.body")
            .mask {
                VStack(spacing: 0) {
                    Rectangle()
                    LinearGradient(colors: [.white, .clear], startPoint: .top, endPoint: .bottom).frame(height: 32)
                }.allowsHitTesting(false).accessibilityHidden(true)
            }
            VStack(spacing: 12) {
                if let index {
                    Text("Lugar \(index + 1) de \(ideas.count)").arkeText(.caption).accessibilityIdentifier("detail.position")
                }
                HStack(spacing: 16) {
                    navigation("Anterior", offset: -1, enabled: (index ?? 0) > 0, id: "detail.previous")
                    navigation("Próximo", offset: 1, enabled: index.map { $0 + 1 < ideas.count } ?? false, id: "detail.next")
                }
                Text("Ideia salva neste aparelho. O endereço ainda não está associado ao mapa.")
                    .arkeText(.caption).foregroundStyle(Color(uiColor: .label))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }.padding(.bottom, 16)
        }
        .buttonStyle(.plain).foregroundStyle(actionInk).padding(.horizontal, 20)
        .background(Color(uiColor: .systemGroupedBackground))
        .presentationDetents([.fraction(0.94)]).presentationDragIndicator(.visible)
        .presentationCornerRadius(28).presentationBackground(Color(uiColor: .systemGroupedBackground))
    }

    private func section(_ label: String, value: String, title: Bool = false, identifier: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label).arkeText(.label)
            Text(value).arkeText(title ? .cardTitle : .body).accessibilityIdentifier(identifier)
                .frame(maxWidth: .infinity, alignment: .leading).padding(16)
                .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 12))
        }.foregroundStyle(Color(uiColor: .label))
    }

    private func navigation(_ title: String, offset: Int, enabled: Bool, id: String) -> some View {
        Button {
            if let index, ideas.indices.contains(index + offset) { selectedID = ideas[index + offset].id }
        } label: {
            Text(title).arkeText(.button).frame(maxWidth: .infinity, minHeight: 48).contentShape(Rectangle())
        }.disabled(!enabled).opacity(enabled ? 1 : 0.4).accessibilityIdentifier(id)
    }
}

#if DEBUG
enum DetailCapture {
    static let ideas = [
        PlaceIdea(draft: PlaceDraft(id: "capture-garden", name: "Jardim das descobertas", address: "Entrada principal, perto da praça central", note: "Reservar uma manhã para caminhar, conhecer os jardins e fazer uma pausa com calma. Esta é uma ideia de demonstração, sem ponto marcado no mapa.")),
        PlaceIdea(draft: PlaceDraft(id: "capture-museum", name: "Museu dos caminhos", address: "Avenida das ideias, 100"))
    ]
}
#endif
