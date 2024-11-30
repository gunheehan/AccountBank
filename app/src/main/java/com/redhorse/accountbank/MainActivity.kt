package com.redhorse.accountbank

import android.Manifest.permission.*
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.redhorse.accountbank.receiver.NotificationReceiverService


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()
        checkAndRequestNotificationPermission()

        val mainboardFragment = MainboardFragment()
        val earningFragment = EarningFragment()
        val expensesFragment = ExpensesFragment()
        val fixedInformationFragment = FixedInformationFragment()
        val yearFragment = YearFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavi)
        replaceFragment(mainboardFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mainItem -> replaceFragment(mainboardFragment)
                R.id.earningItem -> replaceFragment(earningFragment)
                R.id.expensesItem -> replaceFragment(expensesFragment)
                R.id.fixedsettingItem -> replaceFragment(fixedInformationFragment)
                R.id.yearItem -> replaceFragment(yearFragment)
            }
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {  // 권한 요청 코드
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었을 때 처리할 작업
                // 예: 푸시 알림 보내기
            } else {
                // 권한 거부 처리
                Toast.makeText(this, "푸시 알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.commit()
    }

    private fun checkNotificationPermission() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS,
                READ_SMS, RECEIVE_SMS), 1)
        }

        prefs.edit().putBoolean("isFirstLaunch", false).apply()
    }
    private fun checkAndRequestNotificationPermission() {
        if (!isNotificationPermissionGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 이상에서는 POST_NOTIFICATIONS 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            } else {
                // Android 13 미만에서는 Notification Listener 권한 요청
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        // Notification Listener 권한이 있는지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.isNotificationListenerAccessGranted(ComponentName(application, NotificationReceiverService::class.java))
        } else {
            // Notification Listener 권한이 이미 활성화된 앱 목록에 포함되는지 확인
            return NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(applicationContext.packageName)
        }
    }

}