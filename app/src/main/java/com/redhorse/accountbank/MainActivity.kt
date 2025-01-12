package com.redhorse.accountbank

import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.redhorse.accountbank.alarm.MonthlyAlarmScheduler
import com.redhorse.accountbank.modal.SimpleDialogFragment
import com.redhorse.accountbank.utils.PermissionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

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
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_main)

        // ViewPager2 설정
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavi)

        // BottomNavigationView와 ViewPager2 동기화
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.reportItem -> viewPager.setCurrentItem(0, true)
                R.id.calenderitem -> viewPager.setCurrentItem(1, true)
                R.id.regularyItem -> viewPager.setCurrentItem(2, true)
                R.id.settingitem -> viewPager.setCurrentItem(3, true)
                // R.id.yearItem -> viewPager.setCurrentItem(4, true)
            }
            true
        }

        // ViewPager2와 BottomNavigationView 상태 동기화
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // 권한 요청 처리
        if (!hasRequestedPermissions(PREFS_KEY_FIRST_REQUESTED)) {
            showPermissionExplanationDialog(this)
            setPermissionRequested(PREFS_KEY_FIRST_REQUESTED)
        }
    }


    fun showPermissionExplanationDialog(activity: FragmentActivity) {
        val dialog = SimpleDialogFragment.newInstance(
            title = "이 앱은 결제 알림 및 SMS 정보를 자동으로 기록하려면 알림과 SMS 권한이 필요합니다.",
            onYesClick = {
                PermissionUtils.requestNotificationPermission(activity)
            },
            onNoClick = { /* 취소 시 추가 동작 없음 */ }
        )
        dialog.show(activity.supportFragmentManager, "PermissionExplanationDialog")
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
