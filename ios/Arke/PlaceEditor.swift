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
                ZStack(alignment: .bottom) {
                    ScrollViewReader { proxy in
                        ScrollView {
                            VStack(alignment: .leading, spacing: 8) {
                                // Sheet contínuo: o título é o primeiro item e rola com o conteúdo.
                                Text(reviewing ? "Revisar lugar" : (original == nil ? "Adicionar lugar" : "Editar lugar"))
                                    .font(.title.bold()).foregroundStyle(ink)
                                    .fixedSize(horizontal: false, vertical: true)
                                    .padding(.bottom, 8)
                                    .accessibilityAddTraits(.isHeader).accessibilityIdentifier("place.title")
                                if !reviewing || draft.cover != nil { coverField }
                                if reviewing {
                                    VStack(alignment: .leading, spacing: 0) {
                                        summary("Nome do lugar", icon: "bookmark.fill", value: draft.normalized.name)
                                        Divider().padding(.horizontal, 16)
                                        summary("Endereço ou referência", icon: "mappin", value: draft.normalized.address)
                                        if let link = draft.normalized.link, !link.isEmpty {
                                            Divider().padding(.horizontal, 16)
                                            summary("Link", icon: "link", value: link)
                                        }
                                        Divider().padding(.horizontal, 16)
                                        summary("Nota", icon: "text.alignleft", value: draft.normalized.note.isEmpty ? "Sem nota" : draft.normalized.note)
                                    }
                                    .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 22))
                                } else {
                                    input("Nome do lugar", icon: "bookmark.fill", requirement: "Obrigatório", prompt: "Ex.: Pão de Açúcar",
                                          text: $draft.name, editorHeight: nil, minimum: 68, focus: .name, identifier: "place.name")
                                    input("Endereço ou referência", icon: "mappin", requirement: "Obrigatório", prompt: "Rua, bairro ou ponto de referência",
                                          text: $draft.address, editorHeight: 44, minimum: 68, focus: .address, identifier: "place.address")
                                    input("Nota", icon: "text.alignleft", requirement: "Opcional", prompt: "O que não pode esquecer?",
                                          text: $draft.note, editorHeight: 72, minimum: 104, focus: .note, identifier: "place.note")
                                    input("Link", icon: "link", requirement: "Opcional", prompt: "exemplo.com", text: Binding(
                                        get: { draft.link ?? "" },
                                        set: { draft.link = $0.isEmpty ? nil : $0 }
                                    ), editorHeight: nil, minimum: 68, focus: .link, identifier: "place.link")
                                    if submitted, let issue = draft.issue {
                                        Text(issue.message).arkeText(.caption).foregroundStyle(Color(uiColor: .systemRed))
                                            .padding(.horizontal, 4).accessibilityIdentifier("place.validation")
                                    }
                                }
                                if let saveError {
                                    Text(saveError).arkeText(.caption).padding(.horizontal, 4).accessibilityIdentifier("place.saveError")
                                }
                                Label(original == nil ? "Fica salvo só neste aparelho, sem backup." : "Alterações ficam salvas só neste aparelho, sem backup.", systemImage: "lock")
                                    .arkeText(.caption).foregroundStyle(.secondary).padding(.horizontal, 4).padding(.top, 6)
                                    .accessibilityIdentifier("place.infoText")
                                // Cancelar sem gesto: fim do conteúdo, como alternativa ao arrastar para baixo.
                                Button(action: cancel) {
                                    Text("Cancelar").arkeText(.button).foregroundStyle(ink)
                                        .frame(maxWidth: .infinity, minHeight: 48).contentShape(Rectangle())
                                }
                                .disabled(saving).accessibilityIdentifier("place.cancel")
                            }
                            .foregroundStyle(ink).frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 20).padding(.top, 28)
                            // Respiro para a última linha passar por baixo dos botões flutuantes.
                            .padding(.bottom, reviewing ? 156 : 96)
                        }
                        .modifier(ArkeScrollEdges())
                        .scrollDismissesKeyboard(.interactively)
                        .accessibilityIdentifier("place.fields").scrollIndicators(.hidden)
                        .onChange(of: field) { _, focused in
                            // Centraliza o campo ativo na área visível.
                            if let focused { proxy.scrollTo(focused, anchor: .center) }
                        }
                    }
                    // Antes do iOS 26 não há desvanecimento nativo da rolagem: degradê curto sob os botões flutuantes.
                    if #unavailable(iOS 26) {
                        LinearGradient(colors: [.clear, ArkeUI.sheetSurface(colorScheme)], startPoint: .top, endPoint: .bottom)
                            .frame(height: reviewing ? 150 : 96).allowsHitTesting(false)
                    }
                    VStack(spacing: 10) {
                        if reviewing {
                            Button { reviewing = false } label: {
                                Text("Editar informações").arkeText(.button).foregroundStyle(ink)
                                    .frame(maxWidth: .infinity, minHeight: 56)
                                    .arkeGlassCapsule().contentShape(Capsule())
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
                    .buttonStyle(.plain).foregroundStyle(ink)
                    .padding(.horizontal, 20).padding(.bottom, 14)
                }
                .alert("Descartar rascunho?", isPresented: $confirmingDiscard) {
                    Button("Continuar editando", role: .cancel) {}
                    Button("Descartar", role: .destructive) { onCancel() }
                } message: {
                    Text("As informações deste rascunho ainda não foram salvas.")
                }
                .accessibilityAction(.escape) { cancel() }
                .background { ArkeDismissAttempt { if !saving { field = nil; confirmingDiscard = true } } }
                .tint(actionInk)
            }
        }
        .tint(actionInk)
        .presentationDetents([.fraction(0.78), .large], selection: $editorDetent)
        .interactiveDismissDisabled(hasChanges || saving)
        .task(id: coverSelection) { await loadCover() }
    }

    /// Foto de capa: linha-botão quando vazia; miniatura com ações separadas de 48 quando existe.
    private var coverField: some View {
        VStack(alignment: .leading, spacing: 12) {
            if let data = draft.cover, let image = UIImage(data: data) {
                Image(uiImage: image).resizable().scaledToFill()
                    .frame(width: 112, height: 80).clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 16)).accessibilityHidden(true)
                if !reviewing {
                    HStack(spacing: 8) {
                        PhotosPicker(selection: $coverSelection, matching: .images) {
                            Label("Trocar foto de capa", systemImage: "photo")
                                .arkeText(.label).foregroundStyle(ink)
                                .frame(maxWidth: .infinity, minHeight: 48)
                                .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
                        }.disabled(saving)
                        Button(role: .destructive) {
                            coverSelection = nil
                            draft.cover = nil
                            coverError = false
                        } label: {
                            Text("Remover foto").arkeText(.label).foregroundStyle(Color(uiColor: .systemRed))
                                .padding(.horizontal, 16).frame(minHeight: 48)
                        }.disabled(saving)
                    }
                }
            } else if !reviewing {
                PhotosPicker(selection: $coverSelection, matching: .images) {
                    HStack(spacing: 14) {
                        Image(systemName: "photo").font(.system(size: 18))
                            .frame(width: 40, height: 40)
                            .background(Color(uiColor: .tertiarySystemFill), in: RoundedRectangle(cornerRadius: 12))
                        VStack(alignment: .leading, spacing: 0) {
                            Text("Adicionar foto de capa").arkeText(.body).fontWeight(.semibold).foregroundStyle(ink)
                                .multilineTextAlignment(.leading)
                            Text("Opcional").arkeText(.caption).foregroundStyle(.secondary)
                        }
                        Spacer(minLength: 8)
                        Image(systemName: "chevron.right").font(.system(size: 14, weight: .semibold)).foregroundStyle(.secondary)
                    }
                    .frame(minHeight: 48).contentShape(Rectangle())
                }.disabled(saving)
            }
            if !reviewing {
                if coverLoading { ProgressView("Carregando foto…") }
                if coverError { Text("Não foi possível abrir esta foto.").arkeText(.caption).foregroundStyle(Color(uiColor: .systemRed)) }
            }
        }
        .padding(.horizontal, 16).padding(.vertical, 12)
        .frame(maxWidth: .infinity, minHeight: 64, alignment: .leading)
        .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 22))
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

    /// Campo em cartão próprio: rótulo persistente com “Obrigatório/Opcional”, placeholder e a superfície inteira focando o campo.
    /// `editorHeight` nil usa `TextField` de uma linha; com valor, `TextEditor` com essa altura de texto.
    private func input(_ title: String, icon: String, requirement: String, prompt: String, text: Binding<String>,
                       editorHeight: CGFloat?, minimum: CGFloat, focus: Field, identifier: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 8) {
                Label(title, systemImage: icon).arkeText(.label).labelStyle(EditorFieldLabelStyle())
                Spacer(minLength: 8)
                Text(requirement).arkeText(.caption).foregroundStyle(.secondary)
            }
            Group {
                if let editorHeight {
                    TextEditor(text: text)
                        .scrollContentBackground(.hidden)
                        .focused($field, equals: focus)
                        .accessibilityLabel(title)
                        .accessibilityIdentifier(identifier)
                        .frame(height: editorHeight)
                        .overlay(alignment: .topLeading) {
                            if text.wrappedValue.isEmpty {
                                Text(prompt).foregroundStyle(.secondary)
                                    .padding(.top, 8).padding(.leading, 5)
                                    .allowsHitTesting(false).accessibilityHidden(true)
                            }
                        }
                        // O TextEditor traz recuo interno de 5 pt; compensa para alinhar com o rótulo e com o TextField.
                        .padding(.horizontal, -5)
                } else {
                    TextField(title, text: text, prompt: Text(prompt))
                        .focused($field, equals: focus).accessibilityLabel(title).accessibilityIdentifier(identifier)
                        .keyboardType(focus == .link ? .URL : .default)
                        .submitLabel(focus == .link ? .done : .next)
                        .onSubmit { field = focus == .link ? nil : .address }
                }
            }
            .arkeText(.body)
            .textInputAutocapitalization(focus == .link ? .never : (focus == .name ? .words : .sentences))
            .frame(maxWidth: .infinity, alignment: .topLeading)
            .disabled(saving)
        }
        .padding(.horizontal, 16).padding(.vertical, 12)
        .frame(maxWidth: .infinity, minHeight: minimum, alignment: .topLeading)
        .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: 20))
        .overlay { RoundedRectangle(cornerRadius: 20).strokeBorder(ArkeUI.ink, lineWidth: 2).opacity(field == focus ? 1 : 0).allowsHitTesting(false) }
        .contentShape(Rectangle())
        .simultaneousGesture(TapGesture().onEnded { if !saving { field = focus } })
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(identifier + ".region")
        .id(focus)
    }

    private func summary(_ title: String, icon: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Label(title, systemImage: icon).arkeText(.label).labelStyle(EditorFieldLabelStyle())
            Text(value).arkeText(.body)
                .frame(maxWidth: .infinity, alignment: .topLeading)
        }
        .padding(.horizontal, 16).padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .topLeading)
    }

    private func primary(_ title: String, identifier: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title).arkeText(.button).foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: 56)
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
        HStack(spacing: 8) {
            configuration.icon.foregroundStyle(Color(uiColor: .secondaryLabel)).frame(width: 20).accessibilityHidden(true)
            configuration.title.foregroundStyle(Color(uiColor: .secondaryLabel))
        }
    }
}
