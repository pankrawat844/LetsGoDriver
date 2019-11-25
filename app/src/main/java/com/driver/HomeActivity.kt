package com.driver

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    lateinit var map: SupportMapFragment
    var mLocationPermissionGranted = false
    var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
    var mLastKnownLocation: Location? = null
    lateinit var mFusedLocationProviderClient:FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        map = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        map.getMapAsync(this)
        val mGeoDataClient = Places.getGeoDataClient(this)
        val mPlaceDetectionClient = Places.getPlaceDetectionClient(this)
         mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0!!
        updateLocationUI()
        getDeviceLocation()
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

                locationResult.addOnCompleteListener(this@HomeActivity,object:OnCompleteListener<Location>
                {
                    override fun onComplete(task: Task<Location>) {
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


                })
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }
}
