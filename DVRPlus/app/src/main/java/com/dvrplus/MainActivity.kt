package com.dvrplus

import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import com.jiangdongguo.ausbc.base.CameraActivity
import com.jiangdongguo.ausbc.callback.ICameraStateCallBack
import com.jiangdongguo.ausbc.callback.ICaptureCallBack
import com.jiangdongguo.ausbc.MultiCameraClient
import com.jiangdongguo.ausbc.camera.CameraUVC
import com.jiangdongguo.ausbc.camera.bean.CameraRequest
import com.jiangdongguo.ausbc.widget.AspectRatioTextureView
import com.jiangdongguo.ausbc.widget.IAspectRatio
import java.io.File

class MainActivity : CameraActivity() {

    private lateinit var root: View
    private lateinit var textureView: AspectRatioTextureView
    private val prefs by lazy { getSharedPreferences("dvrplus", MODE_PRIVATE) }
    private var recording = false

    // Tên key lưu cài đặt
    private val keys = listOf("brightness", "contrast", "saturation", "gamma", "gain")

    override fun getRootView(layoutInflater: LayoutInflater): View {
        root = layoutInflater.inflate(R.layout.activity_main, null)
        return root
    }

    override fun getCameraView(): IAspectRatio {
        textureView = AspectRatioTextureView(this)
        return textureView
    }

    override fun getCameraViewContainer(): ViewGroup? =
        root.findViewById(R.id.cameraContainer)

    override fun getCameraRequest(): CameraRequest =
        CameraRequest.Builder()
            .setPreviewWidth(1280)
            .setPreviewHeight(720)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
            .setAspectRatioShow(true)
            .create()

    override fun initView() {
        super.initView()
        val panel = root.findViewById<View>(R.id.panel)

        root.findViewById<Button>(R.id.btnSettings).setOnClickListener {
            panel.visibility = if (panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        root.findViewById<Button>(R.id.btnFlip).setOnClickListener {
            prefs.edit().putBoolean("flip", !prefs.getBoolean("flip", false)).apply()
            applyFlip()
        }
        root.findViewById<Button>(R.id.btnPhoto).setOnClickListener { takePhoto() }
        root.findViewById<Button>(R.id.btnRecord).setOnClickListener { toggleRecord(it as Button) }

        val ids = listOf(R.id.sbBrightness, R.id.sbContrast, R.id.sbSaturation, R.id.sbGamma, R.id.sbGain)
        ids.forEachIndexed { i, id ->
            val sb = root.findViewById<SeekBar>(id)
            sb.progress = prefs.getInt(keys[i], 50)
            sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    prefs.edit().putInt(keys[i], p).apply()
                    applyValue(keys[i], p)
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
            })
        }
    }

    // Khi camera mở xong: áp lại lật hình + các giá trị đã lưu
    override fun onCameraState(
        self: MultiCameraClient.ICamera, code: ICameraStateCallBack.State, msg: String?
    ) {
        super.onCameraState(self, code, msg)
        if (code == ICameraStateCallBack.State.OPENED) {
            runOnUiThread {
                applyFlip()
                keys.forEach { k -> if (prefs.contains(k)) applyValue(k, prefs.getInt(k, 50)) }
            }
        }
    }

    private fun applyFlip() {
        // Lật trái-phải hình hiển thị
        textureView.scaleX = if (prefs.getBoolean("flip", false)) -1f else 1f
    }

    private fun applyValue(key: String, v: Int) {
        val cam = getCurrentCamera() as? CameraUVC ?: return
        try {
            when (key) {
                "brightness" -> cam.setBrightness(v)
                "contrast" -> cam.setContrast(v)
                "saturation" -> cam.setSaturation(v)
                "gamma" -> cam.setGamma(v)
                "gain" -> cam.setGain(v)
            }
        } catch (e: Exception) {
            // Camera không hỗ trợ thông số này
        }
    }

    private fun dir(sub: String): String {
        val d = File(Environment.getExternalStorageDirectory(), "DVRPlus/$sub")
        d.mkdirs(); return d.absolutePath
    }

    private fun takePhoto() {
        captureImage(object : ICaptureCallBack {
            override fun onBegin() {}
            override fun onError(error: String?) = toast("Lỗi chụp: $error")
            override fun onComplete(path: String?) = toast("Đã lưu: $path")
        }, "${dir("PHOTO")}/IMG_${System.currentTimeMillis()}.jpg")
    }

    private fun toggleRecord(btn: Button) {
        if (!recording) {
            captureVideoStart(object : ICaptureCallBack {
                override fun onBegin() { recording = true; runOnUiThread { btn.text = "Dừng" } }
                override fun onError(error: String?) { recording = false; toast("Lỗi ghi: $error") }
                override fun onComplete(path: String?) {
                    recording = false
                    runOnUiThread { btn.text = "Ghi" }
                    toast("Đã lưu: $path")
                }
            }, "${dir("VIDEO")}/VID_${System.currentTimeMillis()}.mp4")
        } else captureVideoStop()
    }

    private fun toast(s: String) = runOnUiThread { Toast.makeText(this, s, Toast.LENGTH_SHORT).show() }
}
