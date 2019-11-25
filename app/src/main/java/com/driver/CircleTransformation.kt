package com.driver

import android.content.Context
import android.graphics.*

import com.squareup.picasso.Transformation

class CircleTransformation(internal var cxt: Context) : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)

        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)

        val canvas = Canvas(bitmap)

        val avatarPaint = Paint()
        val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        avatarPaint.shader = shader

        val outlinePaint = Paint()

        //        outlinePaint.setColor(cxt.getResources().getColor(R.color.post_user_image_border));
        outlinePaint.color = Color.WHITE

        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.strokeWidth = STROKE_WIDTH.toFloat()
        outlinePaint.isAntiAlias = true

        val r = size / 2f
        canvas.drawCircle(r, r, r, avatarPaint)
        canvas.drawCircle(r, r, r - STROKE_WIDTH / 2, outlinePaint)

        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "circleTransformation()"
    }

    companion object {

        private val STROKE_WIDTH = 3
    }
}
