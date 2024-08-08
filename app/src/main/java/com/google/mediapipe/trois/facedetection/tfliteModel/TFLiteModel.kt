package com.google.mediapipe.trois.facedetection.tfliteModel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.BufferOverflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteModel(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile(context))
            val inputTensor = interpreter!!.getInputTensor(0)
            Log.d("Tensor Info", "Input tensor shape: ${inputTensor.shape().contentToString()}")
            Log.d("Tensor Info", "Input tensor data type: ${inputTensor.dataType()}")

            val outputTensor = interpreter!!.getOutputTensor(0)
            Log.d("Tensor Info", "Output tensor shape: ${outputTensor.shape().contentToString()}")
            Log.d("Tensor Info", "Output tensor data type: ${outputTensor.dataType()}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("best.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun runInference(bitmap: Bitmap): Array<Array<FloatArray>> {
        try {
            val input = preprocessImage(bitmap)
            Log.d("Inference", "Input buffer: $input")
            val output = Array(1) { Array(7) { FloatArray(1) } }
            Log.d("Inference", "Output buffer: $output")
            interpreter?.run(input, output)
            Log.d("Inference", "Inference result: ${output.contentDeepToString()}")
            return output
        } catch (e: Exception) {
            Log.e("Inference", "Error during inference", e)
            throw e
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputSize = 640
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val intValues = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        try {
            byteBuffer.rewind() // Ensure buffer position is set to zero before writing
            var pixel = 0
            for (i in 0 until inputSize) {
                for (j in 0 until inputSize) {
                    if (byteBuffer.position() + 12 > byteBuffer.capacity()) {
                        throw BufferOverflowException()
                    }
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f)
                    byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)
                    byteBuffer.putFloat((value and 0xFF) / 255.0f)
                }
            }
        } catch (e: BufferOverflowException) {
            Log.e("Buffer Error", "Buffer overflow during image preprocessing", e)
        }

        return byteBuffer
    }


    fun close() {
        interpreter?.close()
    }
}
