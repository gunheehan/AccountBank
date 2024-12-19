import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavePaymentRepository(private val dbHelper: AppDatabaseHelper) {
    val TABLE_SAVE_PAYMENT = "SavePayment"

    // 특정 테이블에 데이터 삽입, 테이블이 없으면 생성 후 삽입
    suspend fun insertOrCreateTableAndInsert(payment: Payment) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            // 1. 테이블 존재 여부 확인
            if (!isTableExists(db)) {
                dbHelper.createSavePaymentTable()
            }

            // 2. 중복 여부 확인 및 데이터 삽입
            val contentValues = ContentValues().apply {
                put("title", payment.title)
                put("type", payment.type)
                put("subtype", payment.subtype)
                put("amount", payment.amount)
                put("date", payment.date)
            }

            val query = "SELECT COUNT(*) FROM $TABLE_SAVE_PAYMENT WHERE date = ? AND title = ?"
            val cursor = db.rawQuery(query, arrayOf(payment.date, payment.title))
            cursor.moveToFirst()
            val isDuplicate = cursor.getInt(0) > 0
            cursor.close()

            if (!isDuplicate) {
                db.insert(TABLE_SAVE_PAYMENT, null, contentValues)
            }
        }
    }

    // 특정 ID의 Payment 가져오기
    fun getPaymentById(date: String, id: Long): Payment? {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM $TABLE_SAVE_PAYMENT WHERE id = ?"
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
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put("title", payment.title)
            put("type", payment.type)
            put("subtype", payment.subtype)
            put("amount", payment.amount)
            put("date", payment.date)
        }
        return db.update(TABLE_SAVE_PAYMENT, contentValues, "id = ?", arrayOf(id.toString()))
    }

    // 특정 ID의 Payment 삭제
    fun deletePaymentById(date: String, id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(TABLE_SAVE_PAYMENT, "id = ?", arrayOf(id.toString()))
    }

    // 특정 테이블의 모든 Payment 가져오기
    suspend fun getAllSavePayments(): List<Payment> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase

        // 테이블 존재 여부 확인
        if (!isTableExists(db)) {
            return@withContext emptyList()
        }

        val query = "SELECT * FROM $TABLE_SAVE_PAYMENT"
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

    suspend fun getPaymentsByType(type: String): List<Payment> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase

        // 테이블 존재 여부 확인
        if (!isTableExists(db)) {
            return@withContext emptyList()
        }

        // 입력받은 type을 이용해 SQL 쿼리 수정
        val query = "SELECT * FROM $TABLE_SAVE_PAYMENT WHERE type = ?"
        val cursor = db.rawQuery(query, arrayOf(type))
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
    private fun isTableExists(db: SQLiteDatabase): Boolean {
        val query = """
            SELECT name 
            FROM sqlite_master 
            WHERE type = 'table' AND name = ?
        """
        val cursor = db.rawQuery(query, arrayOf(TABLE_SAVE_PAYMENT))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}

