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

import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class CreateQrResultPage:BasePage() {
    private var createSuccess=false

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
        iv_back.setOnClickListener { finish() }
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
                showToast("Saved successfully")
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
        val filename: String //???????????????
        //???????????????????????????
        val date = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        filename = sdf.format(date)
        val file = File(extStorageDirectory, "$filename.JPEG") //?????????????????????????????????????????????????????????????????????
        try {
            outStream = FileOutputStream(file) //???????????????
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.close()
            //       ?????????????????????????????????
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = Uri.fromFile(file)
            intent.data = uri
            sendBroadcast(intent)
            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????*/
            return uri
        } catch (e: Exception) {

        }
        return null
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    fun saveImageToGallery2(context: Context, image: Bitmap) :Uri?{
        val mImageTime = System.currentTimeMillis()
        val imageDate: String = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date(mImageTime))
        val SCREENSHOT_FILE_NAME_TEMPLATE = "smart_%s.png" //??????????????????"winetalk"+???????????????
        val mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate)
        val values = ContentValues()
        values.put(
            MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                    + File.separator + "smart"
        ) //Environment.DIRECTORY_SCREENSHOTS:??????,?????????????????????????????????"dh"
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
            // ????????????
            val hints: MutableMap<EncodeHintType, Any> = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            // ????????????
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            // ??????????????????????????????????????????
            val bitMatrix: BitMatrix = QRCodeWriter().encode(
                content, BarcodeFormat.QR_CODE, widthPix,
                heightPix, hints
            )
            val pixels = IntArray(widthPix * heightPix)
            // ????????????????????????????????????????????????????????????????????????
            // ??????for????????????????????????????????????
            for (y in 0 until heightPix) {
                for (x in 0 until widthPix) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = -0x1000000
                    } else {
                        pixels[y * widthPix + x] = -0x1
                    }
                }
            }
            // ???????????????????????????????????????ARGB_8888
            var bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
            //???????????????????????????????????????????????????????????????????????????
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
}