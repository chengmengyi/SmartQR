package com.demo.smartqr.page

import com.demo.smartqr.R
import com.demo.smartqr.base.BasePage
import com.demo.smartqr.conf.LocalConf
import kotlinx.android.synthetic.main.activity_web.*

class WebPage:BasePage() {
    override fun layoutId(): Int = R.layout.activity_web

    override fun view() {
        immersionBar.statusBarView(top_view).init()
        iv_back.setOnClickListener { finish() }

        web_view.apply {
            settings.javaScriptEnabled=true
            loadUrl(LocalConf.url)
        }
    }
}