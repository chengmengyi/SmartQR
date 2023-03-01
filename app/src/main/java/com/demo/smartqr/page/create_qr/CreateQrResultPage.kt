package com.demo.smartqr.page.create_qr

import android.content.Intent
import com.demo.smartqr.base.BasePage
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.blankj.utilcode.util.SizeUtils
import com.demo.smartqr.app.showToast

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.activity_create_qr_result.*
import java.io.*

import java.io.IOException

import java.io.OutputStream


import android.text.format.DateUtils

import java.io.File

import android.os.Environment

import android.content.ContentValues

import android.content.Context

import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import com.demo.smartqr.admob.ShowNativeAd
import com.demo.smartqr.conf.LocalConf
import com.demo.smartqr.page.HomePage
import com.demo.smartqr.util.AdLimitManager

import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class CreateQrResultPage:BasePage() {
    private var createSuccess=false
    private val showNativeAd by lazy { ShowNativeAd(this,LocalConf.CREATE_RESULT) }


    override fun layoutId(): Int= com.demo.smartqr.R.layout.activity_create_qr_result

    override fun view() {
        immersionBar.statusBarView(top_view).init()
        val content = intent.getStringExtra("content") ?: ""
        val bit = createQRCode(content, SizeUtils.dp2px(130F), SizeUtils.dp2px(130F))
        if (null!=bit){
            createSuccess=true
            iv_qr_code.setImageBitmap(bit)
        }else{
            showToast("Failed to generate QR code")
        }
        llc_save.setOnClickListener { saveOrShare(true) }
        llc_share.setOnClickListener { saveOrShare(false) }
        iv_back.setOnClickListener { onBackPressed() }
    }

    private fun saveOrShare(save:Boolean){
        if(!createSuccess){
            return
        }
        iv_qr_code.setDrawingCacheEnabled(true)
        iv_qr_code.buildDrawingCache()
        val bitmap: Bitmap = iv_qr_code.getDrawingCache()
        val uri = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            saveImageToGallery2(this, bitmap)
        }else{
            saveBitmap(bitmap)

        }
        if(save){
            if(null!=uri){
                showToast("Save to Album")
            }else{
                showToast("Save failed")
            }
        }else{
            uri?.let {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, it)
                startActivity(Intent.createChooser(intent, "share qrcode"))
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap):Uri? {
        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        var outStream: OutputStream? = null
        val filename: String //声明文件名
        //以保存时间为文件名
        val date = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        filename = sdf.format(date)
        val file = File(extStorageDirectory, "$filename.JPEG") //创建文件，第一个参数为路径，第二个参数为文件名
        try {
            outStream = FileOutputStream(file) //创建输入流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.close()
            //       这三行可以实现相册更新
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = Uri.fromFile(file)
            intent.data = uri
            sendBroadcast(intent)
            //这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！*/
            return uri
        } catch (e: Exception) {

        }
        return null
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    fun saveImageToGallery2(context: Context, image: Bitmap) :Uri?{
        val mImageTime = System.currentTimeMillis()
        val imageDate: String = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date(mImageTime))
        val SCREENSHOT_FILE_NAME_TEMPLATE = "smart_%s.png" //图片名称，以"winetalk"+时间戳命名
        val mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate)
        val values = ContentValues()
        values.put(
            MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                    + File.separator + "smart"
        ) //Environment.DIRECTORY_SCREENSHOTS:截图,图库中显示的文件夹名。"dh"
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000)
        values.put(
            MediaStore.MediaColumns.DATE_EXPIRES,
            (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000
        )
        values.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            // First, write the actual data for our screenshot
            resolver.openOutputStream(uri!!).use { out ->
                if (!image.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IOException("Failed to compress")
                }
            }
            // Everything went well above, publish it!
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            values.putNull(MediaStore.MediaColumns.DATE_EXPIRES)
            resolver.update(uri, values, null, null)
            return uri
        } catch (e: IOException) {
            resolver.delete(uri!!, null)
        }
        return null
    }


    private fun createQRCode(content: String?, widthPix: Int, heightPix: Int): Bitmap? {
        try {
            if (content == null || "" == content) {
                return null
            }
            // 配置参数
            val hints: MutableMap<EncodeHintType, Any> = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            // 容错级别
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            // 图像数据转换，使用了矩阵转换
            val bitMatrix: BitMatrix = QRCodeWriter().encode(
                content, BarcodeFormat.QR_CODE, widthPix,
                heightPix, hints
            )
            val pixels = IntArray(widthPix * heightPix)
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (y in 0 until heightPix) {
                for (x in 0 until widthPix) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = -0x1000000
                    } else {
                        pixels[y * widthPix + x] = -0x1
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            var bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
            //由于生成的二维码太大，太耗内存，所以要进行一些压缩
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
            val bytes = bos.toByteArray()
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        if(AdLimitManager.canRefresh(LocalConf.CREATE_RESULT)){
            showNativeAd.show()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this,HomePage::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        showNativeAd.endShow()
        AdLimitManager.setRefreshBool(LocalConf.CREATE_RESULT,true)
    }
}