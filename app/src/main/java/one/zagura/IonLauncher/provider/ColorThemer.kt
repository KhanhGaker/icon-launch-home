package one.zagura.IonLauncher.provider

import android.content.Context
import androidx.core.graphics.ColorUtils
import one.zagura.IonLauncher.ui.ionApplication
import kotlin.math.abs
import kotlin.math.max

object ColorThemer {
    const val DEFAULT_BG = 0x111111
    const val DEFAULT_FG = 0xfefefe

    fun backgroundOverlay(context: Context): Int {
        val s = context.ionApplication.settings
        return s["color:bg", DEFAULT_BG] and 0xffffff or (s["color:bg:alpha", 0xdd] shl 24)
    }
    fun background(context: Context): Int {
        return context.ionApplication.settings["color:bg", DEFAULT_BG] or 0xff000000.toInt()
    }
    fun foreground(context: Context): Int {
        val c = context.ionApplication.settings["color:fg", DEFAULT_FG]
        return c or 0xff000000.toInt()
    }
    fun hint(context: Context): Int {
        val c = context.ionApplication.settings["color:fg", DEFAULT_FG] and 0xffffff
        return c or 0x91000000.toInt()
    }

    const val COLOR_CARD = 0xff101010.toInt()
    const val COLOR_CARD_SUNK = 0xff000000.toInt()
    const val COLOR_SEPARATOR = 0xff444444.toInt()
    const val COLOR_HINT = 0xff919191.toInt()
    const val COLOR_TEXT = 0xfffefefe.toInt()

    fun level(color: Int, level: Double): Int {
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(color, lab)
        lab[0] = level * 100.0
        val ls = abs(lab[1]) + abs(lab[2])
        val f = (4.0 / ls).coerceAtMost(1.0) + level * 0.3
        lab[1] *= f
        lab[2] *= f
        return ColorUtils.LABToColor(lab[0], lab[1], lab[2])
    }

    fun contrast(color: Int, level: Double, against: Int): Int {
        val againstLab = DoubleArray(3)
        ColorUtils.colorToLAB(against, againstLab)
        return level(color, if (againstLab[0] > 50.0)
            (1.0 - level) * 0.5 else level * 0.5 + 0.5)
    }

    fun saturate(color: Int): Int {
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(color, lab)
        val f = 70.0 / max(abs(lab[1]), abs(lab[2]))
        lab[0] = 70.0
        lab[1] *= f
        lab[2] *= f
        return ColorUtils.LABToColor(lab[0], lab[1], lab[2])
    }

    fun lightness(color: Int): Double {
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(color, lab)
        return lab[0]
    }
}