package com.redhorse.accountbank.data.helper

import android.content.Context
import android.content.SharedPreferences

class AppinfoHelper(context: Context) {
    companion object {
        private const val PREF_NAME = "AppSettings" // SharedPreferences 파일 이름
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun save(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun save(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun save(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
