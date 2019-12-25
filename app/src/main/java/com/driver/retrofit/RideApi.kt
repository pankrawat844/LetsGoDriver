package com.driver.retrofit

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface RideApi {

    companion object {
        val gson=GsonBuilder()
            .setLenient()
            .create()
        operator fun invoke(): RideApi {
            return Retrofit.Builder()
                .baseUrl("https://digitaldwarka.com/taxiapp/web_service/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RideApi::class.java)
        }
    }

@FormUrlEncoded
    @POST("driver_ride_notification")
    fun get_ride_notification(@Field("driver_id") driver_id:String):Call<ResponseBody>

}