package dev.arke.app

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A ação de cada contexto (no estado vazio, o botão do meio do cartão) abre o sheet DO CONTEXTO atual,
 * nunca "Adicionar lugar", em até 3 s. O "+" do cabeçalho some enquanto o cartão está vazio.
 */
@RunWith(AndroidJUnit4::class)
class ContextSheetsTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun contextActionOpensTheSheetOfTheCurrentContext() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).putExtra("seedTrip", true)
        ActivityScenario.launch<MainActivity>(intent).use {
            compose.waitUntil(15_000) { compose.onAllNodesWithTag("home.contextTitle").fetchSemanticsNodes().isNotEmpty() }
            val steps = listOf(
                Triple(1, "Adicionar gasto", "finance.save"),
                Triple(2, "Definir meta", "finance.save"),
                Triple(3, "Avaliar um lugar", "reviews.save"),
                Triple(4, "Nova memória", "memories.save"),
            )
            for ((position, title, primary) in steps) {
                compose.onNodeWithTag("home.contextDot.$position").performClick()
                compose.waitUntil(5_000) { compose.onAllNodes(hasText(title) and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
                assertEquals("O + deve sumir com o cartão vazio", 0, compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().size)
                val started = System.nanoTime()
                compose.onAllNodes(hasText(title) and hasClickAction()).onFirst().performClick()
                compose.waitUntil(3_000) { compose.onAllNodesWithTag(primary).fetchSemanticsNodes().isNotEmpty() }
                println("Sheet $title abriu em ${(System.nanoTime() - started) / 1_000_000} ms")
                compose.onNodeWithTag("sheet.title").assertTextEquals(title)
                assertEquals("Abriu Adicionar lugar em $title", 0, compose.onAllNodesWithText("Adicionar lugar").fetchSemanticsNodes().size)
                compose.onNodeWithTag("sheet.cancel").performClick()
                compose.waitUntil(5_000) { compose.onAllNodesWithTag("sheet.cancel").fetchSemanticsNodes().isEmpty() }
            }
        }
    }
}
