package com.google.mediapipe.trois.facedetection.apiCall

import com.google.mediapipe.trois.facedetection.model.Item
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST

interface ApiService {

    @GET("/")
    fun getRoot(): Call<Map<String, String>>

    @GET("/items/{item_id}")
    fun getItem(@Path("item_id") itemId: Int, @Query("q") query: String?): Call<Map<String, Any?>>

    @POST("/items/")
    fun createItem(@Body item: Item): Call<Map<String, Any?>>

    @GET("/health")
    fun healthCheck(): Call<Map<String, String>>

    @GET("/api/houses")
    fun getObjects(): Call<Map<String, String>>
}