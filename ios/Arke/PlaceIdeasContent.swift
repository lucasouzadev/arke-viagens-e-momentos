import SwiftUI

struct PlaceIdeasContent: View {
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject var store: PlaceIdeasStore
    let onDelete: (String) -> Void
    var onDetailsVisibilityChanged: (Bool) -> Void = { _ in }
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
                        Button { selected = idea } label: {
                            Text("Ver detalhes").arkeText(.button)
                                .foregroundStyle(colorScheme == .dark ? Color(uiColor: .label) : ArkeUI.blue)
                                .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                                .contentShape(Rectangle())
                        }
                            .buttonStyle(.plain)
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
        .onChange(of: selected?.id) { _, id in onDetailsVisibilityChanged(id != nil) }
        .onDisappear { onDetailsVisibilityChanged(false) }
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
    @State private var showingInfo = false
    @State private var contentHeight: CGFloat = 280

    init(ideas: [PlaceIdea], initialID: String, onClose: @escaping () -> Void) {
        self.ideas = ideas
        self.onClose = onClose
        _selectedID = State(initialValue: initialID)
    }

    private var index: Int? { ideas.firstIndex { $0.id == selectedID } }
    private var ink: Color { Color(uiColor: .label) }

    var body: some View {
        ArkeFloatingSheet(dismissIdentifier: "detail.dismiss", onDismiss: onClose) {
            VStack(spacing: 0) {
                HStack(spacing: 8) {
                    Text("Seus lugares").arkeText(.contextTitle).accessibilityAddTraits(.isHeader)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityIdentifier("detail.title")
                    Button { showingInfo = true } label: {
                        Image(systemName: "info.circle").font(.system(size: 22))
                            .frame(width: 48, height: 48)
                            .background(ArkeUI.insetSurface(colorScheme), in: Circle()).contentShape(Circle())
                    }
                    .accessibilityLabel("Informações sobre este lugar").accessibilityIdentifier("detail.info")
                    .popover(isPresented: $showingInfo) {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Sobre este lugar").arkeText(.contextTitle)
                            Text("Esta ideia fica só neste aparelho, sem backup. Desinstalar o app pode apagá-la. O endereço ainda não aparece no mapa.")
                                .arkeText(.body).accessibilityIdentifier("detail.infoText")
                            Button { showingInfo = false } label: {
                                Text("Entendi").arkeText(.button).frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                                    .contentShape(Rectangle())
                            }.accessibilityIdentifier("detail.infoDismiss")
                        }
                        .foregroundStyle(ink).frame(width: 240, alignment: .leading).padding(20)
                        .presentationCompactAdaptation(.popover)
                    }
                }.padding(.top, 16)
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        if let index {
                            let idea = ideas[index]
                            HStack(alignment: .top, spacing: 12) {
                                Image(systemName: "bookmark").font(.system(size: 22)).frame(width: 24, height: 28)
                                    .accessibilityHidden(true)
                                Text(idea.name).arkeText(.cardTitle).accessibilityAddTraits(.isHeader)
                                    .accessibilityValue("Lugar \(index + 1) de \(ideas.count)")
                                    .accessibilityIdentifier("detail.name")
                            }
                            VStack(alignment: .leading, spacing: 20) {
                                contentRow(icon: "mappin", value: idea.address, label: "Endereço", identifier: "detail.address")
                                if !idea.note.isEmpty {
                                    contentRow(icon: "text.alignleft", value: idea.note, label: "Nota", identifier: "detail.note")
                                }
                            }
                            .padding(20).background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 24))
                        } else {
                            Text("Este lugar não está mais disponível.").arkeText(.body)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading).padding(.top, 24).padding(.bottom, 48)
                    .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                        if abs(contentHeight - height) > 1 { contentHeight = height }
                    }
                }
                .id(selectedID).accessibilityIdentifier("detail.body")
                .mask {
                    VStack(spacing: 0) {
                        Rectangle()
                        LinearGradient(colors: [.white, .clear], startPoint: .top, endPoint: .bottom).frame(height: 32)
                    }.allowsHitTesting(false).accessibilityHidden(true)
                }
                HStack(spacing: 16) {
                    navigation("Anterior", offset: -1, enabled: (index ?? 0) > 0, id: "detail.previous", primary: false)
                    navigation("Próximo", offset: 1, enabled: index.map { $0 + 1 < ideas.count } ?? false, id: "detail.next", primary: true)
                }.padding(.top, 16).padding(.bottom, 16)
            }
            .buttonStyle(.plain).foregroundStyle(ink).padding(.horizontal, 20)
        }
        .presentationDetents([.height(max(300, contentHeight + 204))])
    }

    private func contentRow(icon: String, value: String, label: String, identifier: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon).font(.system(size: 20)).frame(width: 24)
                .foregroundStyle(Color(uiColor: .tertiaryLabel)).accessibilityHidden(true)
            Text(value).arkeText(.body).accessibilityLabel("\(label): \(value)")
                .accessibilityIdentifier(identifier).frame(maxWidth: .infinity, alignment: .leading)
        }

    }

    private func navigation(_ title: String, offset: Int, enabled: Bool, id: String, primary: Bool) -> some View {
        Button {
            if let index, ideas.indices.contains(index + offset) { selectedID = ideas[index + offset].id }
        } label: {
            Text(title).arkeText(.button).foregroundStyle(primary ? .white : ink)
                .frame(maxWidth: .infinity, minHeight: 48)
                .background(primary ? ArkeUI.blue : ArkeUI.insetSurface(colorScheme), in: Capsule())
                .contentShape(Rectangle())
        }.disabled(!enabled).opacity(enabled ? 1 : 0.4).accessibilityIdentifier(id)
    }
}

#if DEBUG
enum DetailCapture {
    static let ideas = [
        PlaceIdea(draft: PlaceDraft(id: "capture-garden", name: "Jardim das descobertas", address: "Entrada principal, perto da praça central", note: ProcessInfo.processInfo.arguments.contains("--test-long-detail") ? String(repeating: "Caminhar pelos jardins e descobrir novos caminhos. ", count: 18) : "Chegar pela manhã, caminhar pelos jardins e reservar um tempo para um café.")),
        PlaceIdea(draft: PlaceDraft(id: "capture-museum", name: "Museu dos caminhos", address: "Avenida das ideias, 100"))
    ]
}
#endif
