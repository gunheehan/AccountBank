package com.redhorse.accountbank

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExpensesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExpensesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        SetButton(view)
        return view
    }

    // 내보내기 버튼 함수
    private fun exportDatabase(context: Context) {
        try {
            // Room DB 파일의 기본 경로
            val dbPath = context.getDatabasePath("app_database.db").absolutePath
            val backupPath = File(Environment.getExternalStorageDirectory(), "BackupDatabase.db")

            FileInputStream(dbPath).use { input ->
                FileOutputStream(backupPath).use { output ->
                    input.copyTo(output)
                    Toast.makeText(context, "DB가 성공적으로 내보내졌습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "DB 내보내기 실패: ${e.message}", Toast.LENGTH_LONG).show()
            Log.d("DBError", "${e.message}")
        }
    }

    // 가져오기 버튼 함수
    private fun importDatabase(context: Context) {
        try {
            val dbPath = context.getDatabasePath("app_database.db").absolutePath
            val backupPath = File(Environment.getExternalStorageDirectory(), "BackupDatabase.db")

            FileInputStream(backupPath).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                    Toast.makeText(context, "DB가 성공적으로 가져와졌습니다.", Toast.LENGTH_SHORT).show()

                    // Room DB 인스턴스 갱신
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "app_database.db"
                    ).build()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "DB 가져오기 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun jsonTest(json:String) {
        val jsonObject = JSONObject(json)
        val layout = jsonObject.getJSONObject("layout")
        val text = when (layout.getString("widget")) {
            "LinearLayout" -> parseLinearLayout(layout)
            "TextView" -> layout.getString("text")
            else -> ""
        }
    }

    private fun parseLinearLayout(widget: JSONObject): String {
        val sb = StringBuilder()
        val isVertical = widget.getString("orientation") == "vertical"
        val children = widget.getJSONArray("children")
        for(i in 0 until children.length()) {
            val widget = children.getJSONObject(i)
            val widgetName = widget.getString("widget")
            if (widgetName == "LinearLayout") {
                val text = parseLinearLayout(widget)
                sb.append(text)
                if (isVertical) sb.append("\n")
            } else if (widgetName == "TextView") {
                val text = widget.getString("text")
                sb.append(text)
                if (isVertical) sb.append("\n")
            }
        }
        return sb.toString()
    }

    fun extractAndFormatDate(dateString: String): String {
        try {
            // 현재 년도 가져오기
            val currentYear = LocalDate.now().year

            // 날짜 정보 추출: 예시 "11/15 22:03"에서 "11/15"만 추출
            val datePart = dateString.substringBefore(" ") // "11/15"

            // 월과 일 분리
            val (month, day) = datePart.split("/").map { it.toInt() }

            // 현재 년도를 추가하여 LocalDate 객체 생성
            val formattedDate = LocalDate.of(currentYear, month, day)

            // 원하는 포맷으로 변환
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return formattedDate.format(formatter)

        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            return "Invalid Date"
        }
    }

    fun parseMessageBody(messageBody: String): String? {
        // 날짜를 추출하기 위한 정규 표현식 (MM/dd 형식만 추출)
        val dateRegex = """(\d{1,2}/\d{1,2})""".toRegex()  // "MM/dd" 형식의 날짜 추출
        val matchResult = dateRegex.find(messageBody)

        return matchResult?.value?.let { extractAndFormatDate(it) }
    }

    suspend fun fetchAndSavePaymentMessages(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val paymentDao = database.paymentDao()
        val thirtyDaysAgo = LocalDate.now().minusDays(30)  // 30일로 수정
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        Log.d("RCSReader", "Start load RCS")
        val rcsUri = Uri.parse("content://im/chat")

        withContext(Dispatchers.IO) {
            try {
                // 30일 전의 날짜로 쿼리
                val cursor = context.contentResolver.query(
                    rcsUri,
                    null,  // 모든 컬럼 선택
                    "date >= ?",  // 조건: 30일 이내
                    arrayOf(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toString()),
                    "date DESC"  // 최신 메시지부터 정렬
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnCount = it.columnCount

                        // 여러 메시지를 처리하기 위한 반복문
                        do {
                            val messageData = mutableMapOf<String, String>()

                            for (i in 0 until columnCount) {
                                val columnName = it.getColumnName(i)
                                val columnValue = it.getString(i) ?: "null"  // null 체크 추가
                                messageData[columnName] = columnValue
                            }

                            val body = messageData["body"] // body 내용 가져오기
                            Log.d("RCSReader", "RCS Message Data: $body")

                            if (body != null) {
                                try {
                                    // JSON 파싱
                                    val jsonObject = JSONObject(body)
                                    val cardLayout = jsonObject.getJSONObject("layout")
                                    val children = cardLayout.getJSONArray("children")

                                    // 첫 번째 child 요소의 내용 추출
                                    val firstChild = children.getJSONObject(1) // 두 번째 child, 텍스트가 포함된 부분
                                    val textWidget = firstChild.getJSONArray("children").getJSONObject(0)
                                    val text = textWidget.getString("text")

                                    Log.d("RCSReader", "Extracted Text: $text")

                                    val formattedDate = parseMessageBody(text)


                                    Log.d("RCSReader", "Extracted Date: ${formattedDate.toString()}")

                                    // 추출된 텍스트 출력

                                    if (RegexUtils.isPaymentMessage(body)) {
                                        try {
                                            val payment = RegexUtils.parsePaymentInfo(body, formattedDate.toString())

                                            // 중복 데이터 확인
                                            val existingCount = paymentDao.countPaymentByDetails(
                                                payment.title,
                                                payment.amount,
                                                payment.date
                                            )
                                            if (existingCount == 0) {
                                                paymentDao.insert(payment)
                                                Log.d("SMSReader", "Inserted Payment: $payment")
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
                            // String으로 데이터를 넘겨주기 위해 jsonTest 호출
                            jsonTest(messageData.toString())
                        } while (it.moveToNext())  // 다음 메시지로 이동
                    } else {
                        Log.d("__T", "No data found in content://im/chat")
                    }
                } ?: Log.e("__T", "Failed to query content://im/chat")
            } catch (e: Exception) {
                Log.e("__T", "Error fetching RCS messages: ${e.message}")
            }
        }
    }



    private suspend fun savePaymentsToDatabase(context: Context, payments: List<Payment>) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val paymentDao = db.paymentDao()
            payments.forEach { paymentDao.insert(it) }
        }
    }

    // 버튼 설정 함수
    private fun SetButton(view: View) {
        val exportButton = view.findViewById<Button>(R.id.export_btn)
        exportButton.setOnClickListener {
            exportDatabase(requireContext())
        }

        val importButton = view.findViewById<Button>(R.id.import_btn)
        importButton.setOnClickListener {
            importDatabase(requireContext())
        }

        val patchButton = view.findViewById<Button>(R.id.patch_btn)
        patchButton.setOnClickListener {
            lifecycleScope.launch {
                fetchAndSavePaymentMessages(requireContext())
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExpensesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExpensesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}