package com.boswelja.autoevent.notificationeventextractor

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

/**
 * A data class holding info about a notification.
 * @param text The notification text.
 * @param senderName The name of the person or group who sent the text, or null if not found.
 * @param packageLabel The label of the package that posted the notification.
 */
data class NotificationDetails(
    val text: String,
    val senderName: String?,
    val packageLabel: String,
    val packageName: String
)

/**
 * Extract some details about the [StatusBarNotification].
 * @return A [NotificationDetails] containing data about this notification.
 */
internal fun StatusBarNotification.getDetails(packageManager: PackageManager): NotificationDetails {
    // Load common details
    val packageLabel = packageManager.getApplicationInfo(packageName, 0)
        .loadLabel(packageManager)

    // Load message style if it exists
    val messageStyle = NotificationCompat.MessagingStyle
        .extractMessagingStyleFromNotification(notification)

    if (messageStyle != null) {
        // Message style found, get details
        val text = messageStyle.messages
            .filter { !it.text.isNullOrBlank() }
            .joinToString { it.text!! }
        val sender = messageStyle.conversationTitle
            ?: messageStyle.messages.firstOrNull { it.person != null }?.person?.name

        return NotificationDetails(
            text,
            sender?.toString(),
            packageLabel.toString(),
            packageName
        )
    } else {
        // No message style found
        val text = notification.extras.getString(Notification.EXTRA_TEXT, "")
        return NotificationDetails(
            text,
            null,
            packageLabel.toString(),
            packageName
        )
    }
}
