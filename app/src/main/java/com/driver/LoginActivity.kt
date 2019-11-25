package com.driver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.driver.utils.Common

import org.json.JSONArray
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var regularRoboto: Typeface
    lateinit var boldRoboto: Typeface
    lateinit var regularOpenSans: Typeface
    lateinit var boldOpenSans: Typeface
    lateinit var edt_reg_username: TextInputEditText
    lateinit var edt_reg_password: TextInputEditText
    lateinit var caption_signin: TextView
    lateinit var tv_signin: TextView
    lateinit var tv_forgot_password: TextView

    //Error Alert
    lateinit var rlMainView: RelativeLayout
    lateinit var tvTitle: TextView

    lateinit var loader: LoaderView

    lateinit var userPref: SharedPreferences

    val isValidLogin: Boolean
        get() {
            var isvalid_details = true
            if (edt_reg_username.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_username.text.toString().trim { it <= ' ' }.length == 0) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.please_enter_username), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_username.text.toString().length != 0 && edt_reg_username.text.toString().length < 4) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.minimum_user_charactor), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_username.text.toString().length != 0 && edt_reg_username.text.toString().length > 30) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.maximum_user_charactor), rlMainView, tvTitle, regularRoboto)
            } else if (!Utility.isValidUserName(edt_reg_username.text.toString())) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.username_error), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_password.text.toString().trim { it <= ' ' }.length == 0) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.please_enter_password), rlMainView, tvTitle, regularRoboto)
            } else if (!edt_reg_password.text.toString().trim { it <= ' ' }.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$".toRegex())) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.hint_password_msg), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_password.text.toString().trim { it <= ' ' }.length < 6 || edt_reg_password.text.toString().trim { it <= ' ' }.length > 32) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.password_length), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_password.text.toString().trim { it <= ' ' }.length > 32) {
                isvalid_details = false
                Utility.showMKPanelError(this@LoginActivity, resources.getString(R.string.large_password), rlMainView, tvTitle, regularRoboto)
            }

            return isvalid_details
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        userPref = PreferenceManager.getDefaultSharedPreferences(this@LoginActivity)

        //Font
        regularRoboto = Typeface.createFromAsset(assets, getString(R.string.font_regular_roboto))
        boldRoboto = Typeface.createFromAsset(assets, getString(R.string.font_bold_roboto))

        regularOpenSans = Typeface.createFromAsset(assets, getString(R.string.font_regular_opensans))
        boldOpenSans = Typeface.createFromAsset(assets, getString(R.string.font_bold_opensans))

        //Error Alert
        rlMainView = findViewById(R.id.rlMainView) as RelativeLayout
        tvTitle = findViewById(R.id.tvTitle) as TextView

        //SIGNIN
        caption_signin = findViewById(R.id.caption_signin) as TextView
        caption_signin.typeface = boldOpenSans

        //UserName & Password
        edt_reg_username = findViewById(R.id.edt_reg_username) as TextInputEditText
        edt_reg_username.typeface = regularOpenSans

        edt_reg_password = findViewById(R.id.edt_reg_password) as TextInputEditText
        edt_reg_password.typeface = regularOpenSans
        edt_reg_password.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b)
                Utility.showMKPanelInfo(this@LoginActivity, resources.getString(R.string.hint_password_msg), rlMainView, tvTitle, regularRoboto)
        }

        //SignIn
        tv_signin = findViewById(R.id.tv_signin) as TextView
        tv_signin.typeface = boldRoboto
        tv_signin.setOnClickListener { view ->
            if (isValidLogin) {

                val focusCurrent = currentFocus
                if (focusCurrent != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }

                loader = LoaderView(this@LoginActivity)
                loader.show()
                callLoginWebservice()

            }
        }

        //ForgotPassword
        tv_forgot_password = findViewById(R.id.tv_forgot_password) as TextView
        tv_forgot_password.typeface = regularOpenSans
        tv_forgot_password.setOnClickListener { startActivity(Intent(this@LoginActivity, ForgotActivity::class.java)) }

        Common.validationGone(this@LoginActivity, rlMainView, edt_reg_username)
        Common.validationGone(this@LoginActivity, rlMainView, edt_reg_password)
    }


    fun callLoginWebservice() {

        val urlLogin = Url.driver_login + "username=" + edt_reg_username.text.toString().trim { it <= ' ' } + "&password=" + edt_reg_password.text.toString().trim { it <= ' ' } + "&isdevice=1"
        Log.d("urlLogin", "urlLogin = $urlLogin")
        Ion.with(this@LoginActivity)
                .load(urlLogin)
                .asJsonObject()
                .setCallback(FutureCallback { e, result ->
                    loader.loaderObject().stop()
                    loader.loaderDismiss()
                    // do stuff with the result or error
                    if (e != null) {
                        // Toast.makeText(LoginActivity.this, "Login Error"+e, Toast.LENGTH_LONG).show();
                        Common.showMkError(this@LoginActivity, e.message!!)
                        return@FutureCallback
                    }
                    try {

                        val jsonObject = JSONObject(result.toString())
                        if (jsonObject.has("status") && jsonObject.getString("status") == "success") {

                            /*set Start Currency*/

                            val currencyArray = JSONArray(jsonObject.getString("country_detail"))
                            for (ci in 0 until currencyArray.length()) {
                                val startEndTimeObj = currencyArray.getJSONObject(ci)
                                Common.Currency = startEndTimeObj.getString("currency")
                                Common.Country = startEndTimeObj.getString("country")

                                val currency = userPref.edit()
                                currency.putString("currency", startEndTimeObj.getString("currency"))
                                currency.commit()
                            }

                            val jsonArray = jsonObject.getJSONArray("Driver_detail")
                            val jsonObjDriver = jsonArray.getJSONObject(0)
                            println("Driver Response >>>$jsonObjDriver")

                            val id = userPref.edit()
                            id.putString("id", jsonObjDriver.getString("id"))
                            id.commit()

                            val name = userPref.edit()
                            name.putString("name", jsonObjDriver.getString("name"))
                            name.commit()

                            val user_name = userPref.edit()
                            user_name.putString("user_name", jsonObjDriver.getString("user_name"))
                            user_name.commit()

                            val email = userPref.edit()
                            email.putString("email", jsonObjDriver.getString("email"))
                            email.commit()

                            val password = userPref.edit()
                            password.putString("password", edt_reg_password.text.toString().trim { it <= ' ' })
                            password.commit()

                            val gender = userPref.edit()
                            gender.putString("gender", jsonObjDriver.getString("gender"))
                            gender.commit()

                            val phone = userPref.edit()
                            phone.putString("phone", jsonObjDriver.getString("phone"))
                            phone.commit()

                            val dob = userPref.edit()
                            dob.putString("dob", jsonObjDriver.getString("dob"))
                            dob.commit()

                            val address = userPref.edit()
                            address.putString("address", jsonObjDriver.getString("address"))
                            address.commit()

                            val license_no = userPref.edit()
                            license_no.putString("license_no", jsonObjDriver.getString("license_no"))
                            license_no.commit()

                            val Lieasence_Expiry_Date = userPref.edit()
                            Lieasence_Expiry_Date.putString("Lieasence_Expiry_Date", jsonObjDriver.getString("Lieasence_Expiry_Date"))
                            Lieasence_Expiry_Date.commit()

                            val license_plate = userPref.edit()
                            license_plate.putString("license_plate", jsonObjDriver.getString("license_plate"))
                            license_plate.commit()

                            val Insurance = userPref.edit()
                            Insurance.putString("Insurance", jsonObjDriver.getString("Insurance"))
                            Insurance.commit()

                            val Car_Model = userPref.edit()
                            Car_Model.putString("Car_Model", jsonObjDriver.getString("Car_Model"))
                            Car_Model.commit()

                            val Car_Make = userPref.edit()
                            Car_Make.putString("Car_Make", jsonObjDriver.getString("Car_Make"))
                            Car_Make.commit()

                            val car_type = userPref.edit()
                            car_type.putString("car_type", jsonObjDriver.getString("car_type"))
                            car_type.commit()

                            val car_no = userPref.edit()
                            car_no.putString("car_no", jsonObjDriver.getString("car_no"))
                            car_no.commit()

                            val Seating_Capacity = userPref.edit()
                            Seating_Capacity.putString("Seating_Capacity", jsonObjDriver.getString("Seating_Capacity"))
                            Seating_Capacity.commit()

                            val image = userPref.edit()
                            image.putString("image", jsonObjDriver.getString("image"))
                            image.commit()

                            val status = userPref.edit()
                            status.putString("status", jsonObjDriver.getString("status"))
                            status.commit()

                            val isLogin = userPref.edit()
                            isLogin.putBoolean("is_login", true)
                            isLogin.commit()

                            val intent = Intent(this@LoginActivity, DriverTripActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()

                        } else if (jsonObject.has("status") && jsonObject.getString("status") == "failed") {
                            Utility.showMKPanelErrorServer(this@LoginActivity, jsonObject.getString("error code").toString(), rlMainView, tvTitle, regularRoboto)
                        } else if (jsonObject.has("status") && jsonObject.getString("status") == "failed") {
                            Utility.showMKPanelErrorServer(this@LoginActivity, jsonObject.getString("error code").toString(), rlMainView, tvTitle, regularRoboto)
                        } else {
                            Utility.showMKPanelErrorServer(this@LoginActivity, jsonObject.getString("error code").toString(), rlMainView, tvTitle, regularRoboto)
                        }
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                })
    }
}
