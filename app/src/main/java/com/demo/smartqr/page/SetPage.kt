package com.demo.smartqr.page

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.smartqr.R
import com.demo.smartqr.adapter.SetAdapter
import com.demo.smartqr.app.showToast
import com.demo.smartqr.base.BasePage
import com.demo.smartqr.conf.LocalConf
import kotlinx.android.synthetic.main.activity_set.*
import java.lang.Exception

class SetPage :BasePage(){
    override fun layoutId(): Int = R.layout.activity_set

    override fun view() {
        immersionBar.statusBarView(top_view).init()
        iv_back.setOnClickListener { finish() }
        rv_set.apply {
            layoutManager=LinearLayoutManager(this@SetPage)
            adapter=SetAdapter(this@SetPage){ clickItem(it) }
        }
    }

    private fun clickItem(index:Int){
        when(index){
            0->{
                try {
                    val uri = Uri.parse("mailto:${LocalConf.email}")
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    startActivity(intent)
                }catch (e: Exception){
                    showToast("Contact us by email：${LocalConf.email}")
                }
            }
            1->{
                startActivity(Intent(this,WebPage::class.java))
            }
            2->{
                val packName = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).packageName
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packName")
                }
                startActivity(intent)
            }
            3->{
                val pm = packageManager
                val packageName=pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).packageName
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=${packageName}"
                )
                startActivity(Intent.createChooser(intent, "share"))
            }
        }
    }
}