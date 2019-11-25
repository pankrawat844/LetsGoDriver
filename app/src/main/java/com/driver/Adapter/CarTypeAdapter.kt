package com.driver.Adapter

import android.app.Activity
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.driver.CircleTransformation
import com.driver.R
import com.driver.Url

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by techintegrity on 12/10/16.
 */
class CarTypeAdapter(internal var activity: Activity, internal var carTypeArray: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener {
    internal var OpenSans_Regular: Typeface
    private var itemsCount = 0
    private val showLoadingView = false

    private var onCarTypeClickListener: OnCarTypeClickListener? = null

    init {
        OpenSans_Regular = Typeface.createFromAsset(activity.assets, "fonts/opensans-regular.ttf")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(activity).inflate(R.layout.car_type_layout, parent, false)
        val carTypeViewHolder = CarTypeViewHolder(view)
        carTypeViewHolder.layout_car_type_main.setOnClickListener(this)
        return carTypeViewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as CarTypeViewHolder
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            bindCarTypeFeedItem(position, holder)
        } else if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem(holder)
        }
    }

    private fun bindCarTypeFeedItem(position: Int, holder: CarTypeViewHolder) {

        val carTypeHasMap = carTypeArray[position]
        Log.d("Car Image", "Car Image = " + Url.carImageUrl + carTypeHasMap["icon"] + "==" + carTypeHasMap["is_selected"])
        Picasso.with(activity)
                .load(Uri.parse(Url.carImageUrl + carTypeHasMap["icon"]))
                .placeholder(R.drawable.car_icon)
                .transform(CircleTransformation(activity))
                .into(holder.img_car_image)

        holder.txt_car_type.text = carTypeHasMap["car_type"]

        if (carTypeHasMap["is_selected"] == "1")
            holder.layout_car_type_main.setBackgroundColor(activity.resources.getColor(R.color.border_color))
        else
            holder.layout_car_type_main.setBackgroundColor(0)

        holder.layout_car_type_main.tag = holder


    }


    private fun bindLoadingFeedItem(holder: CarTypeViewHolder) {
        println("BindLoadingFeedItem >>>>>")
    }

    override fun getItemCount(): Int {
        return carTypeArray.size
    }

    override fun onClick(view: View) {

        val viewId = view.id
        val holder = view.tag as CarTypeViewHolder

        holder.layout_car_type_main.setBackgroundColor(activity.resources.getColor(R.color.border_color))
        if (viewId == R.id.layout_car_type_main) {
            if (onCarTypeClickListener != null)
                onCarTypeClickListener!!.SelectCarType(holder.position)
        }

    }

    fun updateItems() {
        itemsCount = carTypeArray.size
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (showLoadingView && position == 0) {
            VIEW_TYPE_LOADER
        } else {
            VIEW_TYPE_DEFAULT
        }
    }

    inner class CarTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var layout_car_type_main: RelativeLayout
        internal var img_car_image: ImageView
        internal var txt_car_type: TextView

        init {

            layout_car_type_main = view.findViewById(R.id.layout_car_type_main) as RelativeLayout
            img_car_image = view.findViewById(R.id.img_car_image) as ImageView
            txt_car_type = view.findViewById(R.id.txt_car_type) as TextView
        }
    }

    fun setOnCarTypeItemClickListener(onCarTypeClickListener: OnCarTypeClickListener) {
        this.onCarTypeClickListener = onCarTypeClickListener
    }

    interface OnCarTypeClickListener {

        fun SelectCarType(position: Int)
    }

    companion object {
        private val VIEW_TYPE_DEFAULT = 1
        private val VIEW_TYPE_LOADER = 2
    }
}
