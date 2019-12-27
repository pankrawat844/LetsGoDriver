package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.afollestad.materialdialogs.MaterialDialog
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.squareup.picasso.Picasso
import com.driver.utils.Common
import com.driver.utils.DriverAllTripFeed

import org.json.JSONException
import org.json.JSONObject

import java.text.ParseException
import java.text.SimpleDateFormat

class DriverTripDetailActivity : AppCompatActivity() {

    internal var layout_back_arrow: RelativeLayout? = null

    internal var img_user_image: ImageView? = null

    internal var txt_booking_detail: TextView? = null
    lateinit var txt_payment_detial: TextView
    lateinit var txt_vehicle_detail: TextView
    lateinit var txt_to: TextView
    internal var txt_booking_id: TextView? = null
    internal var txt_booking_id_val: TextView? = null
    internal var txt_pickup_point: TextView? = null
    internal var txt_pickup_point_val: TextView? = null
    internal var txt_booking_date: TextView? = null
    internal var txt_drop_point: TextView? = null
    internal var txt_drop_point_val: TextView? = null
    internal var txt_user_name: TextView? = null
    internal var txt_user_email: TextView? = null
    internal var txt_mobile_num: TextView? = null
    internal var txt_distance: TextView? = null
    internal var txt_distance_val: TextView? = null
    internal var txt_distance_km: TextView? = null
    internal var txt_total_price: TextView? = null
    internal var txt_total_price_dol: TextView? = null
    internal var txt_total_price_val: TextView? = null
    lateinit var txt_payment_type: TextView
    lateinit var txt_payment_type_val: TextView
    lateinit var txt_approx_time: TextView
    lateinit var txt_approx_time_val: TextView

    internal var layout_accepted: LinearLayout? = null
    internal var layout_arrived: RelativeLayout? = null
    internal var layout_begin_trip: RelativeLayout? = null
    internal var layout_user_call: RelativeLayout? = null
    internal var layout_finished: RelativeLayout? = null

    lateinit var UserMobileNu: String

    lateinit var OpenSans_Regular: Typeface
    lateinit var Roboto_Regular: Typeface
    lateinit var Roboto_Medium: Typeface
    lateinit var Roboto_Bold: Typeface
    lateinit var OpenSans_Semibold: Typeface

    lateinit var userPref: SharedPreferences

    lateinit var loader: LoaderView
    internal var driverAllTripFeed: DriverAllTripFeed? = null
    internal var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_trip_detail)

        userPref = PreferenceManager.getDefaultSharedPreferences(this@DriverTripDetailActivity)

        position = intent.getIntExtra("position", 0)

        loader = LoaderView(this@DriverTripDetailActivity)

        OpenSans_Regular = Typeface.createFromAsset(assets, resources.getString(R.string.font_regular_opensans))
        Roboto_Regular = Typeface.createFromAsset(assets, resources.getString(R.string.font_regular_roboto))
        Roboto_Medium = Typeface.createFromAsset(assets, resources.getString(R.string.font_medium_roboto))
        Roboto_Bold = Typeface.createFromAsset(assets, resources.getString(R.string.font_bold_roboto))
        OpenSans_Semibold = Typeface.createFromAsset(assets, resources.getString(R.string.font_semibold_opensans))

        driverAllTripFeed = Common.driverAllTripFeed

        layout_back_arrow = findViewById(R.id.layout_back_arrow) as RelativeLayout
        layout_back_arrow!!.setOnClickListener {
            val di = Intent()
            di.putExtra("position", position)
            di.putExtra("status", driverAllTripFeed!!.status)
            di.putExtra("driver_flage", driverAllTripFeed!!.driverFlag)
            setResult(1, di)
            finish()
        }

        txt_booking_detail = findViewById(R.id.txt_booking_detail) as TextView
        txt_booking_id = findViewById(R.id.txt_booking_id) as TextView
        txt_booking_id_val = findViewById(R.id.txt_booking_id_val) as TextView
        txt_pickup_point = findViewById(R.id.txt_pickup_point) as TextView
        txt_pickup_point_val = findViewById(R.id.txt_pickup_point_val) as TextView
        txt_booking_date = findViewById(R.id.txt_booking_date) as TextView
        txt_drop_point = findViewById(R.id.txt_booking_date) as TextView
        txt_drop_point_val = findViewById(R.id.txt_drop_point_val) as TextView
        txt_user_name = findViewById(R.id.txt_user_name) as TextView
        txt_user_email = findViewById(R.id.txt_user_email) as TextView
        txt_mobile_num = findViewById(R.id.txt_mobile_num) as TextView
        txt_distance = findViewById(R.id.txt_distance) as TextView
        txt_distance_val = findViewById(R.id.txt_distance_val) as TextView
        txt_distance_km = findViewById(R.id.txt_distance_km) as TextView
        txt_total_price = findViewById(R.id.txt_total_price) as TextView
        txt_total_price_dol = findViewById(R.id.txt_total_price_dol) as TextView
        txt_total_price_val = findViewById(R.id.txt_total_price_val) as TextView
        txt_payment_type = findViewById(R.id.txt_payment_type) as TextView
        txt_payment_type_val = findViewById(R.id.txt_payment_type_val) as TextView
        txt_approx_time = findViewById(R.id.txt_approx_time) as TextView
        txt_approx_time_val = findViewById(R.id.txt_approx_time_val) as TextView
        txt_payment_detial = findViewById(R.id.txt_payment_detial) as TextView
        txt_vehicle_detail = findViewById(R.id.txt_vehicle_detail) as TextView
        txt_to = findViewById(R.id.txt_to) as TextView

        img_user_image = findViewById(R.id.img_user_image) as ImageView

        txt_booking_detail!!.typeface = OpenSans_Regular

        txt_booking_id!!.typeface = Roboto_Regular
        txt_pickup_point!!.typeface = Roboto_Regular
        txt_drop_point!!.typeface = Roboto_Regular
        txt_distance_km!!.typeface = Roboto_Regular
        txt_total_price_dol!!.typeface = Roboto_Regular
        txt_total_price_dol!!.text = userPref.getString("currency", "")

        txt_user_name!!.typeface = Roboto_Regular
        txt_user_email!!.typeface = Roboto_Regular
        txt_mobile_num!!.typeface = Roboto_Regular

        txt_pickup_point_val!!.typeface = OpenSans_Regular
        txt_booking_date!!.typeface = OpenSans_Regular
        txt_drop_point_val!!.typeface = OpenSans_Regular
        txt_distance!!.typeface = OpenSans_Regular
        txt_distance_val!!.typeface = OpenSans_Regular
        txt_total_price!!.typeface = OpenSans_Regular
        txt_total_price_val!!.typeface = OpenSans_Regular
        txt_payment_type.typeface = OpenSans_Regular
        txt_payment_type_val.typeface = OpenSans_Regular
        txt_approx_time.typeface = OpenSans_Regular
        txt_approx_time_val.typeface = OpenSans_Regular
        txt_payment_detial.typeface = Roboto_Bold
        txt_vehicle_detail.typeface = Roboto_Bold
        txt_to.typeface = Roboto_Bold

        txt_booking_id_val!!.text = driverAllTripFeed!!.id
        txt_pickup_point_val!!.text = driverAllTripFeed!!.pickupArea
        txt_drop_point_val!!.text = driverAllTripFeed!!.dropArea
        txt_distance_val!!.text = driverAllTripFeed!!.km
        txt_total_price_val!!.text = driverAllTripFeed!!.amount

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var pickup_date_time = ""
        try {
            val parceDate = simpleDateFormat.parse(driverAllTripFeed!!.pickupDateTime)
            val parceDateFormat = SimpleDateFormat("h:mm a,dd,MMM yyyy")
            pickup_date_time = parceDateFormat.format(parceDate.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        txt_booking_date!!.text = pickup_date_time

        txt_approx_time_val.text = driverAllTripFeed!!.approxTime

        if (driverAllTripFeed!!.getuserDetail() != null && driverAllTripFeed!!.getuserDetail() != "") {
            try {
                val userObj = JSONObject(driverAllTripFeed!!.getuserDetail())

                if (userObj.getString("facebook_id") != "" && userObj.getString("image") == "") {
                    val facebookImage = Url.FacebookImgUrl + userObj.getString("facebook_id").toString() + "/picture?type=large"
                    Log.d("facebookImage", "facebookImage = $facebookImage")
                    Picasso.with(this@DriverTripDetailActivity)
                            .load(facebookImage)
                            .placeholder(R.drawable.user_photo)
                            .resize(200, 200)
                            .transform(CircleTransformation(this@DriverTripDetailActivity))
                            .into(img_user_image)
                } else {
                    Picasso.with(this@DriverTripDetailActivity)
                            .load(Uri.parse(Url.userImageUrl + userObj.getString("image")))
                            .placeholder(R.drawable.user_photo)
                            .transform(CircleTransformation(this@DriverTripDetailActivity))
                            .into(img_user_image)
                }

                txt_user_name!!.text = userObj.getString("username")
                txt_user_email!!.text = userObj.getString("email")
                UserMobileNu = userObj.getString("mobile")
                txt_mobile_num!!.text = UserMobileNu

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        layout_accepted = findViewById(R.id.layout_accepted) as LinearLayout

        layout_arrived = findViewById(R.id.layout_arrived) as RelativeLayout
        layout_arrived!!.setOnClickListener {
            val builder = MaterialDialog.Builder(this@DriverTripDetailActivity)
                    .content(R.string.arrived_message)
                    .negativeText(R.string.dialog_cancel)
                    .positiveText(R.string.dialog_ok)
                    .onPositive { dialog, which ->
                        dialog.cancel()
                        loader.show()
                        val ArrivedUrl = Url.DriverArrivedTripUrl + "?booking_id=" + driverAllTripFeed!!.id + "&driver_id=" + userPref.getString("id", "")
                        Log.d("ArrivedUrl", "ArrivedUrl = $ArrivedUrl")
                        DriverCall(ArrivedUrl, "7")
                    }

            val dialog = builder.build()
            dialog.show()
        }
        layout_begin_trip = findViewById(R.id.layout_begin_trip) as RelativeLayout
        layout_begin_trip!!.setOnClickListener {
            val builder = MaterialDialog.Builder(this@DriverTripDetailActivity)
                    .content(R.string.begin_message)
                    .negativeText(R.string.dialog_cancel)
                    .positiveText(R.string.dialog_ok)
                    .onPositive { dialog, which ->
                        dialog.cancel()
                        Common.OnTripTime = Common.currentTime
                        driverAllTripFeed!!.startRideTime = Common.currentTime

                        loader.show()
                        val BeginUrl = Url.DriverOnTripUrl + "?booking_id=" + driverAllTripFeed!!.id + "&driver_id=" + userPref.getString("id", "")
                        Log.d("ArrivedUrl", "ArrivedUrl = $BeginUrl")
                        DriverCall(BeginUrl, "8")
                    }

            val dialog = builder.build()
            dialog.show()
        }

        layout_user_call = findViewById(R.id.layout_user_call) as RelativeLayout
        layout_user_call!!.setOnClickListener {
            Handler().postDelayed({
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:$UserMobileNu")
                startActivity(callIntent)
            }, 100)
        }

        layout_finished = findViewById(R.id.layout_finished) as RelativeLayout
        layout_finished!!.setOnClickListener {
            driverAllTripFeed!!.endRideTime = Common.currentTime
            Common.FinishedTripTime = Common.currentTime
            Common.driverAllTripFeed = driverAllTripFeed
            val fi = Intent(this@DriverTripDetailActivity, FinishTripActivity::class.java)
            fi.putExtra("position", position)
            startActivityForResult(fi, 1)
        }

        if (driverAllTripFeed!!.status == "1") {
            layout_accepted!!.visibility = View.GONE
        } else if (driverAllTripFeed!!.status == "3") {
            layout_accepted!!.visibility = View.VISIBLE
            layout_arrived!!.visibility = View.VISIBLE
            layout_user_call!!.visibility = View.VISIBLE
            // layout_begin_trip.setVisibility(View.GONE);
        } else if (driverAllTripFeed!!.status == "7") {
            layout_accepted!!.visibility = View.VISIBLE
            layout_arrived!!.visibility = View.GONE
            layout_user_call!!.visibility = View.VISIBLE
            layout_begin_trip!!.visibility = View.VISIBLE
        } else if (driverAllTripFeed!!.status == "8") {
            layout_accepted!!.visibility = View.GONE
            layout_arrived!!.visibility = View.GONE
            layout_user_call!!.visibility = View.GONE
            layout_begin_trip!!.visibility = View.GONE
            layout_finished!!.visibility = View.VISIBLE
        } else if (driverAllTripFeed!!.status == "9") {
            layout_accepted!!.visibility = View.GONE
            layout_arrived!!.visibility = View.GONE
            layout_user_call!!.visibility = View.GONE
            layout_begin_trip!!.visibility = View.GONE
            layout_finished!!.visibility = View.GONE
        } else if (driverAllTripFeed!!.status == "6") {
            layout_accepted!!.visibility = View.GONE
            layout_arrived!!.visibility = View.GONE
            layout_user_call!!.visibility = View.GONE
            layout_begin_trip!!.visibility = View.GONE
            layout_finished!!.visibility = View.GONE
        }
    }

    /*Driver status change call*/
    fun DriverCall(callFun: String, DriverStatus: String) {

        Ion.with(this@DriverTripDetailActivity)
                .load(callFun)
                .asJsonObject()
                .setCallback(FutureCallback { e, result ->
                    loader.loaderObject().stop()
                    loader.loaderDismiss()
                    // do stuff with the result or error
                    if (e != null) {
                        Toast.makeText(this@DriverTripDetailActivity, "Login Error$e", Toast.LENGTH_LONG).show()
                        return@FutureCallback
                    }
                    try {
                        val jsonObject = JSONObject(result.toString())
                        if (jsonObject.has("status") && jsonObject.getString("status") == "success") {

                            driverAllTripFeed!!.status = DriverStatus
                            if (DriverStatus == "7") {

                                val booking_status = userPref.edit()
                                booking_status.putString("booking_status", "i have arrived")
                                booking_status.commit()

                                layout_accepted!!.visibility = View.VISIBLE
                                layout_arrived!!.visibility = View.GONE
                                layout_user_call!!.visibility = View.VISIBLE
                                layout_begin_trip!!.visibility = View.VISIBLE
                            } else if (DriverStatus == "8") {

                                val booking_status = userPref.edit()
                                booking_status.putString("booking_status", "begin trip")
                                booking_status.commit()

                                layout_accepted!!.visibility = View.GONE
                                layout_arrived!!.visibility = View.GONE
                                layout_user_call!!.visibility = View.GONE
                                layout_begin_trip!!.visibility = View.GONE
                                layout_finished!!.visibility = View.VISIBLE
                                driverAllTripFeed!!.startRideTime = Common.currentTime
                            }
                        } else if (jsonObject.getString("status") == "false") {

                            Common.showMkError(this@DriverTripDetailActivity, jsonObject.getString("error code").toString())

                            if (jsonObject.has("Isactive") && jsonObject.getString("Isactive") == "Inactive") {

                                val editor = userPref.edit()
                                editor.clear()
                                editor.commit()

                                Handler().postDelayed({
                                    val intent = Intent(this@DriverTripDetailActivity, LoginActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finish()
                                }, 2500)
                            }
                        }
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("requestCode", "DETAIL_REQUEST DriverDetail = $requestCode==$resultCode")
        if (requestCode == 1) {
            if (data != null) {
                val di = Intent()
                di.putExtra("position", position)
                di.putExtra("status", driverAllTripFeed!!.status)
                di.putExtra("driver_flage", driverAllTripFeed!!.driverFlag)
                setResult(1, di)
                finish()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        layout_back_arrow = null
        img_user_image = null
        txt_booking_detail = null
        txt_booking_id = null
        txt_booking_id_val = null
        txt_pickup_point = null
        txt_pickup_point_val = null
        txt_booking_date = null
        txt_drop_point = null
        txt_drop_point_val = null
        txt_user_name = null
        txt_user_email = null
        txt_mobile_num = null
        txt_distance = null
        txt_distance_val = null
        txt_distance_km = null
        txt_total_price = null
        txt_total_price_dol = null
        txt_total_price_val = null
        layout_accepted = null
        layout_arrived = null
        layout_begin_trip = null
        layout_user_call = null
        layout_finished = null
    }
}
