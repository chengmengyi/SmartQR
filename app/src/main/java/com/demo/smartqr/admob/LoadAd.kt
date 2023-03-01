package com.demo.smartqr.admob

import com.demo.smartqr.app.log
import com.demo.smartqr.app.smartApp
import com.demo.smartqr.bean.AdConfBean
import com.demo.smartqr.bean.AdResultBean
import com.demo.smartqr.util.AdLimitManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions

abstract class LoadAd {

    protected fun startLoadAd(type: String, adConfBean: AdConfBean, result: (bean: AdResultBean?) -> Unit) {
        "start load $type ad,${adConfBean.toString()}".log()
        when (adConfBean.smartqr_source) {
            "open" -> loadOp(type, adConfBean, result)
            "inter" -> loadInter(type, adConfBean, result)
            "native" -> loadNa(type, adConfBean, result)
        }
    }

    private fun loadOp(
        type: String,
        adConfBean: AdConfBean,
        result: (adResultBean: AdResultBean?) -> Unit
    ) {
        AppOpenAd.load(
            smartApp,
            adConfBean.smartqr_id,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(p0: AppOpenAd) {
                    "load $type success".log()
                    result.invoke(AdResultBean(loadTime = System.currentTimeMillis(), loadAd = p0))
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    "load $type fail,${p0.message}".log()
                    result.invoke(null)
                }
            }
        )
    }

    private fun loadInter(
        type: String,
        adConfBean: AdConfBean,
        result: (adResultBean: AdResultBean?) -> Unit
    ) {
        InterstitialAd.load(
            smartApp,
            adConfBean.smartqr_id,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    "load $type fail,${p0.message}".log()
                    result.invoke(null)
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    "load $type success".log()
                    result.invoke(AdResultBean(loadTime = System.currentTimeMillis(), loadAd = p0))
                }
            }
        )
    }

    private fun loadNa(
        type: String,
        adConfBean: AdConfBean,
        result: (adResultBean: AdResultBean?) -> Unit
    ) {
        AdLoader.Builder(
            smartApp,
            adConfBean.smartqr_id,
        ).forNativeAd {
            "load $type success".log()
            result.invoke(AdResultBean(loadTime = System.currentTimeMillis(), loadAd = it))

        }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    "load $type fail,${p0.message}".log()
                    result.invoke(null)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdLimitManager.clickNumAdd()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(
                        NativeAdOptions.ADCHOICES_TOP_LEFT
                    )
                    .build()
            )
            .build()
            .loadAd(AdRequest.Builder().build())
    }
}