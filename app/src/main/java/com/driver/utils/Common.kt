package com.driver.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.github.lzyzsd.circleprogress.DonutProgress
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.textfield.TextInputEditText
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import com.squareup.picasso.Picasso
import com.driver.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by techintegrity on 08/10/16.
 */
class Common {

    fun SlideMenuDesign(slidingMenu: SlidingMenu, activity: Activity, clickMenu: String) {

        val Roboto_Regular = Typeface.createFromAsset(
            activity.assets,
            activity.getString(R.string.font_regular_roboto)
        )
        val Roboto_Bold =
            Typeface.createFromAsset(activity.assets, activity.getString(R.string.font_bold_roboto))

        val userPref = PreferenceManager.getDefaultSharedPreferences(activity)

        val txt_user_name = slidingMenu.findViewById(R.id.txt_user_name) as TextView
        txt_user_name.text = userPref.getString("user_name", "")
        val txt_user_number = slidingMenu.findViewById(R.id.txt_user_number) as TextView
        txt_user_number.text = userPref.getString("phone", "")
        val img_user = slidingMenu.findViewById(R.id.img_user) as ImageView
        Picasso.with(activity)
            .load(Uri.parse(Url.imageurl + userPref.getString("image", "")!!))
            .placeholder(R.drawable.user_photo)
            .transform(CircleTransformation(activity))
            .into(img_user)

        val layout_my_trip = slidingMenu.findViewById(R.id.layout_my_trip) as RelativeLayout
        layout_my_trip.setOnClickListener {
            slidingMenu.toggle()
            if (clickMenu != "my trip") {
                val mi = Intent(activity, DriverTripActivity::class.java)
                activity.startActivity(mi)
                activity.finish()
            }
        }

        val layout_cahnge_password =
            slidingMenu.findViewById(R.id.layout_cahnge_password) as RelativeLayout
        layout_cahnge_password.setOnClickListener {
            slidingMenu.toggle()
            if (clickMenu != "change password") {
                val mi = Intent(activity, ChangePasswordActivity::class.java)
                activity.startActivity(mi)
                activity.finish()
            }
        }

        val layoutFooterLogout =
            slidingMenu.findViewById(R.id.layout_footer_logout) as RelativeLayout
        layoutFooterLogout.setOnClickListener {
            slidingMenu.toggle()
            val builder = MaterialDialog.Builder(activity)
                .title(R.string.dialog_caption_logout)
                .content(R.string.dialog_logout_msg)
                .negativeText(R.string.dialog_cancel)
                .positiveText(R.string.dialog_ok)
                .onPositive { _, _ ->
                    if (socket != null) {

                        var latitude = 0.0
                        var longitude = 0.0
                        val gps = GPSTracker(activity)
                        if (gps.canGetLocation()) {
                            latitude = gps.latitude
                            longitude = gps.longitude
                        } else {
                            gps.showSettingsAlert()
                        }
                        try {
                            val locAry = JSONArray()
                            locAry.put(latitude)
                            locAry.put(longitude)
                            val emitObj = JSONObject()
                            emitObj.put("coords", locAry)
                            emitObj.put("driver_name", userPref.getString("user_name", ""))
                            emitObj.put("driver_id", userPref.getString("id", ""))
                            emitObj.put("driver_status", "0")
                            emitObj.put("car_type", userPref.getString("car_type", ""))
                            emitObj.put("isdevice", "1")
                            Log.d("emitobj", "emitobj = $emitObj")

                            socket!!.emit("Create Driver Data", emitObj)
                            socket!!.disconnect()

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    val editor = userPref.edit()
                    editor.clear()
                    editor.apply()

                    val logInt = Intent(activity, LoginActivity::class.java)
                    logInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    logInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    logInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(logInt)
                }

            val dialog = builder.build()
            dialog.show()
        }

        val layoutUser = slidingMenu.findViewById(R.id.layout_user) as RelativeLayout
        layoutUser.setOnClickListener {
            slidingMenu.toggle()
            val mi = Intent(activity, ProfileEditActivity::class.java)
            activity.startActivity(mi)
        }
    }

    fun changeLocationSocket(activity: Activity, driver_available: Switch?) {
        val locationListener: LocationListener
        val locationManager: LocationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isPermission = false

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                if (driver_available?.isChecked!=null) {
                    val userPref = PreferenceManager.getDefaultSharedPreferences(activity)

                    try {
                        val locAry = JSONArray()
                        locAry.put(location.latitude)
                        locAry.put(location.longitude)
                        val emitObj = JSONObject()
                        emitObj.put("coords", locAry)
                        emitObj.put("driver_name", userPref.getString("user_name", ""))
                        emitObj.put("driver_id", userPref.getString("id", ""))
                        emitObj.put("driver_status", "1")
                        emitObj.put("car_type", userPref.getString("car_type", ""))
                        emitObj.put("isdevice", "1")
                        emitObj.put("booking_status", userPref.getString("booking_status", ""))
                        Log.d("emitobj", "emitobj = $emitObj")

                        socket!!.emit("Create Driver Data", emitObj)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onProviderDisabled(provider: String) {
                Log.d("Latitude", "disable")
            }

            override fun onProviderEnabled(provider: String) {
                Log.d("Latitude", "enable")
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isPermission = checkLocationPermission(locationManager, activity)
        } else {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                1f,
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                1f,
                locationListener
            )
        }
        if (isPermission) {
            activity.runOnUiThread(object :Runnable
            {
                override fun run() {
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return
                    }
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        1f,
                        locationListener
                    )
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return
                    }
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        1f,
                        locationListener
                    )
                }

            })

        }
    }

    private fun checkLocationPermission(locationManager: LocationManager, activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(
                    activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            return false
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser(activity)
            }

            return true
        }
    }

    private fun showGPSDisabledAlertToUser(activity: Activity) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton("Settings") { _, _ ->
                val callGPSSettingIntent =
                    Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(callGPSSettingIntent)
            }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    companion object {

        //public static CustomCountdownTimer customCountdownTimer;
        var driverAllTripFeed: DriverAllTripFeed? = null
        var Currency = ""
        var Country = ""
        var socket: Socket? = null
        var BookingId = ""
        var ActionClick = ""
        var countDownTimer: CountDownTimer? = null
        var OnTripTime = ""
        var FinishedTripTime = ""
        var device_token = ""
        var profile_edit = 0
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99


        fun validationGone(
            activity: Activity,
            rlMainView: RelativeLayout,
            edt_reg_username: TextInputEditText
        ) {
            edt_reg_username.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    Log.d(
                        "charSequence",
                        "charSequence = " + charSequence.length + "==" + rlMainView.visibility + "==" + View.VISIBLE
                    )
                    if (charSequence.isNotEmpty() && rlMainView.visibility == View.VISIBLE) {
                        if (!activity.isFinishing) {
                            val slideUp = TranslateAnimation(
                                Animation.RELATIVE_TO_SELF,
                                0f,
                                Animation.RELATIVE_TO_SELF,
                                0f,
                                Animation.RELATIVE_TO_SELF,
                                0f,
                                Animation.RELATIVE_TO_SELF,
                                -100f
                            )
                            slideUp.duration = 10
                            slideUp.fillAfter = true
                            rlMainView.startAnimation(slideUp)
                            slideUp.setAnimationListener(object : Animation.AnimationListener {

                                override fun onAnimationStart(animation: Animation) {}

                                override fun onAnimationRepeat(animation: Animation) {}

                                override fun onAnimationEnd(animation: Animation) {
                                    rlMainView.visibility = View.GONE
                                }
                            })

                        }
                    }
                }

                override fun afterTextChanged(editable: Editable) {

                }
            })
        }

        fun showMkError(act: Activity, error_code: String) {
            if (!act.isFinishing) {

                var message = ""
                if (error_code == "1") {
                    message = act.resources.getString(R.string.inactive_user)
                } else if (error_code == "2") {
                    message = act.resources.getString(R.string.enter_correct_login_detail)
                } else if (error_code == "7") {
                    message = act.resources.getString(R.string.email_username_mobile_exit)
                } else if (error_code == "8") {
                    message = act.resources.getString(R.string.email_username_exit)
                } else if (error_code == "9") {
                    message = act.resources.getString(R.string.email_mobile_exit)
                } else if (error_code == "10") {
                    message = act.resources.getString(R.string.mobile_username_exit)
                } else if (error_code == "11") {
                    message = act.resources.getString(R.string.email_exit)
                } else if (error_code == "12") {
                    message = act.resources.getString(R.string.user_exit)
                } else if (error_code == "13") {
                    message = act.resources.getString(R.string.mobile_exit)
                } else if (error_code == "14") {
                    message = act.resources.getString(R.string.somthing_worng)
                } else if (error_code == "15" || error_code == "16") {
                    message = act.resources.getString(R.string.data_not_found)
                } else if (error_code == "19") {
                    message = act.resources.getString(R.string.vehicle_numbet_exits)
                } else if (error_code == "20") {
                    message = act.resources.getString(R.string.license_numbet_exits)
                } else if (error_code == "22") {
                    message = act.resources.getString(R.string.dublicate_booking)
                }

                val slideUpAnimation: Animation = AnimationUtils.loadAnimation(
                    act.applicationContext,
                    R.anim.slide_up_map
                )

                val mkInfoPanelDialog = Dialog(act, android.R.style.Theme_Translucent_NoTitleBar)

                mkInfoPanelDialog.setContentView(R.layout.mk_dialog_panel)
                mkInfoPanelDialog.show()

                val layoutInfoPanel =
                    mkInfoPanelDialog.findViewById(R.id.layout_info_panel) as RelativeLayout
                layoutInfoPanel.startAnimation(slideUpAnimation)

                val buttonLayoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    act.resources.getDimension(R.dimen.height_40).toInt()
                )
                buttonLayoutParams.setMargins(
                    0,
                    act.resources.getDimension(R.dimen.height_50).toInt(),
                    0,
                    0
                )
                layoutInfoPanel.layoutParams = buttonLayoutParams

                val subtitle = mkInfoPanelDialog.findViewById(R.id.subtitle) as TextView
                subtitle.text = message

                Handler().postDelayed({
                    try {
                        if (mkInfoPanelDialog.isShowing && !act.isFinishing)
                            mkInfoPanelDialog.cancel()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 2000)

            }
        }

        fun socketFunction(
            activity: Activity,
            mSocket: Socket,
            driver_status: Switch?,
            latitude: Double,
            longitude: Double,
            common: Common,
            userPref: SharedPreferences
        ) {
            try {
                val locAry = JSONArray()
                locAry.put(latitude)
                locAry.put(longitude)
                val emitobj = JSONObject()
                emitobj.put("coords", locAry)
                emitobj.put("driver_name", userPref.getString("user_name", ""))
                emitobj.put("driver_id", userPref.getString("id", ""))
                emitobj.put("driver_status", "1")
                emitobj.put("car_type", userPref.getString("car_type", ""))
                emitobj.put("isdevice", "1")
                Log.d("emitobj", "emitobj = $emitobj")

                mSocket.emit("Create Driver Data", emitobj)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            common.changeLocationSocket(activity, driver_status)

            Log.d("Socket", "Socket = " + socket!!)
            if (socket != null) {
                socket!!.on(Socket.EVENT_CONNECT_ERROR) {
                    activity.runOnUiThread {
                        if (!mSocket.connected()) {
                            Log.d("connected", "connected error= " + mSocket.connected())
                            //socketConnection();
                        } else {
                            Log.d("connected", "connected three= " + mSocket.connected())
                        }
                    }
                }
                searchedDriverDetail(activity)
            }
        }

        private fun searchedDriverDetail(activity: Activity) {

            socket!!.on("Searched Driver Detail") { args ->
                activity.runOnUiThread {
                    val data = args[0] as JSONObject
                    Log.d("data", "connected data = $data")
                    val ai = Intent(activity, CabPopupActivity::class.java)
                    ai.putExtra("booking_data", data.toString())
                    activity.startActivity(ai)
                }
            }
        }

        fun isNetworkAvailable(act: Activity): Boolean {
            val connMgr = act.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

        fun showInternetInfo(act: Activity, message: String) {
            if (!act.isFinishing) {
                val mk = InternetInfoPanel(
                    act,
                    InternetInfoPanel.InternetInfoPanelType.MKInfoPanelTypeInfo,
                    "SUCCESS!",
                    message,
                    2000
                )
                mk.show()
                mk.iv_ok.setOnClickListener {
                    try {
                        if (mk.isShowing && !act.isFinishing)
                            mk.cancel()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fun showHttpErrorMessage(activity: Activity, ErrorMessage: String?): Boolean {

            Log.d("ErrorMessage", "ErrorMessage = " + ErrorMessage!!)
            var status = true
            if (ErrorMessage != "") {
                when {
                    ErrorMessage.contains("Connect to") -> {
                        showInternetInfo(activity, "")
                        status = false
                    }
                    ErrorMessage.contains("failed to connect to") -> {
                        showInternetInfo(activity, "network not available")
                        status = false
                    }
                    ErrorMessage.contains("Internal Server Error") -> {
                        showMkError(activity, "Internal Server Error")
                        status = false
                    }
                    ErrorMessage.contains("Request Timeout") -> {
                        showMkError(activity, "Request Timeout")
                        status = false
                    }
                }
            } else {
                Toast.makeText(activity, "Server Time Out", Toast.LENGTH_LONG).show()
                status = false
            }
            return status
        }

        val currentTime: String
            @SuppressLint("SimpleDateFormat")
            get() {
                val timeFormat = SimpleDateFormat("HH:mm:ss")
                return timeFormat.format(Calendar.getInstance().time)
            }

        fun showMkSuccess(act: Activity, message: String, isHeader: String) {
            if (!act.isFinishing) {

                val slideUpAnimation: Animation = AnimationUtils.loadAnimation(
                    act.applicationContext,
                    R.anim.slide_up_map
                )

                val mkInfoPanelDialog = Dialog(act, android.R.style.Theme_Translucent_NoTitleBar)

                mkInfoPanelDialog.setContentView(R.layout.mk_dialog_panel)
                mkInfoPanelDialog.show()
                slideUpAnimation.fillAfter = true
                slideUpAnimation.duration = 2000

                val layoutInfoPanel =
                    mkInfoPanelDialog.findViewById(R.id.layout_info_panel) as RelativeLayout
                layoutInfoPanel.setBackgroundResource(R.color.sucess_color)
                layoutInfoPanel.startAnimation(slideUpAnimation)

                if (isHeader == "yes") {
                    val buttonLayoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        act.resources.getDimension(R.dimen.height_40).toInt()
                    )
                    buttonLayoutParams.setMargins(
                        0,
                        act.resources.getDimension(R.dimen.height_50).toInt(),
                        0,
                        0
                    )
                    layoutInfoPanel.layoutParams = buttonLayoutParams
                }

                val subtitle = mkInfoPanelDialog.findViewById(R.id.subtitle) as TextView
                subtitle.text = message

                Handler().postDelayed({
                    try {
                        if (mkInfoPanelDialog.isShowing && !act.isFinishing)
                            mkInfoPanelDialog.cancel()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 3000)
            }
        }

        fun acceptRejectTimer(
            activity: Activity,
            timber_progress: DonutProgress,
            accept_time: Long,
            minutes_value: TextView,
            activityName: String
        ): CountDownTimer {
            val mediaPlayer = MediaPlayer.create(activity.applicationContext, R.raw.timmer_mussic)

            return object : CountDownTimer(accept_time, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    timber_progress.progress = (millisUntilFinished / 1000).toInt()
                    minutes_value.text =
                        (millisUntilFinished / 1000).toInt().toString() + " " + activity.resources.getString(
                            R.string.secound
                        )

                    Log.d("mediaPlayer", "mediaPlayer = " + mediaPlayer.isPlaying)
                    if (!mediaPlayer.isPlaying)
                        mediaPlayer.start()
                }

                override fun onFinish() {
                    timber_progress.progress = 0
                    mediaPlayer.stop()
                    if (activityName == "cabpopupActivity") {
                        activity.finish()
                    }
                }
            }
        }
    }
}
