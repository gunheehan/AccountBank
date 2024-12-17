package com.redhorse.accountbank.data.helper

import android.content.Context
import android.content.SharedPreferences

class AppinfoHelper(context: Context) {
    companion object {
        private const val PREF_NAME = "AppSettings" // SharedPreferences 파일 이름
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // 데이터 저장
    fun save(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun save(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun save(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    // 데이터 가져오기
    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    // 키 존재 여부 확인
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    // 특정 키 데이터 삭제
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // 모든 데이터 삭제
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
