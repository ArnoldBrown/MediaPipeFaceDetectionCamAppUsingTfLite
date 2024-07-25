package com.google.mediapipe.trois.facedetection.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.mediapipe.trois.facedetection.FaceDetectorHelper
import com.google.mediapipe.trois.facedetection.MainViewModel
import com.google.mediapipe.trois.facedetection.R
import com.google.mediapipe.trois.facedetection.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), FaceDetectorHelper.DetectorListener {

    private val TAG = "FaceDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private lateinit var faceDetectorHelper: FaceDetectorHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var frontCameraPreview: Preview? = null
    private var backCameraPreview: Preview? = null
    private var frontImageAnalyzer: ImageAnalysis? = null
    private var backImageAnalyzer: ImageAnalysis? = null
    private var frontCamera: Camera? = null
    private var backCamera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var backgroundExecutor: ExecutorService

    // To track which camera is currently active
    private var isFrontCameraActive = true

    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }

        backgroundExecutor.execute {
            if (faceDetectorHelper.isClosed()) {
                faceDetectorHelper.setupFaceDetector()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::faceDetectorHelper.isInitialized) {
            viewModel.setDelegate(faceDetectorHelper.currentDelegate)
            viewModel.setThreshold(faceDetectorHelper.threshold)
            backgroundExecutor.execute { faceDetectorHelper.clearFaceDetector() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backgroundExecutor = Executors.newSingleThreadExecutor()
        backgroundExecutor.execute {
            faceDetectorHelper =
                FaceDetectorHelper(
                    context = requireContext(),
                    threshold = viewModel.currentThreshold,
                    currentDelegate = viewModel.currentDelegate,
                    faceDetectorListener = this,
                    runningMode = RunningMode.LIVE_STREAM
                )
            fragmentCameraBinding.frontCameraViewFinder.post {
                setUpCamera()
                setupViewTouchListeners()
            }
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            // Bind the front camera by default
            bindFrontCamera()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindFrontCamera() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val frontCameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        frontCameraPreview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.frontCameraViewFinder.display.rotation)
            .build()
        frontImageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.frontCameraViewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor, faceDetectorHelper::detectLivestreamFrame)
            }

        cameraProvider.unbindAll()

        try {
            frontCamera = cameraProvider.bindToLifecycle(
                this,
                frontCameraSelector,
                frontCameraPreview,
                frontImageAnalyzer
            )
            frontCameraPreview?.setSurfaceProvider(fragmentCameraBinding.frontCameraViewFinder.surfaceProvider)
            isFrontCameraActive = true
            Log.d(TAG, "Front camera bound successfully by default.")
        } catch (exc: Exception) {
            Log.e(TAG, "Front camera binding failed", exc)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindBackCamera() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val backCameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        backCameraPreview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.backCameraViewFinder.display.rotation)
            .build()
        backImageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.backCameraViewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor, faceDetectorHelper::detectLivestreamFrame)
            }

        cameraProvider.unbindAll()

        try {
            backCamera = cameraProvider.bindToLifecycle(
                this,
                backCameraSelector,
                backCameraPreview,
                backImageAnalyzer
            )
            backCameraPreview?.setSurfaceProvider(fragmentCameraBinding.backCameraViewFinder.surfaceProvider)
            isFrontCameraActive = false
            Log.d(TAG, "Back camera bound successfully.")
        } catch (exc: Exception) {
            Log.e(TAG, "Back camera binding failed", exc)
        }
    }

    private fun setupViewTouchListeners() {
        fragmentCameraBinding.frontCameraContainer.setOnClickListener {
            if (!isFrontCameraActive) {
                switchToFrontCamera()
            }
        }
        fragmentCameraBinding.backCameraContainer.setOnClickListener {
            if (isFrontCameraActive) {
                switchToBackCamera()
            }
        }
    }

    private fun switchToFrontCamera() {
        if (backCamera != null) {
            bindFrontCamera()
        }
    }

    private fun switchToBackCamera() {
        if (frontCamera != null) {
            bindBackCamera()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        frontImageAnalyzer?.targetRotation = fragmentCameraBinding.frontCameraViewFinder.display.rotation
        backImageAnalyzer?.targetRotation = fragmentCameraBinding.backCameraViewFinder.display.rotation
    }

    override fun onResults(resultBundle: FaceDetectorHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                val detectionResult = resultBundle.results[0]
                Log.d("OVERLAYYY",""+resultBundle.inputImageWidth)
                if (isAdded) {
                    if (isFrontCameraActive) {
                        fragmentCameraBinding.frontOverlay.setResults(
                            detectionResult,
                            resultBundle.inputImageHeight,
                            resultBundle.inputImageWidth
                        )
                    } else {
                        fragmentCameraBinding.backOverlay.setResults(
                            detectionResult,
                            resultBundle.inputImageHeight,
                            resultBundle.inputImageWidth
                        )
                    }
                }
                fragmentCameraBinding.frontOverlay.invalidate()
                fragmentCameraBinding.backOverlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == FaceDetectorHelper.GPU_ERROR) {
                // Handle GPU error if needed
            }
        }
    }
}


//class CameraFragment : Fragment(), FaceDetectorHelper.DetectorListener {
//
//    private val TAG = "FaceDetection"
//
//    private var _fragmentCameraBinding: FragmentCameraBinding? = null
//    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
//
//    private lateinit var faceDetectorHelper: FaceDetectorHelper
//    private val viewModel: MainViewModel by activityViewModels()
//    private var frontCameraPreview: Preview? = null
//    private var backCameraPreview: Preview? = null
//    private var frontImageAnalyzer: ImageAnalysis? = null
//    private var backImageAnalyzer: ImageAnalysis? = null
//    private var frontCamera: Camera? = null
//    private var backCamera: Camera? = null
//    private var cameraProvider: ProcessCameraProvider? = null
//
//    private lateinit var backgroundExecutor: ExecutorService
//
//    // To track which camera is currently active
//    private var isFrontCameraActive = true
//
//    override fun onResume() {
//        super.onResume()
//        if (!PermissionsFragment.hasPermissions(requireContext())) {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container)
//                .navigate(CameraFragmentDirections.actionCameraToPermissions())
//        }
//
//        backgroundExecutor.execute {
//            if (faceDetectorHelper.isClosed()) {
//                faceDetectorHelper.setupFaceDetector()
//            }
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (this::faceDetectorHelper.isInitialized) {
//            viewModel.setDelegate(faceDetectorHelper.currentDelegate)
//            viewModel.setThreshold(faceDetectorHelper.threshold)
//            backgroundExecutor.execute { faceDetectorHelper.clearFaceDetector() }
//        }
//    }
//
//    override fun onDestroyView() {
//        _fragmentCameraBinding = null
//        super.onDestroyView()
//        backgroundExecutor.shutdown()
//        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _fragmentCameraBinding =
//            FragmentCameraBinding.inflate(inflater, container, false)
//        return fragmentCameraBinding.root
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        backgroundExecutor = Executors.newSingleThreadExecutor()
//        backgroundExecutor.execute {
//            faceDetectorHelper =
//                FaceDetectorHelper(
//                    context = requireContext(),
//                    threshold = viewModel.currentThreshold,
//                    currentDelegate = viewModel.currentDelegate,
//                    faceDetectorListener = this,
//                    runningMode = RunningMode.LIVE_STREAM
//                )
//                fragmentCameraBinding.frontCameraViewFinder.post {
//                setUpCamera()
//                setupViewTouchListeners()
//            }
//        }
//    }
//
//    private fun setUpCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener({
//            cameraProvider = cameraProviderFuture.get()
//            bindCameraUseCases()
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
//
//    @SuppressLint("UnsafeOptInUsageError")
//    private fun bindCameraUseCases() {
//        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
//
//        val frontCameraSelector = CameraSelector.Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//            .build()
//        frontCameraPreview = Preview.Builder()
//            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setTargetRotation(fragmentCameraBinding.frontCameraViewFinder.display.rotation)
//            .build()
//        frontImageAnalyzer = ImageAnalysis.Builder()
//            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setTargetRotation(fragmentCameraBinding.frontCameraViewFinder.display.rotation)
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//            .build()
//            .also {
//                it.setAnalyzer(backgroundExecutor, faceDetectorHelper::detectLivestreamFrame)
//            }
//
//        val backCameraSelector = CameraSelector.Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//            .build()
//        backCameraPreview = Preview.Builder()
//            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setTargetRotation(fragmentCameraBinding.backCameraViewFinder.display.rotation)
//            .build()
//        backImageAnalyzer = ImageAnalysis.Builder()
//            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setTargetRotation(fragmentCameraBinding.backCameraViewFinder.display.rotation)
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//            .build()
//            .also {
//                it.setAnalyzer(backgroundExecutor, faceDetectorHelper::detectLivestreamFrame)
//            }
//
//        cameraProvider.unbindAll()
//
//        try {
//            frontCamera = cameraProvider.bindToLifecycle(
//                this,
//                frontCameraSelector,
//                frontCameraPreview,
//                frontImageAnalyzer
//            )
//            backCamera = cameraProvider.bindToLifecycle(
//                this,
//                backCameraSelector,
//                backCameraPreview,
//                backImageAnalyzer
//            )
//            frontCameraPreview?.setSurfaceProvider(fragmentCameraBinding.frontCameraViewFinder.surfaceProvider)
//            backCameraPreview?.setSurfaceProvider(fragmentCameraBinding.backCameraViewFinder.surfaceProvider)
//            Log.d(TAG, "Camera use cases bound successfully.")
//        } catch (exc: Exception) {
//            Log.e(TAG, "Use case binding failed", exc)
//        }
//    }
//
//    private fun setupViewTouchListeners() {
//        fragmentCameraBinding.frontCameraContainer.setOnClickListener {
//            if (!isFrontCameraActive) {
//                switchToFrontCamera()
//            }
//        }
//        fragmentCameraBinding.backCameraContainer.setOnClickListener {
//            if (isFrontCameraActive) {
//                switchToBackCamera()
//            }
//        }
//    }
//
//    private fun switchToFrontCamera() {
//        if (backCamera != null) {
//            cameraProvider?.unbindAll()
//            frontCamera = cameraProvider?.bindToLifecycle(
//                this,
//                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build(),
//                frontCameraPreview,
//                frontImageAnalyzer
//            )
//            frontCameraPreview?.setSurfaceProvider(fragmentCameraBinding.frontCameraViewFinder.surfaceProvider)
//            isFrontCameraActive = true
//            Log.d(TAG, "Switched to front camera.")
//        }
//    }
//
//    private fun switchToBackCamera() {
//        if (frontCamera != null) {
//            cameraProvider?.unbindAll()
//            backCamera = cameraProvider?.bindToLifecycle(
//                this,
//                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(),
//                backCameraPreview,
//                backImageAnalyzer
//            )
//            backCameraPreview?.setSurfaceProvider(fragmentCameraBinding.backCameraViewFinder.surfaceProvider)
//            isFrontCameraActive = false
//            Log.d(TAG, "Switched to back camera.")
//        }
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        frontImageAnalyzer?.targetRotation = fragmentCameraBinding.frontCameraViewFinder.display.rotation
//        backImageAnalyzer?.targetRotation = fragmentCameraBinding.backCameraViewFinder.display.rotation
//    }
//
//    override fun onResults(resultBundle: FaceDetectorHelper.ResultBundle) {
//        activity?.runOnUiThread {
//            if (_fragmentCameraBinding != null) {
//                val detectionResult = resultBundle.results[0]
//                Log.d("OVERLAYYY",""+resultBundle.inputImageWidth)
//                if (isAdded) {
//                    if(isFrontCameraActive == true) {
//                        fragmentCameraBinding.frontOverlay.setResults(
//                            detectionResult,
//                            resultBundle.inputImageHeight,
//                            resultBundle.inputImageWidth
//                        )
//                    }
//                    if(isFrontCameraActive == false) {
//                        fragmentCameraBinding.backOverlay.setResults(
//                            detectionResult,
//                            resultBundle.inputImageHeight,
//                            resultBundle.inputImageWidth
//                        )
//                    }
//                }
//                fragmentCameraBinding.frontOverlay.invalidate()
//                fragmentCameraBinding.backOverlay.invalidate()
//            }
//        }
//    }
//
//    override fun onError(error: String, errorCode: Int) {
//        activity?.runOnUiThread {
//            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
//            if (errorCode == FaceDetectorHelper.GPU_ERROR) {
//                // Handle GPU error if needed
//            }
//        }
//    }
//}
