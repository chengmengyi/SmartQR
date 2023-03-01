package com.demo.smartqr.page


import android.animation.ValueAnimator
import android.content.Intent
import android.view.KeyEvent
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.ActivityUtils
import com.demo.smartqr.R
import com.demo.smartqr.admob.LoadAdManager
import com.demo.smartqr.admob.ShowFullAd
import com.demo.smartqr.base.BasePage
import com.demo.smartqr.conf.LocalConf
import com.demo.smartqr.util.AdLimitManager
import kotlinx.android.synthetic.main.activity_main.*

class LaunchPage : BasePage() {
    private var animator: ValueAnimator?=null
    private val showFullAd by lazy { ShowFullAd(this,LocalConf.OPEN) }

    override fun layoutId(): Int = R.layout.activity_main

    override fun view() {
        AdLimitManager.readLocalNum()
        AdLimitManager.resetRefresh()
        LoadAdManager.preLoadAllAd()
        startAnimator()
    }

    private fun startAnimator(){
        animator=ValueAnimator.ofInt(0, 100).apply {
            duration = 10000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Int
                progress_view.progress = progress
                val pro = (10 * (progress / 100.0F)).toInt()
                if(pro in 2..9){
                    showFullAd.show(
                        showing = {
                            stopAnimator()
                            progress_view.progress = 100
                        },
                        close = {
                            toHome()
                        }
                    )
                }else if (pro>=10){
                    toHome()
                }
            }
            start()
        }
    }

    private fun toHome(){
        if(!ActivityUtils.isActivityExistsInStack(HomePage::class.java)){
            startActivity(Intent(this,HomePage::class.java))
        }
        finish()
    }

    private fun stopAnimator(){
        animator?.removeAllUpdateListeners()
        animator?.cancel()
        animator=null
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode== KeyEvent.KEYCODE_BACK){
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        animator?.resume()
    }

    override fun onPause() {
        super.onPause()
        animator?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimator()
    }
}