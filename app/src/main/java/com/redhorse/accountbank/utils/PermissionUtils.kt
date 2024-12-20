package com.redhorse.accountbank.utils

import android.Manifest
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
import android.net.Uri
import android.os.Build

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
        fun isPushNotificationPermissionGranted(activity: Activity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 이상: POST_NOTIFICATIONS 권한 확인
                ContextCompat.checkSelfPermission(
                    activity,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // Android 13 미만: NotificationManagerCompat로 알림 상태 확인
                NotificationManagerCompat.from(activity).areNotificationsEnabled()
            }
        }

        fun requestPushNotificationPermission(activity: Activity, requestCode: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    requestCode
                )
            } else {
                // Android 13 미만에서는 권한 요청 필요 없음. 사용자 설정 안내만 추가 가능.
                Toast.makeText(
                    activity,
                    "설정에서 알림 권한을 활성화해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                }
                activity.startActivity(intent)
            }
        }

        fun disablePushNotifications(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 이상: 권한 설정 화면으로 이동
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                }
                activity.startActivity(intent)
            } else {
                // Android 12 이하 처리 (아래 참조)
                disablePushNotificationsForOlderVersions(activity)
            }
        }

        fun disablePushNotificationsForOlderVersions(activity: Activity) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
            }
            activity.startActivity(intent)
        }

        fun checkAndRequestSmsPermissions(activity: Activity): Boolean {
            // READ_SMS 권한 상태 확인
            if (ContextCompat.checkSelfPermission(activity, READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    Toast.makeText(
                        activity,
                        "SMS 권한을 허용해야 사용할 수 있습니다.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                activity.startActivity(intent)
                return false
            } else {
                return true
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
