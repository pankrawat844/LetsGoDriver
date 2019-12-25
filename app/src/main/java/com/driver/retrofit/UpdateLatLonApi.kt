package com.driver.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UpdateLatLonApi {
    companion object
    {
        operator fun invoke():UpdateLatLonApi
        {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://digitaldwarka.com/taxiapp/website/")
                .build()
                .create(UpdateLatLonApi::class.java)
        }
    }
    @FormUrlEncoded()
    @POST("update-driver-location.php")
    fun savelocation(
        @Field("driver_id") driver_id:Int,
        @Field("lat") lat:Double,
        @Field("lon")lon:Double,
        @Field("city")city:String
    ): Call<ResponseBody>
}