package com.outofthebox.meowsicattv.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.outofthebox.meowsicattv.MainActivity
import com.outofthebox.meowsicattv.R
import com.meowsicat.android.storage.BehaviorSettings
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

class WebServerService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            WebServerManager.stop()
            stopSelf()
            return START_NOT_STICKY
        }
        acquireLocks()
        createChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        // Verify the web server is actually up. The system can restart this
        // service (START_STICKY) after process death, in which case there is
        // no library loaded and nothing to serve — don't show a lying
        // "Web remote ready" notification in that case.
        if (!WebServerManager.isAlive()) {
            if (WebServerManager.currentLibrary != null) {
                WebServerManager.restart(this)
            } else {
                Log.w("WebServerService", "Server not alive and no library loaded; stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        releaseLocks()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun acquireLocks() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
            if (wakeLock == null) {
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "MeowsiCatTV:WebServerWakeLock"
                )
            }
            wakeLock?.apply {
                if (!isHeld) {
                    acquire(24 * 60 * 60 * 1000L) // 24 hours lock
                }
            }
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as? WifiManager
            if (wifiLock == null) {
                @Suppress("DEPRECATION")
                wifiLock = wifiManager?.createWifiLock(
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    "MeowsiCatTV:WebServerWifiLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            wifiLock?.apply { if (!isHeld) acquire() }
        } catch (e: Exception) {
            Log.e("WebServerService", "Failed to acquire locks", e)
        }
    }

    private fun releaseLocks() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null
            wifiLock?.let { if (it.isHeld) it.release() }
            wifiLock = null
        } catch (_: Exception) {}
    }

    private fun buildNotification(): Notification {
        val port = BehaviorSettings.getWebPort(this)
        val url = "http://${localIpAddress(this)}:$port"
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = Intent(this, WebServerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Web remote ready at $url")
            .setContentIntent(openApp)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Web Remote",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the phone web remote reachable while the app is in the background"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "meowsicat_web_server"
        private const val NOTIFICATION_ID = 4201
        private const val ACTION_STOP = "com.outofthebox.meowsicattv.server.ACTION_STOP"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, WebServerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WebServerService::class.java))
        }

        private fun localIpAddress(context: Context): String {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(WIFI_SERVICE) as? WifiManager
                val ipAddress = wifiManager?.connectionInfo?.ipAddress ?: 0
                if (ipAddress != 0) {
                    return String.format(
                        Locale.US,
                        "%d.%d.%d.%d",
                        ipAddress and 0xff,
                        ipAddress shr 8 and 0xff,
                        ipAddress shr 16 and 0xff,
                        ipAddress shr 24 and 0xff
                    )
                }
            } catch (_: Exception) {}

            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val addrs = interfaces.nextElement().inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.hostAddress ?: "127.0.0.1"
                        }
                    }
                }
            } catch (_: Exception) {}

            return "127.0.0.1"
        }
    }
}
