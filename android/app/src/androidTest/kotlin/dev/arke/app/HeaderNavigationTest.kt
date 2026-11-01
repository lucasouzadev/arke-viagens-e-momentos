package dev.arke.app

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

    @Test fun addPlaceNoticeCanBeOpenedAndClosed() {
        compose.onNodeWithTag("home.addPlace").performClick()
        compose.onNodeWithText("Adicionar lugar").assertIsDisplayed()
        compose.onNodeWithText("Entendi").performClick()
        compose.onNodeWithText("Entendi").assertDoesNotExist()
        compose.onNodeWithTag("home.contextTitle").assertTextEquals("Planejamento")
    }
}
