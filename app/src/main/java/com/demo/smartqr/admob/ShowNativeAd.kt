package com.demo.smartqr.admob

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.blankj.utilcode.util.SizeUtils
import com.demo.smartqr.R
import com.demo.smartqr.app.log
import com.demo.smartqr.app.show
import com.demo.smartqr.base.BasePage
import com.demo.smartqr.util.AdLimitManager
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.*

class ShowNativeAd(
    private val basePage: BasePage,
    private val type:String,
) {
    private var loop=true
    private var last:NativeAd?=null
    private var showJob:Job?=null

    fun show(){
        LoadAdManager.loadAd(type)
        endShow()
        loop=true
        showJob= GlobalScope.launch(Dispatchers.Main) {
            delay(300L)
            if (!basePage.resume){
                return@launch
            }
            while (loop) {
                if (!isActive) {
                    break
                }

                val ad = LoadAdManager.getAd(type)
                if(basePage.resume && null!=ad && ad is NativeAd){
                    cancel()
                    last?.destroy()
                    last=ad
                    loop=false
                    show(ad)
                }

                delay(1000L)
            }
        }
    }

    private fun show(ad:NativeAd){
        "start show $type ad".log()
        val viewNative = basePage.findViewById<NativeAdView>(R.id.native_view)
        viewNative.iconView=basePage.findViewById(R.id.native_logo)
        (viewNative.iconView as ImageFilterView).setImageDrawable(ad.icon?.drawable)

        viewNative.callToActionView=basePage.findViewById(R.id.native_install)
        (viewNative.callToActionView as AppCompatTextView).text=ad.callToAction

        viewNative.mediaView=basePage.findViewById(R.id.native_media)
        ad.mediaContent?.let {
            viewNative.mediaView?.apply {
                setMediaContent(it)
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        if (view == null || outline == null) return
                        outline.setRoundRect(
                            0,
                            0,
                            view.width,
                            view.height,
                            SizeUtils.dp2px(8F).toFloat()
                        )
                        view.clipToOutline = true
                    }
                }
            }
        }

        viewNative.bodyView=basePage.findViewById(R.id.native_desc)
        (viewNative.bodyView as AppCompatTextView).text=ad.body


        viewNative.headlineView=basePage.findViewById(R.id.native_title)
        (viewNative.headlineView as AppCompatTextView).text=ad.headline

        viewNative.setNativeAd(ad)
        basePage.findViewById<AppCompatImageView>(R.id.iv_cover).show(false)

        AdLimitManager.showNumAdd()
        LoadAdManager.removeAd(type)
        LoadAdManager.loadAd(type)
        AdLimitManager.setRefreshBool(type,false)
    }

    fun endShow(){
        loop=false
        showJob?.cancel()
        showJob=null
    }
}