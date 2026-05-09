package com.example.clickassistant
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.*
import android.graphics.Path
import android.os.*
import android.view.accessibility.AccessibilityEvent
class GestureAccessibilityService : AccessibilityService() {
    companion object { const val ACTION_PLAY = "com.example.clickassistant.ACTION_PLAY" }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var repo: GesturesRepository
    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (intent?.action == ACTION_PLAY) playGestures() }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = GesturesRepository(this)
        val filter = IntentFilter(ACTION_PLAY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(playReceiver, filter, RECEIVER_NOT_EXPORTED)
        else registerReceiver(playReceiver, filter)
    }
    private fun playGestures() {
        val steps = repo.getSteps()
        if (steps.isEmpty()) return
        dispatchNext(steps, 0)
    }
    private fun dispatchNext(steps: List<GestureStep>, index: Int) {
        if (index >= steps.size) return
        val step = steps[index]
        val path = Path().apply { moveTo(step.x, step.y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(g: GestureDescription) {
                handler.postDelayed({ dispatchNext(steps, index + 1) }, step.delayMs)
            }
        }, null)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { super.onDestroy(); try { unregisterReceiver(playReceiver) } catch (_: Exception) {} }
}