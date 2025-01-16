package com.redhorse.accountbank

import DatabaseIOController
import PaymentRepository
import RegexUtils
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import com.redhorse.accountbank.item.CustomCardView
import com.redhorse.accountbank.utils.PermissionUtils
import kotlinx.coroutines.*
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class SettingFragment : Fragment() {

    private lateinit var paymentRepository: PaymentRepository
    private lateinit var databaseIOController: DatabaseIOController
    private lateinit var notification_card: CustomCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)
        databaseIOController = DatabaseIOController(dbHelper)

        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        notification_card = view.findViewById<CustomCardView>(R.id.set_notification_card)
        val dbms_card = view.findViewById<CustomCardView>(R.id.set_DBMS_card)

        setNotificationCard(notification_card)
        SetDBMSCard(dbms_card)

        val versionTextView = view.findViewById<TextView>(R.id.set_version_text)
        val versionName = BuildConfig.VERSION_NAME
        versionTextView.text = "버전: $versionName"
        return view
    }

    override fun onResume() {
        super.onResume()
        setNotificationCard(notification_card)
    }

    private fun setNotificationCard(cardView: CustomCardView){
        cardView.Container.removeAllViews()

        val isNotificationGranted = PermissionUtils.isPushNotificationPermissionGranted(requireActivity())
        cardView.addTitle("알림 설정")
        cardView.addTextWithToggle(
            text = "푸시 알림",
            toggleInitialState = isNotificationGranted,
            onToggleValueChange = { isChecked ->
                onValueChangeNotification(isChecked)
            }
        )
    }

    private fun onValueChangeNotification(isOn: Boolean){
        if(isOn) {
            PermissionUtils.requestPushNotificationPermission(requireActivity(),1)
        }
        else{
            PermissionUtils.disablePushNotifications(requireActivity())
        }
    }

    private fun SetDBMSCard(cardView: CustomCardView){
        cardView.addTitle("데이터 관리")
        cardView.addTextWithButton(
            text = "데이터 내보내기",
            imageResId = R.drawable.icon_export,
            onClickAction = {
                exportDatabase()
            }
        )
        cardView.addTextWithButton(
            text = "데이터 가져오기",
            imageResId = R.drawable.icon_import,
            onClickAction = {
                importDatabase()
            }
        )
        cardView.addTextWithButton(
            text = "메시지 읽어오기(90일)",
            imageResId = R.drawable.icon_file_import,
            onClickAction = {
                lifecycleScope.launch {
                    fetchAndSavePaymentMessages()
                }
            })
    }

    // 내보내기 버튼 함수
    private fun exportDatabase() {
        if(!PermissionUtils.checkAndRequestSmsPermissions(requireActivity()))
            return;

        GlobalScope.launch(Dispatchers.Main) {
            databaseIOController.exportDatabases(requireContext())
        }
    }

    // 가져오기 버튼 함수
    private fun importDatabase() {
        if(!PermissionUtils.checkAndRequestSmsPermissions(requireActivity()))
            return;

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try{
            filePickerLauncher.launch(arrayOf("*/*"))
        }catch (e: Exception){

        }
    }
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris != null && uris.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    databaseIOController.importDatabases(
                        context = requireContext(),
                        uris = uris
                    )
                }
            } else {
                Toast.makeText(requireContext(), "파일을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    fun extractAndFormatDate(dateString: String): String {
        try {
            val datePart = dateString.substringBefore(" ")

            val currentDate = LocalDate.now()
            val currentYear = currentDate.year
            val currentMonth = currentDate.monthValue

            val (month, day) = datePart.split("/").map { it.toInt() }

            var year = currentYear
            if (currentMonth <= 3 && month > currentMonth) {
                year = currentYear - 1
            }

            val formattedDate = LocalDate.of(year, month, day)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return formattedDate.format(formatter)

        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            return "Invalid Date"
        }
    }


    fun parseMessageBody(messageBody: String): String? {
        val dateRegex = """(\d{1,2}/\d{1,2})""".toRegex()  // "MM/dd" 형식의 날짜 추출
        val matchResult = dateRegex.find(messageBody)

        return matchResult?.value?.let { extractAndFormatDate(it) }
    }

    suspend fun fetchAndSavePaymentMessages() {
        val thirtyDaysAgo = LocalDate.now().minusDays(90)
        val rcsUri = Uri.parse("content://im/chat")

        withContext(Dispatchers.IO) {
            try {
                val cursor = requireContext().contentResolver.query(
                    rcsUri,
                    null,
                    "date >= ?",
                    arrayOf(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toString()),
                    "date DESC"
                )

                cursor?.use {
                    val messageCount = it.count
                    if (messageCount == 0) {
                        return@use
                    }

                    if (it.moveToFirst()) {
                        val columnCount = it.columnCount

                        do {
                            val messageData = mutableMapOf<String, String>()

                            for (i in 0 until columnCount) {
                                val columnName = it.getColumnName(i)
                                val columnValue = it.getString(i) ?: "null"
                                messageData[columnName] = columnValue
                            }

                            val body = messageData["body"]

                            if (body != null) {
                                try {
                                    val jsonObject = JSONObject(body)
                                    val cardLayout = jsonObject.getJSONObject("layout")
                                    val children = cardLayout.getJSONArray("children")
                                    val firstChild = children.getJSONObject(1)
                                    val textWidget = firstChild.getJSONArray("children").getJSONObject(0)
                                    val text = textWidget.getString("text")

                                    val formattedDate = parseMessageBody(text)

                                    if (RegexUtils.isPaymentMessage(text)) {
                                        try {
                                            val payment = RegexUtils.parsePaymentInfo(text, formattedDate.toString())

                                            if (payment.amount > 0) {
                                                paymentRepository.insertOrCreateTableAndInsert(payment)
                                            } else {
                                                Log.d("SMSReader", "Duplicate Payment Skipped: $payment")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("SMS Parsing", "Error parsing payment info: ${e.message}")
                                        }
                                    }

                                } catch (e: Exception) {
                                    Log.e("RCSReader", "Error parsing body: ${e.message}")
                                }
                            }
                        } while (it.moveToNext()) // 다음 메시지로 이동
                    } else {
                        Log.d("RCSReader", "No messages found in content://im/chat")
                    }
                } ?: Log.e("RCSReader", "Failed to query content://im/chat")
            } catch (e: Exception) {
                Log.e("__T", "Error fetching RCS messages: ${e.message}")
            }
        }
    }
}