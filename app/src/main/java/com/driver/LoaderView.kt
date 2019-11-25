package com.driver

import android.app.Dialog
import android.content.Context
import android.widget.RelativeLayout

import com.victor.loading.rotate.RotateLoading

/**
 * Created by techintegrity on 08/10/16.
 */

class LoaderView( context: Context) : Dialog(context, R.style.Theme_AppCompat_Translucent) {

    private val rl: RelativeLayout
    private val rotateloading: RotateLoading

    init {
        setContentView(R.layout.loader_dialog)
        rl = findViewById(R.id.rlLoaderMainView) as RelativeLayout
        rotateloading = findViewById(R.id.rotateloading) as RotateLoading
        rotateloading.start()
        setCancelable(true)

    }

    fun loaderObject(): RotateLoading {
        return rotateloading
    }

    fun loaderDismiss() {
        cancel()
        dismiss()
    }

}
