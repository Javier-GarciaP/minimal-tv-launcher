package dev.minimaltv.launcher

import android.graphics.drawable.Drawable
import java.text.Collator
import java.util.Locale

/** A launchable app, as shown in the home screen list. */
data class AppEntry(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val icon: Drawable? = null,
)

/** One line of the home screen. */
sealed interface Row {
    /** Small caption above a group of apps. */
    data class Header(val title: String) : Row

    /** A launchable app. */
    data class App(val entry: AppEntry, val featured: Boolean) : Row

    /** The entry that opens the launcher settings. Always the last row. */
    data object Settings : Row
}

/**
 * Builds the ordered list of rows shown on the home screen.
 *
 * Kept free of Android dependencies apart from [Drawable] so it can be unit tested.
 */
object AppListBuilder {

    const val HEADER_FEATURED = "Destacadas"
    const val HEADER_OTHERS = "Todas las apps"

    fun build(
        entries: List<AppEntry>,
        featured: Set<String>,
        ownPackage: String,
        showFeaturedOnly: Boolean = false,
        collator: Collator = defaultCollator(),
    ): List<Row> {
        val visible = entries.filter { it.packageName != ownPackage }
        val sorted = visible.sortedWith { a, b -> collator.compare(a.label, b.label) }
        val (featuredEntries, others) = sorted.partition { it.packageName in featured }

        val rows = ArrayList<Row>(sorted.size + 3)
        if (featuredEntries.isNotEmpty()) {
            rows += Row.Header(HEADER_FEATURED)
            featuredEntries.forEach { rows += Row.App(it, featured = true) }
        }
        if (!showFeaturedOnly) {
            // The second header keeps the rest of the apps visually apart from the
            // featured group, so nothing looks featured by accident.
            if (featuredEntries.isNotEmpty() && others.isNotEmpty()) {
                rows += Row.Header(HEADER_OTHERS)
            }
            others.forEach { rows += Row.App(it, featured = false) }
        }
        rows += Row.Settings
        return rows
    }

    /** Locale aware ordering, so "Ángel" sorts after "Ana" and not before "Abad". */
    fun defaultCollator(): Collator = Collator.getInstance(Locale.getDefault()).apply {
        strength = Collator.SECONDARY
    }
}
