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

    var body: some View {
        NavigationStack {
            Form {
                if reviewing {
                    Section("Confira sua ideia") {
                        Text(draft.normalized.name).font(.headline)
                        Text(draft.normalized.address)
                        if !draft.normalized.note.isEmpty { Text(draft.normalized.note) }
                    }
                    Section {
                        Text("Esta ideia ficará apenas neste aparelho. O endereço não será pesquisado nem marcado no mapa nesta etapa.")
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                    Button("Editar informações") { reviewing = false }
                        .accessibilityIdentifier("place.edit")
                    Button(saving ? "Salvando…" : "Salvar neste aparelho") { onSave(draft.normalized) }
                        .accessibilityIdentifier("place.save")
                        .disabled(saving)
                } else {
                    Section("O lugar que você quer conhecer") {
                        TextField("Nome do lugar", text: $draft.name)
                            .focused($field, equals: .name)
                            .textInputAutocapitalization(.words)
                            .submitLabel(.next).onSubmit { field = .address }
                            .accessibilityIdentifier("place.name")
                        TextField("Endereço ou referência", text: $draft.address, axis: .vertical)
                            .focused($field, equals: .address).lineLimit(2...4)
                            .textContentType(.fullStreetAddress)
                            .accessibilityIdentifier("place.address")
                        TextField("Nota opcional", text: $draft.note, axis: .vertical)
                            .focused($field, equals: .note).lineLimit(3...6)
                            .accessibilityIdentifier("place.note")
                    }
                    if submitted, let issue = draft.issue {
                        Label(issue.message, systemImage: "exclamationmark.circle").foregroundStyle(Color(uiColor: .label)).accessibilityIdentifier("place.validation")
                    }
                    Section {
                        Button("Revisar lugar") {
                            submitted = true
                            if draft.issue == nil { field = nil; reviewing = true }
                        }
                        .accessibilityIdentifier("place.review")
                    } footer: {
                        Text("Nome e endereço são obrigatórios. Você não precisa criar uma viagem agora.")
                    }
                }
                if let saveError {
                    Label(saveError, systemImage: "exclamationmark.circle").foregroundStyle(Color(uiColor: .label)).accessibilityIdentifier("place.saveError")
                }
            }
            .disabled(saving)
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle(reviewing ? "Revisar lugar" : "Adicionar lugar")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") {
                        field = nil
                        if draft.isEmpty { onCancel() } else { confirmingDiscard = true }
                    }
                    .disabled(saving).accessibilityIdentifier("place.cancel")
                }
                ToolbarItemGroup(placement: .keyboard) {
                    Spacer()
                    Button("Concluir") { field = nil }
                }
            }
            .confirmationDialog("Descartar rascunho?", isPresented: $confirmingDiscard, titleVisibility: .visible) {
                Button("Descartar", role: .destructive) { onCancel() }
                Button("Continuar editando", role: .cancel) {}
            } message: {
                Text("As informações deste rascunho ainda não foram salvas.")
            }
        }
        .tint(colorScheme == .dark ? Color(uiColor: .label) : Color(red: 76 / 255, green: 114 / 255, blue: 138 / 255))
        .interactiveDismissDisabled(!draft.isEmpty || saving)
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
