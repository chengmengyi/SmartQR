package com.demo.smartqr.page.scan

import com.demo.smartqr.R
import com.demo.smartqr.app.copy
import com.demo.smartqr.app.searchScanResult
import com.demo.smartqr.app.shareScanResult
import com.demo.smartqr.base.BasePage
import kotlinx.android.synthetic.main.activity_scan_result.*

class ScanResultPage:BasePage() {
    var content=""

    override fun layoutId(): Int = R.layout.activity_scan_result

    override fun view() {
        immersionBar.statusBarView(top_view).init()
        content=intent.getStringExtra("content")?:""
        tv_type.text=if (content.startsWith("http"))"URL" else "Text"
        tv_result.text=content
        iv_copy.setOnClickListener { copy(content) }

        iv_share.setOnClickListener { shareScanResult(content) }

        iv_search.setOnClickListener { searchScanResult(content) }

        iv_back.setOnClickListener { finish() }
    }
}