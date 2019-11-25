package com.driver

import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.driver.utils.Common

import org.json.JSONException
import org.json.JSONObject

class ForgotActivity() : AppCompatActivity(), Parcelable {

    lateinit var regularRoboto: Typeface
    lateinit var boldRoboto: Typeface
    lateinit var regularOpenSans: Typeface
    lateinit var boldOpenSans: Typeface
    lateinit var tv_retrive_your_password: TextView
    lateinit var edt_reg_email: TextInputEditText

    //lateinit Alert
    lateinit var rlMainView: RelativeLayout
    lateinit var tvTitle: TextView

    lateinit var loader: LoaderView

    val isValidForgot: Boolean
        get() {
            var isvalid_details = true
            if (edt_reg_email.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_email.text.toString().trim { it <= ' ' }.length == 0) {
                isvalid_details = false
                Utility.showMKPanelError(this@ForgotActivity, resources.getString(R.string.please_enter_email), rlMainView, tvTitle, regularRoboto)
            } else if (!Utility.isValidEmail(edt_reg_email.text.toString())) {
                isvalid_details = false
                Utility.showMKPanelError(this@ForgotActivity, resources.getString(R.string.please_enter_valid_email), rlMainView, tvTitle, regularRoboto)
            }
            return isvalid_details
        }

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        loader = LoaderView(this@ForgotActivity)


        //Font
        regularRoboto = Typeface.createFromAsset(assets, getString(R.string.font_regular_roboto))
        boldRoboto = Typeface.createFromAsset(assets, getString(R.string.font_bold_roboto))

        regularOpenSans = Typeface.createFromAsset(assets, getString(R.string.font_regular_opensans))
        boldOpenSans = Typeface.createFromAsset(assets, getString(R.string.font_bold_opensans))


        //Error Alert
        rlMainView = findViewById(R.id.rlMainView) as RelativeLayout
        tvTitle = findViewById(R.id.tvTitle) as TextView

        //Email
        edt_reg_email = findViewById(R.id.edt_reg_email) as TextInputEditText
        edt_reg_email.typeface = regularOpenSans

        //Retrive your password
        tv_retrive_your_password = findViewById(R.id.tv_retrive_your_password) as TextView
        tv_retrive_your_password.typeface = boldRoboto
        tv_retrive_your_password.setOnClickListener {
            if (isValidForgot) {
                callForgotPassword()
            }
        }

        Common.validationGone(this@ForgotActivity, rlMainView, edt_reg_email)
    }

    fun callForgotPassword() {
        loader.show()
        val DriverForPass = Url.driver_forgot_password + "email=" + edt_reg_email.text.toString().trim { it <= ' ' } + "&isdevice=1"
        Log.d("DriverForPass", "DriverForPass = $DriverForPass")
        Ion.with(this@ForgotActivity)
                .load(DriverForPass)
                .asJsonObject()
                .setCallback(FutureCallback { e, result ->
                    // do stuff with the result or error
                    println("Forgot Response >>>$result")
                    println("Forgot Response >>>" + e!!)
                    loader.cancel()
                    if (e != null) {
                        //Toast.makeText(ForgotActivity.this, "Forgot Error"+e, Toast.LENGTH_LONG).show();
                        Common.showHttpErrorMessage(this@ForgotActivity, e.message)
                        return@FutureCallback
                    }

                    try {
                        val JsonRes = JSONObject(result.toString())
                        if (JsonRes.getString("status") == "success") {
                            Toast.makeText(this@ForgotActivity, JsonRes.getString("message").toString(), Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } catch (e1: JSONException) {
                        e1.printStackTrace()
                    }
                })


    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ForgotActivity> {
        override fun createFromParcel(parcel: Parcel): ForgotActivity {
            return ForgotActivity(parcel)
        }

        override fun newArray(size: Int): Array<ForgotActivity?> {
            return arrayOfNulls(size)
        }
    }
}
