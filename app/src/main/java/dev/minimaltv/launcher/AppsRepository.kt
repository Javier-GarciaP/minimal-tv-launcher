package dev.minimaltv.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/** Reads the launchable apps installed on the device. */
class AppsRepository(private val context: Context) {

    @Suppress("DEPRECATION")
    fun loadApps(): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)

        return resolved.map { info ->
            val activityInfo = info.activityInfo
            AppEntry(
                packageName = activityInfo.packageName,
                label = info.loadLabel(pm).toString(),
                isSystem = activityInfo.applicationInfo.isSystemApp(),
                icon = info.loadIcon(pm),
            )
        }
    }

    private fun ApplicationInfo.isSystemApp(): Boolean =
        (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
}
