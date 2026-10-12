package io.github.szlynn.fitlogiq.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    // --- formatDuration ---

    @Test
    fun `formatDuration under 1 hour shows mm-ss`() {
        assertEquals("05:30", formatDuration(330))
    }

    @Test
    fun `formatDuration exactly 1 hour shows h-mm-ss`() {
        assertEquals("1:00:00", formatDuration(3600))
    }

    @Test
    fun `formatDuration over 1 hour shows h-mm-ss`() {
        assertEquals("1:23:45", formatDuration(5025))
    }

    @Test
    fun `formatDuration zero shows 00-00`() {
        assertEquals("00:00", formatDuration(0))
    }

    @Test
    fun `formatDuration pads minutes and seconds`() {
        assertEquals("09:05", formatDuration(545))
    }

    // --- formatPace ---

    @Test
    fun `formatPace returns correct min-km string`() {
        // 30 min / 5 km = 6:00 /km
        assertEquals("6:00 /km", formatPace(6.0))
    }

    @Test
    fun `formatPace rounds seconds correctly`() {
        // 5.5 min/km = 5:30 /km
        assertEquals("5:30 /km", formatPace(5.5))
    }

    @Test
    fun `formatPace returns placeholder for zero pace`() {
        assertEquals("--:-- /km", formatPace(0.0))
    }

    @Test
    fun `formatPace returns placeholder for negative pace`() {
        assertEquals("--:-- /km", formatPace(-1.0))
    }

    @Test
    fun `formatPace returns placeholder for infinite pace`() {
        assertEquals("--:-- /km", formatPace(Double.POSITIVE_INFINITY))
    }

    @Test
    fun `formatPace returns placeholder for NaN`() {
        assertEquals("--:-- /km", formatPace(Double.NaN))
    }
}
