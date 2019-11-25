package com.driver.Adapter


import android.app.Activity
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.afollestad.materialdialogs.MaterialDialog
import com.github.lzyzsd.circleprogress.DonutProgress
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.squareup.picasso.Picasso
import com.driver.CircleTransformation
import com.driver.R
import com.driver.Url
import com.driver.utils.Common
import com.driver.utils.CustomMap
import com.driver.utils.DriverAllTripFeed

import org.json.JSONException
import org.json.JSONObject

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList

/**
 * Created by techintegrity on 11/10/16.
 */
class DriverAllTripAdapter(internal var activity: Activity, internal var TripArray: ArrayList<DriverAllTripFeed>, internal var is_pulltorefresh: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener, OnMapReadyCallback {
    private var itemsCount = 0
    private val showLoadingView = false

    lateinit var OpenSans_Regular: Typeface
    lateinit var OpenSans_Semi_Bold: Typeface
    lateinit var OpenSans_Light: Typeface
    lateinit var Roboto_Bold: Typeface

    private var onAllTripClickListener: OnAllTripClickListener? = null

    internal var userPref: SharedPreferences

    internal var accpet_time: Long = 0
    internal var isAccepted = true

    lateinit var AcceptRejectDialog: Dialog

    private var mMap: GoogleMap? = null
    lateinit var customMap: CustomMap
    lateinit var customCountdownTimer: CountDownTimer
    lateinit var UserLarLng: LatLng
    lateinit var addressTitle: String
    lateinit var mediaPlayer: MediaPlayer
    lateinit var timmer_progress: DonutProgress
    lateinit var minutes_value: TextView

    init {
        userPref = PreferenceManager.getDefaultSharedPreferences(activity)

        OpenSans_Regular = Typeface.createFromAsset(activity.assets, "fonts/opensans-regular.ttf")
        OpenSans_Semi_Bold = Typeface.createFromAsset(activity.assets, "fonts/openSans_semibold.ttf")
        OpenSans_Light = Typeface.createFromAsset(activity.assets, "fonts/OpenSans-Light_0.ttf")
        Roboto_Bold = Typeface.createFromAsset(activity.assets, "fonts/roboto_bold.ttf")

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(activity).inflate(R.layout.driver_trip_layout, parent, false)
        val allTrpViewHol = AllTripViewHolder(view)
        allTrpViewHol.layout_footer_detail.setOnClickListener(this)
        allTrpViewHol.layout_all_trip.setOnClickListener(this)
        allTrpViewHol.layout_detail.setOnClickListener(this)

        return allTrpViewHol
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as AllTripViewHolder
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            bindCabDetailFeedItem(position, holder)
        } else if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem(holder)
        }
    }

    private fun bindCabDetailFeedItem(position: Int, holder: AllTripViewHolder) {

        holder.txt_current_booking.typeface = OpenSans_Semi_Bold
        holder.txt_trip_date.typeface = OpenSans_Regular
        holder.txt_pickup_address.typeface = OpenSans_Light
        holder.txt_drop_address.typeface = OpenSans_Light
        holder.txt_booking_id.typeface = OpenSans_Semi_Bold
        holder.txt_booking_id_val.typeface = OpenSans_Regular

        val allTripFeed = TripArray[position]

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Log.d("Timing", "Timing one= " + allTripFeed.driverFlag)
        //        if(position == 0)
        //        {
        //            allTripFeed.setDriverFlag("0");
        //            allTripFeed.setStatus("1");
        //        }
        if (allTripFeed.driverFlag == "0" && allTripFeed.status != "4") {

            holder.txt_current_booking.text = activity.resources.getString(R.string.pending)
            Picasso.with(activity)
                    .load(R.drawable.status_pending)
                    .into(holder.img_status)
            holder.layout_status_cancle.visibility = View.VISIBLE
            holder.img_user_image.visibility = View.GONE
            holder.layout_detail.visibility = View.GONE

            AcceptRejectDialog = Dialog(activity, R.style.Theme_AppCompat_Translucent)
            AcceptRejectDialog.setContentView(R.layout.accept_reject_dialog_panel)
            AcceptRejectDialog.setCancelable(false)

            val display = activity.windowManager.defaultDisplay
            val layout_main = AcceptRejectDialog.findViewById(R.id.layout_main) as RelativeLayout
            layout_main.layoutParams.height = (display.height * 0.72).toInt()

            customMap = AcceptRejectDialog.findViewById(R.id.mapview) as CustomMap
            MapsInitializer.initialize(activity)
//            customMap.onCreate(savedInstState)
            customMap.onCreate(null)
            customMap.onResume()
            customMap.getMapAsync(this)

            minutes_value = AcceptRejectDialog.findViewById(R.id.minutes_value) as TextView
            timmer_progress = AcceptRejectDialog.findViewById(R.id.timmer_progress) as DonutProgress
            val txt_address_val = AcceptRejectDialog.findViewById(R.id.txt_address_val) as TextView
            txt_address_val.text = allTripFeed.pickupArea

            addressTitle = allTripFeed.pickupArea

            Log.d("Lotlon", "dialog Lotlon = " + allTripFeed.pickupLat + "==" + allTripFeed.pickupLongs)
            UserLarLng = LatLng(java.lang.Double.parseDouble(allTripFeed.pickupLat), java.lang.Double.parseDouble(allTripFeed.pickupLongs))

            val EndTime = allTripFeed.endTime
            val ServerTime = allTripFeed.serverTime

            try {
                val date1 = simpleDateFormat.parse(EndTime)
                val date2 = simpleDateFormat.parse(ServerTime)
                accpet_time = date1.time - date2.time
                Log.d("different", "different = $accpet_time")

            } catch (e: ParseException) {
                e.printStackTrace()
            }

            //accpet_time = 60000;

            if (accpet_time > 0 && accpet_time <= 60000) {
                mediaPlayer = MediaPlayer.create(activity.applicationContext, R.raw.timmer_mussic)

                customCountdownTimer = object : CountDownTimer(accpet_time, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        timmer_progress.progress = (millisUntilFinished / 1000).toInt()
                        minutes_value.text = (millisUntilFinished / 1000).toInt().toString() + " " + activity.resources.getString(R.string.secound)

                        Log.d("mediaPlayer", "mediaPlayer = " + mediaPlayer.isPlaying)
                        if (!mediaPlayer.isPlaying)
                            mediaPlayer.start()
                    }

                    override fun onFinish() {
                        timmer_progress.progress = 0
                        mediaPlayer.stop()
                        AcceptRejectDialog.cancel()

                        allTripFeed.driverFlag = "2"
                        allTripFeed.status = "5"
                        // notifyDataSetChanged();
                        if (Common.BookingId != null && Common.BookingId != "") {
                            notifyItemChanged(position)
                        }
                    }
                }
                //if(!Common.BookingId.equals("")) {
                customCountdownTimer.start()
                // }

                val layout_accept_popup = AcceptRejectDialog.findViewById(R.id.layout_accept_popup) as RelativeLayout
                layout_accept_popup.setOnClickListener(this)
                layout_accept_popup.tag = holder
                val layout_decline_popup = AcceptRejectDialog.findViewById(R.id.layout_decline_popup) as RelativeLayout
                layout_decline_popup.setOnClickListener(this)
                layout_decline_popup.tag = holder

                val txt_accept_popup = AcceptRejectDialog.findViewById(R.id.txt_accept_popup) as TextView
                txt_accept_popup.typeface = Roboto_Bold
                val txt_decline_popup = AcceptRejectDialog.findViewById(R.id.txt_decline_popup) as TextView
                txt_decline_popup.typeface = Roboto_Bold

                AcceptRejectDialog.show()
            } else {
                holder.txt_current_booking.text = activity.resources.getString(R.string.user_cancelled)
                Picasso.with(activity)
                        .load(R.drawable.status_user_cancelled)
                        .into(holder.img_status)
                holder.layout_status_cancle.visibility = View.VISIBLE
                holder.img_user_image.visibility = View.VISIBLE

                Picasso.with(activity)
                        .load(R.drawable.cancelled_stemp)
                        .placeholder(R.drawable.cancelled_stemp)
                        .into(holder.img_user_image)

                holder.layout_detail.visibility = View.VISIBLE

            }

        } else if (allTripFeed.driverFlag == "1" && allTripFeed.status != "4" && allTripFeed.status != "7" && allTripFeed.status != "8") {
            holder.txt_current_booking.text = activity.resources.getString(R.string.accepted)
            Picasso.with(activity)
                    .load(R.drawable.status_accepted)
                    .into(holder.img_status)
            holder.layout_status_cancle.visibility = View.VISIBLE
            holder.img_user_image.visibility = View.VISIBLE
            holder.layout_detail.visibility = View.VISIBLE

            if (allTripFeed.getuserDetail() != null && allTripFeed.getuserDetail() != "") {
                try {
                    val DrvObj = JSONObject(allTripFeed.getuserDetail())
                    if (DrvObj.getString("facebook_id") != "" && DrvObj.getString("image") == "") {
                        val facebookImage = Url.FacebookImgUrl + DrvObj.getString("facebook_id").toString() + "/picture?type=large"
                        Log.d("facebookImage", "facebookImage = $facebookImage")
                        Picasso.with(activity)
                                .load(facebookImage)
                                .placeholder(R.drawable.user_photo)
                                .resize(200, 200)
                                .transform(CircleTransformation(activity))
                                .into(holder.img_user_image)
                    } else {
                        Picasso.with(activity)
                                .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                                .placeholder(R.drawable.user_photo)
                                .transform(CircleTransformation(activity))
                                .into(holder.img_user_image)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

        } else if (allTripFeed.driverFlag == "2" || allTripFeed.status == "4") {
            holder.txt_current_booking.text = activity.resources.getString(R.string.user_cancelled)
            Picasso.with(activity)
                    .load(R.drawable.status_user_cancelled)
                    .into(holder.img_status)
            holder.layout_status_cancle.visibility = View.VISIBLE
            holder.img_user_image.visibility = View.VISIBLE

            Picasso.with(activity)
                    .load(R.drawable.cancelled_stemp)
                    .placeholder(R.drawable.cancelled_stemp)
                    .into(holder.img_user_image)

            holder.layout_detail.visibility = View.VISIBLE

        } else if (allTripFeed.driverFlag == "3" || allTripFeed.status == "9") {
            holder.txt_current_booking.text = activity.resources.getString(R.string.completed)
            Picasso.with(activity)
                    .load(R.drawable.status_completed)
                    .into(holder.img_status)
            holder.layout_status_cancle.visibility = View.VISIBLE
            holder.img_user_image.visibility = View.VISIBLE
            holder.layout_detail.visibility = View.VISIBLE

            if (allTripFeed.getuserDetail() != null && allTripFeed.getuserDetail() != "") {
                try {
                    val DrvObj = JSONObject(allTripFeed.getuserDetail())
                    if (DrvObj.getString("facebook_id") != "" && DrvObj.getString("image") == "") {
                        val facebookImage = Url.FacebookImgUrl + DrvObj.getString("facebook_id").toString() + "/picture?type=large"
                        Log.d("facebookImage", "facebookImage = $facebookImage")
                        Picasso.with(activity)
                                .load(facebookImage)
                                .placeholder(R.drawable.user_photo)
                                .resize(200, 200)
                                .transform(CircleTransformation(activity))
                                .into(holder.img_user_image)
                    } else {
                        Picasso.with(activity)
                                .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                                .placeholder(R.drawable.user_photo)
                                .transform(CircleTransformation(activity))
                                .into(holder.img_user_image)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

        } else if (allTripFeed.status == "7" || allTripFeed.status == "8") {
            var StatusImg = R.drawable.status_on_trip
            if (allTripFeed.status == "8") {
                holder.txt_current_booking.text = activity.resources.getString(R.string.on_trip)
                StatusImg = R.drawable.status_on_trip

                val booking_status = userPref.edit()
                booking_status.putString("booking_status", "begin trip")
                booking_status.commit()
            } else if (allTripFeed.status == "7") {
                holder.txt_current_booking.text = activity.resources.getString(R.string.driver_arrived)
                StatusImg = R.drawable.status_driver_arrived
            }
            Picasso.with(activity)
                    .load(StatusImg)
                    .into(holder.img_status)
            holder.layout_status_cancle.visibility = View.VISIBLE
            holder.img_user_image.visibility = View.VISIBLE

            holder.layout_detail.visibility = View.VISIBLE

            try {
                val DrvObj = JSONObject(allTripFeed.getuserDetail())
                if (DrvObj.getString("facebook_id") != "" && DrvObj.getString("image") == "") {
                    val facebookImage = Url.FacebookImgUrl + DrvObj.getString("facebook_id").toString() + "/picture?type=large"
                    Log.d("facebookImage", "facebookImage = $facebookImage")
                    Picasso.with(activity)
                            .load(facebookImage)
                            .placeholder(R.drawable.user_photo)
                            .resize(200, 200)
                            .transform(CircleTransformation(activity))
                            .into(holder.img_user_image)
                } else {
                    Picasso.with(activity)
                            .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                            .placeholder(R.drawable.user_photo)
                            .transform(CircleTransformation(activity))
                            .into(holder.img_user_image)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        var pickup_date_time = ""
        try {
            val parceDate = simpleDateFormat.parse(allTripFeed.pickupDateTime)
            val parceDateFormat = SimpleDateFormat("dd MMM yyyy")
            pickup_date_time = parceDateFormat.format(parceDate.time)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        Log.d("Booking id", "Booking id = " + allTripFeed.id)
        holder.txt_trip_date.text = pickup_date_time
        holder.txt_pickup_address.text = allTripFeed.pickupArea
        holder.txt_drop_address.text = allTripFeed.dropArea
        holder.txt_booking_id_val.text = allTripFeed.id

        holder.layout_footer_detail.tag = holder
        holder.layout_all_trip.tag = holder
        holder.layout_detail.tag = holder


        if (itemCount > 9 && itemCount == position + 1) {
            if (onAllTripClickListener != null) {
                Log.d("position", "position = $position==$itemCount")
                onAllTripClickListener!!.scrollToLoad(position)
            }
        }
    }

    private fun bindLoadingFeedItem(holder: AllTripViewHolder) {
        println("BindLoadingFeedItem >>>>>")
    }

    override fun getItemViewType(position: Int): Int {
        return if (showLoadingView && position == 0) {
            VIEW_TYPE_LOADER
        } else {
            VIEW_TYPE_DEFAULT
        }
    }

    override fun getItemCount(): Int {
        return TripArray.size
    }

    fun updateItems() {
        itemsCount = TripArray.size
        notifyDataSetChanged()
    }

    fun updateItemsFilter(allTripArray: ArrayList<DriverAllTripFeed>) {
        TripArray = allTripArray
        itemsCount = TripArray.size
        notifyDataSetChanged()
    }

    override fun onClick(v: View) {

        val viewId = v.id
        val holder = v.tag as AllTripViewHolder
        if (viewId == R.id.layout_all_trip || viewId == R.id.layout_footer_detail || viewId == R.id.layout_detail) {
            if (this.onAllTripClickListener != null)
                this.onAllTripClickListener!!.GoTripDetail(holder.adapterPosition)
        } else if (viewId == R.id.layout_accept_popup) {
            customCountdownTimer.onFinish()
            AcceptRejectDialog.cancel()
            if (this.onAllTripClickListener != null)
                this.onAllTripClickListener!!.AcceptCabBookin(holder.position)
        } else if (viewId == R.id.layout_decline_popup) {


            val builder = MaterialDialog.Builder(activity)
                    .title(R.string.delete_trip)
                    .content(R.string.are_you_sure_delete_trip)
                    .negativeText(R.string.dialog_cancel)
                    .positiveText(R.string.dialog_ok)
                    .onPositive { dialog, which ->
                        customCountdownTimer.onFinish()
                        AcceptRejectDialog.cancel()
                        if (onAllTripClickListener != null)
                            onAllTripClickListener!!.RejectCabBookin(holder.position, "")
                    }.onNegative { dialog, which -> }

            val dialog = builder.build()
            dialog.show()


        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mMap!!.addMarker(MarkerOptions().position(UserLarLng)
                .title(addressTitle))
        val cameraPosition = CameraPosition.Builder()
                .target(UserLarLng)      // Sets the center of the map to location user
                .zoom(10f)                   // Sets the zoom
                .build()                   // Creates a CameraPosition from the builder
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    inner class AllTripViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var txt_current_booking: TextView
        internal var txt_trip_date: TextView
        internal var txt_pickup_address: TextView
        internal var txt_drop_address: TextView
        internal var txt_booking_id: TextView
        internal var txt_booking_id_val: TextView
        internal var layout_footer_detail: RelativeLayout
        internal var layout_all_trip: LinearLayout
        internal var layout_status_cancle: RelativeLayout
        internal var img_status: ImageView
        internal var img_user_image: ImageView
        internal var layout_detail: RelativeLayout


        init {

            txt_current_booking = view.findViewById(R.id.txt_current_booking) as TextView
            txt_trip_date = view.findViewById(R.id.txt_trip_date) as TextView
            txt_pickup_address = view.findViewById(R.id.txt_pickup_address) as TextView
            txt_drop_address = view.findViewById(R.id.txt_drop_address) as TextView
            txt_booking_id = view.findViewById(R.id.txt_booking_id) as TextView
            txt_booking_id_val = view.findViewById(R.id.txt_booking_id_val) as TextView
            layout_footer_detail = view.findViewById(R.id.layout_footer_detail) as RelativeLayout
            layout_all_trip = view.findViewById(R.id.layout_all_trip) as LinearLayout
            layout_status_cancle = view.findViewById(R.id.layout_status_cancle) as RelativeLayout
            img_status = view.findViewById(R.id.img_status) as ImageView
            img_user_image = view.findViewById(R.id.img_user_image) as ImageView
            layout_detail = view.findViewById(R.id.layout_detail) as RelativeLayout


        }
    }

    fun setOnAllTripItemClickListener(onAllTripClickListener: OnAllTripClickListener) {
        this.onAllTripClickListener = onAllTripClickListener
    }

    interface OnAllTripClickListener {
        fun AcceptCabBookin(position: Int)
        fun RejectCabBookin(position: Int, timerStart: String)
        fun scrollToLoad(position: Int)
        fun GoTripDetail(position: Int)
    }

    companion object {
        private val VIEW_TYPE_DEFAULT = 1
        private val VIEW_TYPE_LOADER = 2
    }
}
