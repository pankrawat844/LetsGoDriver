package com.driver.utils

import java.io.Serializable

/**
 * Created by techintegrity on 11/10/16.
 */
class DriverAllTripFeed : Serializable {

   lateinit var id: String

   lateinit var pickupArea: String

   lateinit var dropArea: String

   lateinit var status: String

   lateinit var carType: String

   lateinit internal var user_detail: String

   lateinit var pickupDateTime: String

   lateinit var driverFlag: String

   lateinit var amount: String

   lateinit var carIcon: String

   lateinit var km: String


   lateinit var startTime: String

   lateinit var endTime: String

   lateinit var serverTime: String

   lateinit var startRideTime: String

   lateinit var endRideTime: String

   lateinit var approxTime: String

   lateinit var perMinuteRate: String

   lateinit var pickupLat: String

   lateinit var pickupLongs: String
    fun getuserDetail(): String {
        return user_detail
    }

    fun setUserDetail(user_detail: String) {
        this.user_detail = user_detail
    }

}
