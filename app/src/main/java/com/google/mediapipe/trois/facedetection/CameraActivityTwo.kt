package com.google.mediapipe.trois.facedetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.trois.facedetection.server.startKtorServer
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.Manifest
import androidx.camera.core.*
import java.io.File
import java.util.concurrent.ExecutorService


class CameraActivityTwo : AppCompatActivity() {

    private lateinit var frontCameraViewFinder: PreviewView
    private lateinit var backCameraViewFinder: PreviewView

    private var isFrontCameraActive = true

    private var job1: Job? = null
    private var current_status: String? = "IDLE"

    private lateinit var apiHitReceiver: BroadcastReceiver
    private var server: NettyApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + Job())

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private lateinit var outputDirectory: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        requestCameraPermission()

        frontCameraViewFinder = findViewById(R.id.front_camera_view_finder)
        backCameraViewFinder = findViewById(R.id.back_camera_view_finder)


        // Start Server & Receive Server Call
        apiHitReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("CameraActivity", "Broadcast received")
                Toast.makeText(this@CameraActivityTwo, "API was hit!", Toast.LENGTH_SHORT).show()

                if(current_status === "IDLE"){
                    current_status = "START"
                }else if(current_status === "START") {
                    current_status = "STOP"
                }
            }
        }
        // Register the receiver
        val filter = IntentFilter("com.example.API_HIT")
        registerReceiver(apiHitReceiver, filter)
       // Start Ktor Server
        startKtorServerInBackground()
        //val intent = Intent(this, ServerService::class.java)
        //startService(intent)

//        startIdealThread()
        setupTouchListeners()
//        startFrontCamera()

    }

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

    private fun startCameraCoroutine() {
        CoroutineScope(Dispatchers.Main).launch {
            startCamera()
            captureImage()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(frontCameraViewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                // Handle exception
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        if (::imageCapture.isInitialized) {
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        processImage(image)
                        image.close() // Make sure to close the image to free up resources
                    }

                    override fun onError(exception: ImageCaptureException) {
                        // Handle error
                    }
                }
            )
        } else {
            // Handle the case where imageCapture is not initialized
        }
    }

//    private fun captureImage() {
//        imageCapture.takePicture(
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageCapturedCallback() {
//                override fun onCaptureSuccess(image: ImageProxy) {
//                    processImage(image)
//                    image.close() // Make sure to close the image to free up resources
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    // Handle error
//                }
//            }
//        )
//    }

    private fun processImage(imageFile: ImageProxy) {
        // Process the image data (e.g., convert to bitmap, analyze, etc.)
        // Here you can call your function with the image data
        Log.d("FILE_Outtt", ""+imageFile);
    }

    private fun startKtorServerInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            // Start the Ktor server
            startKtorServer(this@CameraActivityTwo)
        }
    }

    private fun startIdealThread() {
        job1 = lifecycleScope.launch(Dispatchers.IO) {
//            val result = longRunningTask("Function 1")
            withContext(Dispatchers.Main) {
                while (current_status === "IDLE") {
                    Log.d("Job1", "Function 1 running...")

                    delay(1000)
                }
            }
        }
    }

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

    private fun startFrontCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val frontPreview = Preview.Builder().build()
            val frontCameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
            frontPreview.setSurfaceProvider(frontCameraViewFinder.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, frontCameraSelector, frontPreview)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startBackCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val backPreview = Preview.Builder().build()
            val backCameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            backPreview.setSurfaceProvider(backCameraViewFinder.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, backCameraSelector, backPreview)

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver
        unregisterReceiver(apiHitReceiver)
        // Stop the Ktor server
        stopKtorServer()
    }

    private fun stopKtorServer() {
        serverScope.launch {
            server?.stop(1000, 10000)
        }
    }
}