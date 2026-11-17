package com.v2ray.ang.dto

import java.io.Serializable

/** Outbound counters sampled by the daemon, including proxy and direct traffic. */
data class TrafficSnapshot(
    val uploadBytesPerSecond: Long,
    val downloadBytesPerSecond: Long,
    val totalUploadBytes: Long,
    val totalDownloadBytes: Long,
    val measuredDurationMillis: Long,
) : Serializable

internal fun trafficSnapshot(
    previous: TrafficSnapshot?,
    uploadedBytes: Long,
    downloadedBytes: Long,
    intervalMillis: Long,
    measuredDurationMillis: Long,
): TrafficSnapshot {
    val elapsed = intervalMillis.coerceAtLeast(1L)
    return TrafficSnapshot(
        uploadBytesPerSecond = (uploadedBytes.coerceAtLeast(0L) * 1000L / elapsed),
        downloadBytesPerSecond = (downloadedBytes.coerceAtLeast(0L) * 1000L / elapsed),
        totalUploadBytes = (previous?.totalUploadBytes ?: 0L) + uploadedBytes.coerceAtLeast(0L),
        totalDownloadBytes = (previous?.totalDownloadBytes ?: 0L) + downloadedBytes.coerceAtLeast(0L),
        measuredDurationMillis = measuredDurationMillis.coerceAtLeast(0L),
    )
}
