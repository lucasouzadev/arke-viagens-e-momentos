import SwiftUI

@MainActor
final class PlaceIdeasStore: ObservableObject {
    @Published private(set) var ideas: [PlaceIdea] = []
    @Published private(set) var ready = false
    @Published private(set) var busy = false
    @Published private(set) var error: String?
    private static let disk = PlaceIdeasDisk(directory: FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0].appendingPathComponent("ArkeIdeas", isDirectory: true))

    func load() async {
        guard !busy else { return }
        busy = true
        defer { busy = false }
        do {
            ideas = try await Self.disk.load()
            ready = true
            error = nil
        } catch {
            ready = false
            self.error = "Não foi possível abrir seus lugares. Tente novamente; os dados existentes foram preservados."
        }
    }

    func add(_ draft: PlaceDraft) async -> Bool {
        guard ready, !busy else { return false }
        busy = true
        defer { busy = false }
        do {
            ideas = try await Self.disk.add(draft)
            error = nil
            return true
        } catch {
            self.error = "Não foi possível salvar. Seu rascunho continua aberto para tentar novamente."
            return false
        }
    }

    func update(_ draft: PlaceDraft, replacing original: PlaceIdea) async -> Bool {
        guard ready, !busy else { return false }
        busy = true
        defer { busy = false }
        do {
            ideas = try await Self.disk.update(draft, replacing: original)
            error = nil
            return true
        } catch PlaceIdeasDiskError.identityConflict {
            self.error = "Este lugar mudou ou não está mais disponível. Suas alterações continuam abertas; cancele para abrir a versão atual."
            return false
        } catch {
            self.error = "Não foi possível salvar as alterações. Seu rascunho continua aberto para tentar novamente."
            return false
        }
    }

    func remove(id: String) async {
        guard ready, !busy else { return }
        busy = true
        defer { busy = false }
        do { ideas = try await Self.disk.remove(id: id); error = nil }
        catch { self.error = "Não foi possível excluir o lugar. Tente novamente." }
    }
}
