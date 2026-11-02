package dev.arke.app

import androidx.compose.ui.geometry.Offset
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun clockChangesAllContextsWithoutChangingPanel() {
        compose.onNodeWithTag("home.panelMenu").performClick()
        compose.onNodeWithTag("detent.DETAILS").performClick()
        val contexts = listOf("EXPENSES" to "Gastos", "SAVINGS" to "Cofrinho", "REVIEWS" to "Avaliações", "MEMORIES" to "Memórias", "PLANNING" to "Planejamento")
        for ((id, title) in contexts) {
            compose.onNodeWithTag("home.contextMenu").performClick()
            compose.onNodeWithTag("context.$id").assertIsDisplayed().performClick()
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
        compose.onNodeWithTag("place.review").performClick()
        compose.onNodeWithText("Informe o nome do lugar.").assertExists()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Rascunho de teste")
        compose.onNodeWithTag("place.review").performClick()
        compose.onNodeWithText("Informe um endereço ou referência.").assertExists()
        compose.onNodeWithTag("place.cancel").performClick()
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
        compose.onNodeWithTag("place.review").performClick()
        compose.onNodeWithText("Museu de teste").assertExists()
        compose.onNodeWithTag("place.edit").performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Museu de teste")
        compose.onNodeWithTag("place.review").performClick()
        compose.onNodeWithTag("place.save").performClick()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.savedNotice").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.onActivity { activity -> androidx.lifecycle.ViewModelProvider(activity)[PlaceIdeasViewModel::class.java].load() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu de teste").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Ver detalhes de Museu de teste").performScrollTo().performClick()
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu de teste")
        compose.onNodeWithTag("detail.address").assertTextEquals("Rua de teste, 100")
        compose.onNodeWithTag("detail.previous").assertIsNotEnabled()
        compose.onNodeWithTag("detail.next").assertIsNotEnabled()
        compose.onNodeWithTag("detail.dismiss").performClick()
        val delete = compose.onNodeWithContentDescription("Excluir lugar: Museu de teste")
        delete.performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Museu de teste").assertExists()
        delete.performScrollTo().performClick()
        compose.onNodeWithTag("place.confirmDelete").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu de teste").fetchSemanticsNodes().isEmpty() }
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

    @Test fun floatingDetailsDismissWithSwipeAndSystemBack() {
        compose.activityRule.scenario.onActivity { it.intent.putExtra("capturePlaceDetails", true) }
        compose.activityRule.scenario.recreate()
        val handle = compose.onNodeWithTag("detail.dismiss")
        handle.assertIsDisplayed().performTouchInput { swipe(center, center + Offset(0f, 600f), 600) }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("detail.title").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.title").assertIsDisplayed()
        androidx.test.espresso.Espresso.pressBack()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("detail.title").fetchSemanticsNodes().isEmpty() }
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
        org.junit.Assert.assertTrue(card.left > 0f)
        org.junit.Assert.assertTrue(card.right < compose.activity.resources.displayMetrics.widthPixels)
        org.junit.Assert.assertTrue(card.bottom < compose.activity.resources.displayMetrics.heightPixels)
        org.junit.Assert.assertTrue(handle.bottom <= card.top + 1f)
        compose.onNodeWithTag("detail.infoText").assertDoesNotExist()
        compose.onNodeWithTag("detail.info").performClick()
        compose.onNodeWithTag("detail.infoText").assertIsDisplayed()
        compose.onNodeWithTag("detail.infoDismiss").performClick()
        compose.onNodeWithTag("detail.infoText").assertDoesNotExist()
        compose.onNodeWithTag("detail.close").assertDoesNotExist()
        compose.onNodeWithTag("detail.position").assertDoesNotExist()
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 1 de 2"))
        compose.onNodeWithTag("detail.previous").assertIsNotEnabled()
        val firstCloseY = compose.onNodeWithTag("detail.dismiss").fetchSemanticsNode().boundsInRoot.top
        compose.onNodeWithTag("detail.next").performClick()
        compose.waitUntil(5_000) {
            compose.onNodeWithTag("detail.dismiss").fetchSemanticsNode().boundsInRoot.top > firstCloseY + 20f
        }
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu dos caminhos")
        compose.onNodeWithTag("detail.name").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Lugar 2 de 2"))
        compose.onNodeWithTag("detail.next").assertIsNotEnabled()
        compose.onNodeWithTag("detail.note").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("detail.name").assertTextEquals("Museu dos caminhos")
        compose.onNodeWithTag("detail.previous").performClick()
        compose.onNodeWithTag("detail.name").assertTextEquals("Jardim das descobertas")
        compose.onNodeWithTag("detail.dismiss").performClick()
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
        compose.onNodeWithTag("home.panelMenu").assertContentDescriptionEquals("Ajustar painel. Atual: Equilibrado")
    }

}
