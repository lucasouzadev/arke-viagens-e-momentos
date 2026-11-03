import Foundation

public enum PlaceDraftIssue: Equatable, Sendable {
    case nameRequired, nameTooLong, addressRequired, addressTooLong, noteTooLong
}

public struct PlaceDraft: Equatable, Sendable {
    public var id: String
    public var name: String
    public var address: String
    public var note: String

    public init(id: String = UUID().uuidString, name: String = "", address: String = "", note: String = "") {
        self.id = id
        self.name = name
        self.address = address
        self.note = note
    }

    public var normalized: PlaceDraft {
        PlaceDraft(id: id, name: name.trimmingCharacters(in: .whitespacesAndNewlines),
                   address: address.trimmingCharacters(in: .whitespacesAndNewlines),
                   note: note.trimmingCharacters(in: .whitespacesAndNewlines))
    }

    public var isEmpty: Bool {
        let value = normalized
        return value.name.isEmpty && value.address.isEmpty && value.note.isEmpty
    }

    public var issue: PlaceDraftIssue? {
        let value = normalized
        if value.name.isEmpty { return .nameRequired }
        if value.name.unicodeScalars.count > 100 { return .nameTooLong }
        if value.address.isEmpty { return .addressRequired }
        if value.address.unicodeScalars.count > 240 { return .addressTooLong }
        if value.note.unicodeScalars.count > 1000 { return .noteTooLong }
        return nil
    }
}

/// A written travel idea. No coordinates, geocoding result or map Pin is implied.
public struct PlaceIdea: Codable, Equatable, Identifiable, Sendable {
    public let id: String
    public let name: String
    public let address: String
    public let note: String

    public init(draft: PlaceDraft) {
        let value = draft.normalized
        id = value.id
        name = value.name
        address = value.address
        note = value.note
    }
}
