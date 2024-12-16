package com.redhorse.accountbank.data.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "payments"
        const val DATABASE_VERSION = 1

        // YearMonth 테이블
        const val TABLE_YEAR_MONTH = "YearMonth"
        const val TABLE_SAVE_PAYMENT = "SavePayment"
    }

    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_YEAR_MONTH")
        onCreate(db)
    }

    // 특정 년/월의 Payment 테이블 생성
    fun createPaymentTable(tableName: String) {
        writableDatabase.execSQL("""
            CREATE TABLE IF NOT EXISTS $tableName (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('income', 'expense', 'save')),
                subtype INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                date TEXT NOT NULL,
                UNIQUE(date, title) ON CONFLICT IGNORE
            );
        """.trimIndent())
    }

    fun createSavePaymentTable(){
        writableDatabase.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SAVE_PAYMENT(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('income', 'expense', 'save')),
                subtype INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                date TEXT NOT NULL,
                UNIQUE(title, amount, date) ON CONFLICT IGNORE
            )
        """.trimIndent())
    }
}
