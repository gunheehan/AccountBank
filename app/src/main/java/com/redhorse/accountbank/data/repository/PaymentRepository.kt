import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.*

class PaymentRepository(private val dbHelper: AppDatabaseHelper) {
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
}

