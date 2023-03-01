package com.demo.smartqr.app

import android.app.Application
import com.demo.smartqr.conf.OnlineConf
import com.tencent.mmkv.MMKV

lateinit var smartApp: SmartApp
class SmartApp:Application() {
    override fun onCreate() {
        super.onCreate()
        smartApp=this
        MMKV.initialize(this)
        PageRegister.register(this)
        OnlineConf.getOnlineConf()
    }
}