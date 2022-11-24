package com.github.axel7083.distancetracker.core.service

import android.app.Notification
import android.graphics.Point
import android.view.View
import android.view.WindowManager

interface OverlayService {

    fun getForegroundNotification(): Notification
    fun getInitialWindowSize(): Point

    fun onServiceRun()

    fun getWindowView() : View
    fun getWindowManager() : WindowManager
}