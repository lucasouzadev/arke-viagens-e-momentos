import SwiftUI

struct PlaceIdeasContent: View {
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject var store: PlaceIdeasStore
    let onDelete: (String) -> Void
    var onDetailsVisibilityChanged: (Bool) -> Void = { _ in }
    @State private var deleting: PlaceIdea?
    @State private var selected: PlaceIdea?
    @State private var editing: PlaceIdea?
    @State private var editSaveAttempted = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if store.ideas.isEmpty {
                Text("Qual lugar você quer conhecer?").arkeText(.cardTitle).accessibilityAddTraits(.isHeader)
                Text("Toque em + para guardar uma ideia.").foregroundStyle(.secondary).padding(.vertical, 20)
            } else {
                ForEach(store.ideas) { idea in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(idea.name).arkeText(.contextTitle).frame(maxWidth: .infinity, alignment: .leading)
                            Button { editSaveAttempted = false; editing = idea } label: {
                                Image(systemName: "pencil").font(.system(size: 20)).frame(width: 48, height: 48).contentShape(Rectangle())
                            }.buttonStyle(.plain).disabled(store.busy || !store.ready)
                                .accessibilityLabel("Editar \(idea.name)").accessibilityIdentifier("place.update.\(idea.id)")
                        }
                        Text(idea.address).foregroundStyle(.secondary)
                        if !idea.note.isEmpty { Text(idea.note) }
                        Button { selected = idea } label: {
                            Label("Abrir", systemImage: "arrow.up.right").arkeText(.button)
                                .foregroundStyle(colorScheme == .dark ? Color(uiColor: .label) : ArkeUI.blue)
                                .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                                .contentShape(Rectangle())
                        }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Ver detalhes de \(idea.name)")
                            .accessibilityIdentifier("place.open.\(idea.id)")
                        Button(role: .destructive) { deleting = idea } label: {
                            Image(systemName: "trash").frame(width: 48, height: 48)
                                .background(ArkeUI.sheetSurface(colorScheme), in: Circle())
                        }
                            .disabled(store.busy)
                            .accessibilityLabel("Excluir \(idea.name)")
                            .accessibilityIdentifier("place.delete.\(idea.id)")
                    }
                    .padding(16).frame(maxWidth: .infinity, alignment: .leading)
                    .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 24))
                }
            }
        }
        .onChange(of: selected != nil || editing != nil) { _, visible in onDetailsVisibilityChanged(visible) }
        .onDisappear { onDetailsVisibilityChanged(false) }
        .sheet(item: $selected) { idea in
            PlaceDetails(ideas: store.ideas, initialID: idea.id, onClose: { selected = nil })
        }
        .sheet(item: $editing) { original in
            PlaceEditor(initialIdea: original, saving: store.busy, saveError: editSaveAttempted ? store.error : nil, onSave: { draft in
                editSaveAttempted = true
                Task { if await store.update(draft, replacing: original) { editing = nil } }
            }, onCancel: { editing = nil; Task { await store.load() } })
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
                HStack(spacing: 0) {
                    Color.clear.frame(width: 48, height: 1).accessibilityHidden(true)
                    Text("Seus lugares").arkeText(.contextTitle).accessibilityAddTraits(.isHeader)
                        .multilineTextAlignment(.center).frame(maxWidth: .infinity)
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
                                Image(systemName: "bookmark.fill").font(.system(size: 22)).frame(width: 24, height: 28)
                                    .accessibilityIdentifier("detail.nameIcon").accessibilityHidden(true)
                                Text(idea.name).arkeText(.cardTitle).accessibilityAddTraits(.isHeader)
                                    .accessibilityValue("Lugar \(index + 1) de \(ideas.count)")
                                    .accessibilityIdentifier("detail.name")
                            }.padding(.horizontal, 20)
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
                .arkeScrollEdges()
                HStack(spacing: 16) {
                    if let index, index > 0 {
                        Button { selectedID = ideas[index - 1].id } label: {
                            Image(systemName: "chevron.left").font(.system(size: 20))
                                .frame(width: 48, height: 48)
                                .background(ArkeUI.insetSurface(colorScheme), in: Circle())
                        }.accessibilityLabel("Lugar anterior").accessibilityIdentifier("detail.previous")
                    }
                    let isLast = index.map { $0 == ideas.count - 1 } ?? true
                    Button {
                        if let index, !isLast { selectedID = ideas[index + 1].id } else { onClose() }
                    } label: {
                        Text(isLast ? "Concluído" : "Próximo").arkeText(.button).foregroundStyle(.white)
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .background(ArkeUI.blue, in: Capsule()).contentShape(Rectangle())
                    }.accessibilityIdentifier(isLast ? "detail.done" : "detail.next")
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

}

#if DEBUG
enum DetailCapture {
    static let ideas = [
        PlaceIdea(draft: PlaceDraft(id: "capture-garden", name: "Jardim das descobertas", address: "Entrada principal, perto da praça central", note: ProcessInfo.processInfo.arguments.contains("--test-long-detail") ? String(repeating: "Caminhar pelos jardins e descobrir novos caminhos. ", count: 18) : "Chegar pela manhã, caminhar pelos jardins e reservar um tempo para um café.")),
        PlaceIdea(draft: PlaceDraft(id: "capture-museum", name: "Museu dos caminhos", address: "Avenida das ideias, 100"))
    ]
}
#endif
