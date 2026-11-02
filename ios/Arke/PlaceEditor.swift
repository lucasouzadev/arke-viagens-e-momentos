import SwiftUI
import UIKit

struct PlaceEditor: View {
    @Environment(\.colorScheme) private var colorScheme
    let saving: Bool
    let saveError: String?
    let onSave: (PlaceDraft) -> Void
    let onCancel: () -> Void
    let original: PlaceIdea?
    @State private var draft = PlaceDraft()
    @State private var keyboardOverlap: CGFloat = 0
    @State private var reviewing = false
    @State private var submitted = false
    @State private var confirmingDiscard = false
    @State private var showingInfo = false
    @FocusState private var field: Field?
    private enum Field { case name, address, note }
    private var ink: Color { Color(uiColor: .label) }
    private var actionInk: Color { colorScheme == .dark ? ink : ArkeUI.blue }
    private var hasChanges: Bool {
        guard let original else { return !draft.isEmpty }
        return draft.normalized != PlaceDraft(id: original.id, name: original.name, address: original.address, note: original.note)
    }

    init(initialIdea: PlaceIdea? = nil, saving: Bool, saveError: String?, onSave: @escaping (PlaceDraft) -> Void, onCancel: @escaping () -> Void) {
        original = initialIdea
        self.saving = saving
        self.saveError = saveError
        self.onSave = onSave
        self.onCancel = onCancel
        _draft = State(initialValue: initialIdea.map { PlaceDraft(id: $0.id, name: $0.name, address: $0.address, note: $0.note) } ?? PlaceDraft())
    }

    var body: some View {
        ArkeFloatingSheet(dismissIdentifier: "place.dismiss", enabled: !saving, onDismiss: cancel) {
            Group {
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
                    ScrollView {
                        VStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 16) {
                            VStack(alignment: .leading, spacing: 20) {
                            if reviewing {
                                summary("Nome do lugar", value: draft.normalized.name, height: 56)
                                summary("Endereço ou referência", value: draft.normalized.address, height: 96)
                                summary("Nota opcional", value: draft.normalized.note.isEmpty ? "Sem nota" : draft.normalized.note, height: 120)
                            } else {
                                input("Nome do lugar", text: $draft.name, height: 56, lines: 1...1, focus: .name, identifier: "place.name")
                                Divider()
                                input("Endereço ou referência", text: $draft.address, height: 96, lines: 2...4, focus: .address, identifier: "place.address")
                                Divider()
                                input("Nota opcional", text: $draft.note, height: 120, lines: 3...6, focus: .note, identifier: "place.note")
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
                        .padding(.top, 24).padding(.bottom, 48)
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
                                if draft.issue == nil { field = nil; reviewing = true }
                            }
                        }
                    }
                    .buttonStyle(.plain).foregroundStyle(ink).padding(.bottom, 16)
                    .padding(.bottom, 32)
                        }
                        .padding(.bottom, keyboardOverlap)
                        .background(KeyboardScrollOverlap(overlap: $keyboardOverlap).allowsHitTesting(false))
                    }
                    .scrollDismissesKeyboard(.interactively)
                    .accessibilityIdentifier("place.fields").arkeScrollEdges()

                }
                .padding(.horizontal, ArkeUI.margin)
                .background(ArkeUI.sheetSurface(colorScheme))
                .alert("Descartar rascunho?", isPresented: $confirmingDiscard) {
                    Button("Continuar editando", role: .cancel) {}
                    Button("Descartar", role: .destructive) { onCancel() }
                } message: {
                    Text("As informações deste rascunho ainda não foram salvas.")
                }
            }
        }
        .tint(actionInk)
        .presentationDetents([.fraction(0.94)])
        .interactiveDismissDisabled(hasChanges || saving)
    }

    private func cancel() {
        field = nil
        guard !saving else { return }
        if !hasChanges { onCancel() } else { confirmingDiscard = true }
    }

    private func input(_ title: String, text: Binding<String>, height: CGFloat, lines: ClosedRange<Int>, focus: Field, identifier: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(title, systemImage: title == "Nome do lugar" ? "bookmark.fill" : (title == "Endereço ou referência" ? "mappin" : "text.alignleft"))
                .arkeText(.label).labelStyle(EditorFieldLabelStyle())
            Group {
                if focus == .name {
                    TextField(title, text: text, prompt: Text(""))
                        .focused($field, equals: focus).accessibilityLabel(title).accessibilityIdentifier(identifier)
                        .submitLabel(.next).onSubmit { field = .address }
                } else {
                    ZStack(alignment: .topLeading) {
                        Text(text.wrappedValue + "\n").arkeText(.body)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 5).padding(.vertical, 8)
                            .hidden().accessibilityHidden(true)
                        TextEditor(text: text).scrollContentBackground(.hidden).scrollDisabled(true)
                            .focused($field, equals: focus).accessibilityLabel(title).accessibilityIdentifier(identifier)
                    }.frame(minHeight: height)
                }
            }
            .arkeText(.body)
            .textInputAutocapitalization(focus == .name ? .words : .sentences)
            .frame(maxWidth: .infinity, minHeight: focus == .name ? 48 : height, alignment: .topLeading)
            .disabled(saving)

        }
        .contentShape(Rectangle())
        .simultaneousGesture(TapGesture().onEnded { if !saving { field = focus } })
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

/// Fill only keyboard overlap not already handled by the native scroll insets.
private struct KeyboardScrollOverlap: UIViewRepresentable {
    @Binding var overlap: CGFloat
    func makeUIView(context: Context) -> KeyboardViewportProbe {
        let view = KeyboardViewportProbe()
        view.changed = { value in if abs(overlap - value) > 1 { overlap = value } }
        return view
    }
    func updateUIView(_ view: KeyboardViewportProbe, context: Context) {
        view.changed = { value in if abs(overlap - value) > 1 { overlap = value } }
    }
}

private final class KeyboardViewportProbe: UIView {
    var changed: ((CGFloat) -> Void)?
    private var keyboardFrame = CGRect.null
    private var lastOverlap: CGFloat = -1
    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = false
        for name in [UIResponder.keyboardWillChangeFrameNotification, UIResponder.keyboardDidChangeFrameNotification, UIResponder.keyboardWillHideNotification] {
            NotificationCenter.default.addObserver(self, selector: #selector(keyboardChanged(_:)), name: name, object: nil)
        }
    }
    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    deinit { NotificationCenter.default.removeObserver(self) }
    @objc private func keyboardChanged(_ notification: Notification) {
        keyboardFrame = notification.name == UIResponder.keyboardWillHideNotification ? .null :
            (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect ?? .null)
        updateOverlap()
    }
    override func didMoveToWindow() { super.didMoveToWindow(); updateOverlap() }
    override func layoutSubviews() { super.layoutSubviews(); updateOverlap() }
    private func updateOverlap() {
        guard let window else { return }
        var ancestor = superview
        while let candidate = ancestor, !(candidate is UIScrollView) { ancestor = candidate.superview }
        guard let scroll = ancestor as? UIScrollView else { return }
        let viewport = scroll.convert(scroll.bounds, to: window)
        let keyboard = window.convert(keyboardFrame, from: nil)
        // Floating keyboards keep their native interaction; reserve no full-width band.
        let intersection = viewport.intersection(keyboard)
        let covered = intersection.isNull || intersection.width < viewport.width * 0.8 ? 0 : intersection.height
        let value = max(0, covered - scroll.adjustedContentInset.bottom)
        guard abs(value - lastOverlap) > 1 else { return }
        lastOverlap = value
        DispatchQueue.main.async { [weak self] in self?.changed?(value) }
    }
}
