package com.driver

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout

/**
 * Created by techintegrity on 08/10/16.
 */
class InternetInfoPanel(context: Context, type: InternetInfoPanelType, titletext: String, subtitletext: String, interval: Int) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {
    private val rl: RelativeLayout

    val iv_ok: ImageView

    enum class InternetInfoPanelType {
        MKInfoPanelTypeInfo,
        MKInfoPanelTypeError
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.internetinfopanel)
        rl = findViewById(R.id.main) as RelativeLayout
        iv_ok = findViewById(R.id.iv_ok) as ImageView
        setCancelable(true)

    }

    companion object {

        protected val TAG = InternetInfoPanel::class.java!!.getName()
    }

}
