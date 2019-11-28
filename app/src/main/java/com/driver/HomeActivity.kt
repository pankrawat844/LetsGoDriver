package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.driver.utils.Common
import com.driver.utils.DriverAllTripFeed
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.*

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    lateinit var map: SupportMapFragment
    var mLocationPermissionGranted = false
    var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
    var mLastKnownLocation: Location? = null
    lateinit var slidingMenu: SlidingMenu
    var common = Common()
    var driverAllTripArray: ArrayList<DriverAllTripFeed>? = null
    lateinit var userPref: SharedPreferences
    lateinit var mFusedLocationProviderClient:FusedLocationProviderClient
    lateinit var gps: GPSTracker
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()
    private var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        gps = GPSTracker(this@HomeActivity)
        userPref = PreferenceManager.getDefaultSharedPreferences(this)

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude()
            longitude = gps.getLongitude()
        } else {
            gps.showSettingsAlert()
        }
        map = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        map.getMapAsync(this)
        val mGeoDataClient = Places.getGeoDataClient(this)
        val mPlaceDetectionClient = Places.getPlaceDetectionClient(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        slidingMenu = SlidingMenu(this)
        slidingMenu.mode = SlidingMenu.LEFT
        slidingMenu.touchModeAbove = SlidingMenu.TOUCHMODE_FULLSCREEN
        slidingMenu.setBehindOffsetRes(R.dimen.slide_menu_width)
        slidingMenu.setFadeDegree(0.20f)
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT)
        slidingMenu.setMenu(R.layout.left_menu)
        common.SlideMenuDesign(slidingMenu, this@HomeActivity, "mhome")
        layout_slidemenu.setOnClickListener { slidingMenu.toggle() }
        val timer=Timer()

        timer.scheduleAtFixedRate(object :TimerTask(){
            override fun run() {
                if (gps.canGetLocation()) {
                    try {
                        mSocket = IO.socket(SERVER_IP)
                        mSocket!!.emit(
                            Socket.EVENT_CONNECT_ERROR,
                            onConnectError
                        )
                        mSocket!!.connect()
                        Common.socket = mSocket
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        Log.d("connected ", "connected error = " + e.message)
                    }
                    val driver_status=Switch(this@HomeActivity)
                    Common.socketFunction(
                        this@HomeActivity,
                        mSocket!!,
                        null,
                        latitude,
                        longitude,
                        common,
                        userPref
                    )
                }
            }

        },2000,2000)


    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0!!
        updateLocationUI()
        getDeviceLocation()
    }


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

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            );
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var mLocationPermissionGranted = false;
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION ->
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true;
                }
        }
    }


    fun updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                googleMap!!.isMyLocationEnabled = true;
                googleMap!!.uiSettings.isMyLocationButtonEnabled = true;
            } else {
                googleMap!!.isMyLocationEnabled = false;
                googleMap!!.uiSettings.isMyLocationButtonEnabled = false;
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message);
        }
    }

    private fun getDeviceLocation() {
        /*
       * Get the best and most recent location of the device, which may be null in rare
       * cases when a location is not available.
       */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener(this@HomeActivity
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation?.latitude!!,
                                    mLastKnownLocation?.longitude!!
                                ), 15f
                            )
                        )
                    } else {
                        Log.d("TAG", "Current location is null. Using defaults.")
                        Log.e("TAG", "Exception: %s", task.exception)
                        googleMap!!.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(28.7407056,77.0577491),
                                15f
                            )
                        )
                        googleMap!!.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }


    private fun getDriverAllTrip(offset: Int) {
        userPref = PreferenceManager.getDefaultSharedPreferences(this)

        if (offset == 0) {
            driverAllTripArray = ArrayList()
        }
        //String DrvBookingUrl = Url.DriverTripUrl+"?driver_id="+Utility.userDetails.getId()+"&off="+offset;
        val DrvBookingUrl =
            Url.DriverTripUrl + "?driver_id=" + userPref.getString("id", "") + "&off=" + offset
        Log.d("loadTripsUrl", "loadTripsUrl =$DrvBookingUrl")
        Ion.with(this)
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
                                if(trpObj.getString("status").equals("0")) {
                                    val allTripFeed = DriverAllTripFeed()
                                    allTripFeed.id = trpObj.getString("id")
                                    allTripFeed.driverFlag = trpObj.getString("driver_flag")
                                    allTripFeed.dropArea = trpObj.getString("drop_area")
                                    allTripFeed.pickupArea = trpObj.getString("pickup_area")
                                    allTripFeed.carType = trpObj.getString("car_type")
                                    allTripFeed.pickupDateTime =
                                        trpObj.getString("pickup_date_time")
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

                                }
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
                                this,
                                resObj.getString("error code").toString()
                            )

                            if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {
                                val editor = userPref.edit()
                                editor.clear()
                                editor.commit()
                                Handler().postDelayed({
                                    val intent =
                                        Intent(this, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finish()
                                }, 2500)
                            }
                        } else {
                            if (offset == 0) {

                            } else {
                                //Toast.makeText(DriverTripActivity.this, resObj.getString("message").toString(), Toast.LENGTH_LONG).show();
                                Toast.makeText(
                                    this,
                                    resources.getString(R.string.data_not_found),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    Common.showHttpErrorMessage(this, error.message)
                }
            }

    }


    companion object {
        private val SERVER_IP = "http://162.243.225.225:4040"
        val DETAIL_REQUEST = 1
    }
}
