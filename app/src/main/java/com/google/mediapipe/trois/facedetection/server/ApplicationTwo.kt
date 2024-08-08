package com.google.mediapipe.trois.facedetection.server

import com.google.mediapipe.trois.facedetection.server.plugins.configureRouting
import com.google.mediapipe.trois.facedetection.server.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.*
import io.ktor.server.netty.*
//import kotlinx.serialization.ExperimentalSerializationApi

//@OptIn(ExperimentalSerializationApi::class)
//fun Application.module() {
//    configureSerialization()
////    configureRouting(this)
//}

//fun startKtorServer() {
//    println("KtorServer-Initialize")
//    embeddedServer(Netty, port = 8080) {
//        module()
//    }.start(wait = true)
////    embeddedServer(Netty, port = 8080,host = "127.0.0.1", module = Application::module).start(wait = true)
//    println("KtorServer-Started")
//}
//
//fun main() {
//    startKtorServer()
//}