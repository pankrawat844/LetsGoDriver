package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion

import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap

class SplashActivity : AppCompatActivity() {

    lateinit var userPref: SharedPreferences
    lateinit var txt_progress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        txt_progress = findViewById(R.id.txt_progress) as TextView

        //        Intent intent = new Intent(this, RegistrationIntentService.class);
        //        startService(intent);

        userPref = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
        Log.d("is login", "is login = " + userPref.getBoolean("is_login", false))



        Handler().postDelayed({
            if (Utility.isNetworkAvailable(this@SplashActivity)) {
                getCarType()
            } else {
                //Network
                Utility.showInternetInfo(this@SplashActivity, "Network is not available")
            }
        }, 1000)


        txt_progress.setOnClickListener { getCarType() }

    }

    fun getCarType() {
        Log.d("driver_cartype", "driver_cartype = " + Url.driver_cartype)
        Utility.arrayCarTypeList = ArrayList()
        Ion.with(this@SplashActivity)
                .load(Url.driver_cartype)
                .asJsonObject()
                .setCallback(FutureCallback { e, result ->
                    // do stuff with the result or error

                    if (e != null) {
                        txt_progress.text = resources.getString(R.string.retry)
                        Toast.makeText(this@SplashActivity, e.message, Toast.LENGTH_LONG).show()
                        //Common.showMkError(SplashActivity.this,e.getMessage());
                        return@FutureCallback
                    }

                    try {
                        val jsonObject = JSONObject(result.toString())
                        Log.d("jsonObject", "jsonObject = $jsonObject")
                        if (jsonObject.has("status") && jsonObject.getString("status").equals("success", ignoreCase = true )) {
                            val jsonArray = jsonObject.getJSONArray("Car_Type")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObjectCarType = jsonArray.getJSONObject(i)
                                val hm = HashMap<String, String>()
                                if (jsonObjectCarType.has("cab_id") && !jsonObjectCarType.getString("cab_id").equals("", ignoreCase = true))
                                    hm["car_id"] = jsonObjectCarType.getString("cab_id")
                                else
                                    hm["car_id"] = ""
                                Log.d("jsonObject", "jsonObject = " + hm["car_id"])


                                if (jsonObjectCarType.has("cartype") && !jsonObjectCarType.getString("cartype").equals("", ignoreCase = true))
                                    hm["car_type"] = jsonObjectCarType.getString("cartype")
                                else
                                    hm["car_type"] = ""

                                if (jsonObjectCarType.has("icon") && !jsonObjectCarType.getString("icon").equals("", ignoreCase = true))
                                    hm["icon"] = jsonObjectCarType.getString("icon")
                                else
                                    hm["icon"] = ""

                                if (jsonObjectCarType.has("car_rate") && !jsonObjectCarType.getString("car_rate").equals("", ignoreCase = true))
                                    hm["car_rate"] = jsonObjectCarType.getString("car_rate")
                                else
                                    hm["car_rate"] = ""

                                hm["is_selected"] = "0"

                                if (jsonObjectCarType.has("seating_capecity") && !jsonObjectCarType.getString("seating_capecity").equals("", ignoreCase = true))
                                    hm["seating_capecity"] = jsonObjectCarType.getString("seating_capecity")
                                else
                                    hm["seating_capecity"] = ""

                                Utility.arrayCarTypeList!!.add(hm)
                            }

                            txt_progress.text = resources.getString(R.string.sucess)
                            if (userPref.getBoolean("is_login", false)) {
                                Handler().postDelayed({
//                                    val intent = Intent(this@SplashActivity, DriverTripActivity::class.java)
                                    val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finish()
                                }, 1000)
                            } else {
                                Handler().postDelayed({
                                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finish()
                                }, 1000)
                            }
                        } else {
                            txt_progress.text = jsonObject.getString("message")
                            //                                new Handler().postDelayed(new Runnable() {
                            //                                    @Override
                            //                                    public void run() {
                            //                                        Intent intent = new Intent(SplashActivity.this, DriverTripActivity.class);
                            //                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            //                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //                                        startActivity(intent);
                            //                                        finish();
                            //                                    }
                            //                                }, 1000);
                        }

                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                })
    }


}
