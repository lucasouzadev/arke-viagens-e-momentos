package dev.arke.app

import androidx.compose.ui.geometry.Offset
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
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithText("Informe o nome do lugar.").assertExists()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Rascunho de teste")
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithText("Informe um endereço ou referência.").assertExists()
        compose.onNodeWithTag("place.cancel").performScrollTo().performClick()
        compose.onNodeWithText("Continuar editando").performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Rascunho de teste")
        compose.onNodeWithTag("place.cancel").performScrollTo().performClick()
        compose.onNodeWithTag("place.discard").performClick()
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
    }

    @Test fun saveReviewReloadAndConfirmedDelete() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
        compose.onNodeWithTag("home.addPlace").performClick()
        compose.onNodeWithTag("place.name").performScrollTo().performTextReplacement("Museu de teste")
        compose.onNodeWithTag("place.address").performScrollTo().performTextReplacement("Rua de teste, 100")
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithText("Museu de teste").assertExists()
        compose.onNodeWithTag("place.edit").performScrollTo().performClick()
        compose.onNodeWithTag("place.name").assertTextContains("Museu de teste")
        compose.onNodeWithTag("place.review").performScrollTo().performClick()
        compose.onNodeWithTag("place.save").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("place.savedNotice").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.onActivity { activity -> androidx.lifecycle.ViewModelProvider(activity)[PlaceIdeasViewModel::class.java].load() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu de teste").fetchSemanticsNodes().isNotEmpty() }
        val delete = compose.onNodeWithContentDescription("Excluir lugar: Museu de teste")
        delete.performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Museu de teste").assertExists()
        delete.performScrollTo().performClick()
        compose.onNodeWithTag("place.confirmDelete").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Museu de teste").fetchSemanticsNodes().isEmpty() }
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

}
