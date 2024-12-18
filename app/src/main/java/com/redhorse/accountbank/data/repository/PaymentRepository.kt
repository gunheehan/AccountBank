import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.*
import java.io.*

class PaymentRepository(private val dbHelper: AppDatabaseHelper) {

    fun exportDatabaseToDownloads(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    val db = dbHelper.writableDatabase
                    val allTables = getAllTables(db)  // 모든 테이블 이름을 가져옴

                    for (tableName in allTables) {
                        if (tableName.startsWith("Payment_")) {
                            val cursor = db.rawQuery("SELECT * FROM $tableName", null)

                            val formattedFileName = "$tableName.csv"

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Android 10 이상: MediaStore를 사용
                                val resolver = context.contentResolver
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, formattedFileName)
                                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                                }
                                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                                if (uri != null) {
                                    resolver.openOutputStream(uri).use { outputStream ->
                                        while (cursor.moveToNext()) {
                                            val data = "${cursor.getString(0)}, " +
                                                    "${cursor.getString(1)}, " +
                                                    "${cursor.getString(2)}, " +
                                                    "${cursor.getString(3)}, " +
                                                    "${cursor.getString(4)}, " +
                                                    "${cursor.getString(5)}\n"
                                            outputStream?.write(data.toByteArray())
                                        }
                                    }
                                }
                            } else {
                                // Android 9 이하: 직접 경로 사용
                                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val file = File(downloadDir, formattedFileName)
                                file.outputStream().use { outputStream ->
                                    while (cursor.moveToNext()) {
                                        val data = "${cursor.getString(0)}, " +
                                                "${cursor.getString(1)}, " +
                                                "${cursor.getString(2)}, " +
                                                "${cursor.getString(3)}, " +
                                                "${cursor.getString(4)}, " +
                                                "${cursor.getString(5)}\n"
                                        outputStream.write(data.toByteArray())
                                    }
                                }
                            }

                            cursor.close()
                            withContext(Dispatchers.Main) {
                            Toast.makeText(context, "$tableName exported to Downloads", Toast.LENGTH_SHORT).show()
                        }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed exporting data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    suspend fun importDatabases(context: Context, uris: List<Uri>) {
        CoroutineScope(Dispatchers.IO).launch {
            val contentResolver = context.contentResolver

            for (uri in uris) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))

                        val fileName = getFileNameFromUri(contentResolver, uri)
                        val tableName = fileName?.removeSuffix(".csv") ?: throw IllegalArgumentException("파일 이름이 올바르지 않습니다.")

                        val db = dbHelper.writableDatabase
                        db.beginTransaction()
                        try {
                            // CSV 데이터 읽기
                            reader.forEachLine { line ->
                                try {
                                    val columns = line.split(",").map { it.trim() }
                                    if (columns.size != 6) {
                                        throw IllegalArgumentException("컬럼 수가 올바르지 않습니다.")
                                    }

                                    val newpayment = Payment(
                                        columns[0].toLong(),
                                        columns[1],
                                        columns[2],
                                        columns[3].toInt(),
                                        columns[4].toInt(),
                                        columns[5])

                                    GlobalScope.launch(Dispatchers.Main) {
                                        insertOrCreateTableAndInsert(newpayment)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.w("ImportData", "잘못된 데이터 건너뛰기: $line, 오류: ${e.message}")
                                }
                            }
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "$fileName 데이터가 성공적으로 삽입되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "파일 처리 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        }
    }




    // 특정 테이블에 데이터 삽입, 테이블이 없으면 생성 후 삽입
    suspend fun insertOrCreateTableAndInsert(payment: Payment) {
        withContext(Dispatchers.IO) {
            val tableName = getTableName(payment.date)
            val db = dbHelper.writableDatabase

            // 1. 테이블 존재 여부 확인
            if (!isTableExists(db, tableName)) {
                dbHelper.createPaymentTable(tableName)
            }

            // 2. 중복 여부 확인 및 데이터 삽입
            val contentValues = ContentValues().apply {
                put("title", payment.title)
                put("type", payment.type)
                put("subtype", payment.subtype)
                put("amount", payment.amount)
                put("date", payment.date)
            }

            val query = "SELECT COUNT(*) FROM $tableName WHERE date = ? AND title = ?"
            val cursor = db.rawQuery(query, arrayOf(payment.date, payment.title))
            cursor.moveToFirst()
            val isDuplicate = cursor.getInt(0) > 0
            cursor.close()

            if (!isDuplicate) {
                db.insert(tableName, null, contentValues)
            }
        }
    }

    // 특정 ID의 Payment 가져오기
    fun getPaymentById(date: String, id: Int): Payment? {
        val tableName = getTableName(date)

        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM $tableName WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        var payment: Payment? = null
        if (cursor.moveToFirst()) {
            payment = Payment(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                subtype = cursor.getInt(cursor.getColumnIndexOrThrow("subtype")),
                amount = cursor.getInt(cursor.getColumnIndexOrThrow("amount")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            )
        }
        cursor.close()
        return payment
    }

    // 특정 ID의 Payment 업데이트
    fun updatePaymentById(id: Long, payment: Payment): Int {
        val tableName = getTableName(payment.date)

        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put("title", payment.title)
            put("type", payment.type)
            put("subtype", payment.subtype)
            put("amount", payment.amount)
            put("date", payment.date)
        }
        return db.update(tableName, contentValues, "id = ?", arrayOf(id.toString()))
    }

    // 특정 ID의 Payment 삭제
    fun deletePaymentById(date: String, id: Long): Int {
        val tableName = getTableName(date)

        val db = dbHelper.writableDatabase
        return db.delete(tableName, "id = ?", arrayOf(id.toString()))
    }

    // 특정 title, amount, date가 동일한 Payment 조회
    fun getPaymentByTitleAmountDate(title: String, amount: Int, date: String): Payment? {
        val tableName = getTableName(date)

        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM $tableName WHERE title = ? AND amount = ? AND date = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(title, amount.toString(), date))

        var payment: Payment? = null

        if (cursor.moveToFirst()) {
            payment = Payment(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                subtype = cursor.getInt(cursor.getColumnIndexOrThrow("subtype")),
                amount = cursor.getInt(cursor.getColumnIndexOrThrow("amount")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            )
        }
        cursor.close()
        return payment
    }

    // 특정 테이블의 모든 Payment 가져오기
    suspend fun getAllPaymentsByMonth(date: String): List<Payment> = withContext(Dispatchers.IO) {
        val tableName = getTableName(date)
        val db = dbHelper.readableDatabase

        // 테이블 존재 여부 확인
        if (!isTableExists(db, tableName)) {
            return@withContext emptyList() // 테이블이 없으면 빈 리스트 반환
        }

        val query = "SELECT * FROM $tableName"
        val cursor = db.rawQuery(query, null)
        val payments = mutableListOf<Payment>()

        try {
            // 커서에서 데이터 읽기
            while (cursor.moveToNext()) {
                payments.add(
                    Payment(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        subtype = cursor.getInt(cursor.getColumnIndexOrThrow("subtype")),
                        amount = cursor.getInt(cursor.getColumnIndexOrThrow("amount")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 커서 닫기
            cursor.close()
        }

        payments // 결과 반환
    }



    // 테이블 존재 여부 확인
    private fun isTableExists(db: SQLiteDatabase, tableName: String): Boolean {
        val query = """
            SELECT name 
            FROM sqlite_master 
            WHERE type = 'table' AND name = ?
        """
        val cursor = db.rawQuery(query, arrayOf(tableName))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun getTableName(date: String) : String{
        val datesplit = date.split("-")
        val year = datesplit[0].toInt()
        val month = datesplit[1].toInt()
        return "Payment_${year}_${month}"
    }

    private fun getAllTables(db: SQLiteDatabase): List<String> {
        val tables = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        return tables
    }

}

