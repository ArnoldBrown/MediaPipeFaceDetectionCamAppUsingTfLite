/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.trois.facedetection

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: FaceDetectorResult? = null
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var scaleFactorX: Float = 1f
    private var scaleFactorY: Float = 1f
    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        results = null
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.mp_primary)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results?.let {
            for (detection in it.detections()) {
                val boundingBox = detection.boundingBox()

                val top = boundingBox.top * scaleFactorY
                val bottom = boundingBox.bottom * scaleFactorY
                val left = boundingBox.left * scaleFactorX
                val right = boundingBox.right * scaleFactorX

                // Draw bounding box around detected faces
                val drawableRect = RectF(left, top, right, bottom)
                canvas.drawRect(drawableRect, boxPaint)

                // Create text to display alongside detected faces
                val drawableText =
                    detection.categories()[0].categoryName() +
                            " " +
                            String.format(
                                "%.2f",
                                detection.categories()[0].score()
                            )

                // Draw rect behind display text
                textBackgroundPaint.getTextBounds(
                    drawableText,
                    0,
                    drawableText.length,
                    bounds
                )
                val textWidth = bounds.width()
                val textHeight = bounds.height()
                canvas.drawRect(
                    left + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING,
                    top + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING,
                    left + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + Companion.BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
                )

                // Draw text for detected face
                canvas.drawText(
                    drawableText,
                    left,
                    top + bounds.height(),
                    textPaint
                )
            }
        }
    }

    fun setResults(
        detectionResults: FaceDetectorResult,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        results = detectionResults

        // Calculate the scale factor based on the view size and the image size
        scaleFactorX = width * 1f / imageWidth
        scaleFactorY = height * 1f / imageHeight

        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}