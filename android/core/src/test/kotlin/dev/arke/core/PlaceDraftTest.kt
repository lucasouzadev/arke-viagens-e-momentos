package dev.arke.core

import kotlin.test.*

class PlaceDraftTest {
    @Test fun `required fields reject whitespace`() {
        assertEquals(PlaceDraftIssue.NAME_REQUIRED, PlaceDraft(name = " \n ").issue)
        assertEquals(PlaceDraftIssue.ADDRESS_REQUIRED, PlaceDraft(name = "Museu", address = " \n ").issue)
        assertTrue(PlaceDraft(name = " ", address = "\n").isEmpty)
    }

    @Test fun `normalization preserves identity and optional note`() {
        val draft = PlaceDraft("stable", " Museu ", " Centro\n", " ")
        assertNull(draft.issue)
        assertEquals(PlaceIdea("stable", "Museu", "Centro", ""), PlaceIdea.from(draft))
    }

    @Test fun `limits count unicode codepoints without truncation`() {
        assertNull(PlaceDraft(name = "🌍".repeat(100), address = "Centro").issue)
        assertEquals(PlaceDraftIssue.NAME_TOO_LONG, PlaceDraft(name = "🌍".repeat(101), address = "Centro").issue)
        assertEquals(PlaceDraftIssue.ADDRESS_TOO_LONG, PlaceDraft(name = "Museu", address = "a".repeat(241)).issue)
        assertEquals(PlaceDraftIssue.NOTE_TOO_LONG, PlaceDraft(name = "Museu", address = "Centro", note = "a".repeat(1001)).issue)
    }
}
