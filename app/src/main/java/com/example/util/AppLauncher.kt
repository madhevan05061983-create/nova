package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder
import java.util.Locale

data class LaunchResult(
    val success: Boolean,
    val appName: String,
    val isWebOrStore: Boolean = false,
    val message: String = ""
)

object AppLauncher {

    fun launchAppByName(context: Context, appNameQuery: String): LaunchResult {
        val query = appNameQuery.trim().lowercase(Locale.ROOT)
            .removePrefix("open ")
            .removePrefix("launch ")
            .removePrefix("start ")
            .trim()
        val pm = context.packageManager

        // 1. System Settings shortcuts
        when (query) {
            "settings" -> {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return LaunchResult(true, "Settings", message = "Opening Settings")
            }
            "wifi", "wi-fi", "wifi settings" -> {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return LaunchResult(true, "Wi-Fi Settings", message = "Opening Wi-Fi Settings")
            }
            "bluetooth", "bluetooth settings" -> {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return LaunchResult(true, "Bluetooth Settings", message = "Opening Bluetooth Settings")
            }
            "display", "display settings" -> {
                context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return LaunchResult(true, "Display Settings", message = "Opening Display Settings")
            }
            "sound", "sound settings", "volume settings" -> {
                context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return LaunchResult(true, "Sound Settings", message = "Opening Sound Settings")
            }
            "camera" -> {
                val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (cameraIntent.resolveActivity(pm) != null) {
                    context.startActivity(cameraIntent)
                    return LaunchResult(true, "Camera", message = "Opening Camera")
                }
            }
        }

        // 2. Direct Package Map for major standard apps
        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "google" to "com.google.android.googlequicksearchbox",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "calculator" to "com.google.android.calculator",
            "clock" to "com.google.android.deskclock",
            "calendar" to "com.google.android.calendar",
            "whatsapp" to "com.whatsapp",
            "spotify" to "com.spotify.music",
            "instagram" to "com.instagram.android",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android",
            "telegram" to "org.telegram.messenger",
            "netflix" to "com.netflix.mediaclient",
            "photos" to "com.google.android.apps.photos",
            "files" to "com.google.android.documentsui",
            "play store" to "com.android.vending",
            "playstore" to "com.android.vending",
            "messages" to "com.google.android.apps.messaging",
            "phone" to "com.google.android.dialer",
            "contacts" to "com.google.android.contacts"
        )

        val knownPkg = knownPackages[query]
        if (knownPkg != null) {
            val intent = pm.getLaunchIntentForPackage(knownPkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                val prettyName = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                return LaunchResult(true, prettyName, message = "Opening $prettyName")
            }
        }

        // 3. Scan all installed applications on the device
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val label = pm.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
                if (label == query || label.contains(query) || query.contains(label)) {
                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        val appLabel = pm.getApplicationLabel(app).toString()
                        return LaunchResult(true, appLabel, message = "Opening $appLabel")
                    }
                }
            }
        } catch (_: Exception) {}

        // 4. Web / Play Store fallback
        val webFallback = when (query) {
            "youtube" -> "https://www.youtube.com"
            "google" -> "https://www.google.com"
            "github" -> "https://github.com"
            "spotify" -> "https://open.spotify.com"
            "maps" -> "https://maps.google.com"
            "netflix" -> "https://www.netflix.com"
            "instagram" -> "https://www.instagram.com"
            "twitter", "x" -> "https://twitter.com"
            "reddit" -> "https://www.reddit.com"
            else -> "https://play.google.com/store/search?q=${URLEncoder.encode(query, "UTF-8")}&c=apps"
        }

        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            val prettyName = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return LaunchResult(true, prettyName, isWebOrStore = true, message = "Launching $prettyName")
        } catch (e: Exception) {
            return LaunchResult(false, query, message = "Could not open $query: ${e.message}")
        }
    }

    fun getBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }
}
