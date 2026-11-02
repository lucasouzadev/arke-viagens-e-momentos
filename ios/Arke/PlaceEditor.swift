import SwiftUI

struct PlaceEditor: View {
    @Environment(\.colorScheme) private var colorScheme
    let saving: Bool
    let saveError: String?
    let onSave: (PlaceDraft) -> Void
    let onCancel: () -> Void
    @State private var draft = PlaceDraft()
    @State private var reviewing = false
    @State private var submitted = false
    @State private var confirmingDiscard = false
    @FocusState private var field: Field?
    private enum Field { case name, address, note }
    private var ink: Color { Color(uiColor: .label) }
    private var actionInk: Color { colorScheme == .dark ? ink : ArkeUI.blue }

    var body: some View {
        ArkeFloatingSheet(dismissIdentifier: "place.dismiss", enabled: !saving, onDismiss: cancel) {
            NavigationStack {
                VStack(spacing: 0) {
                    HStack(spacing: 0) {
                        Button(action: cancel) {
                            Text("Cancelar").arkeText(.button).foregroundStyle(actionInk)
                                .frame(width: 76, height: ArkeUI.controlHeight, alignment: .leading)
                                .contentShape(Rectangle())
                        }
                        .disabled(saving).accessibilityIdentifier("place.cancel")
                        Text(reviewing ? "Revisar lugar" : "Adicionar lugar")
                            .arkeText(.contextTitle).foregroundStyle(ink)
                            .multilineTextAlignment(.center).fixedSize(horizontal: false, vertical: true)
                            .frame(maxWidth: .infinity).accessibilityAddTraits(.isHeader)
                            .accessibilityIdentifier("place.title")
                        Color.clear.frame(width: 76, height: 1).accessibilityHidden(true)
                    }
                    .buttonStyle(.plain).padding(.top, 24)
                    ScrollView {
                        VStack(alignment: .leading, spacing: 16) {
                            Text(reviewing ? "Confira sua ideia" : "O lugar que você quer conhecer")
                                .arkeText(.body).fontWeight(.semibold).accessibilityAddTraits(.isHeader)
                            if reviewing {
                                summary("Nome do lugar", value: draft.normalized.name, height: 56)
                                summary("Endereço ou referência", value: draft.normalized.address, height: 96)
                                summary("Nota opcional", value: draft.normalized.note.isEmpty ? "Sem nota" : draft.normalized.note, height: 120)
                            } else {
                                input("Nome do lugar", text: $draft.name, height: 56, lines: 1...1, focus: .name, identifier: "place.name")
                                input("Endereço ou referência", text: $draft.address, height: 96, lines: 2...4, focus: .address, identifier: "place.address")
                                input("Nota opcional", text: $draft.note, height: 120, lines: 3...6, focus: .note, identifier: "place.note")
                                if submitted, let issue = draft.issue {
                                    Text(issue.message).arkeText(.caption).accessibilityIdentifier("place.validation")
                                }
                            }
                            if let saveError {
                                Text(saveError).arkeText(.caption).accessibilityIdentifier("place.saveError")
                            }
                        }
                        .foregroundStyle(ink).frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 24).padding(.bottom, 48)
                    }
                    .scrollDismissesKeyboard(.interactively).accessibilityIdentifier("place.fields")
                    .mask {
                        VStack(spacing: 0) {
                            Rectangle()
                            LinearGradient(colors: [.white, .clear], startPoint: .top, endPoint: .bottom)
                                .frame(height: 32)
                        }
                        .allowsHitTesting(false).accessibilityHidden(true)
                    }
                    VStack(spacing: 12) {
                        if reviewing {
                            Button { reviewing = false } label: {
                                Text("Editar informações").arkeText(.button).foregroundStyle(actionInk)
                                    .frame(maxWidth: .infinity, minHeight: ArkeUI.controlHeight)
                                    .contentShape(Rectangle())
                            }
                            .disabled(saving).accessibilityIdentifier("place.edit")
                            primary(saving ? "Salvando…" : "Salvar neste aparelho", identifier: "place.save") { onSave(draft.normalized) }
                            Text("Esta ideia ficará apenas neste aparelho, sem backup. Desinstalar o app pode removê-la. O endereço não será pesquisado nem marcado no mapa nesta etapa.")
                                .arkeText(.caption).frame(maxWidth: .infinity, alignment: .leading)
                        } else {
                            primary("Revisar lugar", identifier: "place.review") {
                                submitted = true
                                if draft.issue == nil { field = nil; reviewing = true }
                            }
                            Text("Nome e endereço são obrigatórios. Você não precisa criar uma viagem agora.")
                                .arkeText(.caption).frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .buttonStyle(.plain).foregroundStyle(ink).padding(.bottom, 16)
                }
                .padding(.horizontal, ArkeUI.margin)
                .background(ArkeUI.sheetSurface(colorScheme))
                .toolbar(.hidden, for: .navigationBar)
                .toolbar {
                    ToolbarItemGroup(placement: .keyboard) {
                        Spacer()
                        Button("Concluir") { field = nil }
                    }
                }
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
        .interactiveDismissDisabled(!draft.isEmpty || saving)
    }

    private func cancel() {
        field = nil
        if draft.isEmpty { onCancel() } else { confirmingDiscard = true }
    }

    private func input(_ title: String, text: Binding<String>, height: CGFloat, lines: ClosedRange<Int>, focus: Field, identifier: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).arkeText(.label)
            TextField(title, text: text, prompt: Text(""), axis: focus == .name ? .horizontal : .vertical)
                .arkeText(.body).focused($field, equals: focus)
                .lineLimit(lines).textInputAutocapitalization(focus == .name ? .words : .sentences)
                .submitLabel(focus == .name ? .next : .return)
                .onSubmit { if focus == .name { field = .address } }
                .padding(16).frame(maxWidth: .infinity, minHeight: height, alignment: .topLeading)
                .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: ArkeUI.fieldRadius))
                .disabled(saving).accessibilityLabel(title).accessibilityIdentifier(identifier)
        }
    }

    private func summary(_ title: String, value: String, height: CGFloat) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).arkeText(.label)
            Text(value).arkeText(.body)
                .padding(16).frame(maxWidth: .infinity, minHeight: height, alignment: .topLeading)
                .background(ArkeUI.insetSurface(colorScheme), in: RoundedRectangle(cornerRadius: ArkeUI.fieldRadius))
        }
    }

    private func primary(_ title: String, identifier: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title).arkeText(.button).foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: ArkeUI.controlHeight)
                .background(ArkeUI.blue, in: RoundedRectangle(cornerRadius: ArkeUI.fieldRadius))
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
