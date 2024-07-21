package one.zagura.IonLauncher.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextUtils
import android.view.DragEvent
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import one.zagura.IonLauncher.R
import one.zagura.IonLauncher.data.items.LauncherItem
import one.zagura.IonLauncher.provider.ColorThemer
import one.zagura.IonLauncher.provider.items.IconLoader
import one.zagura.IonLauncher.provider.items.LabelLoader
import one.zagura.IonLauncher.ui.ionApplication
import one.zagura.IonLauncher.util.Settings
import one.zagura.IonLauncher.util.TaskRunner
import one.zagura.IonLauncher.util.Utils
import kotlin.math.abs

class SuggestionRowView(
    context: Context,
    private val drawCtx: SharedDrawingContext,
    private val showDropTargets: () -> Unit,
    private val onSearch: () -> Unit,
) : View(context) {

    private var showSearchButton = false
    private var suggestions = emptyList<LauncherItem>()
    private var labels = emptyArray<CharSequence>()

    private val icSearch = resources.getDrawable(R.drawable.ic_search)

    fun update(allSuggestions: List<LauncherItem>) {
        TaskRunner.submit {
            val newSuggestions = takeSuggestions(allSuggestions)
            if (newSuggestions.isEmpty()) post {
                suggestions = emptyList()
                labels = emptyArray()
                isVisible = showSearchButton
            }
            else post {
                suggestions = newSuggestions
                updateLabels()
                invalidate()
                isVisible = true
            }
        }
    }

    fun applyCustomizations(settings: Settings) {
        showSearchButton = settings["layout:search-in-suggestions", false]
        icSearch.setTint(ColorThemer.iconBackgroundOpaque(context))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val pl = paddingLeft
        val pt = paddingTop
        val pr = paddingRight
        val pb = paddingBottom
        val width = width - pl - pr
        val height = height - pt - pb
        val dp = resources.displayMetrics.density

        canvas.drawRoundRect(
            pl.toFloat(),
            pt.toFloat(),
            pl + width.toFloat(),
            pt + height.toFloat(),
            drawCtx.radius,
            drawCtx.radius,
            drawCtx.pillPaint
        )

        val iconPadding = 8 * dp
        if (showSearchButton) {
            val r = (drawCtx.radius - iconPadding)
                .coerceAtLeast(drawCtx.radius / drawCtx.iconSize * (height - iconPadding * 2))
            val x = pl + width - height
            canvas.drawRoundRect(
                x + iconPadding,
                pt + iconPadding,
                x + height - iconPadding,
                pt + height - iconPadding,
                r, r,
                drawCtx.titlePaint
            )
            val p = (12 * dp).toInt()
            icSearch.setBounds(x + p, pt + p, x + height - p, pt + height - p)
            icSearch.draw(canvas)
        }

        val suggestionsWidth = if (showSearchButton) width - height else width
        val singleWidth = suggestionsWidth.toFloat() / suggestions.size
        var x = pl.toFloat()
        for (i in suggestions.indices) {
            val item = suggestions[i]
            val icon = IconLoader.loadIcon(context, item)
            icon.copyBounds(drawCtx.tmpRect)
            icon.setBounds(
                (x + iconPadding).toInt(),
                (pt + iconPadding).toInt(),
                (x + height - iconPadding).toInt(),
                (pt + height - iconPadding).toInt())
            icon.draw(canvas)
            icon.bounds = drawCtx.tmpRect
            val textX = x + height - iconPadding / 2
            val text = labels[i]
            canvas.drawText(text, 0, text.length, textX, pt + (height + drawCtx.textHeight) / 2f, drawCtx.textPaint)
            x += singleWidth
        }
    }

    override fun onTouchEvent(e: MotionEvent) = gestureListener.onTouchEvent(e)

    private val gestureListener = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent) = true
        override fun onShowPress(e: MotionEvent) {}
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float) = false

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
            if (abs(vy) > abs(vx) && vy > 0)
                Utils.pullStatusBar(context)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (showSearchButton && (
                    suggestions.isEmpty() ||
                    e.x > width - paddingRight - (height - paddingTop - paddingBottom))) {
                onSearch()
                return true
            }
            val i = (e.x.toInt() - paddingLeft) * suggestions.size / (width - paddingLeft - paddingRight)
            if (i < 0 || i >= suggestions.size)
                return false
            suggestions[i].open(this@SuggestionRowView, run {
                val w = (width - paddingLeft - paddingRight) / suggestions.size
                val h = (height - paddingTop - paddingBottom)
                val x = paddingLeft + w * i
                Rect(x, paddingTop, w, h)
            })
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            val i = (e.x.toInt() - paddingLeft) * suggestions.size / (width - paddingLeft - paddingRight)
            if (i < 0 || i >= suggestions.size)
                return
            val item = suggestions[i]
            val dp = resources.displayMetrics.density
            val xOff = paddingLeft + (if (showSearchButton)
                (width - paddingLeft - paddingRight - (height - paddingTop - paddingBottom))
            else
                (width - paddingLeft - paddingRight)) * i / suggestions.size
            LongPressMenu.popup(
                this@SuggestionRowView, item,
                Gravity.BOTTOM or Gravity.START,
                xOff,
                height + Utils.getNavigationBarHeight(context) + (4 * dp).toInt()
            )
            Utils.click(context)
            Utils.startDrag(this@SuggestionRowView, item, this@SuggestionRowView)
            showDropTargets()
        }
    })

    override fun onDragEvent(e: DragEvent): Boolean {
        if (e.action == DragEvent.ACTION_DRAG_EXITED) {
            if (e.localState == this)
                LongPressMenu.dismissCurrent()
        }
        return super.onDragEvent(e)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (suggestions.isEmpty())
            return
        updateLabels()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dp = resources.displayMetrics.density
        val height = (context.ionApplication.settings["dock:icon-size", 48] * dp).toInt()
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(height, heightMeasureSpec)
        )
    }

    private fun updateLabels() {
        val dp = resources.displayMetrics.density
        val iconPadding = 8 * dp
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        val suggestionsWidth =
            if (showSearchButton) width - height
            else width
        val w = suggestionsWidth / suggestions.size - height + iconPadding
        labels = Array(suggestions.size) {
            TextUtils.ellipsize(LabelLoader.loadLabel(context, suggestions[it]), drawCtx.textPaint, w, TextUtils.TruncateAt.END)
        }
    }

    private fun takeSuggestions(allSuggestions: List<LauncherItem>): List<LauncherItem> {
        val suggestionCount = context.ionApplication.settings["suggestion:count", 3]
        if (suggestionCount == 0)
            return emptyList()
        return allSuggestions.take(suggestionCount)
    }
}