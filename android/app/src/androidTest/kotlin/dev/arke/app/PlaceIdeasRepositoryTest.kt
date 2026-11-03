package dev.arke.app

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import dev.arke.core.PlaceDraft
import dev.arke.core.PlaceIdea
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class PlaceIdeasRepositoryTest {
    @Test fun updateKeepsIdentityOrderAndRejectsStaleOrMissingIdeas() = runBlocking {
        val (context, directory) = isolatedContext()
        try {
            val repository = PlaceIdeasRepository(context)
            val initial = PlaceDraft("first", "Jardim", "Centro")
            val other = PlaceDraft("other", "Museu", "Praça")
            repository.add(initial)
            repository.add(other)
            val original = PlaceIdea.from(initial)
            val edited = initial.copy(name = "Jardim novo", address = "Nova entrada", note = "Pela manhã")
            val expected = listOf(PlaceIdea.from(edited), PlaceIdea.from(other))
            assertEquals(expected, repository.update(edited, original))
            assertEquals(expected, repository.update(edited, original))
            assertEquals(expected, PlaceIdeasRepository(context).load())
            val file = File(directory, "place-ideas-v1.json")
            val bytes = file.readBytes()
            val missing = PlaceDraft("missing", "Ausente", "Centro")
            for ((draft, snapshot) in listOf(
                initial.copy(name = "Versão antiga") to original,
                missing to PlaceIdea.from(missing),
                edited.copy(id = "different") to original,
                edited.copy(name = "") to PlaceIdea.from(edited),
            )) {
                try { repository.update(draft, snapshot); fail("Unsafe update must fail") }
                catch (error: AssertionError) { throw error }
                catch (_: Exception) { /* Existing snapshot must survive. */ }
                assertArrayEquals(bytes, file.readBytes())
            }
        } finally { directory.deleteRecursively() }
    }

    private fun isolatedContext(): Pair<Context, File> {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "repository-test-${UUID.randomUUID()}").apply { mkdirs() }
        return object : ContextWrapper(base) { override fun getNoBackupFilesDir() = directory } to directory
    }

    @Test fun saveReloadRetryAndDelete() = runBlocking {
        val (context, directory) = isolatedContext()
        try {
            val repository = PlaceIdeasRepository(context)
            val draft = PlaceDraft("fixture", "Museu de teste", "Rua de teste")
            val first = repository.add(draft)
            assertEquals(first, repository.add(draft))
            assertEquals(listOf(PlaceIdea.from(draft)), PlaceIdeasRepository(context).load())
            assertTrue(repository.remove(draft.id).isEmpty())
            assertTrue(PlaceIdeasRepository(context).load().isEmpty())
        } finally { directory.deleteRecursively() }
    }

    @Test fun corruptAndUnknownSnapshotsArePreserved() = runBlocking {
        val (context, directory) = isolatedContext()
        try {
            val file = File(directory, "place-ideas-v1.json")
            for (original in listOf("invalid fixture", "{\"version\":99,\"ideas\":[]}")) {
                file.writeText(original)
                try { PlaceIdeasRepository(context).add(PlaceDraft(name = "Museu", address = "Centro")); fail("Invalid snapshots must block writes") }
                catch (error: AssertionError) { throw error }
                catch (_: Exception) { /* Expected: existing bytes must survive. */ }
                assertEquals(original, file.readText())
            }
        } finally { directory.deleteRecursively() }
    }
}
