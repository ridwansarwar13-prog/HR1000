package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.hardware.HardwareMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayHudService : Service() {

    companion object {
        @Volatile
        var isRunning: Boolean = false
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var hardwareMonitor: HardwareMonitor? = null
    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private var tvFps: TextView? = null
    private var tvCpuTemp: TextView? = null
    private var tvGpuTemp: TextView? = null
    private var tvAura: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundNotification()
        setupFloatingHud()

        hardwareMonitor = HardwareMonitor(applicationContext).apply {
            start()
        }

        monitorJob = scope.launch {
            hardwareMonitor?.telemetry?.collectLatest { telemetry ->
                tvFps?.text = "⚡ %.1f FPS".format(telemetry.fps)
                tvCpuTemp?.text = "🌡️ CPU: %.1f°C".format(telemetry.cpuTemperature)
                tvGpuTemp?.text = "🎮 GPU: %.1f°C".format(telemetry.gpuTemperature)
            }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "aura_booster_hud_channel"
        val channelName = "Aura HUD Active Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active FPS and Temperature corner overlay"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Aura Gaming Booster Active")
            .setContentText("Monitoring Real-time FPS & CPU/GPU Temperatures")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun setupFloatingHud() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 120
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 20, 28, 20)

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(Color.argb(225, 15, 12, 28))
                setStroke(3, Color.parseColor("#B388FF"))
            }
            background = bg
        }

        tvFps = TextView(this).apply {
            text = "⚡ 60.0 FPS"
            setTextColor(Color.parseColor("#00FFB2"))
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tvCpuTemp = TextView(this).apply {
            text = "🌡️ CPU: 38.5°C"
            setTextColor(Color.parseColor("#FF9100"))
            textSize = 12f
        }
        tvGpuTemp = TextView(this).apply {
            text = "🎮 GPU: 39.0°C"
            setTextColor(Color.parseColor("#FFD54F"))
            textSize = 12f
        }
        tvAura = TextView(this).apply {
            text = "AURA🟣 +100 Active"
            setTextColor(Color.parseColor("#D500F9"))
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        layout.addView(tvFps)
        layout.addView(tvCpuTemp)
        layout.addView(tvGpuTemp)
        layout.addView(tvAura)

        // Make HUD draggable
        layout.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX - (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(layout, params)
                        return true
                    }
                }
                return false
            }
        })

        floatingView = layout
        try {
            windowManager?.addView(layout, params)
        } catch (_: Exception) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        monitorJob?.cancel()
        hardwareMonitor?.stop()
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (_: Exception) { }
        }
    }
}
