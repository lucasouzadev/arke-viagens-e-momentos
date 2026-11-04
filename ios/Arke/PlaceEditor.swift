import SwiftUI
import PhotosUI
import ImageIO

struct PlaceEditor: View {
    @Environment(\.colorScheme) private var colorScheme
    let saving: Bool
    let saveError: String?
    let onSave: (PlaceDraft) -> Void
    let onCancel: () -> Void
    let original: PlaceIdea?
    @State private var draft = PlaceDraft()
    @State private var editorDetent: PresentationDetent = .fraction(0.78)
    @State private var reviewing = false
    @State private var submitted = false
    @State private var confirmingDiscard = false
    @State private var showingInfo = false
    @State private var coverSelection: PhotosPickerItem?
    @State private var coverLoading = false
    @State private var coverError = false
    @FocusState private var field: Field?
    private enum Field { case name, address, note, link }
    private var ink: Color { Color(uiColor: .label) }
    private var actionInk: Color { colorScheme == .dark ? ink : ArkeUI.blue }
    private var hasChanges: Bool {
        guard let original else { return !draft.isEmpty }
        return draft.normalized != PlaceDraft(id: original.id, name: original.name, address: original.address, note: original.note, link: original.link, cover: original.cover)
    }

    init(initialIdea: PlaceIdea? = nil, saving: Bool, saveError: String?, onSave: @escaping (PlaceDraft) -> Void, onCancel: @escaping () -> Void) {
        original = initialIdea
        self.saving = saving
        self.saveError = saveError
        self.onSave = onSave
        self.onCancel = onCancel
        _draft = State(initialValue: initialIdea.map { PlaceDraft(id: $0.id, name: $0.name, address: $0.address, note: $0.note, link: $0.link, cover: $0.cover) } ?? PlaceDraft())
    }

    var body: some View {
        ArkeFloatingSheet(dismissIdentifier: "place.dismiss") {
            ArkeKeyboardViewport {
                NavigationStack {
                    VStack(spacing: 0) {
                        HStack(spacing: 0) {
                            Button(action: cancel) {
                                Image(systemName: "xmark").font(.system(size: 20)).foregroundStyle(ink)
                                    .frame(width: 48, height: 48)
                                    .background(ArkeUI.insetSurface(colorScheme), in: Circle())
                                    .contentShape(Rectangle())
                            }
                            .frame(width: 76, alignment: .leading).disabled(saving).accessibilityLabel("Cancelar").accessibilityIdentifier("place.cancel")
                            Text(reviewing ? "Revisar lugar" : (original == nil ? "Adicionar lugar" : "Editar lugar"))
                                .arkeText(.contextTitle).foregroundStyle(ink)
                                .multilineTextAlignment(.center).fixedSize(horizontal: false, vertical: true)
                                .frame(maxWidth: .infinity).accessibilityAddTraits(.isHeader)
                                .accessibilityIdentifier("place.title")
                            Button { field = nil; showingInfo = true } label: {
                                Image(systemName: "info.circle").font(.system(size: 22))
                                    .frame(width: 48, height: 48)
                                    .background(ArkeUI.insetSurface(colorScheme), in: Circle())
                            }.frame(width: 76, alignment: .trailing)
                            .accessibilityLabel("Informações sobre salvar lugares").accessibilityIdentifier("place.info")
                            .popover(isPresented: $showingInfo) {
                                VStack(alignment: .leading, spacing: 12) {
                                    Text("Sobre sua ideia").arkeText(.contextTitle)
                                    Text("Nome e endereço são obrigatórios. A nota é opcional. A ideia fica só neste aparelho, sem backup; desinstalar o app pode apagá-la. O endereço não será marcado no mapa. Você pode salvar sem criar uma viagem.").arkeText(.body)
                                        .accessibilityIdentifier("place.infoText")
                                    Button("Entendi") { showingInfo = false }.frame(minHeight: 48).accessibilityIdentifier("place.infoDismiss")
                                }.foregroundStyle(ink).frame(width: 240).padding(20).presentationCompactAdaptation(.popover)
                            }
                        }
                        .buttonStyle(.plain).padding(.top, 12)
                        ScrollViewReader { proxy in
                        ScrollView {
                            VStack(spacing: 16) {
                            VStack(alignment: .leading, spacing: 16) {
                                if !reviewing || draft.cover != nil {
                                    coverField.frame(maxWidth: .infinity, alignment: .leading)
                                        .padding(16).background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 20))
                                }
                                VStack(alignment: .leading, spacing: 20) {
                                if reviewing {
                                    summary("Nome do lugar", value: draft.normalized.name, height: 56)
                                    summary("Endereço ou referência", value: draft.normalized.address, height: 96)
                                    if let link = draft.normalized.link, !link.isEmpty { summary("Link", value: link, height: 48) }
                                    summary("Nota opcional", value: draft.normalized.note.isEmpty ? "Sem nota" : draft.normalized.note, height: 120)
                                } else {
                                    input("Nome do lugar", text: $draft.name, height: 56, focus: .name, identifier: "place.name")
                                    Divider()
                                    input("Endereço ou referência", text: $draft.address, height: 96, focus: .address, identifier: "place.address")
                                    Divider()
                                    input("Nota opcional", text: $draft.note, height: 120, focus: .note, identifier: "place.note")
                                    Divider()
                                    input("Link opcional", text: Binding(
                                        get: { draft.link ?? "" },
                                        set: { draft.link = $0.isEmpty ? nil : $0 }
                                    ), height: 48, focus: .link, identifier: "place.link")
                                    if submitted, let issue = draft.issue {
                                        Text(issue.message).arkeText(.caption).accessibilityIdentifier("place.validation")
                                    }
                                }
                                }.padding(20).background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 24))
                                if let saveError {
                                    Text(saveError).arkeText(.caption).accessibilityIdentifier("place.saveError")
                                }
                            }
                            .foregroundStyle(ink).frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 24).padding(.bottom, 16)
                            }
                        }
                        .modifier(ArkeScrollEdges(active: editorDetent == .large))
                        .scrollDismissesKeyboard(.interactively)
                        .accessibilityIdentifier("place.fields").scrollIndicators(.hidden)
                        .safeAreaInset(edge: .bottom, spacing: 0) {
                            VStack(spacing: 12) {
                                if reviewing {
                                    Button { reviewing = false } label: {
                                        Text("Editar informações").arkeText(.button).foregroundStyle(ink)
                                            .frame(maxWidth: .infinity, minHeight: ArkeUI.controlHeight)
                                            .background(ArkeUI.insetSurface(colorScheme), in: Capsule()).contentShape(Rectangle())
                                    }
                                    .disabled(saving).accessibilityIdentifier("place.edit")
                                    primary(saving ? "Salvando…" : (original == nil ? "Salvar neste aparelho" : "Salvar alterações"), identifier: "place.save") { onSave(draft.normalized) }
                                } else {
                                    primary("Revisar lugar", identifier: "place.review") {
                                        submitted = true
                                        if draft.issue == nil && !coverLoading { field = nil; reviewing = true }
                                    }
                                }
                            }
                            .buttonStyle(.plain).foregroundStyle(ink).padding(.vertical, 8)
                            .background(ArkeUI.sheetSurface(colorScheme))
                        }
                        .onChange(of: field) { _, focused in
                            if let focused { proxy.scrollTo(focused, anchor: .top) }
                        }
                        }

                    }
                    .padding(.horizontal, ArkeUI.margin)
                    .toolbar(.hidden, for: .navigationBar)
                    .alert("Descartar rascunho?", isPresented: $confirmingDiscard) {
                        Button("Continuar editando", role: .cancel) {}
                        Button("Descartar", role: .destructive) { onCancel() }
                    } message: {
                        Text("As informações deste rascunho ainda não foram salvas.")
                    }
                }
                .tint(actionInk)
            }
        }
        .tint(actionInk)
        .presentationDetents([.fraction(0.78), .large], selection: $editorDetent)
        .interactiveDismissDisabled(hasChanges || saving)
        .task(id: coverSelection) { await loadCover() }
    }

    private var coverField: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let data = draft.cover, let image = UIImage(data: data) {
                Image(uiImage: image).resizable().scaledToFill()
                    .frame(width: 112, height: 80).clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 16)).accessibilityHidden(true)
            }
            if !reviewing {
                PhotosPicker(selection: $coverSelection, matching: .images) {
                    Label(draft.cover == nil ? "Adicionar foto de capa" : "Trocar foto de capa", systemImage: "photo")
                }.disabled(saving)
                if draft.cover != nil {
                    Button("Remover foto", role: .destructive) {
                        coverSelection = nil
                        draft.cover = nil
                        coverError = false
                    }.disabled(saving)
                }
                if coverLoading { ProgressView("Carregando foto…") }
                if coverError { Text("Não foi possível abrir esta foto.").arkeText(.caption) }
            }
        }
    }

    private func loadCover() async {
        guard let selection = coverSelection else { coverLoading = false; return }
        coverLoading = true
        coverError = false
        do {
            let data = try await selection.loadTransferable(type: Data.self)
            try Task.checkCancellation()
            guard coverSelection == selection else { return }
            let jpeg = data.flatMap(Self.makeCover)
            if let jpeg { draft.cover = jpeg } else { coverError = true }
        } catch {
            guard !Task.isCancelled, coverSelection == selection else { return }
            coverError = true
        }
        coverLoading = false
    }

    private static func makeCover(_ data: Data) -> Data? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil),
              let thumbnail = CGImageSourceCreateThumbnailAtIndex(source, 0, [
                kCGImageSourceCreateThumbnailFromImageAlways: true,
                kCGImageSourceCreateThumbnailWithTransform: true,
                kCGImageSourceThumbnailMaxPixelSize: 960,
              ] as CFDictionary),
              let jpeg = UIImage(cgImage: thumbnail).jpegData(compressionQuality: 0.74),
              jpeg.count <= 512_000 else { return nil }
        return jpeg
    }

    private func cancel() {
        field = nil
        guard !saving else { return }
        if !hasChanges { onCancel() } else { confirmingDiscard = true }
    }

    private func input(_ title: String, text: Binding<String>, height: CGFloat, focus: Field, identifier: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(title, systemImage: title == "Nome do lugar" ? "bookmark.fill" : (title == "Endereço ou referência" ? "mappin" : "text.alignleft"))
                .arkeText(.label).labelStyle(EditorFieldLabelStyle())
            Group {
                if focus == .name || focus == .link {
                    TextField(title, text: text, prompt: Text(""))
                        .focused($field, equals: focus).accessibilityLabel(title).accessibilityIdentifier(identifier)
                        .keyboardType(focus == .link ? .URL : .default)
                        .submitLabel(focus == .link ? .done : .next)
                        .onSubmit { field = focus == .link ? nil : .address }
                } else {
                    TextEditor(text: text)
                        .scrollContentBackground(.hidden)
                        .focused($field, equals: focus)
                        .accessibilityLabel(title)
                        .accessibilityIdentifier(identifier)
                        .frame(height: height)
                }
            }
            .arkeText(.body)
            .textInputAutocapitalization(focus == .link ? .never : (focus == .name ? .words : .sentences))
            .frame(maxWidth: .infinity, minHeight: focus == .name ? 48 : height, alignment: .topLeading)
            .disabled(saving)

        }
        .contentShape(Rectangle())
        .simultaneousGesture(TapGesture().onEnded { if !saving { field = focus } })
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier + ".region")
        .id(focus)
    }

    private func summary(_ title: String, value: String, height: CGFloat) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(title, systemImage: title == "Nome do lugar" ? "bookmark.fill" : (title == "Endereço ou referência" ? "mappin" : "text.alignleft"))
                .arkeText(.label).labelStyle(EditorFieldLabelStyle())
            Text(value).arkeText(.body)
                .frame(maxWidth: .infinity, minHeight: height, alignment: .topLeading)
        }
    }

    private func primary(_ title: String, identifier: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title).arkeText(.button).foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: ArkeUI.controlHeight)
                .background(ArkeUI.blue, in: Capsule())
                .contentShape(Rectangle())
        }
        .disabled(saving).opacity(saving ? 0.6 : 1).accessibilityIdentifier(identifier)
    }
}

private extension PlaceDraftIssue {
    var message: String {
        switch self {
        case .nameRequired: "Informe o nome do lugar."
        case .nameTooLong: "Use até 100 caracteres no nome."
        case .addressRequired: "Informe um endereço ou referência."
        case .addressTooLong: "Use até 240 caracteres no endereço."
        case .invalidLink: "Informe um endereço válido, como exemplo.com."
        case .noteTooLong: "Use até 1.000 caracteres na nota."
        }
    }
}

private struct EditorFieldLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 12) {
            configuration.icon.foregroundStyle(Color(uiColor: .tertiaryLabel)).frame(width: 24).accessibilityHidden(true)
            configuration.title
        }
    }
}
