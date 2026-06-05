package com.procool.remotecontrol

import android.app.Application
import com.procool.remotecontrol.data.storage.MmkvStorage

class ProApp : Application() {

    companion object {
        lateinit var instance: ProApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MmkvStorage.init(this)
    }
}
