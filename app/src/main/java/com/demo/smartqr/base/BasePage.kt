package com.demo.smartqr.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.demo.smartqr.app.fitHeight
import com.gyf.immersionbar.ImmersionBar

abstract class BasePage: AppCompatActivity() {
    var resume=false
    protected lateinit var immersionBar: ImmersionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fitHeight()
        setContentView(layoutId())
        immersionBar= ImmersionBar.with(this).apply {
            statusBarAlpha(0f)
            autoDarkModeEnable(true)
            statusBarDarkFont(true)
            init()
        }
        view()
    }

    abstract fun layoutId():Int

    abstract fun view()

    override fun onResume() {
        super.onResume()
        resume=true
    }

    override fun onPause() {
        super.onPause()
        resume=false
    }

    override fun onStop() {
        super.onStop()
        resume=false
    }
}