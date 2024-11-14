package com.redhorse.accountbank

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Environment
import android.provider.Telephony
import android.telephony.SmsMessage
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
import com.redhorse.accountbank.utils.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    suspend fun fetchAndSavePaymentMessages(context: Context) {
        val smsUri = Telephony.Sms.CONTENT_URI
        val thirtyDaysAgo = LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val database = AppDatabase.getDatabase(context)
        val paymentDao = database.paymentDao()

        withContext(Dispatchers.IO) {
            val cursor: Cursor? = context.contentResolver.query(
                smsUri,
                arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE),
                "${Telephony.Sms.DATE} >= ?",
                arrayOf(thirtyDaysAgo.toString()),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val timestamp = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)

                    Log.d("SMSReader", "Message Date: $date")
                    Log.d("SMSReader", "Message Body: $body")

                    if (RegexUtils.isPaymentMessage(body)) {
                        try {
                            val payment = RegexUtils.parsePaymentInfo(body, date)

                            // 중복 데이터 확인
                            val existingCount = paymentDao.countPaymentByDetails(payment.title, payment.amount, payment.date)
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
                }
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