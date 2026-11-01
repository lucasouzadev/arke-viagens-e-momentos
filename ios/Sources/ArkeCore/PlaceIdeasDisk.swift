import Foundation

public enum PlaceIdeasDiskError: Error { case invalidData, unsupportedVersion, invalidDraft, identityConflict }

/// Serialized local writes. A failed read never becomes an empty collection to overwrite.
public actor PlaceIdeasDisk {
    private struct Snapshot: Codable { var version: Int; var ideas: [PlaceIdea] }
    private let directory: URL
    private var file: URL { directory.appendingPathComponent("place-ideas-v1.json") }

    public init(directory: URL) { self.directory = directory }

    public func load() throws -> [PlaceIdea] {
        guard FileManager.default.fileExists(atPath: file.path) else { return [] }
        let attributes = try FileManager.default.attributesOfItem(atPath: file.path)
        guard ((attributes[.size] as? NSNumber)?.intValue ?? Int.max) <= 4_194_304 else { throw PlaceIdeasDiskError.invalidData }
        let snapshot = try JSONDecoder().decode(Snapshot.self, from: Data(contentsOf: file))
        guard snapshot.version == 1 else { throw PlaceIdeasDiskError.unsupportedVersion }
        guard Set(snapshot.ideas.map(\.id)).count == snapshot.ideas.count,
              snapshot.ideas.allSatisfy({ !$0.id.isEmpty && PlaceDraft(id: $0.id, name: $0.name, address: $0.address, note: $0.note).issue == nil })
        else { throw PlaceIdeasDiskError.invalidData }
        return snapshot.ideas
    }

    public func add(_ draft: PlaceDraft) throws -> [PlaceIdea] {
        guard draft.issue == nil, !draft.id.isEmpty else { throw PlaceIdeasDiskError.invalidDraft }
        var ideas = try load()
        let idea = PlaceIdea(draft: draft)
        if let existing = ideas.first(where: { $0.id == idea.id }) {
            guard existing == idea else { throw PlaceIdeasDiskError.identityConflict }
            return ideas
        }
        ideas.append(idea)
        try write(ideas)
        return ideas
    }

    public func remove(id: String) throws -> [PlaceIdea] {
        let ideas = try load().filter { $0.id != id }
        try write(ideas)
        return ideas
    }

    private func write(_ ideas: [PlaceIdea]) throws {
        let data = try JSONEncoder().encode(Snapshot(version: 1, ideas: ideas))
        guard data.count <= 4_194_304 else { throw PlaceIdeasDiskError.invalidData }
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        var excludedDirectory = directory
        var values = URLResourceValues()
        values.isExcludedFromBackup = true
        try excludedDirectory.setResourceValues(values)
        #if os(iOS)
        try data.write(to: file, options: [.atomic, .completeFileProtectionUntilFirstUserAuthentication])
        #else
        try data.write(to: file, options: .atomic)
        #endif
    }
}
