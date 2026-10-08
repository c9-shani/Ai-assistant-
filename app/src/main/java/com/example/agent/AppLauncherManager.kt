package com.example.agent

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.util.Locale

data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null
)

class AppLauncherManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    fun getInstalledApps(): List<InstalledAppInfo> {
        val appList = mutableListOf<InstalledAppInfo>()
        try {
            val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }

            for (app in apps) {
                val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    val label = packageManager.getApplicationLabel(app).toString()
                    val icon = try { packageManager.getApplicationIcon(app) } catch (e: Exception) { null }
                    appList.add(InstalledAppInfo(label = label, packageName = app.packageName, icon = icon))
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return appList.sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    fun launchAppByPackage(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchAppByName(query: String): Pair<Boolean, String> {
        val cleanQuery = query.lowercase(Locale.ROOT)
            .replace("open", "")
            .replace("launch", "")
            .replace("kholo", "")
            .replace("chalao", "")
            .replace("start", "")
            .replace("app", "")
            .trim()

        if (cleanQuery.isBlank()) {
            return Pair(false, "App name not specified.")
        }

        // Check system built-in actions first
        when {
            cleanQuery.contains("wifi") || cleanQuery.contains("wi-fi") -> {
                return launchSystemSetting(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
            }
            cleanQuery.contains("bluetooth") -> {
                return launchSystemSetting(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
            }
            cleanQuery.contains("display") || cleanQuery.contains("brightness") -> {
                return launchSystemSetting(Settings.ACTION_DISPLAY_SETTINGS, "Display Settings")
            }
            cleanQuery.contains("battery") -> {
                return launchSystemSetting(Settings.ACTION_BATTERY_SAVER_SETTINGS, "Battery Settings")
            }
            cleanQuery.contains("setting") -> {
                return launchSystemSetting(Settings.ACTION_SETTINGS, "Android Settings")
            }
            cleanQuery.contains("camera") -> {
                return try {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(cameraIntent)
                    Pair(true, "Camera opened.")
                } catch (e: Exception) {
                    Pair(false, "Could not open camera: ${e.message}")
                }
            }
            cleanQuery.contains("clock") || cleanQuery.contains("alarm") -> {
                return try {
                    val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(alarmIntent)
                    Pair(true, "Clock & Alarms opened.")
                } catch (e: Exception) {
                    launchWebFallback("https://time.is", "Clock")
                }
            }
            cleanQuery.contains("dial") || cleanQuery.contains("phone") || cleanQuery.contains("call") -> {
                return try {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                    Pair(true, "Phone dialer opened.")
                } catch (e: Exception) {
                    Pair(false, "Dialer unavailable.")
                }
            }
            cleanQuery.contains("message") || cleanQuery.contains("sms") -> {
                return try {
                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(smsIntent)
                    Pair(true, "Messages opened.")
                } catch (e: Exception) {
                    Pair(false, "SMS client unavailable.")
                }
            }
        }

        val allApps = getInstalledApps()

        // 1. Exact match
        val exact = allApps.firstOrNull { it.label.equals(cleanQuery, ignoreCase = true) }
        if (exact != null) {
            val success = launchAppByPackage(exact.packageName)
            if (success) return Pair(true, "Opened ${exact.label}")
        }

        // 2. Starts with / contains match
        val fuzzy = allApps.firstOrNull { it.label.lowercase(Locale.ROOT).contains(cleanQuery) }
            ?: allApps.firstOrNull { it.packageName.lowercase(Locale.ROOT).contains(cleanQuery) }

        if (fuzzy != null) {
            val success = launchAppByPackage(fuzzy.packageName)
            if (success) return Pair(true, "Opened ${fuzzy.label}")
        }

        // 3. Common app mappings with automatic Web/Play Store fallback
        val packageMap = mapOf(
            "youtube" to ("com.google.android.youtube" to "https://m.youtube.com"),
            "whatsapp" to ("com.whatsapp" to "https://web.whatsapp.com"),
            "chrome" to ("com.android.chrome" to "https://google.com"),
            "maps" to ("com.google.android.apps.maps" to "https://maps.google.com"),
            "play store" to ("com.android.vending" to "https://play.google.com/store"),
            "calculator" to ("com.google.android.calculator" to "https://www.google.com/search?q=calculator"),
            "photos" to ("com.google.android.apps.photos" to "https://photos.google.com"),
            "gmail" to ("com.google.android.gm" to "https://mail.google.com")
        )

        for ((alias, info) in packageMap) {
            if (cleanQuery.contains(alias)) {
                val launched = launchAppByPackage(info.first)
                if (launched) {
                    return Pair(true, "Opened $alias")
                } else {
                    return launchWebFallback(info.second, alias)
                }
            }
        }

        // 4. Fallback search on Play Store or Web so the task NEVER fails
        return try {
            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(cleanQuery)}&c=apps")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playIntent)
            Pair(true, "Opened Google Play Store to install '$cleanQuery'.")
        } catch (e: Exception) {
            searchWeb(cleanQuery)
            Pair(true, "Searching web for '$cleanQuery'.")
        }
    }

    private fun launchWebFallback(url: String, title: String): Pair<Boolean, String> {
        return try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            Pair(true, "Opened $title via web portal.")
        } catch (e: Exception) {
            Pair(false, "Could not open $title: ${e.message}")
        }
    }

    private fun launchSystemSetting(action: String, name: String): Pair<Boolean, String> {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Pair(true, "$name opened.")
        } catch (e: Exception) {
            Pair(false, "Could not open $name.")
        }
    }

    fun searchWeb(query: String) {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }

    fun dialPhoneNumber(number: String) {
        val uri = if (number.isNotBlank()) Uri.parse("tel:$number") else Uri.parse("tel:")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback
        }
    }
}
