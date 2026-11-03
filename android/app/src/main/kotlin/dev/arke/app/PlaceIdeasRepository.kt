package dev.arke.app

import android.content.Context
import android.util.AtomicFile
import dev.arke.core.PlaceDraft
import dev.arke.core.PlaceIdea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PlaceIdeasRepository(context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "place-ideas-v1.json"))
    companion object { private val lock = Mutex() }

    suspend fun load(): List<PlaceIdea> = withContext(Dispatchers.IO) { lock.withLock { read() } }

    suspend fun add(draft: PlaceDraft): List<PlaceIdea> = withContext(Dispatchers.IO) {
        lock.withLock {
            require(draft.issue == null && draft.id.isNotEmpty())
            val ideas = read()
            val idea = PlaceIdea.from(draft)
            val existing = ideas.firstOrNull { it.id == idea.id }
            if (existing != null) {
                check(existing == idea)
                ideas
            } else (ideas + idea).also { write(it) }
        }
    }

    suspend fun update(draft: PlaceDraft, original: PlaceIdea): List<PlaceIdea> = withContext(Dispatchers.IO) {
        lock.withLock {
            require(draft.issue == null && draft.id.isNotEmpty() && draft.id == original.id)
            val ideas = read().toMutableList()
            val index = ideas.indexOfFirst { it.id == original.id }
            check(index >= 0) { "Idea no longer available" }
            val replacement = PlaceIdea.from(draft)
            if (ideas[index] != replacement) {
                check(ideas[index] == original) { "Idea changed since editing began" }
                ideas[index] = replacement
                write(ideas)
            }
            ideas.toList()
        }
    }

    suspend fun remove(id: String): List<PlaceIdea> = withContext(Dispatchers.IO) {
        lock.withLock { read().filterNot { it.id == id }.also { write(it) } }
    }

    private fun read(): List<PlaceIdea> {
        val bytes = try { file.openRead().use { check(it.channel.size() <= 4_194_304); it.readBytes() } }
        catch (error: java.io.FileNotFoundException) {
            if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return emptyList()
            throw error
        }
        check(bytes.size <= 4_194_304)
        val snapshot = JSONObject(bytes.toString(Charsets.UTF_8))
        check(snapshot.getInt("version") == 1)
        val array = snapshot.getJSONArray("ideas")
        val ideas = (0 until array.length()).map { index ->
            val value = array.getJSONObject(index)
            PlaceIdea(value.getString("id"), value.getString("name"), value.getString("address"), value.getString("note"), value.optString("link", ""), value.optString("cover", ""))
        }
        check(ideas.map { it.id }.toSet().size == ideas.size)
        check(ideas.all { it.id.isNotEmpty() && PlaceDraft(it.id, it.name, it.address, it.note).issue == null })
        return ideas
    }

    private fun write(ideas: List<PlaceIdea>) {
        val array = JSONArray()
        ideas.forEach { array.put(JSONObject().put("id", it.id).put("name", it.name).put("address", it.address).put("note", it.note).put("link", it.link).put("cover", it.cover)) }
        val bytes = JSONObject().put("version", 1).put("ideas", array).toString().toByteArray(Charsets.UTF_8)
        check(bytes.size <= 4_194_304)
        val stream = file.startWrite()
        try {
            stream.write(bytes)
            stream.flush()
            stream.fd.sync()
            file.finishWrite(stream)
            check(read() == ideas) { "Write verification failed" }
        }
        catch (error: Exception) { file.failWrite(stream); throw error }
    }
}
