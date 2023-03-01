package com.demo.smartqr.page

import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.config.ScanType
import cn.mtjsoft.barcodescanning.interfaces.AlbumOnClickListener
import cn.mtjsoft.barcodescanning.interfaces.CallBackFileUri
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import com.demo.smartqr.R
import com.demo.smartqr.app.showToast
import com.demo.smartqr.base.BasePage
import com.demo.smartqr.page.create_qr.CreateQrPage
import com.demo.smartqr.page.scan.ScanResultPage
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.android.synthetic.main.activity_home.*
import android.R.attr.data
import cn.mtjsoft.barcodescanning.ScanningActivity
import com.blankj.utilcode.util.ActivityUtils
import com.demo.smartqr.admob.ShowFullAd
import com.demo.smartqr.admob.ShowNativeAd
import com.demo.smartqr.app.PageRegister
import com.demo.smartqr.conf.LocalConf
import com.demo.smartqr.util.AdLimitManager


class HomePage :BasePage() {
    private val showFullAd by lazy { ShowFullAd(this,LocalConf.CLICK_FUNC) }

    private val showNativeAd by lazy { ShowNativeAd(this,LocalConf.HOME) }

    override fun layoutId(): Int = R.layout.activity_home

    override fun view() {
        immersionBar.statusBarView(top_view).statusBarDarkFont(true).init()
        llc_scan.setOnClickListener {
            showFullAd.show(
                emptyBack = true,
                showing = {},
                close = {
                    toScan()
                }
            )
        }
        llc_create.setOnClickListener {
            showFullAd.show(
                emptyBack = true,
                showing = {},
                close = {
                    startActivity(Intent(this,CreateQrPage::class.java))
                }
            )
        }
        iv_set.setOnClickListener { startActivity(Intent(this,SetPage::class.java)) }
    }

    private fun toScan(){
        XXPermissions.with(this)
            .permission(Permission.CAMERA,Permission.READ_EXTERNAL_STORAGE,Permission.WRITE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    ScanningManager.instance.openScanningActivity(
                        this@HomePage,
                        Config(
                            true,
                            ScanType.QR_CODE,
                            object : AlbumOnClickListener {
                                override fun onClick(choose:Boolean) {
                                    PageRegister.banReload=choose
                                }
                            },
                            object : ScanResultListener {
                                override fun onSuccessListener(value: String?) {
                                    if(value?.isNotEmpty() == true){
                                        startActivity(Intent(this@HomePage,ScanResultPage::class.java).apply {
                                            putExtra("content",value)
                                        })
                                    }
                                }

                                override fun onFailureListener(error: String) {
                                    showToast("Scan failed")
                                }

                                override fun onCompleteListener(value: String?) {
                                }
                            })
                    )
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    showToast("Please agree with the authority")
                    if (doNotAskAgain) {
                        XXPermissions.startPermissionActivity(this@HomePage, permissions)
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if(AdLimitManager.canRefresh(LocalConf.HOME)){
            showNativeAd.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showNativeAd.endShow()
        AdLimitManager.setRefreshBool(LocalConf.HOME,true)
    }
}