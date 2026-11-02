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
            expectation(for: NSPredicate { _, _ in clock.isHittable }, evaluatedWith: clock)
            waitForExpectations(timeout: 5)
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
        app.buttons["place.dismiss"].tap()
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
        XCTAssertEqual(app.staticTexts["detail.address"].label, "Endereço: Rua de teste, 100")
        XCTAssertFalse(app.buttons["detail.previous"].isEnabled)
        XCTAssertFalse(app.buttons["detail.next"].isEnabled)
        app.buttons["detail.dismiss"].tap()
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

    func testFloatingDetailsDismissWithSwipe() {
        let app = XCUIApplication()
        app.launchArguments = ["--capture-place-details"]
        app.launch()
        let handle = app.buttons["detail.dismiss"]
        XCTAssertTrue(handle.waitForExistence(timeout: 15))
        let window = app.coordinate(withNormalizedOffset: .zero)
        let start = window.withOffset(CGVector(dx: handle.frame.midX, dy: handle.frame.midY))
        let end = window.withOffset(CGVector(dx: handle.frame.midX, dy: app.frame.maxY - 30))
        start.press(forDuration: 0.1, thenDragTo: end)
        expectation(for: NSPredicate(format: "exists == false"), evaluatedWith: handle)
        waitForExpectations(timeout: 5)
        XCTAssertTrue(app.buttons["home.contextMenu"].isHittable)
        XCTAssertEqual(app.buttons["home.panelMenu"].value as? String, "Equilibrado")
    }

    func testLongDetailsScrollWhileActionsRemainReachable() {
        let app = XCUIApplication()
        app.launchArguments = ["--capture-place-details", "--test-long-detail"]
        app.launch()
        let note = app.staticTexts["detail.note"]
        XCTAssertTrue(note.waitForExistence(timeout: 15))
        let body = app.scrollViews["detail.body"]
        XCTAssertGreaterThan(note.frame.maxY, body.frame.maxY)
        let originalY = note.frame.minY
        body.swipeUp()
        XCTAssertLessThan(note.frame.minY, originalY)
        XCTAssertTrue(app.buttons["detail.next"].isHittable)
        XCTAssertTrue(app.buttons["detail.dismiss"].isHittable)
        app.buttons["detail.next"].tap()
        XCTAssertEqual(app.staticTexts["detail.name"].label, "Museu dos caminhos")
    }

    func testDetailsHaveFiniteNavigationAndReturnToHome() {
        let app = XCUIApplication()
        app.launchArguments = ["--capture-place-details"]
        app.launch()
        XCTAssertTrue(app.staticTexts["detail.name"].waitForExistence(timeout: 15))
        XCTAssertFalse(app.buttons["detail.close"].exists)
        XCTAssertFalse(app.staticTexts["detail.position"].exists)
        let card = app.otherElements["detail.dismiss.card"]
        XCTAssertTrue(card.exists)
        XCTAssertGreaterThan(card.frame.minX, app.frame.minX)
        XCTAssertLessThan(card.frame.maxX, app.frame.maxX)
        XCTAssertLessThan(card.frame.maxY, app.frame.maxY)
        XCTAssertLessThanOrEqual(app.buttons["detail.dismiss"].frame.maxY, card.frame.minY + 1)
        XCTAssertLessThan(app.staticTexts["detail.title"].frame.minX, app.buttons["detail.info"].frame.minX)
        XCTAssertEqual(app.staticTexts["detail.name"].value as? String, "Lugar 1 de 2")
        XCTAssertFalse(app.staticTexts["detail.infoText"].exists)
        app.buttons["detail.info"].tap()
        XCTAssertTrue(app.staticTexts["detail.infoText"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["detail.infoText"].label.contains("sem backup"))
        app.buttons["detail.infoDismiss"].tap()
        XCTAssertFalse(app.staticTexts["detail.infoText"].exists)
        XCTAssertEqual(app.staticTexts["detail.name"].value as? String, "Lugar 1 de 2")
        XCTAssertFalse(app.buttons["detail.previous"].isEnabled)
        XCTAssertTrue(app.buttons["detail.dismiss"].isHittable)
        let firstCloseY = app.buttons["detail.dismiss"].frame.minY
        app.buttons["detail.next"].tap()
        XCTAssertEqual(app.staticTexts["detail.name"].label, "Museu dos caminhos")
        expectation(for: NSPredicate { _, _ in
            app.buttons["detail.dismiss"].frame.minY > firstCloseY + 20
        }, evaluatedWith: app.buttons["detail.dismiss"])
        waitForExpectations(timeout: 5)
        XCTAssertEqual(app.staticTexts["detail.name"].value as? String, "Lugar 2 de 2")
        XCTAssertFalse(app.buttons["detail.next"].isEnabled)
        XCTAssertFalse(app.staticTexts["detail.note"].exists)
        app.buttons["detail.previous"].tap()
        XCTAssertEqual(app.staticTexts["detail.name"].label, "Jardim das descobertas")
        app.buttons["detail.dismiss"].tap()
        XCTAssertTrue(app.buttons["home.contextMenu"].waitForExistence(timeout: 5))
        XCTAssertEqual(app.staticTexts["home.contextTitle"].label, "Planejamento")
        XCTAssertEqual(app.buttons["home.panelMenu"].value as? String, "Equilibrado")
    }

}
