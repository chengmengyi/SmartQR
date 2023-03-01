package com.demo.smartqr.util

import com.tencent.mmkv.MMKV
import java.text.SimpleDateFormat
import java.util.*

object AdLimitManager {
    private var maxShow=50
    private var maxClick=15

    private var currentShow=0
    private var currentClick=0

    private val refreshMap= hashMapOf<String,Boolean>()

    fun canRefresh(type:String)= refreshMap[type]?:true

    fun setRefreshBool(type: String,boolean: Boolean){
        refreshMap[type]=boolean
    }

    fun resetRefresh(){
        refreshMap.clear()
    }

    fun setMaxNum(maxShow:Int,maxClick:Int){
        this.maxShow=maxShow
        this.maxClick=maxClick
    }

    fun readLocalNum(){
        currentClick= MMKV.defaultMMKV().decodeInt(key("smart_click"),0)
        currentShow= MMKV.defaultMMKV().decodeInt(key("smart_show"),0)
    }

    fun clickNumAdd(){
        currentClick++
        MMKV.defaultMMKV().encode(key("smart_click"), currentClick)
    }

    fun showNumAdd(){
        currentShow++
        MMKV.defaultMMKV().encode(key("smart_show"), currentShow)
    }

    fun checkLimit()= currentShow>= maxShow|| currentClick>= maxClick

    private fun key(string:String)="${string}...${SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))}"
}