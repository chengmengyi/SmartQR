package com.demo.smartqr.bean

class AdConfBean(
    val smartqr_from:String,
    val smartqr_id:String,
    val smartqr_source:String,
    val smartqr_prio:Int
) {
    override fun toString(): String {
        return "AdConfBean(source='$smartqr_from', id='$smartqr_id', type='$smartqr_source', sort=$smartqr_prio)"
    }
}