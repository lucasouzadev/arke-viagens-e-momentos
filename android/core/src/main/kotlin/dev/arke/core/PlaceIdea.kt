package dev.arke.core

import java.util.UUID

enum class PlaceDraftIssue { NAME_REQUIRED, NAME_TOO_LONG, ADDRESS_REQUIRED, ADDRESS_TOO_LONG, NOTE_TOO_LONG }

data class PlaceDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val address: String = "",
    val note: String = "",
) {
    fun normalized() = copy(name = name.trim(), address = address.trim(), note = note.trim())
    val isEmpty: Boolean get() = normalized().let { it.name.isEmpty() && it.address.isEmpty() && it.note.isEmpty() }
    val issue: PlaceDraftIssue? get() {
        val value = normalized()
        return when {
            value.name.isEmpty() -> PlaceDraftIssue.NAME_REQUIRED
            value.name.codePointCount(0, value.name.length) > 100 -> PlaceDraftIssue.NAME_TOO_LONG
            value.address.isEmpty() -> PlaceDraftIssue.ADDRESS_REQUIRED
            value.address.codePointCount(0, value.address.length) > 240 -> PlaceDraftIssue.ADDRESS_TOO_LONG
            value.note.codePointCount(0, value.note.length) > 1000 -> PlaceDraftIssue.NOTE_TOO_LONG
            else -> null
        }
    }
}

/** Written idea only; it does not imply a coordinate, search result or map Pin. */
data class PlaceIdea(val id: String, val name: String, val address: String, val note: String) {
    companion object {
        fun from(draft: PlaceDraft) = draft.normalized().let { PlaceIdea(it.id, it.name, it.address, it.note) }
    }
}
