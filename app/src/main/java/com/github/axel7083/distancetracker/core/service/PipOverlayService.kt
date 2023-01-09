package com.github.axel7083.distancetracker.core.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.*
import androidx.lifecycle.LifecycleService
import com.github.axel7083.distancetracker.core.util.Convertion.toDp


abstract class PipOverlayService : LifecycleService(), View.OnTouchListener, OverlayService {

    lateinit var pipView: OverlayPipCustomView
    private lateinit var wm: WindowManager

    private var oldDraggableRawEventY: Float = 0f
    private var oldDraggableRawEventX: Float = 0f

    private var initialWindowSize: Point = Point(0, 0)

    private lateinit var screenSize: Point

    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    abstract override fun getForegroundNotification(): Notification

    abstract override fun getInitialWindowSize(): Point

    abstract fun getNotificationId(): Int

    abstract override fun onServiceRun()

    abstract fun stopService()

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notification: Notification = getForegroundNotification()
            startForeground(getNotificationId(), notification)
        }

        if(this@PipOverlayService::pipView.isInitialized.not())
            setupView()

        onServiceRun()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun setupView() {
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        pipView = OverlayPipCustomView(this)

        screenSize = getScreenSize(wm)

        initialWindowSize = getInitialWindowSize()

        params = WindowManager.LayoutParams(
            initialWindowSize.x,
            initialWindowSize.y,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            262152, // Do not know how I got this number (took from obstructed application using jadx)
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.START or Gravity.TOP
        params.x = 0
        params.y = 0
        wm.addView(pipView, params)

        pipView.getTouchView().setOnTouchListener(this)

        // Close listener
        pipView.setOnClosedListener {
            println("setOnClosedListener")
            stopService()
        }

        pipView.setOnHidedListener {
            println("on hided")
            if(pipView.isHided) {
                pipView.isHided = false
                pipView.expend()
                setWindowSize(getInitialWindowSize())
            }
            else {
                pipView.isHided = true
                pipView.minimize()
                setWindowSize(Point(50.toDp(), 50.toDp()))
            }

        }
    }

    override fun getWindowView() = pipView
    override fun getWindowManager() = wm

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {

                val changeDistanceY = (event.rawY - oldDraggableRawEventY)
                oldDraggableRawEventY = event.rawY
                var yPos: Float = params.y + changeDistanceY

                if (yPos < 0) {
                    yPos = 0f
                }
                if (screenSize.y < yPos + pipView.height) {
                    yPos = (screenSize.y - pipView.height).toFloat()
                }

                val changeDistanceX = (event.rawX - oldDraggableRawEventX)
                oldDraggableRawEventX = event.rawX
                var xPos: Float = params.x + changeDistanceX

                if (xPos < 0) {
                    xPos = 0f
                }
                if (screenSize.x < xPos + pipView.width) {
                    xPos = (screenSize.x - pipView.width).toFloat()
                }

                params.y = yPos.toInt()
                params.x = xPos.toInt()
                wm.updateViewLayout(pipView, params)
            }
            MotionEvent.ACTION_UP -> {
                pipView.isMoving = false
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            MotionEvent.ACTION_DOWN -> {
                pipView.isMoving = true
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            else -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
        }
        return true
    }

    private fun setWindowSize(point: Point) {
        params.height = point.y
        params.width = point.x

        wm.updateViewLayout(pipView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(pipView)
    }

    companion object {
        @JvmStatic
        private fun getScreenSize(windowManager: WindowManager): Point {
            val metrics = windowManager.currentWindowMetrics

            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            return Point(
                metrics.bounds.width() - insets.left - insets.right,
                metrics.bounds.height() - insets.bottom - insets.top,
            )
        }
    }
}