import Foundation

public enum PlaceDraftIssue: Equatable, Sendable {
    case nameRequired, nameTooLong, addressRequired, addressTooLong, noteTooLong, invalidLink
}

public struct PlaceDraft: Equatable, Sendable {
    public var id: String
    public var name: String
    public var address: String
    public var note: String
    public var link: String?
    public var cover: Data?

    public init(id: String = UUID().uuidString, name: String = "", address: String = "", note: String = "", link: String? = nil, cover: Data? = nil) {
        self.id = id
        self.name = name
        self.address = address
        self.note = note
        self.link = link
        self.cover = cover
    }

    public var normalized: PlaceDraft {
        let cleanLink = link?.trimmingCharacters(in: .whitespacesAndNewlines)
        return PlaceDraft(id: id, name: name.trimmingCharacters(in: .whitespacesAndNewlines),
                   address: address.trimmingCharacters(in: .whitespacesAndNewlines),
                   note: note.trimmingCharacters(in: .whitespacesAndNewlines),
                   link: cleanLink == "" ? nil : cleanLink, cover: cover)
    }

    public var isEmpty: Bool {
        let value = normalized
        return value.name.isEmpty && value.address.isEmpty && value.note.isEmpty && (value.link ?? "").isEmpty && value.cover == nil
    }

    public static func webURL(_ text: String?) -> URL? {
        guard let text, text.utf8.count <= 2048, let url = URL(string: text),
              ["https", "http"].contains(url.scheme?.lowercased() ?? ""),
              let host = url.host, !host.isEmpty, url.user == nil, url.password == nil else { return nil }
        return url
    }

    public var issue: PlaceDraftIssue? {
        let value = normalized
        if value.name.isEmpty { return .nameRequired }
        if value.name.unicodeScalars.count > 100 { return .nameTooLong }
        if value.address.isEmpty { return .addressRequired }
        if value.address.unicodeScalars.count > 240 { return .addressTooLong }
        if value.note.unicodeScalars.count > 1000 { return .noteTooLong }
        if let link = value.link, !link.isEmpty, Self.webURL(link) == nil { return .invalidLink }
        return nil
    }
}

/// A written travel idea. No coordinates, geocoding result or map Pin is implied.
public struct PlaceIdea: Codable, Equatable, Identifiable, Sendable {
    public let id: String
    public let name: String
    public let address: String
    public let note: String
    public let link: String?
    public let cover: Data?

    public init(draft: PlaceDraft) {
        let value = draft.normalized
        id = value.id
        name = value.name
        address = value.address
        note = value.note
        link = value.link
        cover = value.cover
    }
}
