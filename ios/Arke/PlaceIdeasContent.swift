import SwiftUI

struct PlaceIdeasContent: View {
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject var store: PlaceIdeasStore
    let onDelete: (String) -> Void
    let onPin: (PlaceIdea) -> Void
    var onDetailsVisibilityChanged: (Bool) -> Void = { _ in }
    @Binding var focusedID: String?
    let detent: PanelDetent
    @State private var deleting: PlaceIdea?
    @State private var selected: PlaceIdea?
    @State private var editing: PlaceIdea?
    @State private var editSaveAttempted = false

    @ViewBuilder
    private func neighbor(_ idea: PlaceIdea?, icon: String) -> some View {
        if let idea {
            Button { focusedID = idea.id } label: {
                Label {
                    Text(idea.name)
                } icon: {
                    if let cover = idea.cover, let image = UIImage(data: cover) {
                        Image(uiImage: image).resizable().scaledToFill().frame(width: 32, height: 32).clipShape(.rect(cornerRadius: 6))
                    } else { Image(systemName: icon) }
                }
                    .lineLimit(1)
                    .frame(minHeight: 44)
            }
            .buttonStyle(.plain)
            .foregroundStyle(.secondary)
            .accessibilityLabel("Selecionar \(idea.name)")
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if store.ideas.isEmpty {
                Text("Guarde seu próximo lugar com +").arkeText(.body)
                    .foregroundStyle(.secondary).frame(maxWidth: .infinity, minHeight: 48)
            } else if detent == .explore {
                ScrollView(.horizontal) {
                    HStack(spacing: 12) {
                        ForEach(store.ideas) { idea in
                            Button {
                                focusedID = idea.id
                                selected = idea
                            } label: {
                                VStack(alignment: .leading, spacing: 5) {
                                    if let cover = idea.cover, let image = UIImage(data: cover) {
                                        Image(uiImage: image).resizable().scaledToFill()
                                            .frame(width: 80, height: 50).clipShape(.rect(cornerRadius: 8))
                                    } else {
                                        Image(systemName: "mappin").frame(width: 80, height: 50)
                                    }
                                    Text(idea.name).lineLimit(1).frame(width: 80, alignment: .leading)
                                }
                            }.buttonStyle(.plain)
                        }
                    }
                }.scrollIndicators(.hidden)
            } else {
                let index = store.ideas.firstIndex { $0.id == focusedID } ?? 0
                let idea = store.ideas[index]
                let previous = index > 0 ? store.ideas[index - 1] : nil
                let next = index + 1 < store.ideas.count ? store.ideas[index + 1] : nil
                if store.ideas.count > 1 {
                    HStack {
                        neighbor(previous, icon: "chevron.left")
                        Spacer()
                        neighbor(next, icon: "chevron.right")
                    }
                }
                VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(idea.name).arkeText(.contextTitle).frame(maxWidth: .infinity, alignment: .leading)
                            Button { editSaveAttempted = false; editing = idea } label: {
                                Image(systemName: "pencil").font(.system(size: 20)).frame(width: 48, height: 48).contentShape(Rectangle())
                            }.buttonStyle(.plain).disabled(store.busy || !store.ready)
                                .accessibilityLabel("Editar \(idea.name)").accessibilityIdentifier("place.update.\(idea.id)")
                        }
                        Text(idea.address).foregroundStyle(.secondary)
                        if !idea.note.isEmpty { Text(idea.note).lineLimit(3) }
                        if let cover = idea.cover, let image = UIImage(data: cover) {
                            GeometryReader { geometry in
                                Image(uiImage: image).resizable().scaledToFill()
                                    .frame(width: geometry.size.width - 20, height: geometry.size.height - 20)
                                    .clipped().padding(10)
                            }
                            .frame(height: detent == .details ? 210 : 148)
                            .background {
                                Color(uiColor: .systemBackground).mask { StampEdge().fill(style: FillStyle(eoFill: true)) }
                            }
                            .accessibilityHidden(true)
                        }
                        Button { onPin(idea) } label: { Label("Marcar no mapa", systemImage: "mappin") }
                        HStack(spacing: 12) {
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
                    }
                    .padding(16).frame(maxWidth: .infinity, alignment: .leading)
                    .simultaneousGesture(DragGesture(minimumDistance: 28).onEnded { value in
                        let movement = value.translation
                        guard abs(movement.width) > abs(movement.height) * 1.5 else { return }
                        if let target = movement.width < 0 ? next : previous { focusedID = target.id }
                    })
            }
        }
        .padding(.bottom, 24)
        .onChange(of: selected != nil || editing != nil) { _, visible in onDetailsVisibilityChanged(visible) }
        .onChange(of: store.ideas, initial: true) { _, ideas in
            if !ideas.contains(where: { $0.id == focusedID }) { focusedID = ideas.first?.id }
        }
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
        .alert("Excluir ideia?", isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } }), presenting: deleting) { idea in
            Button("Excluir", role: .destructive) {
                onDelete(idea.id)
                deleting = nil
            }
            Button("Cancelar", role: .cancel) { deleting = nil }
        } message: { _ in
            Text("Esta ideia será removida deste aparelho.")
        }
    }
}

struct PlaceDetails: View {
    @Environment(\.colorScheme) private var colorScheme
    let ideas: [PlaceIdea]
    let onClose: () -> Void
    @State private var selectedID: String
    @State private var contentHeight: CGFloat = 300

    init(ideas: [PlaceIdea], initialID: String, onClose: @escaping () -> Void) {
        self.ideas = ideas
        self.onClose = onClose
        _selectedID = State(initialValue: initialID)
    }

    private var index: Int? { ideas.firstIndex { $0.id == selectedID } }
    private var ink: Color { Color(uiColor: .label) }

    var body: some View {
        ArkeFloatingSheet(dismissIdentifier: "detail.dismiss") {
            ZStack(alignment: .bottom) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if let index {
                            let idea = ideas[index]
                            // Sheet contínuo: o nome é o título e rola com o conteúdo.
                            Text(idea.name).font(.title.bold()).accessibilityAddTraits(.isHeader)
                                .fixedSize(horizontal: false, vertical: true)
                                .accessibilityValue("Lugar \(index + 1) de \(ideas.count)")
                                .accessibilityIdentifier("detail.name")
                            VStack(alignment: .leading, spacing: 20) {
                                contentRow(icon: "mappin", value: idea.address, label: "Endereço", identifier: "detail.address")
                                if let url = PlaceDraft.webURL(idea.link) {
                                    Link(destination: url) { Label("Abrir link", systemImage: "link") }
                                }
                                if !idea.note.isEmpty {
                                    contentRow(icon: "text.alignleft", value: idea.note, label: "Nota", identifier: "detail.note")
                                }
                            }
                            .padding(20).frame(maxWidth: .infinity, alignment: .leading)
                            .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 24))
                            Label("Fica só neste aparelho, sem backup. Para ver no mapa, use Marcar no mapa.", systemImage: "lock")
                                .arkeText(.caption).foregroundStyle(.secondary).padding(.horizontal, 4)
                                .accessibilityIdentifier("detail.infoText")
                        } else {
                            Text("Este lugar não está mais disponível.").arkeText(.body)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20).padding(.top, 28).padding(.bottom, 96)
                    .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                        if abs(contentHeight - height) > 1 { contentHeight = height }
                    }
                }
                .id(selectedID).accessibilityIdentifier("detail.body")
                .modifier(ArkeScrollEdges())
                .scrollBounceBehavior(.basedOnSize)
                HStack(spacing: 10) {
                    if let index, index > 0 {
                        Button { selectedID = ideas[index - 1].id } label: {
                            Image(systemName: "chevron.left").font(.system(size: 20))
                                .frame(width: 56, height: 56).arkeGlassCapsule().contentShape(Circle())
                        }.accessibilityLabel("Lugar anterior").accessibilityIdentifier("detail.previous")
                    }
                    let isLast = index.map { $0 == ideas.count - 1 } ?? true
                    Button {
                        if let index, !isLast { selectedID = ideas[index + 1].id } else { onClose() }
                    } label: {
                        Text(isLast ? "Concluído" : "Próximo").arkeText(.button).foregroundStyle(.white)
                            .frame(maxWidth: .infinity, minHeight: 56)
                            .background(ArkeUI.blue, in: Capsule()).contentShape(Rectangle())
                    }.accessibilityIdentifier(isLast ? "detail.done" : "detail.next")
                }
                .buttonStyle(.plain).foregroundStyle(ink).padding(.horizontal, 20).padding(.bottom, 14)
            }
            .accessibilityAction(.escape) { onClose() }
        }
        .presentationDetents([.height(max(300, contentHeight)), .large])
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

struct StampEdge: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path(rect)
        let radius: CGFloat = 4
        for x in stride(from: rect.minX + 12, to: rect.maxX - 8, by: 12) {
            for y in [rect.minY, rect.maxY] {
                path.addEllipse(in: CGRect(x: x - radius, y: y - radius, width: 8, height: 8))
            }
        }
        return path
    }
}
