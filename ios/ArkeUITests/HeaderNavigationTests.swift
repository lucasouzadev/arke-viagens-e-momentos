import XCTest

final class HeaderNavigationTests: XCTestCase {
    override func setUpWithError() throws { continueAfterFailure = false }

    func testClockChangesAllContextsWithoutChangingPanel() {
        let app = XCUIApplication()
        app.launch()
        let clock = app.buttons["home.contextMenu"]
        XCTAssertTrue(clock.waitForExistence(timeout: 15))
        XCTAssertEqual(app.buttons.matching(identifier: "home.panelMenu").count, 1)
        if !app.buttons["home.panelMenu"].isHittable { print(app.debugDescription) }
        XCTAssertTrue(app.buttons["home.panelMenu"].isHittable)
        app.buttons["home.panelMenu"].tap()
        app.buttons["Detalhes"].tap()
        for title in ["Gastos", "Cofrinho", "Avaliações", "Memórias", "Planejamento"] {
            XCTAssertTrue(clock.isHittable)
            clock.tap()
            let option = app.buttons[title].firstMatch
            XCTAssertTrue(option.waitForExistence(timeout: 5))
            option.tap()
            XCTAssertEqual(app.staticTexts["home.contextTitle"].label, title)
            XCTAssertEqual(app.buttons["home.panelMenu"].value as? String, "Detalhes")
        }
        app.buttons["home.panelMenu"].tap()
        app.buttons["Equilibrado"].tap()
    }

    func testAddPlaceNoticeCanBeOpenedAndClosed() {
        let app = XCUIApplication()
        app.launch()
        let add = app.buttons["home.addPlace"]
        XCTAssertTrue(add.waitForExistence(timeout: 15))
        add.tap()
        let notice = app.alerts["Adicionar lugar"]
        XCTAssertTrue(notice.waitForExistence(timeout: 5))
        notice.buttons["Entendi"].tap()
        XCTAssertFalse(notice.exists)
        XCTAssertTrue(app.buttons["home.contextMenu"].exists)
    }
}
