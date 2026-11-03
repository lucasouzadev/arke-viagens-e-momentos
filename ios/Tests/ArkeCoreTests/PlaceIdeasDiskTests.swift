import XCTest
@testable import ArkeCore

final class PlaceIdeasDiskTests: XCTestCase {
    func testUpdateKeepsIdentityOrderAndRejectsStaleOrMissingIdeas() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let disk = PlaceIdeasDisk(directory: directory)
        let initial = PlaceDraft(id: "first", name: "Jardim", address: "Centro")
        let other = PlaceDraft(id: "other", name: "Museu", address: "Praça")
        _ = try await disk.add(initial)
        _ = try await disk.add(other)
        let original = PlaceIdea(draft: initial)
        let edited = PlaceDraft(id: initial.id, name: "Jardim novo", address: "Nova entrada", note: "Pela manhã")
        let expected = [PlaceIdea(draft: edited), PlaceIdea(draft: other)]
        let updated = try await disk.update(edited, replacing: original)
        XCTAssertEqual(updated, expected)
        let retry = try await disk.update(edited, replacing: original)
        XCTAssertEqual(retry, expected)
        let reopened = try await PlaceIdeasDisk(directory: directory).load()
        XCTAssertEqual(reopened, expected)
        let file = directory.appendingPathComponent("place-ideas-v1.json")
        let bytes = try Data(contentsOf: file)
        let missing = PlaceDraft(id: "missing", name: "Ausente", address: "Centro")
        for (draft, snapshot) in [
            (PlaceDraft(id: "first", name: "Versão antiga", address: "Centro"), original),
            (missing, PlaceIdea(draft: missing)),
            (PlaceDraft(id: "different", name: "Outra identidade", address: "Centro"), original),
            (PlaceDraft(id: "first", name: "", address: "Centro"), PlaceIdea(draft: edited))
        ] {
            do { _ = try await disk.update(draft, replacing: snapshot); XCTFail("Unsafe update must fail") } catch {}
            XCTAssertEqual(try Data(contentsOf: file), bytes)
        }
    }

    func testSaveReloadRetryAndDelete() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let disk = PlaceIdeasDisk(directory: directory)
        let draft = PlaceDraft(id: "fixture", name: "Museu de teste", address: "Rua de teste")
        let first = try await disk.add(draft)
        let retry = try await disk.add(draft)
        XCTAssertEqual(first, retry)
        let reopened = PlaceIdeasDisk(directory: directory)
        let restored = try await reopened.load()
        XCTAssertEqual(restored, [PlaceIdea(draft: draft)])
        let deleted = try await reopened.remove(id: draft.id)
        XCTAssertTrue(deleted.isEmpty)
        let afterDelete = try await disk.load()
        XCTAssertTrue(afterDelete.isEmpty)
    }

    func testCorruptFileCannotBeOverwrittenBySave() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        let file = directory.appendingPathComponent("place-ideas-v1.json")
        let original = Data("invalid fixture".utf8)
        try original.write(to: file)
        let disk = PlaceIdeasDisk(directory: directory)
        do {
            _ = try await disk.add(PlaceDraft(name: "Museu", address: "Centro"))
            XCTFail("A corrupt snapshot must block writes")
        } catch {}
        XCTAssertEqual(try Data(contentsOf: file), original)
    }

    func testUnknownVersionCannotBeOverwritten() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        let file = directory.appendingPathComponent("place-ideas-v1.json")
        let original = Data("{\"version\":99,\"ideas\":[]}".utf8)
        try original.write(to: file)
        do {
            _ = try await PlaceIdeasDisk(directory: directory).add(PlaceDraft(name: "Museu", address: "Centro"))
            XCTFail("An unsupported version must block writes")
        } catch {}
        XCTAssertEqual(try Data(contentsOf: file), original)
    }
}
