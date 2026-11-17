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

    /** True while the home screen is the visible screen. */
    private var resumed = false

    /** Wallpaper currently set, so the image is not decoded again on every resume. */
    private var appliedWallpaperMode: String? = null

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
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        // The wallpaper may have been changed in the settings, so it is applied here
        // too: doing it only on create left the old one on screen.
        applyWallpaper()

        if (restartAtFirstApp) {
            restartMenu()
        } else {
            pendingFocusPackage = focusedPackage
        }
        loadApps()
    }

    override fun onPause() {
        super.onPause()
        resumed = false
    }

    /**
     * A HOME press that arrives while the launcher is already on screen.
     *
     * The activity is not resumed again, so the menu is restarted here. When the press
     * comes from another screen, [onResume] picks the flag up instead.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        restartAtFirstApp = true
        if (resumed) {
            restartMenu()
            focusFirstApp()
        }
    }

    /**
     * Leaves the list ready to start from the top, the way the home button behaves on
     * any other launcher.
     */
    private fun restartMenu() {
        restartAtFirstApp = false
        focusedPackage = null
        pendingFocusPackage = null
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
        // With no remembered row the focus goes to the first app, so the list always
        // has a clear starting point.
        val position = target?.let { adapter.positionOf(it) }
            ?: adapter.positionOfFirstApp()
        focusRow(position)
    }

    /** Moves the focus to the first app of the list. */
    private fun focusFirstApp() = focusRow(adapter.positionOfFirstApp())

    /**
     * Focuses the row at [position], scrolling to it first.
     *
     * The rows are synced again once the focus has been granted: rebinding does not
     * fire the focus listener, so the row that keeps it would stay in the unfocused
     * look, with the selection box but without the growth.
     */
    private fun focusRow(position: Int) {
        val list = findViewById<RecyclerView>(R.id.app_list)
        if (position == RecyclerView.NO_POSITION) {
            list.post { syncVisibleRows(list) }
            return
        }
        list.scrollToPosition(position)
        list.post {
            list.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
            list.post { syncVisibleRows(list) }
        }
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

    /**
     * Shows the wallpaper the settings say, and only when it changed: decoding the
     * image again on every resume made the screen flicker.
     */
    private fun applyWallpaper() {
        val mode = prefs.wallpaperMode
        if (mode == appliedWallpaperMode) return
        appliedWallpaperMode = mode
        wallpaper.setImageResource(
            if (mode == SharedPrefsStore.MODE_SOLID) {
                R.drawable.wallpaper_solid
            } else {
                R.drawable.wallpaper_mountains
            },
        )
    }

    companion object {
        /**
         * Set by the settings screens: the home screen goes back to the first app
         * instead of to the row the user had before, so the home button always feels
         * like a starting point of the menu.
         */
        @Volatile
        var restartAtFirstApp: Boolean = false
    }
}
