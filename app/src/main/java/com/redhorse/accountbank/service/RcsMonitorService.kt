package com.redhorse.accountbank.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.redhorse.accountbank.observer.RcsContentObserver

class RcsMonitorService : Service() {

    private lateinit var contentObserver: RcsContentObserver

    override fun onCreate() {
        super.onCreate()
        Log.d("RcsMonitorService", "서비스 시작")

        val rcsUri = Uri.parse("content://im/chat")
        contentObserver = RcsContentObserver(this, Handler())
        contentResolver.registerContentObserver(
            rcsUri,
            true,
            contentObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RcsMonitorService", "서비스 종료")
        contentResolver.unregisterContentObserver(contentObserver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
