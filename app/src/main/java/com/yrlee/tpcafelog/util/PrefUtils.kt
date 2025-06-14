package com.yrlee.tpcafelog.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.yrlee.tpcafelog.R

object PrefUtils {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String){
        sharedPreferences.edit(commit = true) { putString(key, value) }
    }

    fun getString(key: String): String{
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun putInt(key: String, value: Int){
        sharedPreferences.edit(commit = true) { putInt(key, value) }
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, -1)
    }

    fun putBoolean(key: String, value: Boolean){
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean{
        return sharedPreferences.getBoolean(key, false)
    }

    fun clearSharedPrefs(){
        sharedPreferences.edit().clear().apply()
    }
}