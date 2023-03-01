package com.demo.smartqr.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.demo.smartqr.page.LaunchPage
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PageRegister {
    var front=true
    var banReload=false
    private var hotReload=false
    private var job: Job?=null


    fun register(application: Application){
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks{
            private var pages=0
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                pages++
                job?.cancel()
                job=null
                if (pages==1){
                    front=true
                    if (hotReload&&!banReload){
                        activity.startActivity(Intent(activity, LaunchPage::class.java))
                    }
                    hotReload=false
                }
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                pages--
                if (pages<=0){
                    front=false
                    job= GlobalScope.launch {
                        delay(3000L)
                        hotReload=true
                        ActivityUtils.finishActivity(LaunchPage::class.java)
                        ActivityUtils.finishActivity(AdActivity::class.java)
                    }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}