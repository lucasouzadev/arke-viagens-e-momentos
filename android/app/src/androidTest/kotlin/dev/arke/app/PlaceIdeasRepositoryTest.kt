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
