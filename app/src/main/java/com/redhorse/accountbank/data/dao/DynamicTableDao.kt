package com.redhorse.accountbank.data.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.entity.YearMonth

@Dao
interface DynamicTableDao {
    // YearMonthTable에 데이터를 삽입하는 메서드
    @Insert
    suspend fun insertYearMonth(yearMonth: YearMonth)

    // 특정 년월에 해당하는 결제내역 테이블이 없다면 생성
    @Transaction
    suspend fun createYearMonthTable(date: String) {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val yearMonth = YearMonth(year = year, month = month)
        insertYearMonth(yearMonth)

        // 해당 년월 테이블이 없으면 생성
        val tableName = "Payment_${year}_${month}"
        val tableExists = checkIfTableExists(tableName)

        if (tableExists == 0) {
            val createTableQuery = SimpleSQLiteQuery(
                """
                CREATE TABLE IF NOT EXISTS $tableName (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, 
                    title TEXT, 
                    type TEXT, 
                    amount INTEGER, 
                    date TEXT
                )
                """.trimIndent()
            )
            executeSQL(createTableQuery)
        }
    }

    // Payment 데이터를 동적으로 생성된 테이블에 삽입하는 함수
    @RawQuery
    suspend fun insertPaymentToDynamicTable(query: SupportSQLiteQuery) : Int

    // 사용 예시: 특정 년월 테이블에 데이터를 삽입
    suspend fun insertPayment(payment: Payment) : Int {
        val splitday = payment.date.split("-")
        val year = splitday[0]
        val month = splitday[1]
        val tableName = "Payment_${year}_${month}"
        val query = SimpleSQLiteQuery(
            """
            INSERT INTO $tableName (title, type, amount, date) 
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            arrayOf(payment.title, payment.type, payment.amount, payment.date)
        )
        return insertPaymentToDynamicTable(query)
    }

    @RawQuery
    suspend fun updatePaymentInDynamicTable(query: SupportSQLiteQuery): Int

    // 특정 테이블에서 ID에 따라 Payment 데이터를 업데이트
    suspend fun updatePayment(
        paymentId: Long,
        title: String,
        type: String,
        amount: Int,
        date: String
    ): Int {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1]
        val tableName = "Payment_${year}_${month}"

        val query = SimpleSQLiteQuery(
            """
            UPDATE $tableName
            SET title = ?, type = ?, amount = ?, date = ?
            WHERE id = ?
            """.trimIndent(),
            arrayOf(title, type, amount, date, paymentId)
        )
        return updatePaymentInDynamicTable(query)
    }

    @RawQuery
    suspend fun getPaymentsFromDynamicTable(query: SupportSQLiteQuery): List<Payment>

    // 특정 테이블의 데이터를 조회하는 함수
    suspend fun getPaymentsFromTable(date: String): List<Payment> {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1]
        val tableName = "Payment_${year}_${month}"

        val query = SimpleSQLiteQuery(
            """
            SELECT * FROM $tableName WHERE date = ?
            """.trimIndent(),
            arrayOf(date)
        )
        return getPaymentsFromDynamicTable(query)
    }

    // 특정 월의 데이터를 조회하여 반환하는 메서드
    @RawQuery
    suspend fun getAllPaymentsFromDynamicTable(query: SupportSQLiteQuery): List<Payment>

    // 특정 월의 모든 결제 데이터를 반환하는 메서드
    suspend fun getPaymentsForMonth(day: String): List<Payment> {
        val splitday = day.split("-")
        val year = splitday[0]
        val month = splitday[1]
        val tableName = "Payment_${year}_${month}"

        // 테이블 존재 여부를 확인
        val tableExists = checkIfTableExists(tableName)
        if (tableExists == 0) {
            // 테이블이 존재하지 않으면 빈 리스트 반환
            return emptyList()
        }

        // 해당 테이블의 모든 데이터를 조회
        val query = SimpleSQLiteQuery(
            """
            SELECT * FROM $tableName
            """.trimIndent()
        )
        return getAllPaymentsFromDynamicTable(query)
    }

    // 테이블이 존재하는지 확인
    @Query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name = :tableName")
    suspend fun checkIfTableExists(tableName: String): Int

    suspend fun checkInvalidMonth(date: String): Boolean{
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1]
        val tableName = "Payment_${year}_${month}"
        val tableExists = checkIfTableExists(tableName)

        if (tableExists == 0) {
            return false
        }
        return true
    }

    // SQL 쿼리를 실행하는 함수
    @RawQuery
    suspend fun executeSQL(query: SupportSQLiteQuery): Int

    // 특정 테이블에서 조건에 맞는 결제의 개수 조회
    @RawQuery
    suspend fun countPaymentByDetails(query: SupportSQLiteQuery): Int

    // 조건에 맞는 결제의 개수 조회를 위한 메서드
    suspend fun countPayments(
        title: String,
        amount: Int,
        date: String
    ): Int {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1]
        val tableName = "Payment_${year}_${month}"

        val query = SimpleSQLiteQuery(
            """
            SELECT COUNT(*) FROM $tableName 
            WHERE title = ? AND amount = ? AND date = ?
            """.trimIndent(),
            arrayOf(title, amount, date)
        )
        return countPaymentByDetails(query)
    }
    // 특정 날짜와 ID를 기반으로 데이터를 삭제
    @RawQuery
    suspend fun deletePaymentFromDynamicTable(query: SupportSQLiteQuery): Int

    // 특정 날짜와 ID에 해당하는 결제 데이터를 삭제하는 메서드
    suspend fun deletePayment(paymentId: Long, date: String) {
        val parts = date.split("-")
        val year = parts[0]
        val month = parts[1]
        val tableName = "Payment_${year}_${month}"

        // SQL 쿼리 작성: 특정 테이블에서 특정 날짜와 ID에 해당하는 데이터 삭제
        val query = SimpleSQLiteQuery(
            "DELETE FROM $tableName WHERE id = ? AND date = ?",
            arrayOf(paymentId, date)
        )
        deletePaymentFromDynamicTable(query)
    }
}
