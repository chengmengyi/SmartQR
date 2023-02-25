package com.demo.smartqr.page.create_qr

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import com.demo.smartqr.R
import com.demo.smartqr.app.show
import com.demo.smartqr.base.BasePage
import kotlinx.android.synthetic.main.activity_create_qr.*

class CreateQrPage:BasePage() {
    override fun layoutId(): Int = R.layout.activity_create_qr

    override fun view() {
        immersionBar.statusBarView(top_view).init()

        edit_content.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                tv_create.show(p0.toString().isNotEmpty())
            }

            override fun afterTextChanged(p0: Editable) {

            }
        })
        tv_create.setOnClickListener {
            val trim = edit_content.text.toString().trim()
            if (trim.isNotEmpty()){
                startActivity(Intent(this,CreateQrResultPage::class.java).apply {
                    putExtra("content",trim)
                })
            }
        }

        iv_back.setOnClickListener { finish() }
    }
}