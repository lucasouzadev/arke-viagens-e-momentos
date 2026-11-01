import XCTest
@testable import ArkeCore

final class PlaceIdeasDiskTests: XCTestCase {
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
