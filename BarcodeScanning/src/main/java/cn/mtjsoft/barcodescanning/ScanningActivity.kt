package cn.mtjsoft.barcodescanning

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.annotation.SuppressLint
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import cn.mtjsoft.barcodescanning.adapter.ScanTypeAdapter
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.extentions.screenHeight
import cn.mtjsoft.barcodescanning.extentions.screenWidth
import cn.mtjsoft.barcodescanning.interfaces.CallBackFileUri
import cn.mtjsoft.barcodescanning.interfaces.CustomTouchListener
import cn.mtjsoft.barcodescanning.transformer.ScanTypePageTransformer
import cn.mtjsoft.barcodescanning.utils.ScanUtil
import cn.mtjsoft.barcodescanning.utils.ScanUtil.toBitmap
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import cn.mtjsoft.barcodescanning.view.CustomGestureDetectorView
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.gyf.immersionbar.ImmersionBar
import java.io.IOException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author mtj
 * @date 2022/1/19
 * @desc
 * @email mtjsoft3@gmail.com
 */
class ScanningActivity : AppCompatActivity() {

    private val TAG: String = "ScanningActivity"

    private val threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        1, 20, 3, TimeUnit.SECONDS,
        SynchronousQueue<Runnable>()
    )
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private var mCameraControl: CameraControl? = null
    private var mCameraInfo: CameraInfo? = null

    // ???????????????/??????
    private var torchState = TorchState.OFF

    // ????????????
    private var mZoomState: ZoomState? = null

    // ??????????????????
    private val zoomStep = 0.1f

    // ??????????????????
    @Volatile
    private var scanEnable = true

    // ????????????
    private var scanType = 0

    private var config: Config = Config()

    private lateinit var closeImageView: ImageView
    private lateinit var tipView: TextView
    private lateinit var fileScanTipView: TextView
    private lateinit var lineImageView: ImageView
    private lateinit var ivFlashImageView: ImageView
    private lateinit var ivAlbumImageView: ImageView
    private lateinit var mCustomGestureDetectorView: CustomGestureDetectorView
    private lateinit var mViewPager: ViewPager

    private var widthPixels = 0
    private var heightPixels = 0
    private val animationSet = AnimatorSet()

    companion object {
        val mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
        ImmersionBar.with(this).transparentStatusBar().init()
        initView()
        initData()
        preview()
    }

    private fun initView() {
        previewView = findViewById(R.id.previewView)
        closeImageView = findViewById(R.id.iv_close)
        tipView = findViewById(R.id.tv_tip)
        fileScanTipView = findViewById(R.id.tv_file_scane_tip)
        lineImageView = findViewById(R.id.iv_scan_line)
        mCustomGestureDetectorView = findViewById(R.id.gestureDetectorView)
        mViewPager = findViewById(R.id.viewPager)
        ivFlashImageView = findViewById(R.id.iv_flash)
        ivAlbumImageView = findViewById(R.id.iv_album)
    }

    private fun initData() {
        config = ScanningManager.instance.getConfig()
        widthPixels = this.screenWidth
        heightPixels = this.screenHeight
        SoundPoolUtil.instance.loadQrcodeCompletedWav(this)
        mCustomGestureDetectorView.setCustomTouchListener(customTouchListener)
        ivFlashImageView.setOnClickListener {
            enableTorch(torchState == TorchState.OFF)
        }

        ivAlbumImageView.visibility =
            if (ScanningManager.instance.getConfig().albumOnClickListener == null) View.GONE else View.VISIBLE
        ivAlbumImageView.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            startActivityForResult(intent, 2)
        }
        fileScanTipView.setOnClickListener {
            notFindCodeAndGoOn()
        }
        closeImageView.setOnClickListener {
            finish()
        }
        val types = resources.getStringArray(R.array.scan_type)
        mViewPager.adapter = ScanTypeAdapter(this, types)
        mViewPager.offscreenPageLimit = types.size
        mViewPager.setPageTransformer(true, ScanTypePageTransformer())
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                // ????????????
                scanType = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        mViewPager.currentItem = config.scanType
        scanType = config.scanType
        findViewById<FrameLayout>(R.id.layoutBottom).setOnTouchListener { view, motionEvent ->
            mViewPager.onTouchEvent(motionEvent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (data != null) {
                data.data?.let {
                    scanEnable = false
                    stopScanLineAnimator()
                    // ???????????????????????????
                    cameraProviderFuture.get().unbindAll();
                    // ????????????
                    scanningFile(it)
                }
            }
        }

    }

    /**
     * ?????? CameraProvider
     * ?????? CameraProvider ?????????
     */
    private fun preview() {
        // ?????? CameraProvider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // ?????? CameraProvider ?????????
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * ??????????????????????????????????????????
     */
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(threadPoolExecutor) { imageProxy ->
            try {
                if (scanEnable) {
                    scanning(imageProxy)
                } else {
                    imageProxy.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageProxy.close()
            }
        }
        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
        mCameraControl = camera.cameraControl
        mCameraInfo = camera.cameraInfo
        mZoomState = mCameraInfo?.zoomState?.value
        mCameraInfo?.torchState?.observe(this) {
            if (it == TorchState.OFF) {
                ivFlashImageView.setImageResource(R.drawable.icon_off)
            } else {
                ivFlashImageView.setImageResource(R.drawable.icon_on)
                ivFlashImageView.visibility = View.VISIBLE
            }
            torchState = it
        }
        mCameraInfo?.zoomState?.observe(this) {
            mZoomState = it
        }
        startScanLineAnimator()
    }

    /**
     * ??????????????????
     */
    @SuppressLint("Recycle")
    private fun startScanLineAnimator() {
        animationSet.cancel()
        lineImageView.visibility = View.VISIBLE
        val alphaAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(lineImageView, "alpha", 0.2f, 1f, 1f, 0f)
        alphaAnimator.repeatCount = INFINITE
        val translationYAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(lineImageView, "translationY", 0f, heightPixels / 3f * 2)
        translationYAnimator.repeatCount = INFINITE
        animationSet.playTogether(alphaAnimator, translationYAnimator)
        animationSet.duration = 3000
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.start()
    }

    /**
     * ????????????
     */
    private fun stopScanLineAnimator() {
        animationSet.cancel()
        lineImageView.visibility = View.GONE
    }

    /**
     * ??????????????????
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun scanning(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        lowLight(mediaImage)
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        ScanningManager.instance.getBarcodeScanningClient(scanType).process(image)
            .addOnSuccessListener { barcodes ->
                when {
                    barcodes.size == 1 -> {
                        SoundPoolUtil.instance.playQrcodeCompleted()
                        mCustomGestureDetectorView.addScanView(image, barcodes) {
                        }
                        resultOk(barcodes[0], 1000)
                    }
                    barcodes.size > 1 -> {
                        SoundPoolUtil.instance.playQrcodeCompleted()
                        // ?????????????????????
                        if (config.enabled) {
                            mCustomGestureDetectorView.addScanView(image, barcodes) {
                                resultOk(it)
                            }
                        } else {
                            // ??????????????????
                            ScanningManager.instance.getRectMaxResult(barcodes)?.let {
                                mCustomGestureDetectorView.addScanView(
                                    image,
                                    arrayListOf(it)
                                ) {
                                }
                                resultOk(it, 1000)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
            }
            .addOnCompleteListener {
                // ?????????????????????????????????????????????
                if (it.isSuccessful && it.result.isNotEmpty()) {
                    scanEnable = false
                    stopScanLineAnimator()
                    // ???????????????????????????
                    cameraProviderFuture.get().unbindAll()
                }
                mediaImage.close()
                imageProxy.close()
            }
    }

    /**
     * ????????????
     */
    private fun scanningFile(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(baseContext, uri)
            ScanningManager.instance.getBarcodeScanningClient(scanType).process(image)
                .addOnSuccessListener { barcodes ->
                    when {
                        barcodes.size == 1 -> {
                            SoundPoolUtil.instance.playQrcodeCompleted()
                            resultOk(barcodes[0], 0)
                        }
                        barcodes.size > 1 -> {
                            SoundPoolUtil.instance.playQrcodeCompleted()
                            // ?????????????????????
                            // ??????????????????
                            ScanningManager.instance.getRectMaxResult(barcodes)?.let {
                                resultOk(it, 0)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // ????????????
                    it.printStackTrace()
                }
                .addOnCompleteListener {
                    // ????????????
                    if (!it.isSuccessful || it.result.isEmpty()) {
                        fileScanTipView.visibility = View.VISIBLE
                        fileScanTipView.text = getString(R.string.file_scan_code_nodata)
                    }
                }
        } catch (e: IOException) {
            e.printStackTrace()
            fileScanTipView.visibility = View.VISIBLE
            fileScanTipView.text = getString(R.string.file_scan_code_nofile)
        }
    }

    private fun notFindCodeAndGoOn() {
        mCustomGestureDetectorView.removeAllViews()
        scanEnable = true
        // ????????????
        bindPreview(cameraProviderFuture.get())
        startScanLineAnimator()
        fileScanTipView.visibility = View.GONE
    }

    /**
     * ???????????????????????????
     */
    private fun resultOk(barcode: Barcode?, timeMillis: Long = 0) {
        barcode?.let {
            Log.e(TAG, "format: ${barcode.format}")
            mainHandler.postDelayed({
                config.scanResultListener?.apply {
                    onSuccessListener(it.rawValue)
                    onCompleteListener(it.rawValue)
                }
                finish()
            }, timeMillis)
        }
    }

    /**
     * ??????????????????
     */
    private fun lowLight(mediaImage: Image) {
        if (torchState == TorchState.OFF) {
            val bitmap = mediaImage.toBitmap()
            val isLowLight = bitmap?.run {
                ScanUtil.isLowLight(this)
            }
            if (isLowLight == true) {
                // ????????????????????????????????????
                mainHandler.post {
                    tipView.text = getString(R.string.low_light_tip)
                    ivFlashImageView.visibility = View.VISIBLE
                }
            } else {
                // ???????????????????????????????????????
                mainHandler.post {
                    tipView.text = getString(R.string.qr_bar_code)
                    ivFlashImageView.visibility = View.GONE
                }
            }
        } else {
            // ?????????????????????????????????
            mainHandler.post {
                tipView.text = getString(R.string.qr_bar_code)
                ivFlashImageView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * ???????????? | ???????????? | ????????????
     */
    private val customTouchListener: CustomTouchListener = object : CustomTouchListener {
        override fun zoom() {
            // ??????????????????
            mZoomState?.apply {
                if (zoomRatio < maxZoomRatio) {
                    val curZoomRatio = zoomRatio + zoomStep
                    if (curZoomRatio <= maxZoomRatio) {
                        mCameraControl?.setZoomRatio(curZoomRatio)
                    } else {
                        mCameraControl?.setZoomRatio(maxZoomRatio)
                    }
                }
            }
        }

        override fun ZoomOut() {
            // ??????????????????
            mZoomState?.apply {
                if (zoomRatio > minZoomRatio) {
                    val curZoomRatio = zoomRatio - zoomStep
                    if (curZoomRatio >= minZoomRatio) {
                        mCameraControl?.setZoomRatio(curZoomRatio)
                    } else {
                        mCameraControl?.setZoomRatio(minZoomRatio)
                    }
                }
            }
        }

        override fun click(x: Float, y: Float) {
            // ????????????????????????
            clickFocus(x, y)
        }

        override fun doubleClick(x: Float, y: Float) {
            // ????????????????????????
            mZoomState?.apply {
                if (linearZoom > 0) {
                    mCameraControl?.setLinearZoom(0f)
                } else {
                    mCameraControl?.setLinearZoom(0.5f)
                }
            }
        }

        override fun longClick(x: Float, y: Float) {
        }
    }

    /**
     * ????????????
     */
    private fun clickFocus(x: Float, y: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(
            previewView.width.toFloat(),
            previewView.height.toFloat()
        )
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        mCameraControl?.startFocusAndMetering(action)?.addListener({
            // ????????????
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * ???????????????/????????????
     */
    private fun enableTorch(open: Boolean) {
        when {
            open && torchState == TorchState.OFF -> {
                mCameraControl?.enableTorch(true)
            }
            !open && torchState == TorchState.ON -> {
                mCameraControl?.enableTorch(false)
            }
        }
    }

    override fun onDestroy() {
        stopScanLineAnimator()
        enableTorch(false)
        super.onDestroy()
    }
}