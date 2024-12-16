package com.redhorse.accountbank

import PaymentRepository
import RegexUtils
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
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

    private lateinit var paymentRepository: PaymentRepository

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
        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        SetButton(view)
        return view
    }

    // 내보내기 버튼 함수
    private fun exportDatabase(context: Context) {
        try {
            createBackupFile(context)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "DB 내보내기 실패: ${e.message}", Toast.LENGTH_LONG).show()
            Log.d("DBError", "${e.message}")
        }
    }

    private fun createBackupFile(context: Context) {
        try {
            // 현재 데이터베이스 경로
            val currentDB = context.getDatabasePath("payments_db")

            // 백업 파일 경로: 외부 저장소의 Download 폴더
            val backupDB = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "payments_db_backup")

            // 파일 스트림 및 채널 사용
            val src = FileInputStream(currentDB).channel
            val dst = FileOutputStream(backupDB).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()

            // 백업 성공 메시지
            Toast.makeText(context, "Backup successful", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            // 오류 처리
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    @Throws(IOException::class)
    private fun backupEachFile(context: Context, from: String, to: String) {
        val sd = Environment.getExternalStorageDirectory()
        val data = Environment.getDataDirectory()
        if (sd.canWrite()) {
            val currentDB = File(data, from)
            val backupDB = File(sd, to)
            val src: FileChannel = FileInputStream(currentDB).channel
            val dst: FileChannel = FileOutputStream(backupDB).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
            Toast.makeText(context, "success", Toast.LENGTH_LONG)
                .show()
        } else {
            Toast.makeText(
                context,
                "cancel backup",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 가져오기 버튼 함수
    private fun importDatabase(context: Context) {
        try {
            val dbPath = context.getDatabasePath("payments.db").absolutePath
            val backupPath = File(Environment.getExternalStorageDirectory(), "BackupDatabase.db")

            FileInputStream(backupPath).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                    Toast.makeText(context, "DB가 성공적으로 가져와졌습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "DB 가져오기 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
        val thirtyDaysAgo = LocalDate.now().minusDays(90) // 데이터 불러올 날짜(오늘부터 이전 DAY)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val rcsUri = Uri.parse("content://im/chat")

        withContext(Dispatchers.IO) {
            try {
                // 30일 전의 날짜로 쿼리
                val cursor = context.contentResolver.query(
                    rcsUri,
                    null, // 모든 컬럼 선택
                    "date >= ?", // 조건: 30일 이내
                    arrayOf(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toString()),
                    "date DESC" // 최신 메시지부터 정렬
                )

                cursor?.use {
                    val messageCount = it.count

                    if (messageCount == 0) {
                        return@use
                    }

                    if (it.moveToFirst()) {
                        val columnCount = it.columnCount

                        // 모든 메시지를 리스트로 저장
                        val messages = mutableListOf<Map<String, String>>()

                        do {
                            val messageData = mutableMapOf<String, String>()

                            for (i in 0 until columnCount) {
                                val columnName = it.getColumnName(i)
                                val columnValue = it.getString(i) ?: "null" // null 체크 추가
                                messageData[columnName] = columnValue
                            }

                            // 메시지 데이터 로그 출력
                            messages.add(messageData)

                            // body 내용 처리
                            val body = messageData["body"]

                            if (body != null) {
                                try {
                                    val jsonObject = JSONObject(body)
                                    val cardLayout = jsonObject.getJSONObject("layout")
                                    val children = cardLayout.getJSONArray("children")
                                    val firstChild = children.getJSONObject(1) // 두 번째 child
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

                        // 모든 메시지 로그 출력
                        Log.d("RCSReader", "All Messages: $messages")
                    } else {
                        Log.d("RCSReader", "No messages found in content://im/chat")
                    }
                } ?: Log.e("RCSReader", "Failed to query content://im/chat")
            } catch (e: Exception) {
                Log.e("__T", "Error fetching RCS messages: ${e.message}")
            }
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