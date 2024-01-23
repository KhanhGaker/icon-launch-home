package one.zagura.IonLauncher.ui.settings.suggestions

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import one.zagura.IonLauncher.provider.suggestions.SuggestionsManager
import one.zagura.IonLauncher.ui.settings.common.setupWindow
import one.zagura.IonLauncher.util.Utils

class SuggestionsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()

        val dp = resources.displayMetrics.density

        val recycler = RecyclerView(this).apply {
            val p = (8 * dp).toInt()
            setPadding(p, p + Utils.getStatusBarHeight(context), p, p + Utils.getNavigationBarHeight(context))
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        setContentView(recycler)

        recycler.adapter = SuggestionsAdapter().apply {
            update(SuggestionsManager.getResource())
        }
    }
}