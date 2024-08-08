package com.google.mediapipe.trois.facedetection.server

import io.ktor.serialization.*
import android.content.Context
import android.content.Intent
import com.google.mediapipe.trois.facedetection.server.model.StatusRequestData
import com.google.mediapipe.trois.facedetection.server.model.StatusResponseData
import com.google.mediapipe.trois.facedetection.server.model.StatusUpdateRequest
import com.google.mediapipe.trois.facedetection.server.model.User
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.random.Random

fun Application.module(context: Context) {
    install(ContentNegotiation) {
        json()
    }


    routing {
        get("/api/endpoint") {
            call.respondText("Hello, API!")

            // Send broadcast to notify MainActivity
            val intent = Intent("com.example.API_HIT")
            context.sendBroadcast(intent)
        }

        get("/") {
            call.respondText("Hello World!")
            val intent = Intent("com.example.API_HIT")
            context.sendBroadcast(intent)
        }

        get("/user") {
            val user = User( "John Doe", "john.doe@example.com")
            call.respond(user)
        }

        post("/checkStatus") {
            val data = call.receive<StatusRequestData>()
            val randomId = Random.nextInt().toString()
            call.respond(HttpStatusCode.OK, StatusResponseData(randomId, data.currentStatus))
            val intent = Intent("com.example.API_HIT")
            intent.putExtra("status", data.currentStatus)
            context.sendBroadcast(intent)
        }
    }
}

fun startKtorServer(context: Context) {
    println("KtorServer-Initialize")
    embeddedServer(Netty, port = 8080) {
        module(context)
    }.start(wait = true)
//    embeddedServer(Netty, port = 8080,host = "127.0.0.1", module = Application::module).start(wait = true)
    println("KtorServer-Started")
}

