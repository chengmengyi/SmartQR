package com.demo.smartqr.admob

import com.demo.smartqr.app.log
import com.demo.smartqr.base.BasePage
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd

class ShowFullAd(
    private val basePage: BasePage,
    private val type:String,
) {

    fun show(emptyBack:Boolean=false,showing:()->Unit,close:()->Unit){
        val adResult = LoadAdManager.getAd(type)
        if (null!=adResult){
            if (LoadAdManager.showingFullAd||!basePage.resume){
                return
            }
            "start show $type ad".log()
            showing.invoke()
            when(adResult){
                is InterstitialAd ->{
                    adResult.fullScreenContentCallback= FullAdCallback(basePage, type, close)
                    adResult.show(basePage)
                }
                is AppOpenAd ->{
                    adResult.fullScreenContentCallback= FullAdCallback(basePage, type, close)
                    adResult.show(basePage)
                }
            }
        }else{
            if (emptyBack){
                LoadAdManager.loadAd(type)
                close.invoke()
            }
        }
    }
}