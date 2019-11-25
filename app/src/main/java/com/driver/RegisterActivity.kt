package com.driver

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore

import android.os.Bundle

import android.text.InputFilter
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.textfield.TextInputEditText
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.squareup.picasso.Picasso
import com.driver.Adapter.CarTypeAdapter
import com.driver.utils.Common
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog

import org.apache.commons.lang3.text.WordUtils
import org.json.JSONArray
import org.json.JSONObject

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar


class RegisterActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, CarTypeAdapter.OnCarTypeClickListener {


    lateinit var regularRoboto: Typeface
    lateinit var boldRoboto: Typeface
    lateinit var regularOpenSans: Typeface
    lateinit var boldOpenSans: Typeface

    lateinit var layout_next: RelativeLayout
    lateinit var layout_back: RelativeLayout
    lateinit var layout_header_step_first: LinearLayout
    lateinit var layout_header_step_secound: LinearLayout
    lateinit var layout_header_step_thd: LinearLayout
    lateinit var edt_reg_name: TextInputEditText
    lateinit var edt_reg_username: TextInputEditText
    lateinit var edt_reg_mobile: TextInputEditText
    lateinit var edt_reg_email: TextInputEditText
    lateinit var edt_reg_password: TextInputEditText
    lateinit var edt_reg_confirmpassword: TextInputEditText
    lateinit var edt_reg_dob: TextInputEditText
    lateinit var edt_reg_gender: TextInputEditText
    lateinit var edt_reg_address: TextInputEditText
    lateinit var edt_reg_carmake: TextInputEditText
    lateinit var edt_reg_camodel: TextInputEditText
    lateinit var edt_reg_cartype: TextInputEditText
    lateinit var edt_reg_carnumber: TextInputEditText
    lateinit var edt_reg_seating_capacity: TextInputEditText
    lateinit var edt_reg_driving_license: TextInputEditText
    lateinit var edt_reg_license_exp_date: TextInputEditText
    lateinit var edt_reg_license_plate: TextInputEditText
    lateinit var edt_reg_insuarance: TextInputEditText
    lateinit var register_step_one: View
    lateinit var register_step_two: View
    lateinit var register_step_three: View
    lateinit var icon_pending_first: ImageView
    lateinit var icon_pending_fir_thd: ImageView
    lateinit var icon_pending_sec_thd: ImageView
    lateinit var iv_user_photo: ImageView
    lateinit var tv_signin_register: TextView

    internal var RegisterStep = 0

    //Error Alert
    lateinit var rlMainView: RelativeLayout
    lateinit var tvTitle: TextView
    lateinit var CarTypeId: String

    internal var isPersonalDetailsSelected = false
    internal var isVehicleDetailsSelected = false
    internal var isLegalDetailsSelected = false
    internal var SecoundStepValidation: Boolean? = false
    internal var ThrdStepValidation: Boolean? = false
    lateinit var Cameradialog: Dialog
    private var mCapturedImageURI: Uri? = null
    var isSelectPhoto = 0
    lateinit var photoFile: File

    lateinit var loader: LoaderView
    lateinit var userPref: SharedPreferences
    lateinit var carTypeAdapter: CarTypeAdapter
    lateinit var CarTypeDialog: Dialog
    lateinit var recycle_car_type: RecyclerView
    private var CarTypeLayoutManager: RecyclerView.LayoutManager? = null

    private val imageUri: Uri
        get() {
            val file1 = File(Environment.getExternalStorageDirectory().toString() + "/Texi")
            if (!file1.exists()) {
                file1.mkdirs()
            }
            val file2 = File(Environment.getExternalStorageDirectory().toString() + "/Texi/Camera")
            if (!file2.exists()) {
                file2.mkdirs()
            }
            val file = File(Environment.getExternalStorageDirectory().toString() + "/Texi/Camera/" + System.currentTimeMillis() + ".jpg")
            return Uri.fromFile(file)
        }

    val isValidLegalDetails: Boolean
        get() {
            Log.d("isLegalDetailsSelected", "isLegalDetailsSelected = $isLegalDetailsSelected")
            var isvalid_details = true
            if (edt_reg_driving_license.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_driving_license.text.toString().trim { it <= ' ' }.length == 0) {
                isvalid_details = false
                if (isLegalDetailsSelected)
                    Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_driving_license), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_driving_license.text.toString().trim { it <= ' ' }.length > 16) {
                isvalid_details = false
                if (isLegalDetailsSelected)
                    Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.driving_license_length), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_license_plate.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_license_plate.text.toString().trim { it <= ' ' }.length == 0) {
                isvalid_details = false
                if (isLegalDetailsSelected)
                    Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_license_plate), rlMainView, tvTitle, regularRoboto)
            } else if (!Utility.isValidUserName(edt_reg_license_plate.text.toString())) {
                isvalid_details = false
                if (isLegalDetailsSelected)
                    Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.license_plate_error), rlMainView, tvTitle, regularRoboto)
            } else if (edt_reg_insuarance.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_insuarance.text.toString().trim { it <= ' ' }.length == 0) {
                isvalid_details = false
                if (isLegalDetailsSelected)
                    Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_insuarance), rlMainView, tvTitle, regularRoboto)
            } else if (!Utility.isValidUserName(edt_reg_insuarance.text.toString())) {
                isvalid_details = false
                if (isLegalDetailsSelected)
                    Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.insuarance_error), rlMainView, tvTitle, regularRoboto)
            }
            ThrdStepValidation = isvalid_details
            return isvalid_details
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        isPersonalDetailsSelected = false
        isVehicleDetailsSelected = false
        isLegalDetailsSelected = false

        userPref = PreferenceManager.getDefaultSharedPreferences(this@RegisterActivity)

        register_step_one = findViewById(R.id.register_step_one) as View
        register_step_two = findViewById(R.id.register_step_two) as View
        register_step_three = findViewById(R.id.register_step_three) as View

        //Font
        regularRoboto = Typeface.createFromAsset(assets, getString(R.string.font_regular_roboto))
        boldRoboto = Typeface.createFromAsset(assets, getString(R.string.font_bold_roboto))

        regularOpenSans = Typeface.createFromAsset(assets, getString(R.string.font_regular_opensans))
        boldOpenSans = Typeface.createFromAsset(assets, getString(R.string.font_bold_opensans))

        layout_header_step_first = findViewById(R.id.layout_header_step_first) as LinearLayout
        layout_header_step_secound = findViewById(R.id.layout_header_step_secound) as LinearLayout
        layout_header_step_thd = findViewById(R.id.layout_header_step_thd) as LinearLayout

        //Error Alert
        rlMainView = findViewById(R.id.rlMainView) as RelativeLayout
        tvTitle = findViewById(R.id.tvTitle) as TextView

        edt_reg_name = findViewById(R.id.edt_reg_name) as TextInputEditText
        edt_reg_name.typeface = regularOpenSans
        edt_reg_name.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val strName = edt_reg_name.text.toString()
                edt_reg_name.setText(WordUtils.capitalize(strName))
            }
        }
        edt_reg_username = findViewById(R.id.edt_reg_username) as TextInputEditText
        edt_reg_username.typeface = regularOpenSans
        edt_reg_mobile = findViewById(R.id.edt_reg_mobile) as TextInputEditText
        edt_reg_mobile.typeface = regularOpenSans
        edt_reg_email = findViewById(R.id.edt_reg_email) as TextInputEditText
        edt_reg_email.typeface = regularOpenSans
        edt_reg_password = findViewById(R.id.edt_reg_password) as TextInputEditText
        edt_reg_password.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b)
                Utility.showMKPanelInfo(this@RegisterActivity, resources.getString(R.string.hint_password_msg), rlMainView, tvTitle, regularRoboto)
        }
        edt_reg_password.typeface = regularOpenSans
        edt_reg_confirmpassword = findViewById(R.id.edt_reg_confirmpassword) as TextInputEditText
        edt_reg_confirmpassword.typeface = regularOpenSans
        edt_reg_dob = findViewById(R.id.edt_reg_dob) as TextInputEditText
        edt_reg_dob.typeface = regularOpenSans
        edt_reg_confirmpassword.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edt_reg_dob.performClick()
                return@OnEditorActionListener true
            }
            false
        })
        edt_reg_dob.setOnClickListener { view ->
            view.clearFocus()

            val now = Calendar.getInstance()

            val dpd = DatePickerDialog.newInstance(this@RegisterActivity,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            )
            // dpd.setThemeDark(true);
            // dpd.setThemeDark(false);
            dpd.maxDate = now

            val minCal = Calendar.getInstance()
            minCal.add(Calendar.YEAR, -100)
            dpd.minDate = minCal

            dpd.show(fragmentManager, "Datepickerdialog")
        }

        //Gender
        edt_reg_gender = findViewById(R.id.edt_reg_gender) as TextInputEditText
        edt_reg_gender.typeface = regularOpenSans
        edt_reg_gender.setOnClickListener { view ->
            view.clearFocus()

            var selectGender = 0
            if (edt_reg_gender.text.toString().trim { it <= ' ' }.length > 0 && edt_reg_gender.text.toString().trim { it <= ' ' } == "Male")
                selectGender = 0
            else if (edt_reg_gender.text.toString().trim { it <= ' ' }.length > 0 && edt_reg_gender.text.toString().trim { it <= ' ' } == "Female") {
                selectGender = 1
            }

            val mdGender = MaterialDialog.Builder(this@RegisterActivity)
                    .title(R.string.hint_gender)
                    .items(R.array.gender)
                    .itemsCallbackSingleChoice(-1) { dialog, view, which, gender ->
                        edt_reg_gender.setText(gender)
                        true
                    }
                    .positiveText(R.string.dialog_choose)
                    .show()

            if (edt_reg_gender.text.toString().trim { it <= ' ' }.length > 0)
                mdGender.selectedIndex = selectGender
        }
        edt_reg_address = findViewById(R.id.edt_reg_address) as TextInputEditText
        edt_reg_address.typeface = regularOpenSans
        edt_reg_carmake = findViewById(R.id.edt_reg_carmake) as TextInputEditText
        edt_reg_carmake.typeface = regularOpenSans
        edt_reg_camodel = findViewById(R.id.edt_reg_camodel) as TextInputEditText
        edt_reg_camodel.typeface = regularOpenSans
        edt_reg_cartype = findViewById(R.id.edt_reg_cartype) as TextInputEditText
        edt_reg_cartype.typeface = regularOpenSans

        //Car Type
        edt_reg_cartype = findViewById(R.id.edt_reg_cartype) as TextInputEditText
        edt_reg_cartype.typeface = regularOpenSans

        edt_reg_cartype.setOnClickListener { view ->
            view.clearFocus()

            /*Car Type Dialog Start*/
            CarTypeDialog = Dialog(this@RegisterActivity, android.R.style.Theme_Translucent_NoTitleBar)
            CarTypeDialog.setContentView(R.layout.cartype_dialog)
            recycle_car_type = CarTypeDialog.findViewById(R.id.recycle_car_type) as RecyclerView

            CarTypeLayoutManager = LinearLayoutManager(this@RegisterActivity)
            recycle_car_type.layoutManager = CarTypeLayoutManager

            carTypeAdapter = CarTypeAdapter(this@RegisterActivity, Utility.arrayCarTypeList!!)
            carTypeAdapter.updateItems()
            carTypeAdapter.setOnCarTypeItemClickListener(this@RegisterActivity)
            recycle_car_type.adapter = carTypeAdapter

            CarTypeDialog.show()
            /*Car Type Dialog End*/
        }

        edt_reg_carnumber = findViewById(R.id.edt_reg_carnumber) as TextInputEditText
        edt_reg_carnumber.typeface = regularOpenSans
        edt_reg_seating_capacity = findViewById(R.id.edt_reg_seating_capacity) as TextInputEditText
        edt_reg_seating_capacity.typeface = regularOpenSans
        icon_pending_first = findViewById(R.id.icon_pending_first) as ImageView
        icon_pending_fir_thd = findViewById(R.id.icon_pending_fir_thd) as ImageView
        icon_pending_sec_thd = findViewById(R.id.icon_pending_sec_thd) as ImageView

        edt_reg_driving_license = findViewById(R.id.edt_reg_driving_license) as TextInputEditText
        edt_reg_driving_license.typeface = regularOpenSans
        val filter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isLetterOrDigit(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        edt_reg_driving_license.filters = arrayOf(filter)

        edt_reg_license_exp_date = findViewById(R.id.edt_reg_license_exp_date) as TextInputEditText
        edt_reg_license_exp_date.typeface = regularOpenSans
        edt_reg_license_exp_date.setOnClickListener { view ->
            view.clearFocus()
            val now = Calendar.getInstance()
            val dpd = DatePickerDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->
                val expiryDate = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                edt_reg_license_exp_date.setText(expiryDate)
            },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            )
            dpd.minDate = now
            val cMax = Calendar.getInstance()
            cMax.set(Calendar.YEAR, 2050)
            cMax.set(Calendar.MONTH, Calendar.DECEMBER)
            cMax.set(Calendar.DAY_OF_MONTH, 31)
            dpd.maxDate = cMax
            dpd.show(fragmentManager, "Datepickerdialog")
        }
        edt_reg_license_plate = findViewById(R.id.edt_reg_license_plate) as TextInputEditText
        edt_reg_license_plate.typeface = regularOpenSans
        edt_reg_insuarance = findViewById(R.id.edt_reg_insuarance) as TextInputEditText
        edt_reg_insuarance.typeface = regularOpenSans

        edt_reg_insuarance.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val thdStep = isValidLegalDetails
                if (thdStep) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(edt_reg_insuarance.windowToken, 0)
                    return@OnEditorActionListener true
                } else
                    return@OnEditorActionListener false
            }
            false
        })

        layout_back = findViewById(R.id.layout_back) as RelativeLayout
        layout_back.setOnClickListener {
            val height = resources.getDimension(R.dimen.height_25).toInt()
            isLegalDetailsSelected = false
            isVehicleDetailsSelected = false

            RegisterStep = RegisterStep - 1
            Log.d("RegisterStep", "RegisterStep = $RegisterStep==$SecoundStepValidation")
            if (RegisterStep == 0) {
                layout_header_step_first.visibility = View.VISIBLE
                layout_header_step_secound.visibility = View.GONE
                layout_header_step_thd.visibility = View.GONE

                register_step_one.visibility = View.VISIBLE
                register_step_two.visibility = View.GONE
                register_step_three.visibility = View.GONE
                layout_next.visibility = View.VISIBLE
                layout_back.visibility = View.GONE


                if (SecoundStepValidation!!) {
                    Picasso.with(this@RegisterActivity)
                            .load(R.drawable.icon_done)
                            .resize(height, height)
                            .into(icon_pending_first)
                } else {
                    Picasso.with(this@RegisterActivity)
                            .load(R.drawable.icon_pending)
                            .resize(height, height)
                            .into(icon_pending_first)
                }
                if (ThrdStepValidation!!) {
                    Picasso.with(this@RegisterActivity)
                            .load(R.drawable.icon_done)
                            .resize(height, height)
                            .into(icon_pending_fir_thd)
                } else {
                    Picasso.with(this@RegisterActivity)
                            .load(R.drawable.icon_pending)
                            .resize(height, height)
                            .into(icon_pending_fir_thd)
                }

            } else if (RegisterStep == 1) {
                layout_header_step_first.visibility = View.GONE
                layout_header_step_secound.visibility = View.VISIBLE
                layout_header_step_thd.visibility = View.GONE

                register_step_one.visibility = View.GONE
                register_step_two.visibility = View.VISIBLE
                register_step_three.visibility = View.GONE
                layout_next.visibility = View.VISIBLE
                layout_back.visibility = View.VISIBLE

                if (ThrdStepValidation!!) {
                    Picasso.with(this@RegisterActivity)
                            .load(R.drawable.icon_done)
                            .resize(height, height)
                            .into(icon_pending_sec_thd)
                } else {
                    Picasso.with(this@RegisterActivity)
                            .load(R.drawable.icon_pending)
                            .resize(height, height)
                            .into(icon_pending_sec_thd)
                }

            } else if (RegisterStep == 2) {
                layout_header_step_first.visibility = View.GONE
                layout_header_step_secound.visibility = View.GONE
                layout_header_step_thd.visibility = View.VISIBLE

                register_step_one.visibility = View.GONE
                register_step_two.visibility = View.GONE
                register_step_three.visibility = View.VISIBLE
                layout_next.visibility = View.GONE
                layout_back.visibility = View.VISIBLE

            }

            //realViewSwitcher.setCurrentScreen(RegisterStep,true);
        }

        layout_next = findViewById(R.id.layout_next) as RelativeLayout
        layout_next.setOnClickListener {
            val firstStep = PersonalDetailValidation()
            val secoundStep = VehicleDetailValidation()
            val thdStep = isValidLegalDetails

            isVehicleDetailsSelected = true
            Log.d("firstStap", "firstStap = $firstStep==$secoundStep==$RegisterStep")
            if (RegisterStep == 0) {
                if (firstStep) {
                    layout_header_step_first.visibility = View.GONE
                    layout_header_step_secound.visibility = View.VISIBLE
                    layout_header_step_thd.visibility = View.GONE

                    register_step_one.visibility = View.GONE
                    register_step_two.visibility = View.VISIBLE
                    register_step_three.visibility = View.GONE
                    layout_next.visibility = View.VISIBLE
                    layout_back.visibility = View.VISIBLE
                    RegisterStep = RegisterStep + 1
                    if (secoundStep) {
                        Picasso.with(this@RegisterActivity)
                                .load(R.drawable.icon_done)
                                .into(icon_pending_first)
                    } else {
                        Picasso.with(this@RegisterActivity)
                                .load(R.drawable.icon_pending)
                                .into(icon_pending_first)
                    }
                    if (thdStep) {
                        Picasso.with(this@RegisterActivity)
                                .load(R.drawable.icon_done)
                                .into(icon_pending_fir_thd)
                    } else {
                        Picasso.with(this@RegisterActivity)
                                .load(R.drawable.icon_pending)
                                .into(icon_pending_fir_thd)
                    }
                }
            } else if (RegisterStep == 1) {
                if (firstStep && secoundStep) {
                    RegisterStep = RegisterStep + 1
                    layout_header_step_first.visibility = View.GONE
                    layout_header_step_secound.visibility = View.GONE
                    layout_header_step_thd.visibility = View.VISIBLE

                    register_step_one.visibility = View.GONE
                    register_step_two.visibility = View.GONE
                    register_step_three.visibility = View.VISIBLE

                    layout_back.visibility = View.VISIBLE
                    layout_next.visibility = View.GONE
                    if (thdStep) {
                        Picasso.with(this@RegisterActivity)
                                .load(R.drawable.icon_done)
                                .into(icon_pending_fir_thd)
                    } else {
                        Picasso.with(this@RegisterActivity)
                                .load(R.drawable.icon_pending)
                                .into(icon_pending_fir_thd)
                    }
                    isLegalDetailsSelected = true
                }
            }
        }

        iv_user_photo = findViewById(R.id.iv_user_photo) as ImageView
        iv_user_photo.setOnClickListener {
            Cameradialog = Dialog(this@RegisterActivity, android.R.style.Theme_Translucent_NoTitleBar)
            Cameradialog.setContentView(R.layout.custom_dialog)

            val tv_gallery = Cameradialog.findViewById(R.id.tv_gallery) as TextView
            tv_gallery.typeface = boldRoboto
            val galRelLayout = Cameradialog.findViewById(R.id.gallery_layout) as RelativeLayout
            galRelLayout.setOnClickListener {
                if (Utility.checkAndRequestPermissionsGallery(this@RegisterActivity, REQUEST_GALLERY_PERMISSION)) {
                    launchGallery()
                }
            }

            val tv_camera = Cameradialog.findViewById(R.id.tv_camera) as TextView
            tv_camera.typeface = boldRoboto
            val cameraRelLayout = Cameradialog.findViewById(R.id.camera_layout) as RelativeLayout
            cameraRelLayout.setOnClickListener {
                if (Utility.checkAndRequestPermissions(this@RegisterActivity, REQUEST_ID_MULTIPLE_PERMISSIONS)) {
                    // carry on the normal flow, as the case of  permissions  granted.
                    requestForCameraPermission()
                }
            }

            val dialogMainLayout = Cameradialog.findViewById(R.id.dialog_main_layout) as RelativeLayout
            dialogMainLayout.setOnClickListener { Cameradialog.dismiss() }

            val CancelImage = Cameradialog.findViewById(R.id.img_cancel) as ImageView
            CancelImage.setOnClickListener { Cameradialog.dismiss() }

            Cameradialog.show()
        }

        tv_signin_register = findViewById(R.id.tv_signin_register) as TextView
        tv_signin_register.setOnClickListener {
            val firstStep = PersonalDetailValidation()
            val secoundStep = VehicleDetailValidation()
            val thdStep = isValidLegalDetails

            if (firstStep && secoundStep && thdStep) {
                if (Utility.arrayCarTypeList != null && Utility.arrayCarTypeList!!.size > 0) {
                    for (i in Utility.arrayCarTypeList!!.indices) {
                        val hashMap = Utility.arrayCarTypeList!![i]
                        val strCarType = hashMap["car_type"].toString()
                        if (strCarType == edt_reg_cartype.text.toString().trim { it <= ' ' }) {
                            println("Selected CarType >>$strCarType")
                        }
                    }
                }

                loader = LoaderView(this@RegisterActivity)
                loader.show()
                postSignup()
            }
        }

        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_name)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_username)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_mobile)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_email)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_password)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_confirmpassword)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_dob)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_gender)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_address)

        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_carmake)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_camodel)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_cartype)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_carnumber)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_seating_capacity)

        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_driving_license)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_license_exp_date)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_license_plate)
        Common.validationGone(this@RegisterActivity, rlMainView, edt_reg_insuarance)

    }

    private fun launchGallery() {
        val libraryIntent = Intent(Intent.ACTION_PICK)
        libraryIntent.type = "image/*"
        startActivityForResult(libraryIntent, 2)
    }

    fun requestForCameraPermission() {
        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this@RegisterActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@RegisterActivity, permission)) {
                requestForPermission(permission)
            } else {
                requestForPermission(permission)
            }
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {

        val storageState = Environment.getExternalStorageState()

        if (storageState == Environment.MEDIA_MOUNTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            mCapturedImageURI = imageUri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)
            startActivityForResult(intent, 1)
        } else {
            AlertDialog.Builder(this@RegisterActivity).setMessage("External Storeage (SD Card) is required.\n\nCurrent state: $storageState").setCancelable(true).create().show()
        }
    }

    private fun requestForPermission(permission: String) {
        ActivityCompat.requestPermissions(this@RegisterActivity, arrayOf(permission), REQUEST_CAMERA_PERMISSION)
    }

    fun PersonalDetailValidation(): Boolean {

        var isvalid_details = true
        if (isSelectPhoto == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_select_image), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_name.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_name.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_name), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_name.text.toString().length != 0 && edt_reg_name.text.toString().length < 4) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.minimum_name_charactor), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_name.text.toString().length != 0 && edt_reg_name.text.toString().length > 30) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.maximum_name_charactor), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_username.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_username.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_username), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_username.text.toString().length != 0 && edt_reg_username.text.toString().length < 4) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.minimum_user_charactor), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_username.text.toString().length != 0 && edt_reg_username.text.toString().length > 30) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.maximum_user_charactor), rlMainView, tvTitle, regularRoboto)
        } else if (!Utility.isValidUserName(edt_reg_username.text.toString())) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.username_error), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_mobile.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_mobile.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_mobileno), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_mobile.text.toString().trim { it <= ' ' }.length < 10) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.min_mobile_number), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_email.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_email.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_email), rlMainView, tvTitle, regularRoboto)
        } else if (!Utility.isValidEmail(edt_reg_email.text.toString())) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_valid_email), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_password.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_password), rlMainView, tvTitle, regularRoboto)
        } else if (!edt_reg_password.text.toString().trim { it <= ' ' }.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$".toRegex())) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.hint_password_msg), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_password.text.toString().trim { it <= ' ' }.length < 6 || edt_reg_password.text.toString().trim { it <= ' ' }.length > 32) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.password_length), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_password.text.toString().trim { it <= ' ' }.length > 32) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.large_password), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_confirmpassword.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_confirm_password), rlMainView, tvTitle, regularRoboto)
        } else if (!edt_reg_confirmpassword.text.toString().trim { it <= ' ' }.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$".toRegex())) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.hint_password_msg), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_confirmpassword.text.toString().trim { it <= ' ' }.length < 6 || edt_reg_confirmpassword.text.toString().trim { it <= ' ' }.length > 32) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.password_length), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_confirmpassword.text.toString().trim { it <= ' ' }.length > 32) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.large_password), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_confirmpassword.text.toString().trim { it <= ' ' } != edt_reg_password.text.toString().trim { it <= ' ' }) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.password_does_not_match), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_dob.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_dob.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_dob), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_gender.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_gender.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.select_gender), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_address.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_address.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_address), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_address.text.toString().trim { it <= ' ' }.matches("^[0-9]*$".toRegex())) {
            isvalid_details = false
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_address_number), rlMainView, tvTitle, regularRoboto)
        }//        else if(isSelectPhoto==0){
        //            isvalid_details=false;
        //            Utility.showMKPanelError(RegisterActivity.this, getResources().getString(R.string.please_select_image),rlMainView,tvTitle,regularRoboto);
        //        }
        return isvalid_details
    }

    fun VehicleDetailValidation(): Boolean {

        var isvalid_details = true
        if (edt_reg_carmake.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_carmake.text.toString().trim { it <= ' ' }.length == 0) {
            //Toast.makeText(RegisterActivity.this,"Error",Toast.LENGTH_LONG).show();
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_carmake), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_carmake.text.toString().trim { it <= ' ' }.matches("^[0-9]*$".toRegex())) {
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.please_enter_carmake_number), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_camodel.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_camodel.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_carmodel), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_cartype.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_cartype.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_cartype), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_carnumber.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_carnumber.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_carnumber), rlMainView, tvTitle, regularRoboto)
        } else if (!Utility.isValidUserName(edt_reg_carnumber.text.toString())) {
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.carnumber_error), rlMainView, tvTitle, regularRoboto)
        } else if (edt_reg_seating_capacity.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true) || edt_reg_seating_capacity.text.toString().trim { it <= ' ' }.length == 0) {
            isvalid_details = false
            if (isVehicleDetailsSelected)
                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.enter_seating_capacity), rlMainView, tvTitle, regularRoboto)
        } else if (!edt_reg_seating_capacity.text.toString().trim { it <= ' ' }.matches("^[0-9]*$".toRegex())) {
            isvalid_details = false
            if (isVehicleDetailsSelected)

                Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.seating_capacity_error), rlMainView, tvTitle, regularRoboto)
        }
        SecoundStepValidation = isvalid_details
        return isvalid_details
    }


    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
        val userAge = GregorianCalendar(year, monthOfYear, dayOfMonth)
        val minAdultAge = GregorianCalendar()
        minAdultAge.add(Calendar.YEAR, -18)
        if (minAdultAge.before(userAge)) {
            edt_reg_dob.setText("")
            Utility.showMKPanelError(this@RegisterActivity, resources.getString(R.string.dob_error), rlMainView, tvTitle, regularRoboto)
        } else {
            edt_reg_dob.setText(date)
        }
    }

    fun setImage(imagePath: Uri?) {
        try {

            val file = File(imagePath!!.path!!)
            val exif = ExifInterface(file.path)
            val orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
            val orientation = if (orientString != null) Integer.parseInt(orientString) else ExifInterface.ORIENTATION_NORMAL
            var rotationAngle = 0
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270

            val bitmap = resizeBitMapImage(imagePath.path, 200, 200)
            val RotateBitmap = RotateBitmap(bitmap, rotationAngle.toFloat())
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
            val currentDateandTime = sdf.format(Date())
            val myFinalImagePath = saveToInternalSorage(RotateBitmap, currentDateandTime)
            val fileimg = File(myFinalImagePath)
            photoFile = fileimg
            Picasso.with(this@RegisterActivity).load(fileimg).resize(250, 250).centerCrop().transform(CircleTransformation(this)).into(iv_user_photo)
            isSelectPhoto = 1
            Cameradialog.cancel()

        } catch (es: Exception) {
            es.printStackTrace()
            println("==== exceptin in setimage : $es")
        }

    }

    private fun saveToInternalSorage(bitmapImage: Bitmap, imageName: String): String {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "$imageName.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return directory.absolutePath + "/" + imageName + ".jpg"
    }

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (requestCode == 1 || requestCode == 2 || requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 1) {
                    if (mCapturedImageURI != null) {
                        setImage(mCapturedImageURI)//.getPath().toString());
                    } else {
                        setImage(mCapturedImageURI)//.getPath().toString());
                    }
                } else if (requestCode == 2) {
                    val selImagePath = getPath(data?.data)
                    mCapturedImageURI = Uri.parse(selImagePath)
                    setImage(mCapturedImageURI)
                }

                if (!isFinishing && rlMainView.visibility == View.VISIBLE) {
                    val slideUp = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -100f)
                    slideUp.duration = 100
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
    }

    fun postSignup() {
        Log.d("CarTypeId", "CarTypeId = $CarTypeId")
        Ion.with(this@RegisterActivity)
                .load(Url.driver_sign_up)
                .setMultipartParameter("name", edt_reg_name.text.toString().trim { it <= ' ' })
                .setMultipartParameter("username", edt_reg_username.text.toString().trim { it <= ' ' })
                .setMultipartParameter("phone", edt_reg_mobile.text.toString().trim { it <= ' ' })
                .setMultipartParameter("email", edt_reg_email.text.toString().trim { it <= ' ' })
                .setMultipartParameter("password", edt_reg_password.text.toString().trim { it <= ' ' })
                .setMultipartParameter("dob", edt_reg_dob.text.toString().trim { it <= ' ' })
                .setMultipartParameter("gender", edt_reg_gender.text.toString().trim { it <= ' ' })
                .setMultipartParameter("address", edt_reg_address.text.toString().trim { it <= ' ' })
                .setMultipartParameter("Car_Make", edt_reg_carmake.text.toString().trim { it <= ' ' })
                .setMultipartParameter("Car_Model", edt_reg_camodel.text.toString().trim { it <= ' ' })
                .setMultipartParameter("car_type", CarTypeId)
                .setMultipartParameter("car_no", edt_reg_carnumber.text.toString().trim { it <= ' ' })
                .setMultipartParameter("Seating_Capacity", edt_reg_seating_capacity.text.toString().trim { it <= ' ' })
                .setMultipartParameter("license_no", edt_reg_driving_license.text.toString().trim { it <= ' ' })
                .setMultipartParameter("Lieasence_Expiry_Date", edt_reg_license_exp_date.text.toString().trim { it <= ' ' })
                .setMultipartParameter("license_plate", edt_reg_license_plate.text.toString().trim { it <= ' ' })
                .setMultipartParameter("Insurance", edt_reg_insuarance.text.toString().trim { it <= ' ' })
                .setMultipartParameter("isdevice", "1")
                .setMultipartFile("image", photoFile)
                .asJsonObject()
                .setCallback(FutureCallback { e, result ->
                    // do stuff with the result or error
                    println("Register Response >>>$result")
                    println("Register Response >>>" + e!!)

                    loader.loaderObject().stop()
                    loader.loaderDismiss()

                    if (e != null) {
                        Toast.makeText(this@RegisterActivity, "Register Error$e", Toast.LENGTH_LONG).show()
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

                            val intent = Intent(this@RegisterActivity, DriverTripActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()

                        } else {
                            Utility.showMKPanelErrorServer(this@RegisterActivity, jsonObject.getString("error code").toString(), rlMainView, tvTitle, regularRoboto)
                        }
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                })

    }

    override fun SelectCarType(position: Int) {
        if (Utility.arrayCarTypeList!!.size > 0) {

            for (i in Utility.arrayCarTypeList!!.indices) {
                Log.d("Car Image ", "Car Image $position==$i")
                val cartypHasMap = Utility.arrayCarTypeList!![i]
                cartypHasMap["is_selected"] = "0"
                if (position == i) {
                    cartypHasMap["is_selected"] = "1"
                    cartypHasMap["is_selected"] = "1"
//                    cartypHasMap["car_id"] = cartypHasMap["car_id"]
//                    cartypHasMap["car_type"] = cartypHasMap["car_type"]
//                    cartypHasMap["icon"] = cartypHasMap["icon"]
//                    cartypHasMap["car_rate"] = cartypHasMap["car_rate"]
//                    cartypHasMap["seating_capecity"] = cartypHasMap["seating_capecity"]
                    edt_reg_cartype.setText(cartypHasMap["car_type"])
                    CarTypeId = cartypHasMap["car_id"].toString()
                }
                Utility.arrayCarTypeList!![i] = cartypHasMap
            }

            CarTypeDialog.cancel()
        }
    }

    companion object {

        //Photos
        val REQUEST_ID_MULTIPLE_PERMISSIONS = 3
        val REQUEST_GALLERY_PERMISSION = 2
        private val REQUEST_CAMERA_PERMISSION = 1

        fun resizeBitMapImage(filePath: String?, targetWidth: Int, targetHeight: Int): Bitmap? {
            var bitMapImage: Bitmap? = null
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(filePath, options)
                var sampleSize = 0.0
                val scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth - targetWidth)
                if (options.outHeight * options.outWidth * 2 >= 1638) {
                    sampleSize = (if (scaleByHeight) options.outHeight / targetHeight else options.outWidth / targetWidth).toDouble()
                    sampleSize = Math.pow(2.0, Math.floor(Math.log(sampleSize) / Math.log(2.0))).toInt().toDouble()
                }
                options.inJustDecodeBounds = false
                options.inTempStorage = ByteArray(128)
                while (true) {
                    try {
                        options.inSampleSize = sampleSize.toInt()
                        bitMapImage = BitmapFactory.decodeFile(filePath, options)
                        break
                    } catch (ex: Exception) {
                        try {
                            sampleSize = sampleSize * 2
                        } catch (ex1: Exception) {

                        }

                    }

                }
            } catch (ex: Exception) {

            }

            return bitMapImage
        }

        fun RotateBitmap(source: Bitmap?, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source!!, 0, 0, source.width, source.height, matrix, true)
        }
    }
}
