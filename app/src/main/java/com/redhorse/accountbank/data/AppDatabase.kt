package com.redhorse.accountbank.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.redhorse.accountbank.data.entity.MonthTable
import com.redhorse.accountbank.data.entity.YearTable
import com.redhorse.accountbank.data.dao.StaticTableDao
import com.redhorse.accountbank.data.dao.DynamicTableDao
import com.redhorse.accountbank.data.entity.YearMonth

@Database(entities = [Payment::class, YearMonth::class, YearTable::class, MonthTable::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun staticTableDao(): StaticTableDao
    abstract fun dynamicTableDao(): DynamicTableDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
