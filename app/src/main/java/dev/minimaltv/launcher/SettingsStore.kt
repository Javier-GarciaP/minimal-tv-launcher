package dev.minimaltv.launcher

/** Contract for the launcher preferences, so consumers can be tested without Android. */
interface SettingsStore {
    /** Package names of the apps pinned to the top of the home screen. */
    var featuredPackages: Set<String>

    /** Either [SharedPrefsStore.MODE_MOUNTAINS] or [SharedPrefsStore.MODE_SOLID]. */
    var wallpaperMode: String

    /** Multiplier applied to the label text size. */
    var fontScale: Float

    /** When true the home screen only lists the featured apps. */
    var showFeaturedOnly: Boolean
}
