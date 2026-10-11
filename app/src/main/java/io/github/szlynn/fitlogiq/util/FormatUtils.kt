package io.github.szlynn.fitlogiq.util

/**
 * Formats a duration in seconds to display string.
 * Under 1 hour: "mm:ss"
 * 1 hour or more: "h:mm:ss"
 */
fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

/**
 * Formats pace in min/km to "m:ss /km".
 */
fun formatPace(paceMinPerKm: Double): String {
    if (paceMinPerKm <= 0 || paceMinPerKm.isInfinite() || paceMinPerKm.isNaN()) return "--:-- /km"
    val totalSeconds = (paceMinPerKm * 60).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d /km".format(minutes, seconds)
}
