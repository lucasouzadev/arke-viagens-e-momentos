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

/** O "+" do cabeçalho abre o sheet DO CONTEXTO atual (nunca "Adicionar lugar"), com tempo máximo de 3 s. */
@RunWith(AndroidJUnit4::class)
class ContextSheetsTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun headerPlusOpensTheSheetOfTheCurrentContext() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).putExtra("seedTrip", true)
        ActivityScenario.launch<MainActivity>(intent).use {
            compose.waitUntil(15_000) {
                compose.onAllNodesWithTag("home.addPlace").fetchSemanticsNodes().any {
                    !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)
                }
            }
            val steps = listOf(
                Triple("Adicionar gasto", "Adicionar gasto", "finance.save"),
                Triple("Adicionar reserva ou meta", "Definir meta", "finance.save"),
                Triple("Avaliar um lugar", "Avaliar um lugar", "reviews.save"),
                Triple("Nova memória", "Nova memória", "memories.save"),
            )
            for ((label, title, primary) in steps) {
                compose.onNodeWithTag("home.contextNext").performClick()
                compose.waitUntil(5_000) { compose.onAllNodesWithContentDescription(label).fetchSemanticsNodes().isNotEmpty() }
                val started = System.nanoTime()
                compose.onNodeWithTag("home.addPlace").performClick()
                compose.waitUntil(3_000) { compose.onAllNodesWithTag(primary).fetchSemanticsNodes().isNotEmpty() }
                println("Sheet $title abriu em ${(System.nanoTime() - started) / 1_000_000} ms")
                compose.onNodeWithTag("sheet.title").assertTextEquals(title)
                assertEquals("Abriu Adicionar lugar em $label", 0, compose.onAllNodesWithText("Adicionar lugar").fetchSemanticsNodes().size)
                compose.onNodeWithTag("sheet.cancel").performClick()
                compose.waitUntil(5_000) { compose.onAllNodesWithTag("sheet.cancel").fetchSemanticsNodes().isEmpty() }
            }
        }
    }
}
