package com.google.mediapipe.trois.facedetection.server.model

import kotlinx.serialization.Serializable

@Serializable
data class StatusUpdateRequest(val newStatus: String)


