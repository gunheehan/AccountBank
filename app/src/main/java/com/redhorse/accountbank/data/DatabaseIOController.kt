import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream

class DatabaseIOController(private val dbHelper: AppDatabaseHelper) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // CSV 내보내기
    fun exportDatabases(context: Context) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val db = dbHelper.writableDatabase
                    val allTables = getAllTables(db)

                    allTables.filter { it.startsWith("Payment_") }
                        .forEach { tableName ->
                            exportTableToCSV(context, db, tableName)
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast(context, "Failed exporting data: ${e.message}")
                }
            }
        }
    }

    // 모든 테이블 가져오기
    private fun getAllTables(db: SQLiteDatabase): List<String> {
        val tables = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        return tables
    }

    // 테이블 데이터를 CSV로 내보내는 함수
    private suspend fun exportTableToCSV(context: Context, db: SQLiteDatabase, tableName: String) {
        val cursor = db.rawQuery("SELECT * FROM $tableName", null)
        val formattedFileName = "$tableName.csv"

        try {
            val outputStream = getOutputStreamForCSV(context, formattedFileName)
            outputStream?.use { stream ->
                while (cursor.moveToNext()) {
                    val data = buildCSVData(cursor)
                    stream.write(data.toByteArray())
                }
            }
            showToast(context, "$tableName exported to Downloads")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Failed to export $tableName: ${e.message}")
        } finally {
            cursor.close()
        }
    }

    // Android 버전에 맞는 OutputStream 얻기
    private fun getOutputStreamForCSV(context: Context, formattedFileName: String): OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, formattedFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { resolver.openOutputStream(it) }
        } else {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadDir, formattedFileName)
            file.outputStream()
        }
    }

    // 데이터베이스에서 가져온 커서로부터 CSV 데이터를 문자열로 변환
    private fun buildCSVData(cursor: android.database.Cursor): String {
        return (0 until cursor.columnCount).joinToString(", ") { cursor.getString(it) } + "\n"
    }

    // 토스트 메시지 표시
    private suspend fun showToast(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // CSV 데이터 가져오기 및 데이터베이스에 삽입
    suspend fun importDatabases(context: Context, uris: List<Uri>) {
        coroutineScope.launch {
            val contentResolver = context.contentResolver
            val db = dbHelper.writableDatabase
            val paymentRepository = PaymentRepository(dbHelper)

            uris.forEach { uri ->
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val fileName = getFileNameFromUri(contentResolver, uri) ?: throw IllegalArgumentException("Invalid file name")

                        db.beginTransaction()
                        try {
                            reader.forEachLine { line ->
                                // suspend 함수 호출을 launch로 감싸서 처리
                                coroutineScope.launch(Dispatchers.IO) {
                                    processCSVLine(line, paymentRepository)
                                }
                            }
                            db.setTransactionSuccessful()
                            showToast(context, "$fileName data successfully imported")
                        } finally {
                            db.endTransaction()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast(context, "File processing failed: ${e.message}")
                }
            }
        }
    }

    // URI로부터 파일 이름 가져오기
    private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null
        }
    }

    // CSV 한 줄 처리 및 데이터베이스에 저장
    private suspend fun processCSVLine(line: String, paymentRepository: PaymentRepository) {
        try {
            val columns = line.split(",").map { it.trim() }
            if (columns.size != 6) throw IllegalArgumentException("Invalid column count")
            val newPayment = Payment(
                columns[0].toLong(),
                columns[1],
                columns[2],
                columns[3].toInt(),
                columns[4].toInt(),
                columns[5]
            )
            // suspend 함수 호출 처리
            paymentRepository.insertOrCreateTableAndInsert(newPayment)
        } catch (e: Exception) {
            Log.w("ImportData", "Skipping invalid data: $line, Error: ${e.message}")
        }
    }
}
