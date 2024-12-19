package com.redhorse.accountbank.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest.permission.*

class PermissionUtils {

    companion object {

        // 알림 접근 권한 확인
        fun isNotificationListenerPermissionGranted(activity: Activity): Boolean {
            val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(activity)
            return enabledListeners.contains(activity.packageName)
        }

        // 알림 접근 권한 요청
        fun requestNotificationPermission(activity: Activity) {
            if (!isNotificationListenerPermissionGranted(activity)) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                activity.startActivity(intent)
                Toast.makeText(
                    activity,
                    "알림 접근 권한을 허용해야 앱이 정상 동작합니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // SMS 권한 요청 시 사용자에게 설명 추가
        fun requestSmsPermissionWithExplanation(activity: Activity, requestCode: Int) {
            if (ContextCompat.checkSelfPermission(activity, READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                // 사용자에게 권한 요청 이유를 설명
                Toast.makeText(
                    activity,
                    "SMS 권한을 허용해야 결제 정보가 자동으로 저장됩니다.",
                    Toast.LENGTH_LONG
                ).show()
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(READ_SMS, RECEIVE_SMS),
                    requestCode
                )
            }
        }

        // 여러 권한 요청
        fun checkPermissions(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
            val missingPermissions = permissions.filter {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }

            return if (missingPermissions.isEmpty()) {
                true
            } else {
                ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), requestCode)
                false
            }
        }

        // 권한 요청 전에 설명을 제공하는 다이얼로그
        fun showPermissionExplanationDialog(activity: Activity, requestCode: Int) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("권한 요청")
            builder.setMessage("이 앱은 결제 알림 및 SMS 정보를 자동으로 기록하려면 알림과 SMS 권한이 필요합니다.")
            builder.setPositiveButton("허용") { _, _ ->
                requestNotificationPermission(activity)
            }
            builder.setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        // 알림 권한이 허용되면 SMS 권한 요청
        fun requestSmsPermissionAfterNotification(activity: Activity, requestCode: Int) {
            if (isNotificationListenerPermissionGranted(activity)) {
                requestSmsPermissionWithExplanation(activity, requestCode)
            } else {
                // 알림 권한이 아직 허용되지 않은 경우
                Toast.makeText(
                    activity,
                    "알림 권한을 먼저 허용해야 SMS 권한을 요청할 수 있습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
