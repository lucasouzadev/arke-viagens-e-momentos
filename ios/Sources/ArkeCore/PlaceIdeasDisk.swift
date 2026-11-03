import Foundation

public enum PlaceIdeasDiskError: Error { case invalidData, unsupportedVersion, invalidDraft, identityConflict }

public struct LocalMapPin: Codable, Equatable, Sendable {
    public let ideaID: String
    public let latitude: Double
    public let longitude: Double
    public let tripID: String?
}

public struct LocalPinTrip: Codable, Equatable, Sendable {
    public let id: String
    public let title: String
}

public struct LocalPinPlan: Codable, Equatable, Sendable {
    public fileprivate(set) var pins: [LocalMapPin] = []
    public fileprivate(set) var trips: [LocalPinTrip] = []
}

public enum LocalPinTripChoice: Sendable {
    case none
    case existing(String)
    case create(id: String, title: String)
}

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

    public func update(_ draft: PlaceDraft, replacing original: PlaceIdea) throws -> [PlaceIdea] {
        guard draft.issue == nil, !draft.id.isEmpty, draft.id == original.id else { throw PlaceIdeasDiskError.invalidDraft }
        var ideas = try load()
        guard let index = ideas.firstIndex(where: { $0.id == original.id }) else { throw PlaceIdeasDiskError.identityConflict }
        let replacement = PlaceIdea(draft: draft)
        if ideas[index] == replacement { return ideas }
        guard ideas[index] == original else { throw PlaceIdeasDiskError.identityConflict }
        ideas[index] = replacement
        try write(ideas)
        return ideas
    }

    public func remove(id: String) throws -> [PlaceIdea] {
        let ideas = try load().filter { $0.id != id }
        try write(ideas)
        return ideas
    }

    private var pinFile: URL { directory.appendingPathComponent("pin-plan-v1.json") }

    public func loadPinPlan() throws -> LocalPinPlan {
        do {
            let data = try Data(contentsOf: pinFile)
            guard data.count <= 4_194_304 else { throw PlaceIdeasDiskError.invalidData }
            return try JSONDecoder().decode(LocalPinPlan.self, from: data)
        } catch let error as CocoaError where error.code == .fileReadNoSuchFile {
            return LocalPinPlan()
        }
    }

    public func savePin(
        ideaID: String, latitude: Double, longitude: Double, trip: LocalPinTripChoice
    ) throws -> LocalPinPlan {
        guard (-90...90).contains(latitude), (-180...180).contains(longitude),
              try load().contains(where: { $0.id == ideaID })
        else { throw PlaceIdeasDiskError.invalidDraft }

        var plan = try loadPinPlan()
        let tripID: String?
        switch trip {
        case .none:
            tripID = nil
        case .existing(let id):
            guard plan.trips.contains(where: { $0.id == id }) else { throw PlaceIdeasDiskError.identityConflict }
            tripID = id
        case .create(let id, let title):
            let name = title.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !id.isEmpty, !name.isEmpty, name.count <= 120 else { throw PlaceIdeasDiskError.invalidDraft }
            if let existing = plan.trips.first(where: { $0.id == id }) {
                guard existing.title == name else { throw PlaceIdeasDiskError.identityConflict }
            } else {
                plan.trips.append(LocalPinTrip(id: id, title: name))
            }
            tripID = id
        }

        let pin = LocalMapPin(ideaID: ideaID, latitude: latitude, longitude: longitude, tripID: tripID)
        if let index = plan.pins.firstIndex(where: { $0.ideaID == ideaID }) {
            plan.pins[index] = pin
        } else {
            plan.pins.append(pin)
        }
        try writeProtected(try JSONEncoder().encode(plan), to: pinFile)
        return plan
    }

    private func write(_ ideas: [PlaceIdea]) throws {
        try writeProtected(try JSONEncoder().encode(Snapshot(version: 1, ideas: ideas)), to: file)
    }

    private func writeProtected(_ data: Data, to file: URL) throws {
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
