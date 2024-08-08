package com.google.mediapipe.trois.facedetection.server.plugins


import android.content.Context
import android.content.Intent
import com.google.mediapipe.trois.facedetection.server.model.LoginRequestData
import com.google.mediapipe.trois.facedetection.server.model.LoginResponseData
import com.google.mediapipe.trois.facedetection.server.model.StatusRequestData
import com.google.mediapipe.trois.facedetection.server.model.StatusResponseData
import com.google.mediapipe.trois.facedetection.server.model.User
import com.google.mediapipe.trois.facedetection.server.model.UserResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlin.random.Random

fun Application.configureRouting(context: Context) {
    println("Comming Here Five")
    routing {
        get("/") {
            call.respondText("Hello World!")
            val intent = Intent("com.example.API_HIT")
            context.sendBroadcast(intent)
        }

        get("/demo") {
            call.respondText("Hello Demo!")
        }
        post("/login") {
            val data = call.receive<LoginRequestData>()
            // TODO Verify email/password
            val randomId = Random.nextInt().toString()
            call.respond(HttpStatusCode.OK, LoginResponseData(randomId, data.email))
        }

        get("/user") {
            val user = User( "John Doe", "john.doe@example.com")
            call.respond(user)
        }

        post("/checkStatus") {
            val data = call.receive<StatusRequestData>()
            val randomId = Random.nextInt().toString()
            call.respond(HttpStatusCode.OK, StatusResponseData(randomId, data.currentStatus))
        }

        post("/receive-json") {
            val data = call.receive<User>()
//            println("Received: $data")
            val randomId = Random.nextInt().toString()
            call.respond(HttpStatusCode.OK, UserResponseData(randomId, data.name, data.email))
        }

    }
}
