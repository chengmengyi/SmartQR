package com.demo.smartqr.admob


import com.demo.smartqr.base.BasePage
import com.demo.smartqr.conf.LocalConf
import com.demo.smartqr.util.AdLimitManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FullAdCallback(
    private val basePage: BasePage,
    private val type:String,
    private val close:()->Unit
): FullScreenContentCallback()  {

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        LoadAdManager.showingFullAd =false
        onClose()
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        LoadAdManager.showingFullAd =true
        AdLimitManager.showNumAdd()
        LoadAdManager.removeAd(type)
    }

    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
        super.onAdFailedToShowFullScreenContent(p0)
        LoadAdManager.showingFullAd =false
        LoadAdManager.removeAd(type)
        onClose()
    }


    override fun onAdClicked() {
        super.onAdClicked()
        AdLimitManager.clickNumAdd()
    }

    private fun onClose(){
        if (type!= LocalConf.OPEN){
            LoadAdManager.loadAd(type)
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(200L)
            if (basePage.resume){
                close.invoke()
            }
        }
    }
}