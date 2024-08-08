package com.google.mediapipe.trois.facedetection.server.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val email: String)

@Serializable
data class UserResponseData(
    val id: String,
    val name: String,
    val email: String,
)
