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
        compose.onNodeWithTag("place.review").scrollIntoView().performClick()
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

    @Test fun editorWholeFieldFocusAndScrollableReview() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
        compose.onNodeWithTag("home.addPlace").performClick()
        compose.onNodeWithTag("place.name").scrollIntoView().performTouchInput {
            click(Offset(width * 0.8f, height * 0.8f))
        }
        compose.onNodeWithTag("place.name").assertIsFocused().performTextInput("Lugar com toque amplo")
        compose.onNodeWithTag("place.address").scrollIntoView().performTextInput("Referência para teste")
        compose.onNodeWithTag("place.review").scrollIntoView().assertIsDisplayed().performClick()
        compose.onNodeWithText("Lugar com toque amplo").assertExists()
        compose.onNodeWithTag("place.cancel").scrollIntoView().performClick()
        compose.onNodeWithTag("place.discard").performClick()
    }

    @Test fun inlineNavigationChangesContextWithoutChangingPanel() {
        val dragDistance = compose.onRoot().fetchSemanticsNode().size.height * 0.35f
        compose.onNodeWithTag("home.panelMenu").performTouchInput {
            swipe(center, center + Offset(0f, -dragDistance), 600)
        }
        val contexts = listOf("EXPENSES" to "Gastos", "SAVINGS" to "Cofrinho", "REVIEWS" to "Avaliações", "MEMORIES" to "Memórias", "REVIEWS" to "Avaliações")
        for ((index, entry) in contexts.withIndex()) {
            val title = entry.second
            compose.onNodeWithTag("home.contextTitle").performTouchInput {
                if (index == contexts.lastIndex) swipeRight() else swipeLeft()
            }
            compose.onNodeWithTag("home.contextTitle").assertTextEquals(title)
            compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Detalhes")
        }
    }

    @Test fun addPlaceValidationAndDiscardPreserveDraftUntilConfirmed() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
        compose.onNodeWithTag("home.addPlace").performClick()
        compose.onNodeWithTag("place.info").assertDoesNotExist()
        compose.onNodeWithTag("place.infoText").scrollIntoView().assertIsDisplayed()
        compose.onNodeWithTag("place.review").scrollIntoView().performClick()
        compose.onNodeWithText("Informe o nome do lugar.").assertExists()
        compose.onNodeWithTag("place.name").scrollIntoView().performTextReplacement("Rascunho de teste")
        compose.onNodeWithTag("place.review").scrollIntoView().performClick()
        compose.onNodeWithText("Informe um endereço ou referência.").assertExists()
        // Back (e o gesto de arrastar) com rascunho abrem a confirmação em vez de fechar.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Continuar editando").performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho de teste")
        compose.onNodeWithTag("place.cancel").scrollIntoView().performClick()
        compose.onNodeWithTag("place.discard").performClick()
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
    }

    @Test fun saveReviewReloadAndConfirmedDelete() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
        compose.onNodeWithTag("home.addPlace").performClick()
        compose.onNodeWithTag("place.name").scrollIntoView().performTextReplacement("Museu de teste")
        compose.onNodeWithTag("place.address").scrollIntoView().performTextReplacement("Rua de teste, 100")
        reviewPlaceAndWaitForKeyboard()
        compose.onNode(hasText("Museu de teste") and hasAnyAncestor(hasTestTag("place.fields"))).assertExists()
        returnToPlaceFields()
        compose.onNodeWithTag("place.name").assertTextContains("Museu de teste")
        compose.onNodeWithTag("place.review").scrollIntoView().performClick()
        compose.onNodeWithTag("place.save").performClick()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.savedNotice").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.onActivity { activity -> androidx.lifecycle.ViewModelProvider(activity)[PlaceIdeasViewModel::class.java].load() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu de teste").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Ver detalhes de Museu de teste").scrollIntoView().performClick()
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu de teste")
        compose.onNodeWithTag("detail.address").assertTextEquals("Rua de teste, 100")
        compose.onNodeWithTag("detail.previous").assertDoesNotExist()
        compose.onNodeWithTag("detail.next").assertDoesNotExist()
        compose.onNodeWithTag("detail.done").assertTextEquals("Concluído").performClick()
        val edit = compose.onNodeWithContentDescription("Editar Museu de teste")
        edit.scrollIntoView().performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Museu de teste")
        compose.onNodeWithTag("place.cancel").scrollIntoView().performClick()
        compose.onNodeWithText("Descartar rascunho?").assertDoesNotExist()
        edit.scrollIntoView().performClick()
        compose.onNodeWithTag("place.name").scrollIntoView().performTextReplacement("Rascunho temporário")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho temporário")
        compose.onNodeWithTag("place.cancel").scrollIntoView().performClick()
        compose.onNodeWithText("Continuar editando").performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho temporário")
        compose.onNodeWithTag("place.cancel").scrollIntoView().performClick()
        compose.onNodeWithTag("place.discard").performClick()
        compose.onNodeWithTag("home.ideaName").assertTextEquals("Museu de teste")
        edit.scrollIntoView().performClick()
        compose.onNodeWithTag("place.name").scrollIntoView().performTextReplacement("Museu atualizado")
        compose.onNodeWithTag("place.address").scrollIntoView().performTextReplacement("Nova entrada, 200")
        reviewPlaceAndWaitForKeyboard()
        compose.onNode(hasText("Museu atualizado") and hasAnyAncestor(hasTestTag("place.fields"))).assertExists()
        returnToPlaceFields()
        compose.onNodeWithTag("place.name").assertTextContains("Museu atualizado")
        compose.onNodeWithTag("place.review").scrollIntoView().performClick()
        compose.onNodeWithTag("place.save").assertTextEquals("Salvar alterações").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.title").fetchSemanticsNodes().isEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu atualizado").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Ver detalhes de Museu atualizado").scrollIntoView().performClick()
        compose.onNodeWithTag("detail.address").assertTextEquals("Nova entrada, 200")
        compose.onNodeWithTag("detail.done").performClick()
        val delete = compose.onNodeWithContentDescription("Excluir lugar: Museu atualizado")
        delete.scrollIntoView().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithTag("home.ideaName").assertTextEquals("Museu atualizado")
        delete.scrollIntoView().performClick()
        compose.onNodeWithTag("place.confirmDelete").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu atualizado").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("place.savedNotice").assertDoesNotExist()
    }

    @Test fun handleDragsSnapAndBodyScrollDoesNotResize() {
        val handle = compose.onNodeWithTag("home.panelMenu")
        val distance = compose.onRoot().fetchSemanticsNode().size.height * 0.35f
        handle.performTouchInput { swipe(center, center + Offset(0f, -distance), 600) }
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Detalhes")
        compose.onNodeWithTag("home.contentBody").performTouchInput { swipeUp() }
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Detalhes")
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        handle.performTouchInput { swipe(center, center + Offset(0f, distance), 600) }
        handle.assertContentDescriptionEquals("Ajustar painel. Atual: Explorar")
    }

    @Test fun floatingDetailsDismissWithSwipe() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        val handle = compose.onNodeWithTag("detail.dismiss.card")
        handle.assertIsDisplayed()
        val distance = compose.activity.resources.displayMetrics.heightPixels - 100f - handle.fetchSemanticsNode().boundsInRoot.top
        handle.performTouchInput { swipe(Offset(centerX, 20f), Offset(centerX, 20f + distance), 350) }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("detail.name").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Equilibrado")
    }

    @Test fun floatingDetailsDismissWithSystemBack() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.name").assertIsDisplayed()
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
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("detail.name").fetchSemanticsNodes().isEmpty() }
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
        compose.onNodeWithTag("detail.next").assertDoesNotExist()
        compose.onNodeWithTag("detail.done").assertIsDisplayed()
    }

    @Test fun detailsHaveFiniteNavigationAndSurviveRecreation() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.close").assertDoesNotExist()
        compose.onNodeWithTag("detail.position").assertDoesNotExist()
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 1 de 2"))
        val card = compose.onNodeWithTag("detail.dismiss.card").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue(card.left >= 0f)
        org.junit.Assert.assertTrue(card.right <= compose.activity.resources.displayMetrics.widthPixels)
        org.junit.Assert.assertTrue(card.bottom <= compose.activity.resources.displayMetrics.heightPixels)
        compose.onNodeWithTag("detail.info").assertDoesNotExist()
        compose.onNodeWithTag("detail.infoText").assertExists()
        compose.onNodeWithTag("detail.close").assertDoesNotExist()
        compose.onNodeWithTag("detail.position").assertDoesNotExist()
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 1 de 2"))
        compose.onNodeWithTag("detail.previous").assertDoesNotExist()
        // O título (nome) alinha à margem do conteúdo; o endereço fica dentro do cartão, com ícone.
        org.junit.Assert.assertTrue(compose.onNodeWithTag("detail.name").fetchSemanticsNode().boundsInRoot.left <=
            compose.onNodeWithTag("detail.address").fetchSemanticsNode().boundsInRoot.left)
        org.junit.Assert.assertTrue(compose.onNodeWithTag("detail.next").fetchSemanticsNode().boundsInRoot.width > card.width * 0.8f)
        val firstCloseY = compose.onNodeWithTag("detail.dismiss.card").fetchSemanticsNode().boundsInRoot.top
        compose.onNodeWithTag("detail.next").performClick()
        compose.waitUntil(5_000) {
            compose.onNodeWithTag("detail.dismiss.card").fetchSemanticsNode().boundsInRoot.top > firstCloseY + 20f
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


    /** O conteúdo só rola quando excede a altura; com tudo visível não há ação de rolagem. */
    private fun androidx.compose.ui.test.SemanticsNodeInteraction.scrollIntoView() = apply {
        runCatching { performScrollTo() }
    }
}
