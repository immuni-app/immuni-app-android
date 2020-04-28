import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Push notifications utility methods.
 */
object PushNotificationUtils {

    /**
     * Returns true is push notifications are enabled for this app.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return getPushNotificationState(context) == PushNotificationState.AUTHORIZED
    }

    /**
     * Returns the state of push notifications for this app.
     * If [PushNotificationState.PARTIAL] it means that some notification channel is disabled.
     *
     * @return [PushNotificationState]
     */
    fun getPushNotificationState(context: Context): PushNotificationState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!manager.areNotificationsEnabled()) {
                return PushNotificationState.DENIED
            }
            val channels = manager.notificationChannels

            when {
                channels.none { it.importance == NotificationManager.IMPORTANCE_NONE } -> {
                    PushNotificationState.AUTHORIZED
                }
                channels.all { it.importance == NotificationManager.IMPORTANCE_NONE } -> {
                    PushNotificationState.DENIED
                }
                else -> PushNotificationState.PARTIAL
            }
        } else {
            when (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                true -> PushNotificationState.AUTHORIZED
                false -> PushNotificationState.DENIED
            }
        }
    }
}

enum class PushNotificationState {
    DENIED, AUTHORIZED, PARTIAL
}
