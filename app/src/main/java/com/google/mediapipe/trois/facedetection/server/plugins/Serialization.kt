package com.google.mediapipe.trois.facedetection.server.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
//import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

//@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSerialization() {
    println("Comming Here Two")
    install(ContentNegotiation) {

        println("Comming Here Three")
        json()
//        json(Json {
//            println("Comming Here Four")
//            namingStrategy = JsonNamingStrategy.SnakeCase
//            prettyPrint = true; ignoreUnknownKeys = true
//        })
    }
}