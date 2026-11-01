import XCTest
@testable import ArkeCore

final class HomeStateTests: XCTestCase {
    func testInitialHome() {
        XCTAssertEqual(HomeState(), HomeState(context: .planning, detent: .balanced))
    }

    func testAllContextsPreservePanelPosition() {
        for detent in PanelDetent.allCases {
            for context in TravelContext.allCases {
                var state = HomeState(detent: detent)
                state.selectContext(context)
                XCTAssertEqual(state, HomeState(context: context, detent: detent))
            }
        }
    }

    func testAllPanelPositionsPreserveContext() {
        for context in TravelContext.allCases {
            for detent in PanelDetent.allCases {
                var state = HomeState(context: context)
                state.setDetent(detent)
                XCTAssertEqual(state, HomeState(context: context, detent: detent))
            }
        }
    }
}
