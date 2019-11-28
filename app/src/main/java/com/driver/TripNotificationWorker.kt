package com.driver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.driver.Adapter.DriverAllTripAdapter
import com.driver.utils.Common
import com.driver.utils.DriverAllTripFeed
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class TripNotificationWorker(val context: Context, workerParams: WorkerParameters) :Worker( context,
    workerParams
) {
    var driverAllTripArray: ArrayList<DriverAllTripFeed>? = null
    lateinit var userPref: SharedPreferences

    private val WORK_RESULT = "work_result"

    override fun doWork(): Result {
//        val data:Data=inputData
        Log.e("TAG", "doWork:");
        getDriverAllTrip(0)
        val outData by lazy {
            Data.Builder().putString(WORK_RESULT,"Completed").build()
        }

        return Result.success()

    }


    private fun getDriverAllTrip(offset: Int) {
        userPref = PreferenceManager.getDefaultSharedPreferences(context)

        if (offset == 0) {
            driverAllTripArray = ArrayList()
        }
        //String DrvBookingUrl = Url.DriverTripUrl+"?driver_id="+Utility.userDetails.getId()+"&off="+offset;
        val DrvBookingUrl =
            Url.DriverTripUrl + "?driver_id=" + userPref.getString("id", "") + "&off=" + offset
        Log.d("loadTripsUrl", "loadTripsUrl =$DrvBookingUrl")
        Ion.with(context)
            .load(DrvBookingUrl)
            .asJsonObject()
            .setCallback { error, result ->
                // do stuff with the result or error
                Log.d("trips driver result", "load_trips result = $result==$error")

                if (error == null) {

                    try {
                        val resObj = JSONObject(result.toString())
                        Log.d("loadTripsUrl", "loadTripsUrl two= $resObj")
                        if (resObj.getString("status") == "success") {

                            val tripArray = JSONArray(resObj.getString("all_trip"))
                            for (t in 0 until tripArray.length()) {
                                val trpObj = tripArray.getJSONObject(t)
                                val allTripFeed = DriverAllTripFeed()
                                allTripFeed.id = trpObj.getString("id")
                                allTripFeed.driverFlag = trpObj.getString("driver_flag")
                                allTripFeed.dropArea = trpObj.getString("drop_area")
                                allTripFeed.pickupArea = trpObj.getString("pickup_area")
                                allTripFeed.carType = trpObj.getString("car_type")
                                allTripFeed.pickupDateTime = trpObj.getString("pickup_date_time")
                                allTripFeed.amount = trpObj.getString("amount")
                                allTripFeed.carIcon = trpObj.getString("icon")
                                allTripFeed.km = trpObj.getString("km")
                                allTripFeed.setUserDetail(trpObj.getString("user_detail"))
                                allTripFeed.status = trpObj.getString("status")
                                allTripFeed.startTime = trpObj.getString("start_time")
                                allTripFeed.endTime = trpObj.getString("end_time")
                                allTripFeed.serverTime = trpObj.getString("server_time")
                                allTripFeed.approxTime = trpObj.getString("approx_time")
                                allTripFeed.perMinuteRate = trpObj.getString("per_minute_rate")
                                allTripFeed.pickupLat = trpObj.getString("pickup_lat")
                                allTripFeed.pickupLongs = trpObj.getString("pickup_longs")
                                driverAllTripArray!!.add(allTripFeed)
                            }
                            Log.d(
                                "loadTripsUrl",
                                "loadTripsUrl three= " + driverAllTripArray!!.size
                            )
                            if (driverAllTripArray != null && driverAllTripArray!!.size > 0) {
                                if (offset == 0) {

                                } else {

                                }
                            }
                        } else if (resObj.getString("status") == "false") {

                            Common.showMkError(
                                context as Activity,
                                resObj.getString("error code").toString()
                            )

                            if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {
                                val editor = userPref.edit()
                                editor.clear()
                                editor.commit()
                                Handler().postDelayed({
                                    val intent =
                                        Intent(context, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    context.startActivity(intent)
                                    context.finish()
                                }, 2500)
                            }
                        } else {
                            if (offset == 0) {

                            } else {
                                //Toast.makeText(DriverTripActivity.this, resObj.getString("message").toString(), Toast.LENGTH_LONG).show();
                                Toast.makeText(
                                    context,
                                    context.resources.getString(R.string.data_not_found),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    Common.showHttpErrorMessage(context as Activity, error.message)
                }
            }

    }
}