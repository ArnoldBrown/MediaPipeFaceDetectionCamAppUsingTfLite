package com.google.mediapipe.trois.facedetection

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FastCallActivity : AppCompatActivity() {

    private lateinit var textViewResult: TextView
    private lateinit var buttonGetRoot: Button
    private lateinit var buttonPostItem: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_call)

        textViewResult = findViewById(R.id.textViewResult)
        buttonGetRoot = findViewById(R.id.buttonGetRoot)
        buttonPostItem = findViewById(R.id.buttonPostItem)



    }


}