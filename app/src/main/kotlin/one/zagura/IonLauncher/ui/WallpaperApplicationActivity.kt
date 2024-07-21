package one.zagura.IonLauncher.ui

import android.app.Activity
import android.app.Dialog
import android.app.WallpaperManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import one.zagura.IonLauncher.R
import one.zagura.IonLauncher.ui.view.settings.WallpaperDragView
import one.zagura.IonLauncher.util.TaskRunner
import one.zagura.IonLauncher.util.Utils
import kotlin.math.max
import kotlin.math.min

class WallpaperApplicationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data ?: return
        var wallpaper = Drawable.createFromStream(contentResolver.openInputStream(data), null)
            ?: return finish()
        if (wallpaper is BitmapDrawable) {
            val b = wallpaper.bitmap
            val w = resources.displayMetrics.widthPixels
            val h = resources.displayMetrics.heightPixels
            if (b.width > w && b.height > h) {
                val s = max(w.toFloat() / b.width, h.toFloat() / b.height)
                val bb = Bitmap.createScaledBitmap(b, (b.width * s).toInt(), (b.height * s).toInt(), true)
                wallpaper = BitmapDrawable(resources, bb)
            }
        }
        setContentView(createView(wallpaper))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.setDecorFitsSystemWindows(false)
        else window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )
    }

    private fun createView(wallpaper: Drawable): View {
        return FrameLayout(this).apply {
            val wallView = WallpaperDragView(context, wallpaper)
            val dp = resources.displayMetrics.density
            addView(wallView, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            addView(View(context).apply {
                background = LayerDrawable(arrayOf(
                    GradientDrawable().apply {
                        setStroke(dp.toInt(), 0x44000000, dp * 4, dp * 4)
                    },
                    GradientDrawable().apply {
                        setStroke(dp.toInt(), 0x55ffffff, dp * 4, dp * 4)
                    },
                )).apply {
                    setLayerInset(1, -dp.toInt(), 4 * dp.toInt(), 0, 0)
                    setLayerInset(0, -dp.toInt(), 0, 0, 0)
                }
            }, FrameLayout.LayoutParams(dp.toInt(), MATCH_PARENT, Gravity.CENTER))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                val h = (18 * dp).toInt()
                val v = (8 * dp).toInt()
                setPadding(h, v, h, v + Utils.getNavigationBarHeight(context))
                addView(TextView(context).apply {
                    setText(R.string.apply)
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.color_button_text))
                    val r = 99 * dp
                    background = RippleDrawable(
                        ColorStateList.valueOf(resources.getColor(R.color.color_hint)),
                        ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)).apply {
                            paint.color = resources.getColor(R.color.color_button)
                        }, null)
                    val h = (32 * dp).toInt()
                    val v = (15 * dp).toInt()
                    setPadding(h, v, h, v)
                    gravity = Gravity.CENTER_HORIZONTAL
                    typeface = Typeface.DEFAULT_BOLD
                    setSingleLine()
                    isAllCaps = true
                    setOnClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Dialog(context).apply {
                                val r = 24 * dp
                                window!!.setBackgroundDrawable(ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)).apply {
                                    paint.color = resources.getColor(R.color.color_bg)
                                })
                                setContentView(LinearLayout(context).apply {
                                    orientation = LinearLayout.VERTICAL
                                    val p = (15 * dp).toInt()
                                    val tp = (12 * dp).toInt()
                                    setPadding(p, p, p, p)
                                    addView(TextView(context).apply {
                                        setText(R.string.home_screen)
                                        setTextColor(resources.getColor(R.color.color_text))
                                        setPadding(tp, tp, tp, tp)
                                        textSize = 16f
                                        setOnClickListener {
                                            TaskRunner.submit {
                                                wallView.applyWallpaper(WallpaperManager.FLAG_SYSTEM)
                                            }
                                            finish()
                                        }
                                        background = RippleDrawable(
                                            ColorStateList.valueOf(resources.getColor(R.color.color_disabled)),
                                            ColorDrawable(resources.getColor(R.color.color_bg)), null)
                                    }, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                                    addView(TextView(context).apply {
                                        setText(R.string.lock_screen)
                                        setTextColor(resources.getColor(R.color.color_text))
                                        setPadding(tp, tp, tp, tp)
                                        textSize = 16f
                                        setOnClickListener {
                                            TaskRunner.submit {
                                                wallView.applyWallpaper(WallpaperManager.FLAG_LOCK)
                                            }
                                            finish()
                                        }
                                        background = RippleDrawable(
                                            ColorStateList.valueOf(resources.getColor(R.color.color_disabled)),
                                            ColorDrawable(resources.getColor(R.color.color_bg)), null)
                                    }, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                                    addView(TextView(context).apply {
                                        setText(R.string.both)
                                        setTextColor(resources.getColor(R.color.color_text))
                                        setPadding(tp, tp, tp, tp)
                                        textSize = 16f
                                        setOnClickListener {
                                            TaskRunner.submit {
                                                wallView.applyWallpaper(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                                            }
                                            finish()
                                        }
                                        background = RippleDrawable(
                                            ColorStateList.valueOf(resources.getColor(R.color.color_disabled)),
                                            ColorDrawable(resources.getColor(R.color.color_bg)), null)
                                    }, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                                }, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
                            }.show()
                        } else wallView.applyWallpaper()
                    }
                }, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM))
        }
    }
}