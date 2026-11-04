package dev.arke.core

import java.util.UUID
import java.net.URI

enum class PlaceDraftIssue { NAME_REQUIRED, NAME_TOO_LONG, ADDRESS_REQUIRED, ADDRESS_TOO_LONG, NOTE_TOO_LONG, INVALID_LINK }

data class PlaceDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val address: String = "",
    val note: String = "",
    val link: String = "",
    val cover: String = "",
) {
    fun normalized() = copy(name = name.trim(), address = address.trim(), note = note.trim(), link = webLink(link) ?: link.trim())
    val isEmpty: Boolean get() = normalized().let { it.name.isEmpty() && it.address.isEmpty() && it.note.isEmpty() && it.link.isEmpty() && it.cover.isEmpty() }
    val issue: PlaceDraftIssue? get() {
        val value = normalized()
        return when {
            value.name.isEmpty() -> PlaceDraftIssue.NAME_REQUIRED
            value.name.codePointCount(0, value.name.length) > 100 -> PlaceDraftIssue.NAME_TOO_LONG
            value.address.isEmpty() -> PlaceDraftIssue.ADDRESS_REQUIRED
            value.address.codePointCount(0, value.address.length) > 240 -> PlaceDraftIssue.ADDRESS_TOO_LONG
            value.note.codePointCount(0, value.note.length) > 1000 -> PlaceDraftIssue.NOTE_TOO_LONG
            value.link.isNotEmpty() && webLink(value.link) == null -> PlaceDraftIssue.INVALID_LINK
            else -> null
        }
    }
}

/** Written idea only; it does not imply a coordinate, search result or map Pin. */
data class PlaceIdea(val id: String, val name: String, val address: String, val note: String, val link: String = "", val cover: String = "") {
    companion object {
        fun from(draft: PlaceDraft) = draft.normalized().let { PlaceIdea(it.id, it.name, it.address, it.note, it.link, it.cover) }
    }
}

fun webLink(value: String): String? {
    val trimmed = value.trim()
    val explicitScheme = Regex("^[A-Za-z][A-Za-z0-9+.-]*:").containsMatchIn(trimmed)
    val candidate = if (explicitScheme) trimmed else "https://$trimmed"
    if (trimmed.startsWith("/") || candidate.toByteArray().size > 2048) return null
    val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
    val host = uri.host ?: return null
    return candidate.takeIf {
        uri.scheme?.lowercase() in setOf("http", "https") && uri.userInfo == null &&
            (explicitScheme || '.' in host)
    }
}
