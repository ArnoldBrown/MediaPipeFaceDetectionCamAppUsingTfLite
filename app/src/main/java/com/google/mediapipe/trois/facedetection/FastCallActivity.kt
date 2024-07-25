package com.google.mediapipe.trois.facedetection

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.trois.facedetection.apiCall.RetrofitClient
import com.google.mediapipe.trois.facedetection.model.Item

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        buttonGetRoot.setOnClickListener {
            fetchRoot()
        }
        buttonPostItem.setOnClickListener {
            postItem()
        }

    }

    private fun fetchRoot() {
        RetrofitClient.apiService.getObjects().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val result = response.body()?.get("message") ?: "No message"
                    textViewResult.text = result
                } else {
                    textViewResult.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                textViewResult.text = "Failure: ${t.message}"
            }
        })
    }

    private fun postItem() {
        val newItem = Item(name = "Sample Item", description = "Sample Description", price = 10.0f, tax = 2.0f)
        RetrofitClient.apiService.createItem(newItem).enqueue(object : Callback<Map<String, Any?>> {
            override fun onResponse(call: Call<Map<String, Any?>>, response: Response<Map<String, Any?>>) {
                if (response.isSuccessful) {
                    val result = response.body()?.get("name") ?: "No name"
                    textViewResult.text = "Item created: $result"
                } else {
                    textViewResult.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<Map<String, Any?>>, t: Throwable) {
                textViewResult.text = "Failure: ${t.message}"
            }
        })
    }
}