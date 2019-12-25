package com.driver

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Handler

import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

/**
 * Created by techintegrity on 08/10/16.
 */
object Utility {
    var arrayCarTypeList: ArrayList<HashMap<String, String>>? = null
    var userDetails = UserDetails()
    /* To restrict Space Bar in Keyboard */
    var filter: InputFilter = InputFilter { source, start, end, dest, dstart, dend ->
        for (i in start until end) {
            if (Character.isWhitespace(source[i])) {
                return@InputFilter ""
            }
        }
        null
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidUserName(str: String): Boolean {
        var isValid = false
        val expression = "^[a-z_A-Z0-9]*$"
        val pattern = Pattern.compile(expression)
        val matcher = pattern.matcher(str)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }


    fun showMKPanelErrorServer(act: Activity, error_code: String,error_msg:String, rlMainView: RelativeLayout, tvTitle: TextView, typeface: Typeface) {
        if (!act.isFinishing && rlMainView.visibility == View.GONE) {

            Log.d("rlMainView", "rlMainView = " + rlMainView.visibility + "==" + View.GONE)
            if (rlMainView.visibility == View.GONE) {
                rlMainView.visibility = View.VISIBLE
            }

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
            }else
            {
                message = error_msg

            }

            rlMainView.setBackgroundResource(R.color.mk_error)
            tvTitle.text = message

            tvTitle.typeface = typeface
            val slideUpAnimation = AnimationUtils.loadAnimation(act.applicationContext, R.anim.slide_up_map)
            rlMainView.startAnimation(slideUpAnimation)

        }
    }

    fun showMKPanelError(act: Activity, message: String, rlMainView: RelativeLayout, tvTitle: TextView, typeface: Typeface) {
        if (!act.isFinishing && rlMainView.visibility == View.GONE) {

            Log.d("rlMainView", "rlMainView = " + rlMainView.visibility + "==" + View.GONE)
            if (rlMainView.visibility == View.GONE) {
                rlMainView.visibility = View.VISIBLE
            }

            rlMainView.setBackgroundResource(R.color.mk_error)
            tvTitle.text = message

            tvTitle.typeface = typeface
            val slideUpAnimation = AnimationUtils.loadAnimation(act.applicationContext, R.anim.slide_up_map)
            rlMainView.startAnimation(slideUpAnimation)

        }
    }


    fun showMKPanelInfo(act: Activity, message: String, rlMainView: RelativeLayout, tvTitle: TextView, typeface: Typeface) {
        if (!act.isFinishing && rlMainView.visibility == View.GONE) {

            rlMainView.setBackgroundResource(R.color.mk_info)
            if (rlMainView.visibility == View.GONE) {
                rlMainView.visibility = View.VISIBLE
            }
            tvTitle.text = message
            tvTitle.typeface = typeface
            val slideUpAnimation = AnimationUtils.loadAnimation(act.applicationContext, R.anim.slide_up_map)
            rlMainView.startAnimation(slideUpAnimation)
            Handler().postDelayed({
                try {
                    if (!act.isFinishing) {
                        val slideUp = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -100f)
                        slideUp.duration = 2000
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

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 3000)
        }
    }

    //    public static void showMkError(final Activity act,String message)
    //    {
    //        if(!act.isFinishing()){
    //            final MKInfoPanel mk=new MKInfoPanel(act, MKInfoPanel.MKInfoPanelType.MKInfoPanelTypeError, "",message, 2000);
    //            mk.show();
    //            new Handler().postDelayed(new Runnable() {
    //                @Override
    //                public void run() {
    //                    try {
    //                        if(mk.isShowing() && !act.isFinishing())
    //                            mk.cancel();
    //                    } catch (Exception e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            }, 4000);
    //        }
    //    }

    fun showInternetInfo(act: Activity, message: String) {
        if (!act.isFinishing) {
            val mk = InternetInfoPanel(act, InternetInfoPanel.InternetInfoPanelType.MKInfoPanelTypeInfo, "SUCCESS!", message, 2000)
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

    //    public static void showMkInfo(final Activity act,String message)
    //    {
    //        if(!act.isFinishing()){
    //            final MKInfoPanel mk=new MKInfoPanel(act, MKInfoPanel.MKInfoPanelType.MKInfoPanelTypeInfo, "SUCCESS!",message, 2000);
    //            mk.show();
    //            new Handler().postDelayed(new Runnable() {
    //                @Override
    //                public void run() {
    //                    try {
    //                        if(mk.isShowing() && !act.isFinishing())
    //                            mk.cancel();
    //                    } catch (Exception e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            }, 4000);
    //        }
    //    }


    fun showDialogOK(activity: Activity, message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(activity.resources.getString(R.string.dialog_ok), okListener)
                .setNegativeButton(activity.resources.getString(R.string.dialog_cancel), okListener)
                .create()
                .show()
    }


    fun isNetworkAvailable(act: Activity): Boolean {
        val connMgr = act.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            // fetch data
            true
        } else {
            // display error
            false
        }
    }


    fun checkAndRequestPermissionsGallery(activity: Activity, REQUEST_GALLERY_PERMISSION: Int): Boolean {
        val permissionSendMessage = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        val locationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray<String>(), REQUEST_GALLERY_PERMISSION)
            return false
        }
        return true
    }

    fun checkAndRequestPermissions(activity: Activity, REQUEST_ID_MULTIPLE_PERMISSIONS: Int): Boolean {
        val permissionSendMessage = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        val locationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray<String>(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }
}
