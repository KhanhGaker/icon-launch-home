package one.zagura.IonLauncher.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.TextUtils
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import one.zagura.IonLauncher.data.items.LauncherItem
import one.zagura.IonLauncher.provider.ColorThemer
import one.zagura.IonLauncher.provider.Dock
import one.zagura.IonLauncher.provider.items.IconLoader
import one.zagura.IonLauncher.provider.suggestions.SuggestionsManager
import one.zagura.IonLauncher.ui.ionApplication
import one.zagura.IonLauncher.util.Utils

@SuppressLint("ViewConstructor")
class SuggestionsView(
    context: Context,
    val showDropTargets: () -> Unit,
) : LinearLayout(context) {

    fun update() {
        removeAllViews()
        val suggestions = loadSuggestions()
        if (suggestions.isEmpty()) {
            isVisible = false
            return
        }
        isVisible = true
        val dp = resources.displayMetrics.density
        val height = (36 * dp).toInt()
        val l = LayoutParams(0, height, 1f).apply {
            leftMargin = (12 * dp).toInt()
        }
        for ((i, s) in suggestions.withIndex()) {
            addView(createItemView(s, i, suggestions.size), if (i == 0) LayoutParams(0, LayoutParams.MATCH_PARENT, 1f) else l)
        }
    }

    private fun createItemView(s: LauncherItem, i: Int, columns: Int): View = LinearLayout(context).apply {
        val dp = resources.displayMetrics.density
        orientation = HORIZONTAL
        val r = 99 * dp
        background = ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null))
        backgroundTintList = ColorStateList.valueOf(ColorThemer.foreground(context))
        val height = (36 * dp).toInt()
        addView(ImageView(context).apply {
            setImageDrawable(IconLoader.loadIcon(context, s))
            setPadding((dp * 4).toInt())
        }, LayoutParams(height, LayoutParams.MATCH_PARENT))
        addView(TextView(context).apply {
            text = s.label
            setTextColor(ColorThemer.background(context))
            gravity = Gravity.CENTER_VERTICAL
            ellipsize = TextUtils.TruncateAt.END
            setSingleLine()
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            val m = (2 * dp).toInt()
            leftMargin = m
            rightMargin = m
        })
        setOnClickListener(s::open)
        setOnLongClickListener {
            LongPressMenu.popup(
                it, s,
                Gravity.BOTTOM or Gravity.START,
                this@SuggestionsView.paddingLeft + (this@SuggestionsView.width - this@SuggestionsView.paddingLeft - this@SuggestionsView.paddingRight) / columns * i,
                this@SuggestionsView.height + Utils.getNavigationBarHeight(it.context) + (4 * dp).toInt()
            )
            Utils.startDrag(it, s, it)
            showDropTargets()
            true
        }
        setOnDragListener { v, e ->
            if (e.action == DragEvent.ACTION_DRAG_EXITED) {
                if (e.localState == v)
                    LongPressMenu.dismissCurrent()
            }
            true
        }
    }

    private fun loadSuggestions(): List<LauncherItem> {
        val suggestionCount = context.ionApplication.settings["suggestion:count", 3]
        if (suggestionCount == 0)
            return emptyList()
        val dockItems = Dock.getItems(context)
        return SuggestionsManager.getResource()
            .asSequence()
            .filter { !dockItems.contains(it) }
            .take(suggestionCount)
            .toList()
    }


}