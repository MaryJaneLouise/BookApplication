package com.mariejuana.bookapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors

class BookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
            private set

    }
}