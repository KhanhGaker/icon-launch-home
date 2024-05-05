package one.zagura.IonLauncher.ui

import android.app.Activity
import android.os.Bundle
import one.zagura.IonLauncher.BuildConfig
import one.zagura.IonLauncher.R
import one.zagura.IonLauncher.provider.ColorThemer
import one.zagura.IonLauncher.ui.view.settings.colorSettings
import one.zagura.IonLauncher.ui.view.settings.onClick
import one.zagura.IonLauncher.ui.view.settings.seekbar
import one.zagura.IonLauncher.ui.view.settings.setSettingsContentView
import one.zagura.IonLauncher.ui.view.settings.setting
import one.zagura.IonLauncher.ui.view.settings.switch

class SuggestionsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSettingsContentView(R.string.suggestions) {
            if (BuildConfig.DEBUG) {
                setting(R.string.suggestions) {
                    onClick(BuildConfig.APPLICATION_ID + ".debug.suggestions.DebugSuggestionsActivity")
                }
            }
            setting(R.string.count, isVertical = true) {
                seekbar("suggestion:count", 3, min = 0, max = 4)
            }
            setting(R.string.show_search_in_suggestions) {
                switch("layout:search-in-suggestions", false)
            }
            colorSettings("pill", ColorThemer.DEFAULT_FG, ColorThemer.DEFAULT_BG, 0xff)
        }
    }
}