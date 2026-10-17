package com.outofthebox.meowsicattv.ui.lyrics

/**
 * Lower bound for automatic lyric font shrink. A long phrase is scaled down (never up) so it fits
 * the available width instead of being clipped/ellipsized; the floor keeps the text readable.
 */
internal const val MIN_LYRIC_SCALE = 0.3f

/**
 * Greedily breaks a phrase (per-fragment [widths]) into visual lines that each fit within
 * [maxWidthPx]. A single fragment wider than [maxWidthPx] is kept alone on its own line.
 */
internal fun greedyRanges(widths: List<Float>, maxWidthPx: Float): List<IntRange> {
    if (widths.isEmpty() || maxWidthPx <= 0f) return listOf(widths.indices)
    val ranges = ArrayList<IntRange>()
    var start = 0
    var acc = 0f
    for (i in widths.indices) {
        val w = widths[i]
        if (i > start && acc + w > maxWidthPx) {
            ranges.add(start until i)
            start = i
            acc = 0f
        }
        acc += w
    }
    ranges.add(start until widths.size)
    return ranges
}

/**
 * Merges overflowing ranges into the last allowed line so at most [maxLines] are returned. Used only
 * as a safety net when even the smallest supported font scale cannot fit the phrase within [maxLines].
 */
internal fun widened(ranges: List<IntRange>, maxLines: Int): List<IntRange> {
    if (ranges.size <= maxLines || maxLines <= 0) return ranges
    val merged = ArrayList<IntRange>(maxLines)
    merged.addAll(ranges.take(maxLines - 1))
    val lastStart = ranges[maxLines - 1].first
    var lastEnd = lastStart
    for (i in maxLines - 1 until ranges.size) {
        lastEnd = ranges[i].last
    }
    merged.add(lastStart..lastEnd)
    return merged
}

/** Greedy wrap, then merges any overflow into the last line so at most [maxLines] are returned. */
internal fun wrapFragmentRanges(widths: List<Float>, maxWidthPx: Float, maxLines: Int): List<IntRange> {
    val ranges = greedyRanges(widths, maxWidthPx)
    return if (ranges.size <= maxLines) ranges else widened(ranges, maxLines)
}

/**
 * True when the phrase's natural (unwidened) wrap stays within [maxLines] and every visual line
 * fits within [maxWidthPx].
 */
internal fun fitsAllRanges(widths: List<Float>, maxWidthPx: Float, maxLines: Int): Boolean {
    val ranges = greedyRanges(widths, maxWidthPx)
    if (ranges.size > maxLines) return false
    for (range in ranges) {
        var w = 0f
        for (i in range) w += widths[i]
        if (w > maxWidthPx + 0.5f) return false
    }
    return true
}

/** Finds a font scale (<= 1) that lets [widths] wrap within [maxLines], preserving size when able. */
internal fun fitScaleForLines(widths: List<Float>, maxWidthPx: Float, maxLines: Int): Float {
    val total = widths.sum()
    if (total <= 0f || maxWidthPx <= 0f) return 1f
    if (maxLines <= 1) return (maxWidthPx / total).coerceIn(MIN_LYRIC_SCALE, 1f)
    if (fitsAllRanges(widths, maxWidthPx, maxLines)) return 1f
    var s = 1f
    while (s > MIN_LYRIC_SCALE) {
        s = (s - 0.02f).coerceAtLeast(MIN_LYRIC_SCALE)
        val test = widths.map { it * s }
        if (fitsAllRanges(test, maxWidthPx, maxLines)) return s
    }
    return MIN_LYRIC_SCALE
}
