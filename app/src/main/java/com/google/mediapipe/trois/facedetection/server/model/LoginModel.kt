package com.google.mediapipe.trois.facedetection.server.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestData(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponseData(
    val id: String,
    val email: String,
)