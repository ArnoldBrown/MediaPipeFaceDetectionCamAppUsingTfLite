package com.google.mediapipe.trois.facedetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.trois.facedetection.server.startKtorServer
import com.google.mediapipe.trois.facedetection.tfliteModel.TFLiteModel
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.*

class CameraActivity : AppCompatActivity() {

    private lateinit var frontCameraViewFinder: PreviewView
    private lateinit var backCameraViewFinder: PreviewView
    private lateinit var frontCameraTextView: TextView

    private var isFrontCameraActive = true
    private var job1: Job? = null
    private var current_status: String = "IDLE"

    private lateinit var apiHitReceiver: BroadcastReceiver
    private var server: NettyApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + Job())

    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //init_views
        frontCameraViewFinder = findViewById(R.id.front_camera_view_finder)
        backCameraViewFinder = findViewById(R.id.back_camera_view_finder)
        frontCameraTextView = findViewById(R.id.front_camera_text_view)

        updateStatus("IDLE")

        //Camera_Permission
        requestCameraPermission()

        //Start_Server_and_Listener_Api_Hit_Change_Status
        apiHitReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("CameraActivity", "Broadcast received")
                Toast.makeText(this@CameraActivity, "Your test " + intent?.getStringExtra("status"), Toast.LENGTH_SHORT).show()
                current_status = intent?.getStringExtra("status") ?: "IDLE"
//                Log.d("received_Response",""+intent?.getStringExtra("status"))
//                current_status = when (current_status) {
//                    "IDLE" -> "START"
//                    "START" -> "STOP"
//                    else -> "IDLE"
//                }
                updateStatus(current_status)
                handleCameraState()
            }
        }
        val filter = IntentFilter("com.example.API_HIT")
        registerReceiver(apiHitReceiver, filter)
        startKtorServerInBackground()
        setupTouchListeners()
    }

    //Update_Status_Text_Color
    private fun updateStatus(status: String) {
        frontCameraTextView.text = status
        // Update text color based on status
        val color = when (status) {
            "IDLE" -> Color.parseColor("#FF8000") // Orange
            "START" -> Color.parseColor("#00FF00") // Green
            "STOP" -> Color.parseColor("#FF0000") // Red
            else -> Color.BLACK // Default color
        }
        frontCameraTextView.setTextColor(color)
    }

    //Camera_Permission
    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCameraCoroutine()
        } else {
            // Handle the case where the permission is not granted
        }
    }

    private fun requestCameraPermission() {
        cameraPermissionRequest.launch(Manifest.permission.CAMERA)
    }
    //end

    private fun startCameraCoroutine() {
        CoroutineScope(Dispatchers.Main).launch {
            startBackCamera()
        }
        CoroutineScope(Dispatchers.Main).launch {
            startFrontCamera()
        }
    }

    private fun startCamera(cameraSelector: CameraSelector, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                Log.d("CameraActivity", "Camera started and bound to lifecycle")
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startFrontCamera() {
//        val frontCameraSelector = CameraSelector.Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//            .build()
        startCamera(CameraSelector.DEFAULT_FRONT_CAMERA, frontCameraViewFinder)
    }

    private fun startBackCamera() {
//        val backCameraSelector = CameraSelector.Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//            .build()
        startCamera(CameraSelector.DEFAULT_BACK_CAMERA, backCameraViewFinder)
    }

    private fun captureImage() {
        if (::imageCapture.isInitialized) {
            Log.d("CameraActivity", "Attempting to capture image")
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        Log.d("CameraActivity", "Image captured successfully")
                        processImage(image)
                        image.close() // Make sure to close the image to free up resources
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraActivity", "Image capture failed", exception)
                    }
                }
            )
        } else {
            Log.w("CameraActivity", "ImageCapture is not initialized")
        }
    }

    private fun handleCameraState() {
        when (current_status) {
            "START" -> startCapturingImages()
            "STOP" -> stopCapturingImages()
        }
    }

    private fun startCapturingImages() {
        Log.d("CameraActivity", "Starting image capture")
        captureImage() // Start capturing images when the state is START
    }

    private fun stopCapturingImages() {
        Log.d("CameraActivity", "Stopping image capture")
        // Implement logic to stop capturing images if needed
    }

    private fun processImage(image: ImageProxy) {
        Log.e("dsdsds","weeeee")
        val tfliteModel = TFLiteModel(this)
        val bitmap = imageProxyToBitmap(image)
        val output = tfliteModel.runInference(bitmap)
        println("Model output: ${output}")
//
        tfliteModel.close()
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val byteArray = ByteArray(buffer.remaining())
        buffer.get(byteArray)

        // Assuming the image is in NV21 format (common for camera image formats)
        // Convert NV21 byte array to Bitmap (you might need additional libraries for this step)
        // Here, we use a method from the Android framework to create a bitmap from the byte array.
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListeners() {
        frontCameraViewFinder.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!isFrontCameraActive) {
                    startFrontCamera()
                    isFrontCameraActive = true
                }
                true
            } else {
                false
            }
        }

        backCameraViewFinder.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (isFrontCameraActive) {
                    startBackCamera()
                    isFrontCameraActive = false
                }
                true
            } else {
                false
            }
        }
    }

    private fun startKtorServerInBackground() {
        serverScope.launch {
            // Start the Ktor server
            startKtorServer(this@CameraActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(apiHitReceiver)
        stopKtorServer()
    }

    private fun stopKtorServer() {
        serverScope.launch {
            server?.stop(1000, 10000)
        }
    }
}
