package com.driver

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import com.koushikdutta.ion.Ion
import com.driver.Adapter.DriverAllTripAdapter
import com.driver.utils.Common
import com.driver.utils.DriverAllTripFeed

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.util.ArrayList

import cz.msebera.android.httpclient.client.ClientProtocolException
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.entity.StringEntity
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient
import cz.msebera.android.httpclient.params.BasicHttpParams
import cz.msebera.android.httpclient.params.HttpConnectionParams
import cz.msebera.android.httpclient.protocol.HTTP

class DriverTripActivity : AppCompatActivity(), DriverAllTripAdapter.OnAllTripClickListener {


    lateinit var slidingMenu: SlidingMenu

    lateinit var layout_slidemenu: RelativeLayout
    lateinit var txt_all_trip: TextView
    lateinit var layout_filter: RelativeLayout
    lateinit var recycle_all_trip: RecyclerView
    lateinit var swipe_refresh_layout: SwipeRefreshLayout
    lateinit var layout_background: RelativeLayout
    lateinit var layout_no_recourd_found: RelativeLayout
    lateinit var layout_recycleview: LinearLayout

    lateinit var OpenSans_Bold: Typeface
    lateinit var OpenSans_Regular: Typeface
    private var AllTripLayoutManager: RecyclerView.LayoutManager? = null

    lateinit var gps: GPSTracker
     var latitude: Double = 0.toDouble()
     var longitude: Double = 0.toDouble()
    private var mSocket: Socket? = null
    lateinit var driver_status: Switch
    lateinit var switch_driver_status: TextView

     var DriverAllTripArray: ArrayList<DriverAllTripFeed>? = null
    lateinit var DrvAllTripAdapter: DriverAllTripAdapter

     var common = Common()
    lateinit var loader: LoaderView
    lateinit var filterDialog: Dialog
     var FilterString = ""

    lateinit var chk_all: CheckBox
    lateinit var chk_pen_book: CheckBox
    lateinit var chk_acp_book: CheckBox
    lateinit var chk_drv_can: CheckBox
    lateinit var chk_com_book: CheckBox
     var checkAllClick = false

    lateinit var userPref: SharedPreferences

     var receiver: BroadcastReceiver? = null
     var savedInstState: Bundle? = null

    /**
     * Listener for socket connection error.. listener registered at the time of socket connection
     */
    private val onConnectError = Emitter.Listener {
        runOnUiThread {
            if (mSocket != null)
                if (mSocket!!.connected() == false) {
                    Log.d("connected", "connected error= " + mSocket!!.connected())
                    //socketConnection();
                } else {
                    Log.d("connected", "connected three= " + mSocket!!.connected())
                }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstState = savedInstanceState
        setContentView(R.layout.activity_driver_trip)

        userPref = PreferenceManager.getDefaultSharedPreferences(this@DriverTripActivity)

        layout_slidemenu = findViewById(R.id.layout_slidemenu) as RelativeLayout
        txt_all_trip = findViewById(R.id.txt_all_trip) as TextView
        layout_filter = findViewById(R.id.layout_filter) as RelativeLayout
        recycle_all_trip = findViewById(R.id.recycle_all_trip) as RecyclerView
        swipe_refresh_layout = findViewById(R.id.swipe_refresh_layout) as SwipeRefreshLayout
        layout_background = findViewById(R.id.layout_background) as RelativeLayout
        layout_no_recourd_found = findViewById(R.id.layout_no_recourd_found) as RelativeLayout
        layout_recycleview = findViewById(R.id.layout_recycleview) as LinearLayout

        loader = LoaderView(this@DriverTripActivity)

        OpenSans_Bold = Typeface.createFromAsset(assets, getString(R.string.font_bold_opensans))
        OpenSans_Regular = Typeface.createFromAsset(assets, getString(R.string.font_regular_opensans))

        txt_all_trip.typeface = OpenSans_Bold

        slidingMenu = SlidingMenu(this)
        slidingMenu.mode = SlidingMenu.LEFT
        slidingMenu.touchModeAbove = SlidingMenu.TOUCHMODE_FULLSCREEN
        slidingMenu.setBehindOffsetRes(R.dimen.slide_menu_width)
        slidingMenu.setFadeDegree(0.20f)
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT)
        slidingMenu.setMenu(R.layout.left_menu)

        gps = GPSTracker(this@DriverTripActivity)
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude()
            longitude = gps.getLongitude()
        } else {
            gps.showSettingsAlert()
        }

        common.SlideMenuDesign(slidingMenu, this@DriverTripActivity, "my trip")

        driver_status = slidingMenu.findViewById(R.id.switch_driver_status) as Switch
        switch_driver_status = slidingMenu.findViewById(R.id.txt_driver_status) as TextView
        if (mSocket != null && mSocket!!.connected() || Common.socket != null && Common.socket!!.connected()) {
            driver_status.isChecked = true
            switch_driver_status.text = resources.getString(R.string.on_duty)
        } else {
            driver_status.isChecked = false
            switch_driver_status.text = resources.getString(R.string.off_duty)
        }
        driver_status.setOnCheckedChangeListener { compoundButton, b ->
            Log.d("is Checked", "is Checked = " + b + "==" + userPref.getBoolean("isBookingAccept", false))
            if (b) {
                switch_driver_status.text = resources.getString(R.string.on_duty)
                if (gps.canGetLocation()) {
                    try {
                        mSocket = IO.socket(SERVER_IP)
                        mSocket!!.emit(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_ERROR, onConnectError)
                        mSocket!!.connect()
                        Common.socket = mSocket
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        Log.d("connected ", "connected error = " + e.message)
                    }

                    Common.socketFunction(this@DriverTripActivity, mSocket!!, driver_status, latitude, longitude, common, userPref)
                } else {
                    switch_driver_status.text = resources.getString(R.string.off_duty)
                    driver_status.isChecked = false
                    gps.showSettingsAlert()

                    common.changeLocationSocket(this@DriverTripActivity, driver_status)
                }
            } else {
                switch_driver_status.text = resources.getString(R.string.off_duty)
                try {
                    val locAry = JSONArray()
                    locAry.put(latitude)
                    locAry.put(longitude)
                    val emitobj = JSONObject()
                    emitobj.put("coords", locAry)
                    emitobj.put("driver_name", userPref.getString("user_name", ""))
                    emitobj.put("driver_id", userPref.getString("id", ""))
                    emitobj.put("driver_status", "0")
                    emitobj.put("car_type", userPref.getString("car_type", ""))
                    emitobj.put("isdevice", "1")
                    emitobj.put("booking_status", userPref.getString("booking_status", ""))
                    Log.d("emitobj", "emitobj = $emitobj")

                    Common.socket!!.emit("Create Driver Data", emitobj)
                    Common.socket!!.disconnect()


                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                common.changeLocationSocket(this@DriverTripActivity, driver_status)
            }
        }

        recycle_all_trip = findViewById(R.id.recycle_all_trip) as RecyclerView
        swipe_refresh_layout = findViewById(R.id.swipe_refresh_layout) as SwipeRefreshLayout
        layout_recycleview = findViewById(R.id.layout_recycleview) as LinearLayout

        AllTripLayoutManager = LinearLayoutManager(this)
        recycle_all_trip.layoutManager = AllTripLayoutManager


        layout_slidemenu.setOnClickListener { slidingMenu.toggle() }

        swipe_refresh_layout.setOnRefreshListener {
            if (Common.isNetworkAvailable(this@DriverTripActivity)) {
                recycle_all_trip.isClickable = false
                recycle_all_trip.isEnabled = false
                var allFilter = false
                if (userPref.getBoolean("setFilter", false) == true) {
                    if (userPref.getInt("pending booking", 4) == 0) {
                        FilterString += 0.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("accepted booking", 4) == 1) {
                        FilterString += 1.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("driver cancel", 4) == 2) {
                        FilterString += 2.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("completed booking", 4) == 3) {
                        FilterString += 3.toString() + ","
                        allFilter = true
                    }
                    if (FilterString.length > 0)
                        FilterString = FilterString.substring(0, FilterString.length - 1)

                    val clickfilter = userPref.edit()
                    clickfilter.putBoolean("setFilter", allFilter)
                    clickfilter.commit()

                    if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
                        FilterString = ""
                    }
                    FilterAllDriverTrips(0, "", true)
                    FilterString = ""

                } else {
                    getDriverAllTrip(0, true)
                }
            } else {
                recycle_all_trip.isClickable = true
                recycle_all_trip.isEnabled = true
                //Network is not available
                Common.showInternetInfo(this@DriverTripActivity, "Network is not available")
            }
        }

        loader.show()
        //        if(!userPref.getString("id_device_token","").equals("1")) {
        //            if(Utility.isNetworkAvailable(DriverTripActivity.this)) {
        //                new CallUnsubscribe(DriverTripActivity.this, Common.device_token).execute();
        //            }else {
        //                Utility.showInternetInfo(DriverTripActivity.this, "Network is not available");
        //            }
        //        }else{
        Handler().postDelayed({
            if (Common.isNetworkAvailable(this@DriverTripActivity)) {
                var allFilter = false
                if (userPref.getBoolean("setFilter", false) == true) {
                    if (userPref.getInt("pending booking", 4) == 0) {
                        FilterString += 0.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("accepted booking", 4) == 1) {
                        FilterString += 1.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("driver cancel", 4) == 2) {
                        FilterString += 2.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("completed booking", 4) == 3) {
                        FilterString += 3.toString() + ","
                        allFilter = true
                    }
                    if (FilterString.length > 0)
                        FilterString = FilterString.substring(0, FilterString.length - 1)

                    val clickfilter = userPref.edit()
                    clickfilter.putBoolean("setFilter", allFilter)
                    clickfilter.commit()

                    if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
                        FilterString = ""
                    }
                    FilterAllDriverTrips(0, "filter", false)
                    FilterString = ""

                } else {
                    getDriverAllTrip(0, false)
                }
            } else {
                Common.showInternetInfo(this@DriverTripActivity, "Network is not available")
                swipe_refresh_layout.isEnabled = false
            }
        }, 500)
        //        }


        /*Filter Dialog Start*/

        filterDialog = Dialog(this@DriverTripActivity, R.style.DialogSlideAnim)
        filterDialog.setContentView(R.layout.all_trip_filter_dialog)

        layout_filter.setOnClickListener {
            layout_background.visibility = View.VISIBLE
            filterDialog.show()
        }

        val txt_all = filterDialog.findViewById(R.id.txt_all) as TextView
        txt_all.typeface = OpenSans_Regular
        val txt_pending_booking = filterDialog.findViewById(R.id.txt_pending_booking) as TextView
        txt_pending_booking.typeface = OpenSans_Regular
        val txt_accept_booking = filterDialog.findViewById(R.id.txt_accept_booking) as TextView
        txt_accept_booking.typeface = OpenSans_Regular
        val txt_drv_can = filterDialog.findViewById(R.id.txt_drv_can) as TextView
        txt_drv_can.typeface = OpenSans_Regular
        val txt_com_book = filterDialog.findViewById(R.id.txt_com_book) as TextView
        txt_com_book.typeface = OpenSans_Regular

        chk_all = filterDialog.findViewById(R.id.chk_all) as CheckBox
        val layout_all_check = filterDialog.findViewById(R.id.layout_all_check) as RelativeLayout
        CheckBoxChecked(layout_all_check, chk_all, "all")

        chk_pen_book = filterDialog.findViewById(R.id.chk_pen_book) as CheckBox
        val layour_pen_book_check = filterDialog.findViewById(R.id.layour_pen_book_check) as RelativeLayout
        CheckBoxChecked(layour_pen_book_check, chk_pen_book, "pending booking")

        chk_acp_book = filterDialog.findViewById(R.id.chk_acp_book) as CheckBox
        val layout_acp_book_check = filterDialog.findViewById(R.id.layout_acp_book_check) as RelativeLayout
        CheckBoxChecked(layout_acp_book_check, chk_acp_book, "accept booking")

        chk_com_book = filterDialog.findViewById(R.id.chk_com_book) as CheckBox
        val layout_com_book_check = filterDialog.findViewById(R.id.layout_com_book_check) as RelativeLayout
        CheckBoxChecked(layout_com_book_check, chk_com_book, "completed booking")

        chk_drv_can = filterDialog.findViewById(R.id.chk_drv_can) as CheckBox
        val layout_drv_reject_check = filterDialog.findViewById(R.id.layout_drv_reject_check) as RelativeLayout
        CheckBoxChecked(layout_drv_reject_check, chk_drv_can, "driver cancel")

        Log.d("checkbox checked", "checkbox checked = " + userPref.getInt("pending booking", 4) + "==" + userPref.getInt("accepted booking", 4) + "==" + userPref.getInt("driver cancel", 4) + "==" + userPref.getInt("completed booking", 4))
        if (userPref.getInt("pending booking", 4) == 0)
            chk_pen_book.isChecked = true
        if (userPref.getInt("accepted booking", 4) == 1)
            chk_acp_book.isChecked = true
        if (userPref.getInt("driver cancel", 4) == 2)
            chk_drv_can.isChecked = true
        if (userPref.getInt("completed booking", 4) == 3)
            chk_com_book.isChecked = true

        if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
            chk_all.isChecked = true
        }

        val img_close_icon = filterDialog.findViewById(R.id.img_close_icon) as ImageView
        img_close_icon.setOnClickListener {
            layout_background.visibility = View.GONE
            filterDialog.cancel()
        }

        val img_calcel = filterDialog.findViewById(R.id.img_calcel) as ImageView
        img_calcel.setOnClickListener {
            layout_background.visibility = View.GONE
            filterDialog.cancel()
        }

        val img_apply = filterDialog.findViewById(R.id.img_apply) as ImageView
        img_apply.setOnClickListener {
            layout_background.visibility = View.GONE
            filterDialog.cancel()
            var setFilter = false

            val pending_booking = userPref.edit()
            if (chk_pen_book.isChecked) {
                FilterString += 0.toString() + ","
                pending_booking.putInt("pending booking", 0)
                setFilter = true
            } else
                pending_booking.putInt("pending booking", 4)
            pending_booking.commit()

            val accepted_booking = userPref.edit()
            if (chk_acp_book.isChecked) {
                FilterString += 1.toString() + ","
                accepted_booking.putInt("accepted booking", 1)
                setFilter = true
            } else {
                accepted_booking.putInt("accepted booking", 4)
            }
            accepted_booking.commit()

            val driver_cancel = userPref.edit()
            if (chk_drv_can.isChecked) {
                FilterString += 2.toString() + ","
                driver_cancel.putInt("driver cancel", 2)
                setFilter = true
            } else {
                driver_cancel.putInt("driver cancel", 4)
            }
            driver_cancel.commit()

            val completed_booking = userPref.edit()
            if (chk_com_book.isChecked) {
                FilterString += 3.toString() + ","
                completed_booking.putInt("completed booking", 3)
                setFilter = true
            } else {
                completed_booking.putInt("completed booking", 4)
            }
            completed_booking.commit()

            Log.d("FilterString", "FilterString = $FilterString")
            if (FilterString.length > 0)
                FilterString = FilterString.substring(0, FilterString.length - 1)

            Log.d("FilterString", "FilterString = $FilterString")
            val clickfilter = userPref.edit()
            clickfilter.putBoolean("setFilter", setFilter)
            clickfilter.commit()

            if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
                FilterString = ""
            }

            loader.show()
            FilterAllDriverTrips(0, "filter", true)

            FilterString = ""
        }
    }

    override fun AcceptCabBookin(position: Int) {
        if (DriverAllTripArray!!.size > 0) {
            loader.show()
            val driverAllTripFeed = DriverAllTripArray!![position]
            val DrvBookingUrl = Url.DriverAcceptTripUrl + "?booking_id=" + driverAllTripFeed.id + "&driver_id=" + userPref.getString("id", "")
            Log.d("DrvBookingUrl", "DrvBookingUrl =$DrvBookingUrl")
            Ion.with(this@DriverTripActivity)
                    .load(DrvBookingUrl)
                    .asJsonObject()
                    .setCallback { error, result ->
                        // do stuff with the result or error
                        Log.d("trips accept result", "load_trips result = $result==$error")
                        loader.cancel()
                        if (error == null) {
                            try {
                                val resObj = JSONObject(result.toString())

                                if (resObj.getString("status") == "success") {

                                    val booking_status = userPref.edit()
                                    booking_status.putString("booking_status", "Accepted")
                                    booking_status.commit()

                                    driverAllTripFeed.driverFlag = "1"
                                    driverAllTripFeed.status = "3"
                                    DrvAllTripAdapter.notifyItemChanged(position)
                                } else if (resObj.getString("status") == "false") {
                                    Common.showMkError(this@DriverTripActivity, resObj.getString("error code").toString())

                                    if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {

                                        val editor = userPref.edit()
                                        editor.clear()
                                        editor.commit()

                                        Handler().postDelayed({
                                            val intent = Intent(this@DriverTripActivity, MainActivity::class.java)
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
                            Common.showHttpErrorMessage(this@DriverTripActivity, error.message)
                        }
                    }

        }
    }

    override fun RejectCabBookin(position: Int, timerStart: String) {
        if (DriverAllTripArray!!.size > 0) {
            loader.show()
            Log.d("position", "position = $position")
            val driverAllTripFeed = DriverAllTripArray!![position]
            val DrvRejectUrl = Url.DriverRejectTripUrl + "?booking_id=" + driverAllTripFeed.id + "&driver_id=" + userPref.getString("id", "")
            Log.d("DrvRejectUrl", "DrvRejectUrl = $DrvRejectUrl")
            if (timerStart == "timer reject") {
                Ion.with(this@DriverTripActivity)
                        .load(DrvRejectUrl)
                        .asJsonObject()
                        .setCallback { error, result ->
                            // do stuff with the result or error
                            Log.d("trips rejeect result", "load_trips result = $result==$error")
                            loader.cancel()


                            if (error == null) {

                                try {
                                    val resObj = JSONObject(result.toString())

                                    if (resObj.getString("status") == "success") {

                                        val booking_status = userPref.edit()
                                        booking_status.putString("booking_status", "Rejected")
                                        booking_status.commit()

                                        driverAllTripFeed.driverFlag = "2"
                                        driverAllTripFeed.status = "5"
                                        DrvAllTripAdapter.updateItems()
                                    } else if (resObj.getString("status") == "false") {
                                        Common.showMkError(this@DriverTripActivity, resObj.getString("error code").toString())

                                        if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {

                                            val editor = userPref.edit()
                                            editor.clear()
                                            editor.commit()

                                            Handler().postDelayed({
                                                val intent = Intent(this@DriverTripActivity, MainActivity::class.java)
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
                                Common.showHttpErrorMessage(this@DriverTripActivity, error.message)
                            }
                        }
            } else {
                Ion.with(this@DriverTripActivity)
                        .load(DrvRejectUrl)
                        .asJsonObject()
                        .setCallback { error, result ->
                            // do stuff with the result or error
                            Log.d("trips reject result", "load_trips result = $result==$error")
                            loader.cancel()
                            if (error == null) {
                                driverAllTripFeed.driverFlag = "2"
                                driverAllTripFeed.status = "5"
                                DrvAllTripAdapter.updateItems()
                            } else {
                                Common.showHttpErrorMessage(this@DriverTripActivity, error.message)
                            }
                        }
            }
        }
    }

    override fun scrollToLoad(position: Int) {
        var allFilter = false
        if (userPref.getBoolean("setFilter", false) == true) {
            if (userPref.getInt("pending booking", 4) == 0) {
                FilterString += 0.toString() + ","
                allFilter = true
            }
            if (userPref.getInt("accepted booking", 4) == 1) {
                FilterString += 1.toString() + ","
                allFilter = true
            }
            if (userPref.getInt("driver cancel", 4) == 2) {
                FilterString += 2.toString() + ","
                allFilter = true
            }
            if (userPref.getInt("completed booking", 4) == 3) {
                FilterString += 3.toString() + ","
                allFilter = true
            }
            if (FilterString.length > 0)
                FilterString = FilterString.substring(0, FilterString.length - 1)

            val clickfilter = userPref.edit()
            clickfilter.putBoolean("setFilter", allFilter)
            clickfilter.commit()

            if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
                FilterString = ""
            }
            FilterAllDriverTrips(position + 1, "", true)
            FilterString = ""
        } else {
            getDriverAllTrip(position + 1, true)
        }
    }

    override fun GoTripDetail(position: Int) {
        if (DriverAllTripArray!!.size > 0) {
            Common.driverAllTripFeed = DriverAllTripArray!![position]
            val di = Intent(this@DriverTripActivity, DriverTripDetailActivity::class.java)
            //startActivity(di);
            di.putExtra("position", position)
            startActivityForResult(di, DETAIL_REQUEST)
        }

    }

    fun CheckBoxChecked(relativeLayout: RelativeLayout, checkBox: CheckBox, checkBoxValue: String) {

        checkBox.setOnClickListener {
            Log.d("checkAllClick", "checkAllClick = $checkAllClick==$checkAllClick")
            if (checkBoxValue == "all") {
                if (checkAllClick) {
                    chk_all.isChecked = false
                    chk_pen_book.isChecked = false
                    chk_com_book.isChecked = false
                    chk_drv_can.isChecked = false
                    chk_acp_book.isChecked = false
                    checkAllClick = false
                } else {
                    chk_all.isChecked = true
                    chk_pen_book.isChecked = true
                    chk_com_book.isChecked = true
                    chk_drv_can.isChecked = true
                    chk_acp_book.isChecked = true
                    checkAllClick = true
                }
            } else {
                if (chk_pen_book.isChecked && chk_com_book.isChecked && chk_drv_can.isChecked && chk_acp_book.isChecked) {
                    chk_all.isChecked = true
                    checkAllClick = true
                } else {
                    chk_all.isChecked = false
                    checkAllClick = false
                }

            }
        }

        relativeLayout.setOnClickListener {
            if (checkBox.isChecked)
                checkBox.isChecked = false
            else
                checkBox.isChecked = true
            Log.d("checkAllClick", "checkAllClick = $checkAllClick==$checkAllClick")
            if (checkBoxValue == "all") {
                if (checkAllClick) {
                    chk_all.isChecked = false
                    chk_pen_book.isChecked = false
                    chk_com_book.isChecked = false
                    chk_drv_can.isChecked = false
                    chk_acp_book.isChecked = false
                    checkAllClick = false
                } else {
                    chk_all.isChecked = true
                    chk_pen_book.isChecked = true
                    chk_com_book.isChecked = true
                    chk_drv_can.isChecked = true
                    chk_acp_book.isChecked = true
                    checkAllClick = true
                }
            } else {
                if (chk_pen_book.isChecked && chk_com_book.isChecked && chk_drv_can.isChecked && chk_acp_book.isChecked) {
                    chk_all.isChecked = true
                    checkAllClick = true
                } else {
                    chk_all.isChecked = false
                    checkAllClick = false
                }
            }
        }
    }

    fun FilterAllDriverTrips(offset: Int, filter: String, is_pull: Boolean) {
        if (offset == 0)
            DriverAllTripArray = ArrayList()

        val DriverFilterTripUrl = Url.DriverFilterTripUrl + "?filter=" + FilterString + "&off=" + offset + "&driver_id=" + userPref.getString("id", "bgfv")
        Log.d("DriverFilterTripUrl", "DriverFilterTripUrl = $DriverFilterTripUrl")
        Ion.with(this@DriverTripActivity)
                .load(DriverFilterTripUrl)
                .asJsonObject()
                .setCallback { error, result ->
                    // do stuff with the result or error
                    Log.d("trips filter result", "load_trips result = $result==$error")
                    loader.cancel()
                    recycle_all_trip.isClickable = true
                    recycle_all_trip.isEnabled = true
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
                                    DriverAllTripArray!!.add(allTripFeed)
                                }
                                Log.d("loadTripsUrl", "loadTripsUrl three= " + DriverAllTripArray!!.size)
                                if (DriverAllTripArray != null && DriverAllTripArray!!.size > 0) {
                                    if (offset == 0) {
                                        layout_recycleview.visibility = View.VISIBLE
                                        layout_no_recourd_found.visibility = View.GONE
                                        DrvAllTripAdapter = DriverAllTripAdapter(this@DriverTripActivity, DriverAllTripArray!!, false)
                                        recycle_all_trip.adapter = DrvAllTripAdapter
                                        DrvAllTripAdapter.setOnAllTripItemClickListener(this@DriverTripActivity)
                                        swipe_refresh_layout.isRefreshing = false
                                    }
                                    DrvAllTripAdapter.updateItems()
                                    swipe_refresh_layout.isEnabled = true
                                }
                            } else if (resObj.getString("status") == "false") {

                                Common.showMkError(this@DriverTripActivity, resObj.getString("error code").toString())

                                if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {

                                    val editor = userPref.edit()
                                    editor.clear()
                                    editor.commit()

                                    Handler().postDelayed({
                                        val intent = Intent(this@DriverTripActivity, LoginActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        finish()
                                    }, 2500)
                                }
                            } else {
                                if (offset == 0) {
                                    layout_recycleview.visibility = View.GONE
                                    layout_no_recourd_found.visibility = View.VISIBLE
                                } else {

                                    Toast.makeText(this@DriverTripActivity, resources.getString(R.string.data_not_found), Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    } else {
                        Common.showHttpErrorMessage(this@DriverTripActivity, error.message)
                    }
                }
    }

    fun getDriverAllTrip(offset: Int, is_pull: Boolean) {

        if (offset == 0) {
            DriverAllTripArray = ArrayList()
        }
        //String DrvBookingUrl = Url.DriverTripUrl+"?driver_id="+Utility.userDetails.getId()+"&off="+offset;
        val DrvBookingUrl = Url.DriverTripUrl + "?driver_id=" + userPref.getString("id", "") + "&off=" + offset
        Log.d("loadTripsUrl", "loadTripsUrl =$DrvBookingUrl==$offset")
        Ion.with(this@DriverTripActivity)
                .load(DrvBookingUrl)
                .asJsonObject()
                .setCallback { error, result ->
                    // do stuff with the result or error
                    Log.d("trips driver result", "load_trips result = $result==$error")
                    loader.cancel()
                    recycle_all_trip.isClickable = true
                    recycle_all_trip.isEnabled = true
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
                                    DriverAllTripArray!!.add(allTripFeed)
                                }
                                Log.d("loadTripsUrl", "loadTripsUrl three= " + DriverAllTripArray!!.size)
                                if (DriverAllTripArray != null && DriverAllTripArray!!.size > 0) {
                                    if (offset == 0) {
                                        layout_recycleview.visibility = View.VISIBLE
                                        layout_no_recourd_found.visibility = View.GONE
                                        Log.e("DriverAllTripArray",savedInstState.toString());
                                        DrvAllTripAdapter = DriverAllTripAdapter(this, DriverAllTripArray!!, is_pull)
                                        recycle_all_trip.adapter = DrvAllTripAdapter
                                        DrvAllTripAdapter.setOnAllTripItemClickListener(this@DriverTripActivity)
                                        DrvAllTripAdapter.updateItems()
                                        swipe_refresh_layout.isRefreshing = false
                                    } else {
                                        DrvAllTripAdapter.updateItems()
                                        swipe_refresh_layout.isRefreshing = false
                                    }
                                    swipe_refresh_layout.isEnabled = true
                                }
                            } else if (resObj.getString("status") == "false") {

                                Common.showMkError(this@DriverTripActivity, resObj.getString("error code").toString())

                                if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive")
                                {
                                    val editor = userPref.edit()
                                    editor.clear()
                                    editor.commit()
                                    Handler().postDelayed({
                                        val intent = Intent(this@DriverTripActivity, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        finish()
                                    }, 2500)
                                }
                            } else {
                                if (offset == 0) {
                                    layout_recycleview.visibility = View.GONE
                                    layout_no_recourd_found.visibility = View.VISIBLE
                                } else {
                                    //Toast.makeText(DriverTripActivity.this, resObj.getString("message").toString(), Toast.LENGTH_LONG).show();
                                    Toast.makeText(this@DriverTripActivity, resources.getString(R.string.data_not_found), Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    } else {
                        Common.showHttpErrorMessage(this@DriverTripActivity, error.message)
                    }
                }

    }

    inner class CallUnsubscribe(internal var activity: Activity, internal var DeviceToken: String) : AsyncTask<String, Void, String>() {


        private val userPref: SharedPreferences

        init {
            userPref = PreferenceManager.getDefaultSharedPreferences(activity)
        }

        override fun doInBackground(vararg args: String): String {
            // Create a new HttpClient and Post Header
            val httpclient = DefaultHttpClient()
            val myParams = BasicHttpParams()
            HttpConnectionParams.setConnectionTimeout(myParams, 10000)
            HttpConnectionParams.setSoTimeout(myParams, 10000)

            val JSONResponse: JSONObject? = null
            var contentStream: InputStream? = null
            var resultString = ""

            try {

                val passObj = JSONObject()
                passObj.put("user", "d_" + userPref.getString("id", "")!!)
                passObj.put("type", "android")
                passObj.put("token", DeviceToken)

                Log.d("passObj", "response passObj = $passObj")

                val httppost = HttpPost(Url.unsubscribeUrl)
                httppost.setHeader("Content-Type", "application/json")
                httppost.setHeader("Accept", "application/json")

                //                StringEntity se = new StringEntity(passObj.toString());
                //                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                //                httppost.setEntity(se);
                httppost.entity = StringEntity(passObj.toString(), HTTP.UTF_8)

                for (i in 0 until httppost.allHeaders.size) {
                    Log.v("set header", httppost.allHeaders[i].value)
                }

                val response = httpclient.execute(httppost)

                // Do some checks to make sure that the request was processed properly
                val headers = response.allHeaders
                val entity = response.entity
                contentStream = entity.content

                Log.d("response", "response = $response==$entity==$contentStream")
                resultString = response.toString()
            } catch (e: ClientProtocolException) {
                e.printStackTrace()
                Log.d("Error", "response Error one = " + e.message)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("Error", "response Error two = " + e.message)
                return e.message!!
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("Error", "response Error three = " + e.message)
                return e.message!!
            }


            return resultString
        }

        override fun onPostExecute(result: String) {

            if (result.contains("HTTP/1.1 200 OK")) {
                CallSubscribe(activity, Common.device_token).execute()
            } else if (result.contains("failed to connect to")) {
                if (loader.isShowing)
                    loader.cancel()
                Common.showHttpErrorMessage(activity, "network not available")
            }
        }
    }

    inner class CallSubscribe(internal var activity: Activity, internal var DeviceToken: String) : AsyncTask<String, Void, String>() {


        private val userPref: SharedPreferences

        init {
            userPref = PreferenceManager.getDefaultSharedPreferences(activity)
        }

        override fun doInBackground(vararg args: String): String {
            // Create a new HttpClient and Post Header
            val httpclient = DefaultHttpClient()
            val myParams = BasicHttpParams()
            HttpConnectionParams.setConnectionTimeout(myParams, 10000)
            HttpConnectionParams.setSoTimeout(myParams, 10000)

            val JSONResponse: JSONObject? = null
            var contentStream: InputStream? = null
            var resultString = ""

            try {

                val passObj = JSONObject()
                passObj.put("user", "d_" + userPref.getString("id", "")!!)
                passObj.put("type", "android")
                passObj.put("token", DeviceToken)

                Log.d("passObj", "response passObj = $passObj")

                val httppost = HttpPost(Url.subscribeUrl)
                httppost.setHeader("Content-Type", "application/json")
                httppost.setHeader("Accept", "application/json")

                //                StringEntity se = new StringEntity(passObj.toString());
                //                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                //                httppost.setEntity(se);
                httppost.entity = StringEntity(passObj.toString(), HTTP.UTF_8)

                for (i in 0 until httppost.allHeaders.size) {
                    Log.v("set header", httppost.allHeaders[i].value)
                }

                val response = httpclient.execute(httppost)

                // Do some checks to make sure that the request was processed properly
                val headers = response.allHeaders
                val entity = response.entity
                contentStream = entity.content

                Log.d("response", "response = $response==$entity==$contentStream")
                resultString = response.toString()
            } catch (e: ClientProtocolException) {
                e.printStackTrace()
                Log.d("Error", "response Error one = " + e.message)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("Error", "response Error two = " + e.message)
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("Error", "response Error three = " + e.message)
            }


            return resultString
        }

        override fun onPostExecute(result: String) {

            if (result.contains("HTTP/1.1 200 OK")) {
                val isDeviceToken = userPref.edit()
                isDeviceToken.putString("id_device_token", "1")
                isDeviceToken.commit()

                Handler().postDelayed({
                    if (Common.isNetworkAvailable(this@DriverTripActivity)) {

                        var allFilter = false
                        if (userPref.getBoolean("setFilter", false) == true) {
                            if (userPref.getInt("pending booking", 4) == 0) {
                                FilterString += 0.toString() + ","
                                allFilter = true
                            }
                            if (userPref.getInt("accepted booking", 4) == 1) {
                                FilterString += 1.toString() + ","
                                allFilter = true
                            }
                            if (userPref.getInt("driver cancel", 4) == 2) {
                                FilterString += 2.toString() + ","
                                allFilter = true
                            }
                            if (userPref.getInt("completed booking", 4) == 3) {
                                FilterString += 3.toString() + ","
                                allFilter = true
                            }
                            if (FilterString.length > 0)
                                FilterString = FilterString.substring(0, FilterString.length - 1)

                            val clickfilter = userPref.edit()
                            clickfilter.putBoolean("setFilter", allFilter)
                            clickfilter.commit()

                            if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
                                FilterString = ""
                            }
                            FilterAllDriverTrips(0, "filter", false)
                            FilterString = ""

                        } else {
                            getDriverAllTrip(0, false)
                        }
                    } else {
                        Common.showInternetInfo(this@DriverTripActivity, "Network is not available")
                        swipe_refresh_layout.isEnabled = false
                    }
                }, 500)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("DETAIL_REQUEST", "DriverTrip DETAIL_REQUEST = $DETAIL_REQUEST==$requestCode==$data")
        if (requestCode == DETAIL_REQUEST) {
            if (data != null) {
                val position = data.getIntExtra("position", 0)
                val driverAllTripFeed = DriverAllTripArray!![position]
                driverAllTripFeed.driverFlag = data.getStringExtra("driver_flage")
                driverAllTripFeed.status = data.getStringExtra("status")
                DrvAllTripAdapter.notifyItemChanged(position)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (Common.profile_edit == 1) {
            Common.showMkSuccess(this@DriverTripActivity, resources.getString(R.string.update_profile), "yes")
            Common.profile_edit = 0
        }
        if (Common.BookingId != null && Common.BookingId != "") {

            if (Common.isNetworkAvailable(this@DriverTripActivity)) {
                Common.BookingId = ""
                recycle_all_trip.isClickable = false
                recycle_all_trip.isEnabled = false
                var allFilter = false
                if (userPref.getBoolean("setFilter", false) == true) {
                    if (userPref.getInt("pending booking", 4) == 0) {
                        FilterString += 0.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("accepted booking", 4) == 1) {
                        FilterString += 1.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("driver cancel", 4) == 2) {
                        FilterString += 2.toString() + ","
                        allFilter = true
                    }
                    if (userPref.getInt("completed booking", 4) == 3) {
                        FilterString += 3.toString() + ","
                        allFilter = true
                    }
                    if (FilterString.length > 0)
                        FilterString = FilterString.substring(0, FilterString.length - 1)

                    val clickfilter = userPref.edit()
                    clickfilter.putBoolean("setFilter", allFilter)
                    clickfilter.commit()

                    if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3) {
                        FilterString = ""
                    }
                    Handler().postDelayed({ FilterAllDriverTrips(0, "", true) }, 1000)

                    FilterString = ""

                } else {
                    Handler().postDelayed({ getDriverAllTrip(0, true) }, 1000)

                }

            } else {
                recycle_all_trip.isClickable = true
                recycle_all_trip.isEnabled = true
                //Network is not available
                Common.showInternetInfo(this@DriverTripActivity, "Network is not available")
            }
            Common.ActionClick = ""
            Common.BookingId = ""
        }

    }

    companion object {
        private val SERVER_IP = "http://162.243.225.225:4040"
        val DETAIL_REQUEST = 1
    }
}
