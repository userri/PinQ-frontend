package com.example.pinq_frontend

import android.app.Application
import com.example.pinq_frontend.data.local.LocalModule

class PinQApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalModule.init(this)
    }
}
