package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.squareup.picasso.Picasso
import com.driver.utils.Common
import com.driver.utils.DriverAllTripFeed

import org.json.JSONException
import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat

class FinishTripActivity : AppCompatActivity() {

    lateinit var layout_finished: RelativeLayout
    lateinit var layout_back_arrow: RelativeLayout
    lateinit var txt_booking_detail: TextView
    lateinit var img_user: ImageView
    lateinit var txt_user_name: TextView
    lateinit var txt_booking_date: TextView
    lateinit var txt_pickup_location: TextView
    lateinit var txt_pickup_time: TextView
    lateinit var txt_pickup_location_val: TextView
    lateinit var txt_drop_location: TextView
    lateinit var txt_drop_time: TextView
    lateinit var txt_drop_location_val: TextView
    lateinit var txt_distance: TextView
    lateinit var txt_distance_val: TextView
    lateinit var txt_total_time: TextView
    lateinit var txt_approc_price: TextView
    lateinit var txt_approc_price_val: TextView
    lateinit var txt_final_price: TextView
    lateinit var txt_final_price_val: TextView
    lateinit var txt_appr_currence: TextView
    lateinit var txt_final_currence: TextView
    lateinit var edt_final_amount: EditText
    lateinit var edt_reason_ride: EditText

    internal var driverAllTripFeed: DriverAllTripFeed? = null

    lateinit var userPref: SharedPreferences
    lateinit var loader: LoaderView
    var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_trip)

        userPref = PreferenceManager.getDefaultSharedPreferences(this@FinishTripActivity)
        loader = LoaderView(this@FinishTripActivity)

        position = intent.getIntExtra("position", 0)

        driverAllTripFeed = Common.driverAllTripFeed

        txt_booking_detail = findViewById(R.id.txt_booking_detail) as TextView
        img_user = findViewById(R.id.img_user) as ImageView
        txt_user_name = findViewById(R.id.txt_user_name) as TextView
        txt_booking_date = findViewById(R.id.txt_booking_date) as TextView
        txt_pickup_location = findViewById(R.id.txt_pickup_location) as TextView
        txt_pickup_time = findViewById(R.id.txt_pickup_time) as TextView
        txt_pickup_location_val = findViewById(R.id.txt_pickup_location_val) as TextView
        txt_drop_location = findViewById(R.id.txt_drop_location) as TextView
        txt_drop_time = findViewById(R.id.txt_drop_time) as TextView
        txt_drop_location_val = findViewById(R.id.txt_drop_location_val) as TextView
        layout_finished = findViewById(R.id.layout_finished) as RelativeLayout
        txt_distance = findViewById(R.id.txt_distance) as TextView
        txt_distance_val = findViewById(R.id.txt_distance_val) as TextView
        txt_total_time = findViewById(R.id.txt_total_time) as TextView
        txt_approc_price = findViewById(R.id.txt_approc_price) as TextView
        txt_approc_price_val = findViewById(R.id.txt_approc_price_val) as TextView
        txt_final_price = findViewById(R.id.txt_final_price) as TextView
        txt_final_price_val = findViewById(R.id.txt_final_price_val) as TextView
        txt_appr_currence = findViewById(R.id.txt_appr_currence) as TextView
        txt_final_currence = findViewById(R.id.txt_final_currence) as TextView
        edt_final_amount = findViewById(R.id.edt_final_amount) as EditText
        edt_reason_ride = findViewById(R.id.edt_reason_ride) as EditText

        txt_appr_currence.text = userPref.getString("currency", "")
        txt_final_currence.text = userPref.getString("currency", "")
        if (Common.OnTripTime !== "")
            txt_pickup_time.text = Common.OnTripTime
        if (Common.FinishedTripTime !== "")
            txt_drop_time.text = Common.FinishedTripTime
        txt_approc_price_val.text = driverAllTripFeed!!.amount


        val timeDifFormate = SimpleDateFormat("HH:mm:ss")
        var TotalTime = ""
        try {
            val starttime = timeDifFormate.parse(Common.OnTripTime)
            val endtime = timeDifFormate.parse(Common.FinishedTripTime)

            val TwoTimeDif = endtime.time - starttime.time

            val mills = Math.abs(TwoTimeDif)

            val Hours = (mills / (1000 * 60 * 60)).toInt()
            val Mins = (mills / (1000 * 60)).toInt() % 60
            val Secs = ((mills / 1000).toInt() % 60).toLong()

            Log.d("TwoTimeDif", "TwoTimeDif = " + Common.FinishedTripTime + "==" + Common.OnTripTime)
            Log.d("TwoTimeDif", "TwoTimeDif = " + endtime.time + "==" + starttime.time)
            Log.d("TwoTimeDif", "TwoTimeDif = $TwoTimeDif==$Hours==$Mins==$Secs")

            if (Hours > 0)
                TotalTime += "$Hours hr "
            if (Mins > 0)
                TotalTime += "$Mins min "

            txt_total_time.text = TotalTime

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        txt_distance_val.text = driverAllTripFeed!!.km + " km"

        if (driverAllTripFeed!!.getuserDetail() != null && driverAllTripFeed!!.getuserDetail() != "") {
            try {
                val DrvObj = JSONObject(driverAllTripFeed!!.getuserDetail())
                Picasso.with(this@FinishTripActivity)
                        .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                        .placeholder(R.drawable.user_photo)
                        .transform(CircleTransformation(this@FinishTripActivity))
                        .into(img_user)
                txt_user_name.text = DrvObj.getString("username")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var pickup_date_time = ""
        try {
            val parceDate = simpleDateFormat.parse(driverAllTripFeed!!.pickupDateTime)
            val parceDateFormat = SimpleDateFormat("dd MMM yyyy")
            pickup_date_time = parceDateFormat.format(parceDate.time)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        txt_booking_date.text = pickup_date_time

        txt_pickup_location_val.text = driverAllTripFeed!!.pickupArea
        txt_drop_location_val.text = driverAllTripFeed!!.dropArea

        /*Final amount caculation*/

        val approxTime = driverAllTripFeed!!.approxTime.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        var hours = 0
        var mintus = 0
        if (approxTime.size == 4) {
            hours = Integer.parseInt(approxTime[0]) * 60
            mintus = Integer.parseInt(approxTime[2])
        } else if (approxTime.size == 2) {
            if (approxTime[1].contains("mins"))
                mintus = Integer.parseInt(approxTime[0])
            else
                mintus = Integer.parseInt(approxTime[0]) * 3600
        }

        val aprTime = (hours + mintus) * java.lang.Float.parseFloat(driverAllTripFeed!!.perMinuteRate)
        Log.d("Driver Price", "Driver Price = $aprTime==$hours==$mintus")
        val RideAmount = Integer.parseInt(driverAllTripFeed!!.amount) - aprTime

        var final_hours = 0
        var final_mintus = 0
        val timeSplite = TotalTime.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (timeSplite.size == 2) {
            final_mintus = Integer.parseInt(timeSplite[0])
        } else if (timeSplite.size == 4) {
            final_hours = Integer.parseInt(timeSplite[0]) * 60
            final_mintus = Integer.parseInt(timeSplite[2])
        }

        val DriverAmount = (final_hours + final_mintus) * java.lang.Float.parseFloat(driverAllTripFeed!!.perMinuteRate)
        val finalPrice = (DriverAmount + RideAmount).toInt()
        txt_final_price_val.text = finalPrice.toString() + ""
        edt_final_amount.setText(finalPrice.toString() + "")
        Log.d("Driver Price", "Driver Price = $aprTime==$RideAmount==$final_mintus==$final_hours==$DriverAmount")

        layout_finished.setOnClickListener(View.OnClickListener {
            if (edt_final_amount.text.toString().length == 0) {
                Common.showMkError(this@FinishTripActivity, resources.getString(R.string.please_enter_amount))
                return@OnClickListener
            }

            loader.show()
            var FinishUrl = ""
            try {
                FinishUrl = Url.DriverCompletedTripUrl + "?booking_id=" + driverAllTripFeed!!.id + "&driver_id=" + userPref.getString("id", "") + "&final_amount=" + edt_final_amount.text.toString() + "&delay_reason=" + URLEncoder.encode(edt_reason_ride.text.toString(), "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            Log.d("FinishUrl", "FinishUrl = $FinishUrl")
            Ion.with(this@FinishTripActivity)
                    .load(FinishUrl)
                    .asJsonObject()
                    .setCallback(FutureCallback { e, result ->
                        loader.loaderObject().stop()
                        loader.loaderDismiss()


                        // do stuff with the result or error
                        if (e != null) {
                            //Toast.makeText(FinishTripActivity.this, "Login Error" + e, Toast.LENGTH_LONG).show();
                            Common.showMkError(this@FinishTripActivity, e.message!!)
                            return@FutureCallback
                        }
                        layout_finished.visibility = View.GONE
                        try {
                            val jsonObject = JSONObject(result.toString())
                            if (jsonObject.has("status") && jsonObject.getString("status") == "success") {

                                layout_finished.visibility = View.GONE

                                val isBookingAccept = userPref.edit()
                                isBookingAccept.putBoolean("isBookingAccept", false)
                                isBookingAccept.commit()

                                val booking_status = userPref.edit()
                                booking_status.putString("booking_status", "finished")
                                booking_status.commit()

                                driverAllTripFeed!!.status = "9"
                                driverAllTripFeed!!.driverFlag = "3"

                                Handler().postDelayed({
                                    val di = Intent()
                                    di.putExtra("position", position)
                                    di.putExtra("status", driverAllTripFeed!!.status)
                                    di.putExtra("driver_flage", driverAllTripFeed!!.driverFlag)
                                    setResult(1, di)
                                    finish()
                                }, 500)

                            }
                        } catch (e1: Exception) {
                            e1.printStackTrace()
                        }
                    })
        })

        layout_back_arrow = findViewById(R.id.layout_back_arrow) as RelativeLayout
        layout_back_arrow.setOnClickListener { finish() }
    }
}
