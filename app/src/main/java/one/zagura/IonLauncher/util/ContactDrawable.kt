package one.zagura.IonLauncher.util

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable

internal class ContactDrawable(
    private val text: String,
) : Drawable() {

    companion object {
        private val textPaint = Paint().apply {
            color = 0xffdddddd.toInt()
            typeface = Typeface.SERIF
            textAlign = Paint.Align.CENTER
            textSize = 48f
            isAntiAlias = true
            isSubpixelText = true
        }

        private val bgPaint = Paint().apply {
            this.color = 0xff444444.toInt()
            isAntiAlias = true
        }
    }

    override fun draw(canvas: Canvas) {
        val w = bounds.width() / 2f
        canvas.drawCircle(w, w, w - 1f, bgPaint)
        val x = bounds.width() / 2f
        val y = (bounds.height() - (textPaint.descent() + textPaint.ascent())) / 2f
        canvas.drawText(text, x, y, textPaint)
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}

    override fun getIntrinsicWidth() = 128
    override fun getIntrinsicHeight() = 128
    override fun getMinimumWidth() = 128
    override fun getMinimumHeight() = 128
}