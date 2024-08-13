package one.zagura.IonLauncher.util.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Path
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.record

internal class ReshapedAdaptiveIcon(
    val w: Int,
    background: Drawable,
    foreground: Drawable,
    path: Path,
) : Drawable() {

    private val pic = Picture().record(w, w) {
        clipPath(path)
        background.setBounds(-w / 4, -w / 4, w * 5 / 4, w * 5 / 4)
        background.draw(this)
        foreground.setBounds(-w / 4, -w / 4, w * 5 / 4, w * 5 / 4)
        foreground.draw(this)
    }

    override fun draw(canvas: Canvas) {
        val s = bounds.width() / w.toFloat()
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(s, s)
        pic.draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth() = w
    override fun getIntrinsicHeight() = w
}