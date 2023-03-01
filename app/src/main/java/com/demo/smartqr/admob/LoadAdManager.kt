package com.demo.smartqr.admob

import com.demo.smartqr.app.log
import com.demo.smartqr.bean.AdConfBean
import com.demo.smartqr.bean.AdResultBean
import com.demo.smartqr.conf.LocalConf
import com.demo.smartqr.conf.OnlineConf
import com.demo.smartqr.util.AdLimitManager
import org.json.JSONObject

object LoadAdManager:LoadAd() {
    var showingFullAd=false
    private val loadingList= arrayListOf<String>()
    private val loadResultMap= hashMapOf<String,AdResultBean>()

    fun loadAd(type:String,retry:Int=0){
        if(AdLimitManager.checkLimit()){
            "limit".log()
            return
        }

        if (loadingList.contains(type)){
            "$type is loading".log()
            return
        }

        if(loadResultMap.containsKey(type)){
            val resultAdBean = loadResultMap[type]
            if(resultAdBean?.loadAd!=null){
                if(resultAdBean.expired()){
                    removeAd(type)
                }else{
                    "$type has cache".log()
                    return
                }
            }
        }

        val parseAdList = parseAdList(type)
        if(parseAdList.isEmpty()){
            "$type ad list empty"
            return
        }
        loadingList.add(type)
        loopLoadAd(type,parseAdList.iterator(),retry)

    }

    private fun loopLoadAd(type: String, iterator: Iterator<AdConfBean>, retry:Int){
        startLoadAd(type,iterator.next()){
            if(null!=it){
                loadingList.remove(type)
                loadResultMap[type]=it
            }else{
                if(iterator.hasNext()){
                    loopLoadAd(type,iterator,retry)
                }else{
                    loadingList.remove(type)
                    if(retry>0&&type==LocalConf.OPEN){
                        loadAd(type,retry=0)
                    }
                }
            }
        }
    }

    private fun parseAdList(key:String):List<AdConfBean>{
        val list= arrayListOf<AdConfBean>()
        try {
            val jsonArray = JSONObject(OnlineConf.getAdJson()).getJSONArray(key)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                list.add(
                    AdConfBean(
                        jsonObject.optString("smartqr_from"),
                        jsonObject.optString("smartqr_id"),
                        jsonObject.optString("smartqr_source"),
                        jsonObject.optInt("smartqr_prio"),
                    )
                )
            }
        }catch (e:Exception){
        }
        return list.filter { it.smartqr_from == "admob" }.sortedByDescending { it.smartqr_prio }
    }

    fun removeAd(type: String){
        loadResultMap.remove(type)
    }

    fun getAd(type: String)= loadResultMap[type]?.loadAd

    fun preLoadAllAd(){
        loadAd(LocalConf.OPEN, retry = 1)
        loadAd(LocalConf.HOME)
        loadAd(LocalConf.SCAN_RESULT)
        loadAd(LocalConf.CREATE_RESULT)
        loadAd(LocalConf.CLICK_FUNC)
    }
}