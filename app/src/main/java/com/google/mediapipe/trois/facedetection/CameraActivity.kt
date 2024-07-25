package com.google.mediapipe.trois.facedetection

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture

class CameraActivity : AppCompatActivity() {

    private lateinit var frontCameraViewFinder: PreviewView
    private lateinit var backCameraViewFinder: PreviewView

    private var isFrontCameraActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        frontCameraViewFinder = findViewById(R.id.front_camera_view_finder)
        backCameraViewFinder = findViewById(R.id.back_camera_view_finder)

        setupTouchListeners()
        startFrontCamera()
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
}