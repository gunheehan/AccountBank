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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.redhorse.accountbank.alarm.MonthlyAlarmScheduler
import com.redhorse.accountbank.receiver.NotificationReceiverService


class MainActivity : AppCompatActivity(){
    private val generalPermissions = arrayOf(
        POST_NOTIFICATIONS,
        READ_SMS,
        RECEIVE_SMS,
        )

    private val REQUEST_GENERAL_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MonthlyAlarmScheduler().scheduleMonthlyAlarm(this)

        setContentView(R.layout.activity_main)

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

    private fun replaceFragment(fragment: Fragment) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestGeneralPermissions()
    }

    private fun checkAndRequestGeneralPermissions() {
        val missingPermissions = generalPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_GENERAL_PERMISSIONS)
        } else {
            checkAndRequestNotificationAccess()
        }
    }

    private fun checkAndRequestNotificationAccess() {
        if (!isNotificationListenerPermissionGranted()) {
            // 알림 접근 권한이 없는 경우 설정 화면으로 이동
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "알림 접근 권한을 허용해야 앱이 정상 동작합니다.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isNotificationListenerPermissionGranted(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledListeners.contains(packageName)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_GENERAL_PERMISSIONS) {
            val deniedPermissions = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }

            if (deniedPermissions.isEmpty()) {
                // 일반 권한 요청이 모두 허용되었으므로 알림 접근 권한 요청 진행
                checkAndRequestNotificationAccess()
            } else {
                Toast.makeText(
                    this,
                    "모든 권한을 허용해야 앱이 정상 동작합니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}