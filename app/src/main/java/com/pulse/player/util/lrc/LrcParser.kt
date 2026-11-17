package com.pulse.player.util.lrc

import com.pulse.player.domain.model.LyricLine
import com.pulse.player.domain.model.Lyrics
import com.pulse.player.domain.model.LyricsSource
import java.util.regex.Pattern

object LrcParser {

    private val TIME_TAG_PATTERN: Pattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
    private val OFFSET_PATTERN: Pattern = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\]", Pattern.CASE_INSENSITIVE)

    fun parse(trackId: String, lrcContent: String, source: LyricsSource = LyricsSource.SIDECAR_LRC): Lyrics {
        if (lrcContent.isBlank()) {
            return Lyrics(
                trackId = trackId,
                lines = emptyList(),
                plainText = "",
                isSynchronized = false,
                source = source
            )
        }

        var offsetMs = 0L
        val rawLines = lrcContent.lines()
        val parsedLines = mutableListOf<LyricLine>()
        var foundTimeTag = false

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check for offset tag
            val offsetMatcher = OFFSET_PATTERN.matcher(trimmed)
            if (offsetMatcher.find()) {
                offsetMs = offsetMatcher.group(1)?.toLongOrNull() ?: 0L
                continue
            }

            // Extract all timestamps in this line
            val timeMatcher = TIME_TAG_PATTERN.matcher(trimmed)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (timeMatcher.find()) {
                foundTimeTag = true
                val minutes = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = timeMatcher.group(2)?.toLongOrNull() ?: 0L
                val fractionStr = timeMatcher.group(3)

                val millis = when {
                    fractionStr == null -> 0L
                    fractionStr.length == 2 -> (fractionStr.toLongOrNull() ?: 0L) * 10L
                    fractionStr.length == 3 -> fractionStr.toLongOrNull() ?: 0L
                    else -> (fractionStr.take(3).toLongOrNull() ?: 0L)
                }

                val totalMs = (minutes * 60L + seconds) * 1000L + millis
                timestamps.add(totalMs)
                lastMatchEnd = timeMatcher.end()
            }

            if (timestamps.isNotEmpty()) {
                val text = trimmed.substring(lastMatchEnd).trim()
                for (ts in timestamps) {
                    val adjustedTs = (ts + offsetMs).coerceAtLeast(0L)
                    parsedLines.add(LyricLine(timestampMs = adjustedTs, text = text))
                }
            } else if (!trimmed.startsWith("[") && !trimmed.endsWith("]")) {
                // Ordinary text line without timestamp
                parsedLines.add(LyricLine(timestampMs = -1L, text = trimmed))
            }
        }

        val sortedLines = if (foundTimeTag) {
            parsedLines
                .filter { it.timestampMs >= 0L }
                .sortedBy { it.timestampMs }
        } else {
            parsedLines
        }

        val plainText = sortedLines.joinToString("\n") { it.text }

        return Lyrics(
            trackId = trackId,
            lines = sortedLines,
            plainText = plainText,
            isSynchronized = foundTimeTag && sortedLines.isNotEmpty(),
            source = source
        )
    }

    /**
     * Efficiently determines the active line index given the current playback position in milliseconds.
     */
    fun findActiveLineIndex(lines: List<LyricLine>, currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        if (lines.size == 1) return 0
        if (currentPositionMs < lines.first().timestampMs) return 0

        var low = 0
        var high = lines.size - 1
        var bestIndex = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midTime = lines[mid].timestampMs
            if (midTime <= currentPositionMs) {
                bestIndex = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return bestIndex
    }
}
