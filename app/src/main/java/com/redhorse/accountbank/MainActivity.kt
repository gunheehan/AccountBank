package com.redhorse.accountbank

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.redhorse.accountbank.alarm.MonthlyAlarmScheduler
import com.redhorse.accountbank.utils.PermissionUtils

class MainActivity : AppCompatActivity() {

    private val generalPermissions = arrayOf(
        POST_NOTIFICATIONS,
        READ_SMS,
        RECEIVE_SMS
    )

    private val REQUEST_GENERAL_PERMISSIONS = 1
    private val PREFS_NAME = "AppPrefs"
    private val PREFS_KEY_PERMISSION_REQUESTED = "permissionRequested"
    private val PREFS_KEY_FIRST_REQUESTED = "firstRequested"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        val reportFragment = ReportFragment()
        val calenderFragment = CalenderFragment()
        val settingFragment = SettingFragment()
        val fixedInformationFragment = RegularyFragment()
        val yearFragment = YearFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavi)
        replaceFragment(reportFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.reportItem -> replaceFragment(reportFragment)
                R.id.calenderitem -> replaceFragment(calenderFragment)
                R.id.regularyItem -> replaceFragment(fixedInformationFragment)
                R.id.settingitem -> replaceFragment(settingFragment)
                // R.id.yearItem -> replaceFragment(yearFragment)
            }
            true
        }
        if (!hasRequestedPermissions(PREFS_KEY_FIRST_REQUESTED)) {
            PermissionUtils.showPermissionExplanationDialog(this, REQUEST_GENERAL_PERMISSIONS)
            setPermissionRequested(PREFS_KEY_FIRST_REQUESTED)
        }
    }

    override fun onResume() {
        super.onResume()

        // 권한이 요청된 상태인지 확인하고, 필요시 다시 요청
        if (!hasRequestedPermissions(PREFS_KEY_PERMISSION_REQUESTED)) {
            // 알림 권한이 허용되었는지 확인
            if (PermissionUtils.isNotificationListenerPermissionGranted(this)) {
                // 알림 권한이 허용되었으면 SMS 권한 요청
                PermissionUtils.checkPermissions(this, generalPermissions, REQUEST_GENERAL_PERMISSIONS)
                setPermissionRequested(PREFS_KEY_PERMISSION_REQUESTED)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
    }

    private fun hasRequestedPermissions(key: String): Boolean {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPrefs.getBoolean(key, false)
    }

    private fun setPermissionRequested(key: String) {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(key, true).apply()
    }

    // 권한 요청 결과 처리
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
                // 모든 권한이 허용되었을 때 알림 접근 권한 확인
                PermissionUtils.requestSmsPermissionAfterNotification(this, REQUEST_GENERAL_PERMISSIONS)
                MonthlyAlarmScheduler().scheduleMonthlyAlarm(this)
            } else {
                // 권한이 거부된 경우
                Toast.makeText(
                    this,
                    "권한을 허용하지 않으면 앱에 기능을 정상적으로 사용할 수 없어요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
