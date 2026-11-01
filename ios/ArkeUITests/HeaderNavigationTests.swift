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
            let selectedTitle = NSPredicate(format: "label == %@", title)
            expectation(for: selectedTitle, evaluatedWith: app.staticTexts["home.contextTitle"])
            waitForExpectations(timeout: 5)
            XCTAssertEqual(app.buttons["home.panelMenu"].value as? String, "Detalhes")
        }
        app.buttons["home.panelMenu"].tap()
        app.buttons.matching(NSPredicate(format: "label == %@ OR label == %@", "Equilibrado", "Equilibrado ✓")).firstMatch.tap()
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
    func testPanelDragSnapsAndBodyScrollDoesNotResize() {
        let app = XCUIApplication()
        app.launch()
        let handle = app.buttons["home.panelMenu"]
        XCTAssertTrue(handle.waitForExistence(timeout: 15))
        handle.tap()
        app.buttons.matching(NSPredicate(format: "label == %@ OR label == %@", "Equilibrado", "Equilibrado ✓")).firstMatch.tap()
        let distance = app.frame.height * 0.35
        XCTAssertTrue(handle.isHittable)
        let window = app.coordinate(withNormalizedOffset: .zero)
        var start = window.withOffset(CGVector(dx: handle.frame.midX, dy: handle.frame.midY))
        start.press(forDuration: 0.1, thenDragTo: start.withOffset(CGVector(dx: 0, dy: -distance)))
        expectation(for: NSPredicate(format: "value == %@", "Detalhes"), evaluatedWith: handle)
        waitForExpectations(timeout: 5)
        let body = app.scrollViews["home.contentBody"]
        XCTAssertTrue(body.exists)
        body.swipeUp()
        XCTAssertEqual(handle.value as? String, "Detalhes")
        XCTAssertEqual(app.staticTexts["home.contextTitle"].label, "Planejamento")
        XCTAssertTrue(handle.isHittable)
        start = window.withOffset(CGVector(dx: handle.frame.midX, dy: handle.frame.midY))
        start.press(forDuration: 0.1, thenDragTo: start.withOffset(CGVector(dx: 0, dy: distance)))
        print("Panel after downward drag: \(String(describing: handle.value))")
        let dragCapture = XCTAttachment(screenshot: app.screenshot())
        dragCapture.lifetime = .keepAlways
        add(dragCapture)
        expectation(for: NSPredicate(format: "value == %@", "Explorar"), evaluatedWith: handle)
        waitForExpectations(timeout: 5)
        handle.tap()
        app.buttons.matching(NSPredicate(format: "label == %@ OR label == %@", "Equilibrado", "Equilibrado ✓")).firstMatch.tap()
        expectation(for: NSPredicate(format: "value == %@", "Equilibrado"), evaluatedWith: handle)
        waitForExpectations(timeout: 5)
    }

}
