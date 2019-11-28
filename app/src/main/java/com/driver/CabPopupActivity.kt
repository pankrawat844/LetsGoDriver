package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.CountDownTimer
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.afollestad.materialdialogs.MaterialDialog
import com.github.lzyzsd.circleprogress.DonutProgress
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.koushikdutta.ion.Ion
import com.driver.utils.Common
import com.driver.utils.CustomMap

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.ParseException
import java.text.SimpleDateFormat

class CabPopupActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var layout_accept: RelativeLayout
    lateinit var layout_decline: RelativeLayout
    lateinit var minutes_value: TextView
    lateinit var txt_accept: TextView
    lateinit var txt_decline: TextView
    lateinit var timmer_progress: DonutProgress
    lateinit var txt_address_val: TextView
    lateinit var customMap: CustomMap

     var accpet_time: Long = 0

    lateinit var booking_data: JSONObject
     var customCountdownTimer: CountDownTimer? = null

    private var mMap: GoogleMap? = null
    lateinit var loader: LoaderView
    lateinit var userPref: SharedPreferences

    lateinit var Roboto_Bold: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cab_popup)

        val display = windowManager.defaultDisplay

        Roboto_Bold = Typeface.createFromAsset(assets, "fonts/roboto_bold.ttf")

        userPref = PreferenceManager.getDefaultSharedPreferences(this@CabPopupActivity)

        loader = LoaderView(this@CabPopupActivity)

        try {
            booking_data = JSONObject(intent.getStringExtra("booking_data"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        customMap = findViewById(R.id.mapview)
        MapsInitializer.initialize(this@CabPopupActivity)
        customMap.onCreate(savedInstanceState)

        customMap.getMapAsync(this@CabPopupActivity)

        minutes_value = findViewById(R.id.minutes_value)
        txt_address_val = findViewById(R.id.txt_address_val)
        timmer_progress = findViewById(R.id.timmer_progress)
        txt_accept = findViewById(R.id.txt_accept)
        txt_accept.typeface = Roboto_Bold
        txt_decline = findViewById(R.id.txt_decline)
        txt_decline.typeface = Roboto_Bold

        val layout_main = findViewById(R.id.layout_main) as RelativeLayout
        layout_main.layoutParams.height = (display.height * 0.72).toInt()

        layout_accept = findViewById(R.id.layout_accept) as RelativeLayout
        layout_accept.setOnClickListener {
            layout_accept.isClickable = false
            layout_accept.isEnabled = false

            AcceptBooking()
        }

        layout_decline = findViewById(R.id.layout_decline) as RelativeLayout
        layout_decline.setOnClickListener {
            layout_decline.isEnabled = false

            RejectBooking()
        }


        Handler().postDelayed({ DriverPopup() }, 1000)
    }

    fun AcceptBooking() {
        loader.show()
        val DrvBookingUrl = Url.DriverAcceptTripUrl + "?booking_id=" + Common.BookingId + "&driver_id=" + userPref.getString("id", "")
        Log.d("DrvBookingUrl", "DrvBookingUrl =$DrvBookingUrl")
        Ion.with(this@CabPopupActivity)
                .load(DrvBookingUrl)
                .asJsonObject()
                .setCallback { error, result ->
                    // do stuff with the result or error
                    Log.d("load_trips result", "load_trips result = $result==$error")
                    customCountdownTimer!!.onFinish()

                    loader.cancel()
                    if (error == null) {

                        try {
                            val resObj = JSONObject(result.toString())

                            if (resObj.getString("status") == "success") {

                                Common.ActionClick = "accept"

                                val booking_status = userPref.edit()
                                booking_status.putString("booking_status", "Accepted")
                                booking_status.commit()

                                val intent = Intent(this@CabPopupActivity, DriverTripActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()

                            } else if (resObj.getString("status") == "false") {
                                Common.showMkError(this@CabPopupActivity, resObj.getString("error code").toString())

                                if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {

                                    val editor = userPref.edit()
                                    editor.clear()
                                    editor.commit()

                                    Handler().postDelayed({
                                        val intent = Intent(this@CabPopupActivity, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        finish()
                                    }, 2500)
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    } else {
                        layout_accept.isClickable = true
                        layout_accept.isEnabled = true
                        Common.showHttpErrorMessage(this@CabPopupActivity, error.message)
                    }
                }
    }

    fun RejectBooking() {

        val DrvRejectUrl = Url.DriverRejectTripUrl + "?booking_id=" + Common.BookingId + "&driver_id=" + userPref.getString("id", "")
        Log.d("DrvRejectUrl", "DrvRejectUrl = $DrvRejectUrl")

        val builder = MaterialDialog.Builder(this@CabPopupActivity)
                .cancelable(false)
                .title(R.string.delete_trip)
                .content(R.string.are_you_sure_delete_trip)
                .negativeText(R.string.dialog_cancel)
                .positiveText(R.string.dialog_ok)
                .onNegative { dialog, which -> layout_decline.isEnabled = true }
                .onPositive { dialog, which ->
                    val loader = LoaderView(this@CabPopupActivity)
                    loader.show()
                    Ion.with(this@CabPopupActivity)
                            .load(DrvRejectUrl)
                            .asJsonObject()
                            .setCallback { error, result ->
                                // do stuff with the result or error
                                Log.d("load_trips result", "load_trips result = $result==$error")
                                loader.cancel()
                                customCountdownTimer!!.onFinish()

                                if (error == null) {
                                    try {
                                        val resObj = JSONObject(result.toString())

                                        if (resObj.getString("status") == "success") {

                                            Common.ActionClick = "reject"

                                            val booking_status = userPref.edit()
                                            booking_status.putString("booking_status", "Rejected")
                                            booking_status.commit()

                                            val intent = Intent(this@CabPopupActivity, DriverTripActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                            finish()

                                        } else if (resObj.getString("status") == "false") {
                                            Common.showMkError(this@CabPopupActivity, resObj.getString("error code").toString())

                                            if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {

                                                val editor = userPref.edit()
                                                editor.clear()
                                                editor.commit()

                                                Handler().postDelayed({
                                                    val intent = Intent(this@CabPopupActivity, MainActivity::class.java)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                    startActivity(intent)
                                                    finish()
                                                }, 2500)
                                            }
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }


                                } else {
                                    Common.showHttpErrorMessage(this@CabPopupActivity, error.message)
                                }
                            }
                }

        val dialog = builder.build()
        dialog.show()

    }

    fun DriverPopup() {

        try {

            //Log.d("dialog dataArray","dialog pickup location = "+ URLDecoder.decode(booking_data.getString("pickup"),"UTF-8"));

            txt_address_val.text = URLDecoder.decode(booking_data.getString("pickup"), "UTF-8")

            val dataArray = JSONArray(booking_data.getString("data"))
            Log.d("dialog dataArray", "dialog dataArray = " + dataArray.length())
            for (di in 0 until dataArray.length()) {
                val dataObj = dataArray.getJSONObject(di)
                val Lotlon = dataObj.getString("loc")
                //JSONArray LotLanArray = new JSONArray(dataObj.getString("loc"));
                val SplLotlon = Lotlon.split("\\,".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                val DrvLat = SplLotlon[0].replace("[", "")
                val DrvLng = SplLotlon[1].replace("]", "")
                val UserLarLng = LatLng(java.lang.Double.parseDouble(DrvLat), java.lang.Double.parseDouble(DrvLng))

                Log.d("Lotlon", "dialog Lotlon = $DrvLng==$DrvLat")

                mMap!!.addMarker(MarkerOptions().position(UserLarLng)
                        .title(txt_address_val.text.toString()))
                val cameraPosition = CameraPosition.Builder()
                        .target(UserLarLng)      // Sets the center of the map to location user
                        .zoom(10f)                   // Sets the zoom
                        .build()                   // Creates a CameraPosition from the builder
                mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            val EndTime = URLDecoder.decode(booking_data.getString("end_time"), "UTF-8")
            val StartTime = URLDecoder.decode(booking_data.getString("start_time"), "UTF-8")
            Log.d("Lotlon", "dialog StartTime = $StartTime==$EndTime")

            try {
                val date1 = simpleDateFormat.parse(EndTime)
                val date2 = simpleDateFormat.parse(StartTime)
                accpet_time = date1.time - date2.time

                Log.d("different", "different = $accpet_time")

            } catch (e: ParseException) {
                e.printStackTrace()
            }

            customCountdownTimer = Common.acceptRejectTimer(this@CabPopupActivity, timmer_progress, accpet_time, minutes_value, "cabpopupActivity")
            customCountdownTimer!!.start()

            Common.BookingId = booking_data.getString("booking_id")

        } catch (e: JSONException) {
            e.printStackTrace()
            Log.d("Error", "Error one" + e.message)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Log.d("Error", "Error one" + e.message)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onResume() {
        customMap.onResume()
        super.onResume()
    }

    override fun onPause() {
        customMap.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        customMap.onLowMemory()
        super.onLowMemory()
    }

    public override fun onDestroy() {
        customMap.onDestroy()
        super.onDestroy()
        if (customCountdownTimer != null)
            customCountdownTimer!!.onFinish()
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}
