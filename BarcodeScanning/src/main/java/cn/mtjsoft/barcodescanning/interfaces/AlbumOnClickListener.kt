package cn.mtjsoft.barcodescanning.interfaces

import android.net.Uri
import android.view.View
import java.io.Serializable

interface AlbumOnClickListener : Serializable {
    fun onClick(choose:Boolean)
}

interface CallBackFileUri : Serializable {
    fun callBackUri(uri: Uri)
}