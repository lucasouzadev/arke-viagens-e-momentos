import SwiftUI

struct PlaceIdeasContent: View {
    @ObservedObject var store: PlaceIdeasStore
    let onDelete: (String) -> Void
    @State private var deleting: PlaceIdea?

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if store.ideas.isEmpty {
                Text("Sua próxima viagem começa com uma ideia").font(.title2.bold()).accessibilityAddTraits(.isHeader)
                Text("Guarde os lugares que você quer conhecer, mesmo antes de criar uma viagem.")
            } else {
                Text("Ideias de lugares").font(.title2.bold()).accessibilityAddTraits(.isHeader)
                ForEach(store.ideas) { idea in
                    VStack(alignment: .leading, spacing: 8) {
                        Text(idea.name).font(.headline)
                        Text(idea.address).foregroundStyle(.secondary)
                        if !idea.note.isEmpty { Text(idea.note) }
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
                .font(.footnote).foregroundStyle(.secondary)
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
