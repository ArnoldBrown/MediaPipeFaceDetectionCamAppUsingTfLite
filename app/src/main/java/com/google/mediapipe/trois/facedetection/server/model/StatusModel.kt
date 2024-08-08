package com.google.mediapipe.trois.facedetection.server.model

import kotlinx.serialization.Serializable

@Serializable
data class StatusRequestData(val currentStatus: String)

@Serializable
data class StatusResponseData(
    val id: String,
    val currentStatus: String,
)
