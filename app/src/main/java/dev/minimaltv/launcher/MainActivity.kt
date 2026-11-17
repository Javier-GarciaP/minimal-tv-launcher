package dev.minimaltv.launcher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Home screen of the launcher.
 *
 * A single vertical list on the left over the wallpaper. Everything is loaded on a
 * background thread so the first frame is not delayed by the package manager.
 */
class MainActivity : Activity() {

    private lateinit var prefs: SharedPrefsStore
    private lateinit var adapter: LauncherAdapter
    private lateinit var wallpaper: ImageView

    /** Package of the row that had the focus, so it can be restored after a reload. */
    private var focusedPackage: String? = null
    private var pendingFocusPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = SharedPrefsStore(this)
        wallpaper = findViewById(R.id.wallpaper)

        adapter = LauncherAdapter(
            onAppClick = { entry -> launch(entry) },
            onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
            onFocusChanged = { entry -> focusedPackage = entry?.packageName },
        )
        adapter.fontScale = prefs.fontScale

        val list = findViewById<RecyclerView>(R.id.app_list)
        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)
        list.itemAnimator = null
        list.adapter = adapter

        applyWallpaper()
    }

    override fun onResume() {
        super.onResume()
        pendingFocusPackage = focusedPackage
        loadApps()
    }

    /** Loads the apps off the main thread: the package manager call can be slow. */
    private fun loadApps() {
        Thread({
            val entries = AppsRepository(this).loadApps()
            val rows = AppListBuilder.build(
                entries = entries,
                featured = prefs.featuredPackages,
                ownPackage = packageName,
                showFeaturedOnly = prefs.showFeaturedOnly,
            )
            runOnUiThread {
                adapter.submit(rows)
                restoreFocus()
            }
        }, "app-loader").apply { isDaemon = true }.start()
    }

    /** Puts the focus back on the row the user was on, instead of jumping to the top. */
    private fun restoreFocus() {
        val target = pendingFocusPackage ?: return
        pendingFocusPackage = null
        val position = adapter.positionOf(target)
        if (position == RecyclerView.NO_POSITION) return
        val list = findViewById<RecyclerView>(R.id.app_list)
        list.post {
            val holder = list.findViewHolderForAdapterPosition(position)
            holder?.itemView?.requestFocus()
        }
    }

    private fun launch(entry: AppEntry) {
        val intent = packageManager.getLaunchIntentForPackage(entry.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // The app was uninstalled between the list build and the click.
        }
    }

    private fun applyWallpaper() {
        val solid = prefs.wallpaperMode == SharedPrefsStore.MODE_SOLID
        wallpaper.setImageResource(
            if (solid) R.drawable.wallpaper_solid else R.drawable.wallpaper_mountains,
        )
    }
}
