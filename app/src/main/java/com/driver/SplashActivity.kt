package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.driver.utils.SimUtils

import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion

import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap

class SplashActivity : AppCompatActivity() {

    lateinit var userPref: SharedPreferences
    lateinit var txt_progress: TextView

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
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
//                sendSmsMsgFnc("8447992236","dffdsfsd")
//                SimUtils.sendSMS(this,1,"8447992236",null,"Hi Stackoverflow! its me Maher. Sent by sim1",null,null);
//                    SimUtils.sendDirectSMS(this)
//                SmsManager.getSmsManagerForSubscriptionId(4).sendTextMessage( "8595008237", "", "String text",null, null);
//                SimUtils.sendDualSimSMSOption("8447992236","test",this)
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
                                    val intent = Intent(this@SplashActivity, DriverTripActivity::class.java)
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

    fun sendSmsMsgFnc(mblNumVar:String, smsMsgVar:String) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) === PackageManager.PERMISSION_GRANTED)
        {
            try
            {
                val smsMgrVar = SmsManager.getDefault()
                smsMgrVar.sendTextMessage(mblNumVar, null, smsMsgVar, null, null)
                Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show()
            }
            catch (ErrVar:Exception) {
                Toast.makeText(getApplicationContext(), ErrVar.message.toString(),
                    Toast.LENGTH_LONG).show()
                ErrVar.printStackTrace()
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(arrayOf<String>(android.Manifest.permission.SEND_SMS), 10)
            }
        }
    }
}
