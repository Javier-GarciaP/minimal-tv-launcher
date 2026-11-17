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

    /**
     * Puts the focus back on the row the user was on, instead of jumping to the top.
     *
     * Rebinding the list does not fire the focus listener, so a row that keeps the
     * focus would stay in the unfocused look: the selection box without the growth.
     * The rows are synced again once the focus has been granted.
     */
    private fun restoreFocus() {
        val target = pendingFocusPackage
        pendingFocusPackage = null
        val list = findViewById<RecyclerView>(R.id.app_list)

        val position = target?.let { adapter.positionOf(it) } ?: RecyclerView.NO_POSITION
        if (position != RecyclerView.NO_POSITION) {
            list.scrollToPosition(position)
            list.post {
                list.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
                list.post { syncVisibleRows(list) }
            }
            return
        }
        list.post { syncVisibleRows(list) }
    }

    /** Applies the focus look to every row currently on screen. */
    private fun syncVisibleRows(list: RecyclerView) {
        for (index in 0 until list.childCount) {
            FocusEffects.sync(list.getChildAt(index))
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
