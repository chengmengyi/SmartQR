package com.demo.smartqr.conf

import com.demo.smartqr.util.AdLimitManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tencent.mmkv.MMKV
import org.json.JSONObject

object OnlineConf {

    fun getOnlineConf(){
//        val remoteConfig = Firebase.remoteConfig
//        remoteConfig.fetchAndActivate().addOnCompleteListener {
//            if (it.isSuccessful){
//                parseAdJson(remoteConfig.getString("smartqr_ad"))
//            }
//        }
    }

    private fun parseAdJson(json:String){
        try{
            MMKV.defaultMMKV().encode("smartqr_ad",json)
            val jsonObject = JSONObject(json)
            AdLimitManager.setMaxNum(jsonObject.optInt("show_num"),jsonObject.optInt("click_limit"))
        }catch (e:Exception){

        }
    }

    fun getAdJson():String{
        val smartqr_ad = MMKV.defaultMMKV().decodeString("smartqr_ad") ?: ""
        if(smartqr_ad.isEmpty()){
            return LocalConf.localAd
        }
        return smartqr_ad
    }
}