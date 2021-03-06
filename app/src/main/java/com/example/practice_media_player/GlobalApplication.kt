package com.example.practice_media_player

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.addLogAdapter(object : AndroidLogAdapter(){
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}