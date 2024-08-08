package com.google.mediapipe.trois.facedetection.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
//import com.login.main
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import com.login.startKtorServer

class ServerService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ServerService", "Ktor server Initiated")
//                main(arrayOf("-config=ktorServer/src/main/resources/application.conf"))
//                startKtorServer() // Start the Ktor server
//                main()
                Log.d("ServerService", "Ktor server started")
            } catch (e: Exception) {
                Log.e("ServerService", "Error starting Ktor server", e)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

//import android.app.IntentService
//import android.content.Intent
//import android.util.Log
//import com.login.main
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class ServerService : IntentService("ServerService") {
//    override fun onHandleIntent(intent: Intent?) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                main(arrayOf("-config=ktorServer/src/main/resources/application.conf"))
//
////                main() // Start the Ktor server
//            } catch (e: Exception) {
//                Log.e("ServerService", "Error starting Ktor server", e)
//            }
//        }
//    }
//}