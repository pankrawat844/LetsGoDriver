package com.driver.retrofit

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitApi {

    companion object {
        val gson=GsonBuilder()
            .setLenient()
            .create()
        operator fun invoke(): RetrofitApi {
            return Retrofit.Builder()
                .baseUrl("https://lezcocabs.klifftechnologies.com/api/driver/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitApi::class.java)
        }
    }

    @Headers("Content-Type: application/json")
    @POST("insert.php")
    fun setLatLon(@Body  body:JSONObject):Call<ResponseBody>

}