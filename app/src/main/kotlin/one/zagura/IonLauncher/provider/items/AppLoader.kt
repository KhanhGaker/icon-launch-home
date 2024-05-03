package one.zagura.IonLauncher.provider.items

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import one.zagura.IonLauncher.data.items.App
import one.zagura.IonLauncher.provider.HiddenApps
import one.zagura.IonLauncher.provider.UpdatingResource
import one.zagura.IonLauncher.provider.suggestions.SuggestionsManager
import one.zagura.IonLauncher.ui.IonLauncherApp
import one.zagura.IonLauncher.util.TaskRunner
import java.util.TreeSet

object AppLoader : UpdatingResource<List<App>>() {
    override fun getResource() = apps

    private var apps: MutableList<App> = ArrayList()

    fun reloadApps(ionApplication: IonLauncherApp) {
        TaskRunner.submit {
            val userManager = ionApplication.getSystemService(Context.USER_SERVICE) as UserManager
            val launcherApps = ionApplication.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val collection = TreeSet<App> { a, b -> a.label.compareTo(b.label) }

            for (user in userManager.userProfiles) {
                val appList = launcherApps.getActivityList(null, user)
                for (i in appList.indices) {
                    val packageName = appList[i].applicationInfo.packageName
                    val name = appList[i].name
                    val label = appList[i].label.toString().ifEmpty { packageName }
                    val app = App(packageName, name, user, label)
                    if (!HiddenApps.isHidden(ionApplication.settings, app))
                        collection.add(app)
                }
            }
            apps = collection.toMutableList()
            update(apps)
            SuggestionsManager.onAppsLoaded(ionApplication)
        }
    }

    fun loadApp(
        context: Context,
        packageName: String,
        name: String,
        user: UserHandle,
    ): App? {
        return apps.find { it.packageName == packageName && it.name == name && it.userHandle == user } ?: run {
            val launcherApps =
                context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val info = launcherApps.getActivityList(packageName, user)?.find { it.name == name }
                ?: return null
            App(packageName, name, user, info.label.toString())
        }
    }

    fun onHide(app: App) {
        apps.remove(app)
        update(apps)
    }

    fun onShow(app: App) {
        val i = apps.binarySearchBy(app.label) { it.label }
        if (i < 0)
            apps.add(-i - 1, app)
        update(apps)
    }

    class AppCallback(
        val ionApplication: IonLauncherApp,
    ) : LauncherApps.Callback() {

        override fun onPackageRemoved(packageName: String, user: UserHandle) {
            apps.removeAll { it.packageName == packageName && it.userHandle == user }
            update(apps)
            SuggestionsManager.onAppUninstalled(ionApplication, packageName, user)
            IconLoader.removePackage(packageName)
        }

        override fun onPackageAdded(packageName: String?, user: UserHandle?) = reloadApps(ionApplication)

        override fun onPackageChanged(packageName: String, user: UserHandle?) {
            reloadApps(ionApplication)
            IconLoader.removePackage(packageName)
        }

        override fun onPackagesAvailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean
        ) = reloadApps(ionApplication)

        override fun onPackagesUnavailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean
        ) = reloadApps(ionApplication)

        override fun onPackagesSuspended(packageNames: Array<out String>?, user: UserHandle?) = reloadApps(ionApplication)

        override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) = reloadApps(ionApplication)
    }
}