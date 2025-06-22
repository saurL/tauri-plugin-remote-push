package app.tauri.remotepush

import android.Manifest
import android.app.Activity
import android.webkit.WebView
import app.tauri.annotation.Command
import app.tauri.annotation.Permission
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSObject
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

@TauriPlugin(
    permissions = [
        Permission(strings = [Manifest.permission.POST_NOTIFICATIONS], alias = "notifications")
    ]
)
class PushNotificationPlugin(private val activity: Activity) : Plugin(activity) {

    companion object {
        var instance: PushNotificationPlugin? = null
    }

    override fun load(webView: WebView) {
        super.load(webView)
        instance = this
    }

    @Command
    fun getToken(invoke: Invoke) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                invoke.reject("Failed to get FCM token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            val result = JSObject()
            result.put("token", token)
            invoke.resolve(result)
        }
    }

    @Command
    override fun requestPermissions(invoke: Invoke) {
        super.requestPermissions(invoke)
    }

    fun handleNewToken(token: String) {
        val data = JSObject()
        data.put("token", token)
        trigger("token-received", data)
    }

    fun handleMessage(message: RemoteMessage) {
        val data = JSObject()
        message.notification?.let {
            val notification = JSObject()
            notification.put("title", it.title)
            notification.put("body", it.body)
            data.put("notification", notification)
        }
        data.put("data", message.data)
        trigger("notification-received", data)
    }
} 