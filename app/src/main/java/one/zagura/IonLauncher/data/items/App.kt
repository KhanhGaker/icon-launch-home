package one.zagura.IonLauncher.data.items

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.UserHandle
import android.view.View

class App(
    val packageName: String,
    val name: String,
    val userHandle: UserHandle,
    override val label: String
) : LauncherItem() {

    override fun open(view: View, bounds: Rect) {
        super.open(view, bounds)
        val anim = createOpeningAnimation(view, bounds.left, bounds.top, bounds.right, bounds.bottom)
        try {
            val launcherApps = view.context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            launcherApps.startMainActivity(ComponentName(packageName, name), userHandle, bounds, anim)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun open(view: View) {
        super.open(view)
        val anim = createOpeningAnimation(view)
        try {
            val launcherApps = view.context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            launcherApps.startMainActivity(ComponentName(packageName, name), userHandle, null, anim)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun toString() = "${APP.toString(16)}$packageName/$name/${userHandle.hashCode().toString(16)}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as App
        if (packageName != other.packageName) return false
        if (name != other.name) return false
        return userHandle == other.userHandle
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + userHandle.hashCode()
        return result
    }
}