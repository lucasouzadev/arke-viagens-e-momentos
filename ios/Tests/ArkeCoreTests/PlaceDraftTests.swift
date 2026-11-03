import XCTest
@testable import ArkeCore

final class PlaceDraftTests: XCTestCase {
    func testRequiredFieldsAndWhitespace() {
        XCTAssertEqual(PlaceDraft(name: " \n ").issue, .nameRequired)
        XCTAssertEqual(PlaceDraft(name: "Museu", address: " \n ").issue, .addressRequired)
        XCTAssertTrue(PlaceDraft(name: " ", address: "\n").isEmpty)
    }

    func testNormalizationPreservesStableIdentityAndOptionalNote() {
        let draft = PlaceDraft(id: "stable", name: " Museu ", address: " Centro\n", note: " ")
        XCTAssertNil(draft.issue)
        XCTAssertEqual(PlaceIdea(draft: draft), PlaceIdea(draft: PlaceDraft(id: "stable", name: "Museu", address: "Centro")))
        XCTAssertEqual(PlaceIdea(draft: draft).id, "stable")
    }

    func testLimitsCountUnicodeScalarsAndDoNotTruncate() {
        XCTAssertNil(PlaceDraft(name: String(repeating: "🌍", count: 100), address: "Centro").issue)
        XCTAssertEqual(PlaceDraft(name: String(repeating: "🌍", count: 101), address: "Centro").issue, .nameTooLong)
        XCTAssertEqual(PlaceDraft(name: "Museu", address: String(repeating: "a", count: 241)).issue, .addressTooLong)
        XCTAssertEqual(PlaceDraft(name: "Museu", address: "Centro", note: String(repeating: "a", count: 1001)).issue, .noteTooLong)
    }
}
