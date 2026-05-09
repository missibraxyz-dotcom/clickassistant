package com.example.clickassistant
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import androidx.core.app.NotificationCompat
class OverlayService : Service() {
    companion object {
        const val ACTION_TAP_CAPTURED = "com.example.clickassistant.TAP_CAPTURED"
        const val EXTRA_X = "extra_x"
        const val EXTRA_Y = "extra_y"
        private const val CHANNEL_ID = "overlay_channel"
        private const val NOTIF_ID = 1
    }
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Overlay Capture", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        startForeground(NOTIF_ID, NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Click Assistant").setContentText("Tap anywhere to record")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).build())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START }
        val view = View(this).apply {
            setBackgroundColor(Color.argb(30, 0, 120, 255))
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    sendBroadcast(Intent(ACTION_TAP_CAPTURED).apply {
                        putExtra(EXTRA_X, event.rawX); putExtra(EXTRA_Y, event.rawY); setPackage(packageName) })
                    stopSelf()
                }
                false
            }
        }
        overlayView = view
        windowManager.addView(view, params)
    }
    override fun onDestroy() { super.onDestroy(); overlayView?.let { windowManager.removeView(it) } }
}