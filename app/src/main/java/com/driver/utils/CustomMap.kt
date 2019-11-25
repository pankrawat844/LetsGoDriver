package com.driver.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet

/**
 * Created by techintegrity on 11/10/16.
 */
class CustomMap : com.google.android.gms.maps.MapView {

    internal var rectF = RectF()

    //    private int cornerRadiusXDP = (int) getResources().getDimension(R.dimen.height_80);
    //    private int cornerRadiusYDP = (int) getResources().getDimension(R.dimen.height_80);
    //
    //    private int mapWidthDP = (int) getResources().getDimension(R.dimen.height_160);
    //    private int mapHeightDP = (int) getResources().getDimension(R.dimen.height_160);

    private val cornerRadiusXDP = 100
    private val cornerRadiusYDP = 100

    private val mapWidthDP = 200
    private val mapHeightDP = 200

    private var ctx: Context? = null

    constructor(context: Context) : super(context) {
        this.ctx = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.ctx = context
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.ctx = context
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val mapWidth = convertDpToPixel(mapWidthDP.toFloat(), this.ctx!!)
        val mapHeigth = convertDpToPixel(mapHeightDP.toFloat(), this.ctx!!)

        rectF.set(0f, 0f, mapWidth, mapHeigth)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val path = Path()
        val count = canvas.save()

        val cornerRadiusX = convertDpToPixel(cornerRadiusXDP.toFloat(), this.ctx!!)
        val cornerRadiusY = convertDpToPixel(cornerRadiusYDP.toFloat(), this.ctx!!)

        path.addRoundRect(rectF, cornerRadiusX, cornerRadiusY, Path.Direction.CW)

        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(count)
    }

    companion object {

        /**
         * Converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @return A float value to represent px equivalent to dp depending on device density
         */
        fun convertDpToPixel(dp: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi / 160f)
        }
    }

}
