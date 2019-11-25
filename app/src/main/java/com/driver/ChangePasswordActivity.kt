package com.driver

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import com.koushikdutta.ion.Ion
import com.driver.utils.Common

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.net.URISyntaxException

class ChangePasswordActivity : AppCompatActivity() {

    lateinit var txt_change_password: TextView
    lateinit var txt_change_password_button: TextView
    lateinit var edit_current_pass: EditText
    lateinit var edit_new_pass: EditText
    lateinit var edit_con_pass: EditText
    lateinit var layout_change_password: RelativeLayout
    lateinit var layout_menu: RelativeLayout
    lateinit var txt_forgot_password: TextView

    lateinit var OpenSans_Regular: Typeface
    lateinit var OpenSans_Bold: Typeface
    lateinit var regularRoboto: Typeface
    lateinit var Roboto_Bold: Typeface
    lateinit var slidingMenu: SlidingMenu

    lateinit var userPref: SharedPreferences

     var common = Common()
    lateinit var driver_status: Switch
    lateinit var switch_driver_status: TextView

    lateinit var gps: GPSTracker
     var latitude: Double = 0.toDouble()
     var longitude: Double = 0.toDouble()
    lateinit var loader: LoaderView

    private var mSocket: Socket? = null

    //Error Alert
    lateinit  var rlMainView: RelativeLayout
    lateinit var tvTitle: TextView

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
        setContentView(R.layout.activity_change_password)

        loader = LoaderView(this@ChangePasswordActivity)

        gps = GPSTracker(this@ChangePasswordActivity)
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude()
            longitude = gps.getLongitude()
        } else {
            gps.showSettingsAlert()
        }

        //Error Alert
        rlMainView = findViewById(R.id.rlMainView) as RelativeLayout
        val rlMainParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rlMainParam.setMargins(0, resources.getDimension(R.dimen.height_50).toInt(), 0, 0)
        rlMainView.layoutParams = rlMainParam
        tvTitle = findViewById(R.id.tvTitle) as TextView

        slidingMenu = SlidingMenu(this)
        slidingMenu.mode = SlidingMenu.LEFT
        slidingMenu.touchModeAbove = SlidingMenu.TOUCHMODE_FULLSCREEN
        slidingMenu.setBehindOffsetRes(R.dimen.slide_menu_width)
        slidingMenu.setFadeDegree(0.20f)
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT)
        slidingMenu.setMenu(R.layout.left_menu)

        common.SlideMenuDesign(slidingMenu, this@ChangePasswordActivity, "change password")

        txt_change_password = findViewById(R.id.txt_change_password) as TextView
        txt_change_password_button = findViewById(R.id.txt_change_password_button) as TextView
        edit_current_pass = findViewById(R.id.edit_current_pass) as EditText
        edit_new_pass = findViewById(R.id.edit_new_pass) as EditText
        edit_con_pass = findViewById(R.id.edit_con_pass) as EditText
        layout_change_password = findViewById(R.id.layout_change_password) as RelativeLayout
        layout_menu = findViewById(R.id.layout_menu) as RelativeLayout
        txt_forgot_password = findViewById(R.id.txt_forgot_password) as TextView

        userPref = PreferenceManager.getDefaultSharedPreferences(this@ChangePasswordActivity)

        OpenSans_Regular = Typeface.createFromAsset(assets, "fonts/opensans-regular.ttf")
        OpenSans_Bold = Typeface.createFromAsset(assets, "fonts/opensans-bold.ttf")
        regularRoboto = Typeface.createFromAsset(assets, getString(R.string.font_regular_roboto))
        Roboto_Bold = Typeface.createFromAsset(assets, "fonts/roboto_bold.ttf")

        txt_change_password.typeface = OpenSans_Bold
        edit_new_pass.typeface = OpenSans_Regular
        edit_con_pass.typeface = OpenSans_Regular
        edit_current_pass.typeface = OpenSans_Regular
        txt_forgot_password.typeface = Roboto_Bold
        txt_change_password_button.typeface = Roboto_Bold

        layout_change_password.setOnClickListener(View.OnClickListener {
            if (edit_current_pass.text.toString().trim { it <= ' ' }.length == 0) {
                Utility.showMKPanelError(this@ChangePasswordActivity, resources.getString(R.string.please_enter_current_password), rlMainView, tvTitle, regularRoboto)
                edit_current_pass.requestFocus()
                return@OnClickListener
            } else if (edit_current_pass.text.toString().trim { it <= ' ' } != userPref.getString("password", "")) {
                Utility.showMKPanelError(this@ChangePasswordActivity, resources.getString(R.string.please_current_password), rlMainView, tvTitle, regularRoboto)
                edit_current_pass.requestFocus()
                return@OnClickListener
            } else if (edit_new_pass.text.toString().trim { it <= ' ' }.length == 0) {
                Utility.showMKPanelError(this@ChangePasswordActivity, resources.getString(R.string.please_enter_new_password), rlMainView, tvTitle, regularRoboto)
                edit_new_pass.requestFocus()
                return@OnClickListener
            } else if (edit_new_pass.text.toString().trim { it <= ' ' }.length < 8 || edit_new_pass.text.toString().trim { it <= ' ' }.length > 32) {
                Utility.showMKPanelError(this@ChangePasswordActivity, resources.getString(R.string.password_new_length), rlMainView, tvTitle, regularRoboto)
                edit_new_pass.requestFocus()
                return@OnClickListener
            } else if (edit_con_pass.text.toString().trim { it <= ' ' }.length == 0) {
                Utility.showMKPanelError(this@ChangePasswordActivity, resources.getString(R.string.please_enter_confirm_password), rlMainView, tvTitle, regularRoboto)
                edit_con_pass.requestFocus()
                return@OnClickListener
            } else if (edit_new_pass.text.toString() != edit_con_pass.text.toString()) {
                Utility.showMKPanelError(this@ChangePasswordActivity, resources.getString(R.string.password_new_confirm), rlMainView, tvTitle, regularRoboto)
                edit_con_pass.requestFocus()
                return@OnClickListener
            }

            if (Common.isNetworkAvailable(this@ChangePasswordActivity)) {
                loader.show()
                val DrvChangPasswordUrl = Url.DriverChangPasswordUrl + "?password=" + edit_new_pass.text.toString() + "&did=" + userPref.getString("id", "")
                Log.d("DrvBookingUrl", "DrvBookingUrl =$DrvChangPasswordUrl")
                Ion.with(this@ChangePasswordActivity)
                        .load(DrvChangPasswordUrl)
                        .asJsonObject()
                        .setCallback { error, result ->
                            // do stuff with the result or error
                            Log.d("load_trips result", "load_trips result = $result==$error")
                            loader.cancel()
                            if (error == null) {
                                try {
                                    val password = userPref.edit()
                                    password.putString("password", edit_new_pass.text.toString().trim { it <= ' ' })
                                    password.commit()

                                    val resObj = JSONObject(result.toString())
                                    if (resObj.getString("status") == "success") {

                                        Common.showMkSuccess(this@ChangePasswordActivity, resObj.getString("message"), "yes")

                                        edit_new_pass.setText("")
                                        edit_current_pass.setText("")
                                        edit_con_pass.setText("")
                                    } else if (resObj.getString("status") == "false") {

                                        Common.showMkError(this@ChangePasswordActivity, resObj.getString("error code").toString())

                                        if (resObj.has("Isactive") && resObj.getString("Isactive") == "Inactive") {

                                            val editor = userPref.edit()
                                            editor.clear()
                                            editor.commit()

                                            Handler().postDelayed({
                                                val intent = Intent(this@ChangePasswordActivity, MainActivity::class.java)
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
                                Common.showHttpErrorMessage(this@ChangePasswordActivity, error.message)
                            }
                        }
            }
        })

        layout_menu.setOnClickListener { slidingMenu.toggle() }

        /*Change driver Status*/

        //Log.d("Socket is Conntecte","Socket is Conntecte = "+Common.socket.connected());

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
                if (gps.canGetLocation()) {
                    try {
                        mSocket = IO.socket(SERVER_IP)
                        mSocket!!.emit(Socket.EVENT_CONNECT_ERROR, onConnectError)
                        mSocket!!.connect()
                        Common.socket = mSocket
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        Log.d("connected ", "connected error = " + e.message)
                    }

                    Common.socketFunction(this@ChangePasswordActivity, mSocket!!, driver_status, latitude, longitude, common, userPref)
                    switch_driver_status.text = resources.getString(R.string.on_duty)
                } else {
                    switch_driver_status.text = resources.getString(R.string.off_duty)
                    driver_status.isChecked = false
                    gps.showSettingsAlert()

                    common.changeLocationSocket(this@ChangePasswordActivity, driver_status)
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

                common.changeLocationSocket(this@ChangePasswordActivity, driver_status)
            }
        }

        PasswordValidationGone(edit_current_pass)
        PasswordValidationGone(edit_new_pass)
        PasswordValidationGone(edit_con_pass)
    }

    fun PasswordValidationGone(edt_reg_username: EditText) {
        edt_reg_username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                Log.d("charSequence", "charSequence = " + charSequence.length + "==" + rlMainView.visibility + "==" + View.VISIBLE)
                if (charSequence.length > 0 && rlMainView.visibility == View.VISIBLE) {
                    if (!isFinishing) {
                        val slideUp = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -100f)
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

    companion object {
        private val SERVER_IP = "http://162.243.225.225:4040"
    }
}
