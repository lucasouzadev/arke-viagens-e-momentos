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

    func testAddPlaceValidationAndDiscardPreserveDraftUntilConfirmed() {
        let app = XCUIApplication()
        app.launch()
        let addButton = app.buttons["home.addPlace"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 15))
        expectation(for: NSPredicate(format: "enabled == true"), evaluatedWith: addButton)
        waitForExpectations(timeout: 5)
        addButton.tap()
        let editorTitle = app.staticTexts["place.title"]
        XCTAssertTrue(editorTitle.waitForExistence(timeout: 5))
        XCTAssertLessThan(app.buttons["place.cancel"].frame.midX, editorTitle.frame.midX)
        app.buttons["place.review"].tap()
        XCTAssertTrue(app.staticTexts["Informe o nome do lugar."].exists)
        let name = app.descendants(matching: .any).matching(identifier: "place.name").firstMatch
        name.tap()
        name.typeText("Rascunho de teste")
        if app.buttons["Concluir"].exists { app.buttons["Concluir"].tap() }
        app.buttons["place.review"].tap()
        XCTAssertTrue(app.staticTexts["Informe um endereço ou referência."].exists)
        app.buttons["place.cancel"].tap()
        app.buttons["Continuar editando"].tap()
        XCTAssertEqual(name.value as? String, "Rascunho de teste")
        app.buttons["place.cancel"].tap()
        app.buttons["Descartar"].tap()
        XCTAssertTrue(app.buttons["home.contextMenu"].waitForExistence(timeout: 5))
    }

    func testSaveReviewReopenAndConfirmedDelete() {
        let app = XCUIApplication()
        app.launch()
        let addButton = app.buttons["home.addPlace"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 15))
        expectation(for: NSPredicate(format: "enabled == true"), evaluatedWith: addButton)
        waitForExpectations(timeout: 5)
        addButton.tap()
        let name = app.descendants(matching: .any).matching(identifier: "place.name").firstMatch
        name.tap()
        name.typeText("Museu de teste")
        let address = app.descendants(matching: .any).matching(identifier: "place.address").firstMatch
        address.tap()
        address.typeText("Rua de teste, 100")
        if app.buttons["Concluir"].exists { app.buttons["Concluir"].tap() }
        app.buttons["place.review"].tap()
        XCTAssertTrue(app.staticTexts["Museu de teste"].exists)
        app.buttons["place.edit"].tap()
        XCTAssertEqual(name.value as? String, "Museu de teste")
        app.buttons["place.review"].tap()
        app.buttons["place.save"].tap()
        XCTAssertTrue(app.staticTexts["place.savedNotice"].waitForExistence(timeout: 10))
        app.terminate()
        app.launch()
        XCTAssertTrue(app.staticTexts["Museu de teste"].waitForExistence(timeout: 15))
        let openDetails = app.buttons["Ver detalhes de Museu de teste"]
        if !openDetails.isHittable { app.scrollViews["home.contentBody"].swipeUp() }
        openDetails.tap()
        XCTAssertTrue(app.staticTexts["detail.name"].waitForExistence(timeout: 5))
        XCTAssertEqual(app.staticTexts["detail.name"].label, "Museu de teste")
        XCTAssertEqual(app.staticTexts["detail.address"].label, "Rua de teste, 100")
        XCTAssertFalse(app.buttons["detail.previous"].isEnabled)
        XCTAssertFalse(app.buttons["detail.next"].isEnabled)
        app.buttons["detail.close"].tap()
        let delete = app.buttons["Excluir Museu de teste"]
        delete.tap()
        app.alerts["Excluir ideia?"].buttons["Cancelar"].tap()
        XCTAssertTrue(app.staticTexts["Museu de teste"].exists)
        delete.tap()
        app.alerts["Excluir ideia?"].buttons["Excluir"].tap()
        expectation(for: NSPredicate(format: "exists == false"), evaluatedWith: app.staticTexts["Museu de teste"])
        waitForExpectations(timeout: 10)
        XCTAssertFalse(app.staticTexts["place.savedNotice"].exists)
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

    func testDetailsHaveFiniteNavigationAndReturnToHome() {
        let app = XCUIApplication()
        app.launchArguments = ["--capture-place-details"]
        app.launch()
        XCTAssertTrue(app.staticTexts["detail.position"].waitForExistence(timeout: 15))
        XCTAssertEqual(app.staticTexts["detail.position"].label, "Lugar 1 de 2")
        XCTAssertFalse(app.buttons["detail.previous"].isEnabled)
        XCTAssertLessThan(app.buttons["detail.close"].frame.midX, app.staticTexts["detail.title"].frame.midX)
        app.buttons["detail.next"].tap()
        XCTAssertEqual(app.staticTexts["detail.name"].label, "Museu dos caminhos")
        XCTAssertEqual(app.staticTexts["detail.position"].label, "Lugar 2 de 2")
        XCTAssertFalse(app.buttons["detail.next"].isEnabled)
        XCTAssertEqual(app.staticTexts["detail.note"].label, "Sem nota")
        app.buttons["detail.previous"].tap()
        XCTAssertEqual(app.staticTexts["detail.name"].label, "Jardim das descobertas")
        app.buttons["detail.close"].tap()
        XCTAssertTrue(app.buttons["home.contextMenu"].waitForExistence(timeout: 5))
        XCTAssertEqual(app.staticTexts["home.contextTitle"].label, "Planejamento")
        XCTAssertEqual(app.buttons["home.panelMenu"].value as? String, "Equilibrado")
    }

}
