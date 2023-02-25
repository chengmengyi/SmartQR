package com.demo.smartqr.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.widget.Toast
import android.net.Uri
import android.view.View

import java.net.URLEncoder

fun View.show(show:Boolean){
    visibility=if (show) View.VISIBLE else View.GONE
}

fun Context.showToast(string:String){
    Toast.makeText(this,string,Toast.LENGTH_SHORT).show()
}

fun Context.copy(string: String){
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setText(string)
    showToast("Copy successful")
}

fun Context.shareScanResult(text:String){
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(
        Intent.EXTRA_TEXT,
        text
    )
    startActivity(Intent.createChooser(intent, "share"))
}

fun Context.searchScanResult(text:String){
    val query = URLEncoder.encode(text, "utf-8")
    val url = "http://www.google.com/search?q=$query"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun Context.fitHeight(){
    val metrics: DisplayMetrics = resources.displayMetrics
    val td = metrics.heightPixels / 760f
    val dpi = (160 * td).toInt()
    metrics.density = td
    metrics.scaledDensity = td
    metrics.densityDpi = dpi
}