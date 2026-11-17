package com.pulse.player.util.lrc

import com.pulse.player.domain.model.LyricsSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parse standard two-digit millisecond timestamps`() {
        val lrc = """
            [ti:Test Song]
            [ar:Test Artist]
            [00:10.50]First line
            [00:20.00]Second line
            [01:05.25]Third line
        """.trimIndent()

        val parsed = LrcParser.parse("track_1", lrc, LyricsSource.SIDECAR_LRC)

        assertTrue(parsed.isSynchronized)
        assertEquals(3, parsed.lines.size)
        assertEquals(10500L, parsed.lines[0].timestampMs)
        assertEquals("First line", parsed.lines[0].text)
        assertEquals(20000L, parsed.lines[1].timestampMs)
        assertEquals("Second line", parsed.lines[1].text)
        assertEquals(65250L, parsed.lines[2].timestampMs)
        assertEquals("Third line", parsed.lines[2].text)
    }

    @Test
    fun `parse three-digit millisecond precision timestamps`() {
        val lrc = """
            [00:05.123]Line with high precision
            [00:15.890]Another line
        """.trimIndent()

        val parsed = LrcParser.parse("track_2", lrc, LyricsSource.LRCLIB_REMOTE)

        assertTrue(parsed.isSynchronized)
        assertEquals(2, parsed.lines.size)
        assertEquals(5123L, parsed.lines[0].timestampMs)
        assertEquals(15890L, parsed.lines[1].timestampMs)
    }

    @Test
    fun `parse multiple timestamps per line and sort chronologically`() {
        val lrc = """
            [00:10.00][00:40.00]Repeated chorus
            [00:25.00]Verse line
        """.trimIndent()

        val parsed = LrcParser.parse("track_3", lrc)

        assertEquals(3, parsed.lines.size)
        assertEquals(10000L, parsed.lines[0].timestampMs)
        assertEquals("Repeated chorus", parsed.lines[0].text)
        assertEquals(25000L, parsed.lines[1].timestampMs)
        assertEquals("Verse line", parsed.lines[1].text)
        assertEquals(40000L, parsed.lines[2].timestampMs)
        assertEquals("Repeated chorus", parsed.lines[2].text)
    }

    @Test
    fun `apply timestamp offset correctly`() {
        val lrc = """
            [offset:500]
            [00:10.00]Shifted line
        """.trimIndent()

        val parsed = LrcParser.parse("track_4", lrc)

        assertEquals(1, parsed.lines.size)
        assertEquals(10500L, parsed.lines[0].timestampMs)
    }

    @Test
    fun `parse plain unsynchronized lyrics`() {
        val plain = """
            Just a line of text
            Another line of text
            Final lyric line
        """.trimIndent()

        val parsed = LrcParser.parse("track_5", plain)

        assertFalse(parsed.isSynchronized)
        assertEquals(3, parsed.lines.size)
        assertEquals(-1L, parsed.lines[0].timestampMs)
    }

    @Test
    fun `findActiveLineIndex locates active line accurately`() {
        val lrc = """
            [00:10.00]Line 0
            [00:20.00]Line 1
            [00:30.00]Line 2
            [00:40.00]Line 3
        """.trimIndent()

        val parsed = LrcParser.parse("track_6", lrc)

        // Before first line
        assertEquals(0, LrcParser.findActiveLineIndex(parsed.lines, 5000L))

        // Exactly at Line 0
        assertEquals(0, LrcParser.findActiveLineIndex(parsed.lines, 10000L))

        // Between Line 0 and Line 1
        assertEquals(0, LrcParser.findActiveLineIndex(parsed.lines, 15000L))

        // Exactly at Line 1
        assertEquals(1, LrcParser.findActiveLineIndex(parsed.lines, 20000L))

        // Between Line 2 and Line 3
        assertEquals(2, LrcParser.findActiveLineIndex(parsed.lines, 35000L))

        // Beyond last line
        assertEquals(3, LrcParser.findActiveLineIndex(parsed.lines, 50000L))
    }
}
