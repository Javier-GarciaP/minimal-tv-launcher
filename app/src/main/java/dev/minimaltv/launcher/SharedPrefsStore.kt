package dev.minimaltv.launcher

import android.content.SharedPreferences

/**
 * Launcher preferences.
 *
 * Takes a [SharedPreferences] instead of a `Context` so it can be unit tested with a
 * fake implementation. No storage permission is involved: everything lives in the app's
 * private directory.
 */
class SharedPrefsStore(private val prefs: SharedPreferences) : SettingsStore {

    constructor(context: android.content.Context) : this(
        context.applicationContext.getSharedPreferences(FILE_NAME, android.content.Context.MODE_PRIVATE),
    )

    override var featuredPackages: Set<String>
        get() = prefs.getStringSet(KEY_FEATURED, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_FEATURED, value).apply()

    override var wallpaperMode: String
        get() = prefs.getString(KEY_WALLPAPER, MODE_MOUNTAINS) ?: MODE_MOUNTAINS
        set(value) = prefs.edit().putString(KEY_WALLPAPER, value).apply()

    override var fontScale: Float
        get() = prefs.getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE)
        set(value) = prefs.edit().putFloat(KEY_FONT_SCALE, value).apply()

    override var showFeaturedOnly: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FEATURED_ONLY, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FEATURED_ONLY, value).apply()

    companion object {
        const val FILE_NAME = "launcher"
        const val KEY_FEATURED = "featured"
        const val KEY_WALLPAPER = "wallpaper"
        const val KEY_FONT_SCALE = "font_scale"
        const val KEY_SHOW_FEATURED_ONLY = "show_featured_only"

        const val MODE_MOUNTAINS = "mountains"
        const val MODE_SOLID = "solid"

        const val DEFAULT_FONT_SCALE = 1.0f
    }
}
