package one.zagura.IonLauncher.ui

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Build
import androidx.annotation.RequiresApi
import one.zagura.IonLauncher.provider.items.AppLoader
import one.zagura.IonLauncher.provider.items.IconLoader
import one.zagura.IonLauncher.provider.notification.NotificationService
import one.zagura.IonLauncher.provider.search.Search
import one.zagura.IonLauncher.provider.suggestions.SuggestionsManager
import one.zagura.IonLauncher.util.Settings

val Context.ionApplication
    get() = applicationContext as IonLauncherApp
val Activity.ionApplication
    get() = applicationContext as IonLauncherApp

class IonLauncherApp : Application() {

    val settings = Settings("settings")

    override fun onCreate() {
        super.onCreate()
        settings.init(applicationContext)
        SuggestionsManager.onCreate(this)
        setupApps()

        NotificationService.MediaObserver.updateMediaItem(this)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    object AppReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = AppLoader.reloadApps(context)
    }

    private fun setupApps() {
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        launcherApps.registerCallback(AppLoader.AppCallback(applicationContext))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                AppReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                    addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                    addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
                }
            )
        }
        AppLoader.reloadApps(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN)
            IconLoader.clearCache()
        Search.clearData()
        SuggestionsManager.saveToStorage(this)
    }
}