package dev.arke.app

import androidx.compose.ui.geometry.Offset
import androidx.activity.compose.setContent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun reviewPlaceAndWaitForKeyboard() {
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        // IME/layout work is outside Compose's test clock. Wait before the next physical tap.
        compose.waitUntil(10_000) {
            var keyboardVisible = true
            compose.activityRule.scenario.onActivity { activity ->
                keyboardVisible = ViewCompat.getRootWindowInsets(activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
            }
            !keyboardVisible && compose.onAllNodesWithTag("place.edit").fetchSemanticsNodes().size == 1
        }
        compose.waitForIdle()
    }

    private fun returnToPlaceFields() {
        compose.onNodeWithTag("place.edit").assertIsDisplayed().performClick()
        try {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.name").fetchSemanticsNodes().size == 1 }
        } catch (failure: Throwable) {
            compose.onRoot(useUnmergedTree = true).printToLog("ArkeEditReturnFailure")
            throw failure
        }
    }

    @Test fun clockChangesAllContextsWithoutChangingPanel() {
        compose.onNodeWithTag("home.panelMenu").performClick()
        compose.onNodeWithTag("detent.DETAILS").performClick()
        val contexts = listOf("EXPENSES" to "Gastos", "SAVINGS" to "Cofrinho", "REVIEWS" to "Avaliações", "MEMORIES" to "Memórias", "PLANNING" to "Planejamento")
        for ((id, title) in contexts) {
            compose.onNodeWithTag("home.contextMenu").performClick()
            compose.onNodeWithTag("context.$id").performScrollTo().performClick()
            compose.waitForIdle()
            compose.onNodeWithText("Concluído").performClick()
            compose.onNodeWithTag("home.contextTitle").assertTextEquals(title)
            compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Detalhes")
        }
    }

    @Test fun addPlaceValidationAndDiscardPreserveDraftUntilConfirmed() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
        compose.onNodeWithTag("home.addPlace").performClick()
        org.junit.Assert.assertTrue(
            compose.onNodeWithTag("place.cancel").fetchSemanticsNode().boundsInRoot.center.x <
                compose.onNodeWithTag("place.title").fetchSemanticsNode().boundsInRoot.center.x,
        )
        compose.onNodeWithTag("place.info").performClick()
        compose.onNodeWithTag("place.infoText").assertIsDisplayed()
        compose.onNodeWithTag("place.infoDismiss").performClick()
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithText("Informe o nome do lugar.").assertExists()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Rascunho de teste")
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithText("Informe um endereço ou referência.").assertExists()
        compose.onNodeWithTag("place.dismiss").performClick()
        compose.onNodeWithText("Continuar editando").performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho de teste")
        compose.onNodeWithTag("place.cancel").performClick()
        compose.onNodeWithTag("place.discard").performClick()
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
    }

    @Test fun saveReviewReloadAndConfirmedDelete() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
        compose.onNodeWithTag("home.addPlace").performClick()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Museu de teste")
        compose.onNodeWithTag("place.address").performScrollTo().performTextReplacement("Rua de teste, 100")
        reviewPlaceAndWaitForKeyboard()
        compose.onNodeWithText("Museu de teste").assertExists()
        returnToPlaceFields()
        compose.onNodeWithTag("place.name").assertTextContains("Museu de teste")
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithTag("place.save").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.savedNotice").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.onActivity { activity -> androidx.lifecycle.ViewModelProvider(activity)[PlaceIdeasViewModel::class.java].load() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu de teste").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Ver detalhes de Museu de teste").performScrollTo().performClick()
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu de teste")
        compose.onNodeWithTag("detail.address").assertTextEquals("Rua de teste, 100")
        compose.onNodeWithTag("detail.previous").assertDoesNotExist()
        compose.onNodeWithTag("detail.next").assertDoesNotExist()
        compose.onNodeWithTag("detail.done").assertTextEquals("Concluído").performClick()
        val edit = compose.onNodeWithContentDescription("Editar Museu de teste")
        edit.performScrollTo().performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Museu de teste")
        compose.onNodeWithTag("place.cancel").performClick()
        compose.onNodeWithText("Descartar rascunho?").assertDoesNotExist()
        edit.performScrollTo().performClick()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Rascunho temporário")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho temporário")
        compose.onNodeWithTag("place.cancel").performClick()
        compose.onNodeWithText("Continuar editando").performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho temporário")
        compose.onNodeWithTag("place.cancel").performClick()
        compose.onNodeWithTag("place.discard").performClick()
        compose.onNodeWithText("Museu de teste").assertExists()
        edit.performScrollTo().performClick()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Museu atualizado")
        compose.onNodeWithTag("place.address").performScrollTo().performTextReplacement("Nova entrada, 200")
        reviewPlaceAndWaitForKeyboard()
        compose.onNodeWithText("Museu atualizado").assertExists()
        returnToPlaceFields()
        compose.onNodeWithTag("place.name").assertTextContains("Museu atualizado")
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithTag("place.save").assertTextEquals("Salvar alterações").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.title").fetchSemanticsNodes().isEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu atualizado").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Ver detalhes de Museu atualizado").performScrollTo().performClick()
        compose.onNodeWithTag("detail.address").assertTextEquals("Nova entrada, 200")
        compose.onNodeWithTag("detail.done").performClick()
        val delete = compose.onNodeWithContentDescription("Excluir lugar: Museu atualizado")
        delete.performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Museu atualizado").assertExists()
        delete.performScrollTo().performClick()
        compose.onNodeWithTag("place.confirmDelete").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu atualizado").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("place.savedNotice").assertDoesNotExist()
    }

    @Test fun handleDragsSnapAndBodyScrollDoesNotResize() {
        val handle = compose.onNodeWithTag("home.panelMenu")
        handle.performClick()
        compose.onNodeWithTag("detent.BALANCED").performClick()
        val distance = compose.onRoot().fetchSemanticsNode().size.height * 0.35f
        handle.performTouchInput { swipe(center, center + Offset(0f, -distance), 600) }
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Detalhes")
        compose.onNodeWithTag("home.contentBody").performTouchInput { swipeUp() }
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Detalhes")
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        handle.performTouchInput { swipe(center, center + Offset(0f, distance), 600) }
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Explorar")
        handle.performClick()
        compose.onNodeWithTag("detent.BALANCED").performClick()
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Equilibrado")
    }

    @Test fun floatingDetailsDismissWithSwipe() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        val handle = compose.onNodeWithTag("detail.dismiss")
        handle.assertIsDisplayed()
        val distance = compose.activity.resources.displayMetrics.heightPixels - 100f - handle.fetchSemanticsNode().boundsInRoot.center.y
        handle.performTouchInput { swipe(center, center + Offset(0f, distance), 350) }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("detail.title").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Equilibrado")
    }

    @Test fun floatingDetailsDismissWithSystemBack() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.title").assertIsDisplayed()
        val automation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.waitForIdle(200, 5_000)
        // The API 35 CI image can show a launcher ANR over the app. Recover only
        // that identified infrastructure dialog; an Arke ANR must still fail.
        fun findText(node: android.view.accessibility.AccessibilityNodeInfo?, text: String): android.view.accessibility.AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.text?.toString() == text) return node
            for (index in 0 until node.childCount) {
                findText(node.getChild(index), text)?.let { return it }
            }
            return null
        }
        val activeWindow = automation.rootInActiveWindow
        if (activeWindow?.packageName?.toString() == "android" &&
            findText(activeWindow, "Pixel Launcher isn't responding") != null) {
            org.junit.Assert.assertTrue("Close only the known emulator launcher ANR",
                findText(activeWindow, "Close app")?.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK) == true)
        }
        compose.waitUntil(5_000) {
            automation.rootInActiveWindow?.packageName?.toString() == compose.activity.packageName
        }
        val before = automation.rootInActiveWindow?.let { "${it.packageName}/${it.className}" }
        val beforeCapture = automation.executeShellCommand("screencap -p /sdcard/arke-back-before.png")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(beforeCapture).use { it.readBytes() }
        org.junit.Assert.assertTrue(automation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
        try {
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("detail.title").fetchSemanticsNodes().isEmpty() }
        } catch (failure: androidx.compose.ui.test.ComposeTimeoutException) {
            val after = automation.rootInActiveWindow?.let { "${it.packageName}/${it.className}" }
            val command = automation.executeShellCommand("screencap -p /sdcard/arke-back-failure.png")
            android.os.ParcelFileDescriptor.AutoCloseInputStream(command).use { it.readBytes() }
            throw AssertionError("System Back did not dismiss. Active window before=$before after=$after", failure)
        }
        compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Equilibrado")
    }

    @Test fun longDetailsScrollWhileActionsRemainReachable() {
        val idea = dev.arke.core.PlaceIdea.from(dev.arke.core.PlaceDraft(
            id = "long-detail", name = "Jardim de teste", address = "Entrada principal",
            note = "Caminhar pelos jardins e descobrir novos caminhos. ".repeat(18),
        ))
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                androidx.compose.material3.MaterialTheme(typography = ArkeTypography) {
                    PlaceDetails(listOf(idea), idea.id, onClose = {})
                }
            }
        }
        val body = compose.onNodeWithTag("detail.body")
        val range = body.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange]
        org.junit.Assert.assertTrue(range.maxValue() > 0f)
        body.performTouchInput { swipeUp() }
        org.junit.Assert.assertTrue(range.value() > 0f)
        compose.onNodeWithTag("detail.dismiss").assertIsDisplayed().performClick()
    }

    @Test fun detailsHaveFiniteNavigationAndSurviveRecreation() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.close").assertDoesNotExist()
        compose.onNodeWithTag("detail.position").assertDoesNotExist()
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 1 de 2"))
        val card = compose.onNodeWithTag("detail.dismiss.card").fetchSemanticsNode().boundsInRoot
        val handle = compose.onNodeWithTag("detail.dismiss").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue(card.left >= 0f)
        org.junit.Assert.assertTrue(card.right <= compose.activity.resources.displayMetrics.widthPixels)
        org.junit.Assert.assertTrue(card.bottom < compose.activity.resources.displayMetrics.heightPixels)
        org.junit.Assert.assertTrue(handle.top >= card.top)
        compose.onNodeWithTag("detail.infoText").assertDoesNotExist()
        compose.onNodeWithTag("detail.info").performClick()
        compose.onNodeWithTag("detail.infoText").assertIsDisplayed()
        compose.onNodeWithTag("detail.infoDismiss").performClick()
        compose.onNodeWithTag("detail.infoText").assertDoesNotExist()
        compose.onNodeWithTag("detail.close").assertDoesNotExist()
        compose.onNodeWithTag("detail.position").assertDoesNotExist()
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 1 de 2"))
        compose.onNodeWithTag("detail.previous").assertDoesNotExist()
        val header = compose.onNodeWithTag("detail.title").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertEquals(card.center.x, header.center.x, 1f)
        org.junit.Assert.assertEquals(compose.onNodeWithTag("detail.name").fetchSemanticsNode().boundsInRoot.left,
            compose.onNodeWithTag("detail.address").fetchSemanticsNode().boundsInRoot.left, 1f)
        org.junit.Assert.assertTrue(compose.onNodeWithTag("detail.next").fetchSemanticsNode().boundsInRoot.width > card.width * 0.8f)
        val firstCloseY = compose.onNodeWithTag("detail.dismiss").fetchSemanticsNode().boundsInRoot.top
        compose.onNodeWithTag("detail.next").performClick()
        compose.waitUntil(5_000) {
            compose.onNodeWithTag("detail.dismiss").fetchSemanticsNode().boundsInRoot.top > firstCloseY + 20f
        }
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu dos caminhos")
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 2 de 2"))
        compose.onNodeWithTag("detail.next").assertDoesNotExist()
        compose.onNodeWithTag("detail.done").assertTextEquals("Concluído").assertIsEnabled()
        val back = compose.onNodeWithTag("detail.previous")
        back.assertContentDescriptionEquals("Anterior")
        org.junit.Assert.assertTrue(compose.onNodeWithTag("detail.done").fetchSemanticsNode().boundsInRoot.width > back.fetchSemanticsNode().boundsInRoot.width * 2)
        compose.onNodeWithTag("detail.note").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu dos caminhos")
        compose.onNodeWithTag("detail.previous").performClick()
        compose.onNodeWithTag("detail.name").assertTextEquals("Jardim das descobertas")
        compose.onNodeWithTag("detail.previous").assertDoesNotExist()
        compose.onNodeWithTag("detail.next").performClick()
        compose.onNodeWithTag("detail.done").performClick()
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Equilibrado")
    }

}
