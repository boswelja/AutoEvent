package com.boswelja.autoevent.notificationeventextractor

/**
 * A data class holding info about a notification.
 * @param text The notification text.
 * @param senderName The name of the person or group who sent the text, or null if not found.
 * @param packageLabel The label of the package that posted the notification.
 */
data class NotificationDetails(
    val text: String,
    val senderName: String?,
    val packageLabel: String
)
