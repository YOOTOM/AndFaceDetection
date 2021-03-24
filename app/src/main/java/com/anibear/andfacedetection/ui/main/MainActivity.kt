package com.anibear.andfacedetection.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.anibear.andfacedetection.R
import com.anibear.andfacedetection.ui.photo_viewer.PhotoViewerActivity
import com.anibear.andfacedetection.utils.base.BaseActivity
import com.anibear.andfacedetection.utils.base.Cons
import com.anibear.andfacedetection.utils.base.Cons.IMG_EXTRA_KEY
import com.anibear.andfacedetection.utils.base.PublicMethods
import com.anibear.andfacedetection.utils.common.CameraSource
import com.anibear.andfacedetection.utils.common.CameraSourcePreview
import com.anibear.andfacedetection.utils.common.FrameMetadata
import com.anibear.andfacedetection.utils.common.GraphicOverlay
import com.anibear.andfacedetection.utils.interfaces.FaceDetectStatus
import com.anibear.andfacedetection.utils.interfaces.FrameReturn
import com.anibear.andfacedetection.utils.models.RectModel
import com.anibear.andfacedetection.utils.visions.FaceDetectionProcessor
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.hsalf.smilerating.BaseRating
import com.hsalf.smilerating.SmileRating
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : BaseActivity(), FrameReturn, FaceDetectStatus {
    private val FACE_DETECTION = "Face Detection"
    private val TAG = "MLKitTAG"

    var originalImage: Bitmap? = null
    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var faceFrame: ImageView? = null
    private var test: ImageView? = null
    private var takePhoto: Button? = null
    private var smile_rating: SmileRating? = null
    private var croppedImage: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test = findViewById(R.id.test)
        preview = findViewById(R.id.firePreview)
        takePhoto = findViewById(R.id.takePhoto)
        faceFrame = findViewById(R.id.faceFrame)
        graphicOverlay = findViewById(R.id.fireFaceOverlay)
        smile_rating = findViewById(R.id.smile_rating)

        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource()
        } else {
            PublicMethods.getRuntimePermissions(this)
        }

        takePhoto?.setOnClickListener {
            takePhoto()
            //throw RuntimeException("Test Crash") // Force a crash
        }
    }
    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay)
        }
        try {
            val processor = FaceDetectionProcessor(resources)
            processor.frameHandler = this
            processor.faceDetectStatus = this
            cameraSource?.setMachineLearningFrameProcessor(processor)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Can not create image processor: $FACE_DETECTION",
                e
            )
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }


    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        preview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource?.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //calls with each frame includes by face
    override fun onFrame(
        image: Bitmap,
        face: FirebaseVisionFace,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay?
    ) {
        originalImage = image
        if (face.leftEyeOpenProbability < 0.4) {
            rightEyeStatus.visibility = View.VISIBLE
        } else {
            rightEyeStatus.visibility = View.INVISIBLE
        }
        if (face.rightEyeOpenProbability < 0.4) {
            leftEyeStatus.visibility = View.VISIBLE
        } else {
            leftEyeStatus.visibility = View.INVISIBLE
        }
        var smile = 0
        if (face.smilingProbability > .8) {
            smile = BaseRating.GREAT
        } else if (face.smilingProbability <= .8 && face.smilingProbability > .6) {
            smile = BaseRating.GOOD
        } else if (face.smilingProbability <= .6 && face.smilingProbability > .4) {
            smile = BaseRating.OKAY
        } else if (face.smilingProbability <= .4 && face.smilingProbability > .2) {
            smile = BaseRating.BAD
        }
        smile_rating!!.setSelectedSmile(smile, true)
    }

    override fun onFaceLocated(rectModel: RectModel?) {
        faceFrame!!.setColorFilter(ContextCompat.getColor(this, R.color.green))
        takePhoto!!.isEnabled = true
        val left = (originalImage!!.width * 0.2).toFloat()
        val newWidth = (originalImage!!.width * 0.6).toFloat()
        val top = (originalImage!!.height * 0.2).toFloat()
        val newHeight = (originalImage!!.height * 0.6).toFloat()
        croppedImage = Bitmap.createBitmap(
            originalImage!!,
            left.toInt(),
            top.toInt(),
            newWidth.toInt(),
            newHeight.toInt()
        )
        test!!.setImageBitmap(croppedImage)
    }

    private fun takePhoto() {
        if (croppedImage != null) {
            val path = PublicMethods.saveToInternalStorage(croppedImage, Cons.IMG_FILE, mActivity)
            startActivity(
                Intent(mActivity, PhotoViewerActivity::class.java)
                    .putExtra(IMG_EXTRA_KEY, path)
            )
        }
    }

    override fun onFaceNotLocated() {
        faceFrame!!.setColorFilter(ContextCompat.getColor(this, R.color.red))
        takePhoto!!.isEnabled = false
    }
}