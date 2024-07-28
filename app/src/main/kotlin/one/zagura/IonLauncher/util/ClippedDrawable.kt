package one.zagura.IonLauncher.util

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

internal class ClippedDrawable(
    private val content: Drawable,
    private val path: Path,
    color: Int
) : Drawable() {

    private val shadowPaint = Paint().apply {
        this.color = color
    }

    override fun draw(canvas: Canvas) {
        val s = bounds.width() / content.bounds.width().toFloat()
        shadowPaint.setShadowLayer(21f / s, 0f, 6f / s, 0x22000000)
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(s, s)
        canvas.drawPath(path, shadowPaint)
        canvas.clipPath(path)
        content.draw(canvas)
        canvas.restore()
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) { content.alpha = alpha }
    override fun getAlpha() = content.alpha
    override fun setColorFilter(cf: ColorFilter?) { content.colorFilter = cf }

    override fun getIntrinsicWidth() = content.intrinsicWidth
    override fun getIntrinsicHeight() = content.intrinsicHeight
    override fun getMinimumWidth() = content.minimumWidth
    override fun getMinimumHeight() = content.minimumHeight
}