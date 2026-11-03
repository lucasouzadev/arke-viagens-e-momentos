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

data class LocalMapPin(val ideaID: String, val latitude: Double, val longitude: Double, val tripID: String?)
data class LocalPinTrip(val id: String, val title: String)
data class LocalPinPlan(val pins: List<LocalMapPin>, val trips: List<LocalPinTrip>)

sealed interface LocalPinTripChoice {
    data object None : LocalPinTripChoice
    data class Existing(val id: String) : LocalPinTripChoice
    data class Create(val id: String, val title: String) : LocalPinTripChoice
}

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

    private val pinFile = AtomicFile(File(context.noBackupFilesDir, "pin-plan-v1.json"))

    suspend fun loadPinPlan(): LocalPinPlan = withContext(Dispatchers.IO) {
        lock.withLock { readPinPlan() }
    }

    suspend fun savePin(ideaID: String, latitude: Double, longitude: Double, trip: LocalPinTripChoice): LocalPinPlan =
        withContext(Dispatchers.IO) {
            lock.withLock {
                require(latitude in -90.0..90.0 && longitude in -180.0..180.0 && read().any { it.id == ideaID })
                val previous = readPinPlan()
                val trips = previous.trips.toMutableList()
                val tripID = when (trip) {
                    LocalPinTripChoice.None -> null
                    is LocalPinTripChoice.Existing -> trip.id.also { id -> check(trips.any { it.id == id }) }
                    is LocalPinTripChoice.Create -> {
                        val title = trip.title.trim()
                        require(trip.id.isNotBlank() && title.isNotEmpty() && title.length <= 120)
                        val existing = trips.firstOrNull { it.id == trip.id }
                        if (existing == null) trips.add(LocalPinTrip(trip.id, title))
                        else check(existing.title == title)
                        trip.id
                    }
                }
                val pin = LocalMapPin(ideaID, latitude, longitude, tripID)
                val pins = previous.pins.toMutableList()
                val index = pins.indexOfFirst { it.ideaID == ideaID }
                if (index < 0) pins.add(pin) else pins[index] = pin
                val pinRows = JSONArray()
                pins.forEach {
                    pinRows.put(JSONObject().put("ideaID", it.ideaID).put("latitude", it.latitude)
                        .put("longitude", it.longitude).put("tripID", it.tripID ?: JSONObject.NULL))
                }
                val tripRows = JSONArray()
                trips.forEach { tripRows.put(JSONObject().put("id", it.id).put("title", it.title)) }
                writeAtomic(pinFile, JSONObject().put("pins", pinRows).put("trips", tripRows).toString().toByteArray(Charsets.UTF_8))
                LocalPinPlan(pins.toList(), trips.toList())
            }
        }

    private fun readPinPlan(): LocalPinPlan {
        val bytes = try {
            pinFile.openRead().use { check(it.channel.size() <= 4_194_304); it.readBytes() }
        } catch (error: java.io.FileNotFoundException) {
            if (pinFile.baseFile.exists() || File(pinFile.baseFile.path + ".bak").exists()) throw error
            return LocalPinPlan(emptyList(), emptyList())
        }
        val data = JSONObject(bytes.toString(Charsets.UTF_8))
        val pins = data.getJSONArray("pins")
        val trips = data.getJSONArray("trips")
        return LocalPinPlan(
            (0 until pins.length()).map { index ->
                val row = pins.getJSONObject(index)
                LocalMapPin(row.getString("ideaID"), row.getDouble("latitude"), row.getDouble("longitude"),
                    if (row.isNull("tripID")) null else row.getString("tripID"))
            },
            (0 until trips.length()).map { index ->
                val row = trips.getJSONObject(index)
                LocalPinTrip(row.getString("id"), row.getString("title"))
            },
        )
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
        writeAtomic(file, bytes)
        check(read() == ideas) { "Write verification failed" }
    }

    private fun writeAtomic(file: AtomicFile, bytes: ByteArray) {
        check(bytes.size <= 4_194_304)
        val stream = file.startWrite()
        try {
            stream.write(bytes)
            stream.flush()
            stream.fd.sync()
            file.finishWrite(stream)
        }
        catch (error: Exception) { file.failWrite(stream); throw error }
    }
}
