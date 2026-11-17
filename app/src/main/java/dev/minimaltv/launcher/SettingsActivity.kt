package dev.minimaltv.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Launcher settings: featured apps, wallpaper and text size.
 *
 * Reachable from the "Configuración" entry at the end of the home screen list.
 */
class SettingsActivity : Activity() {

    private lateinit var prefs: SharedPrefsStore
    private lateinit var adapter: OptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = SharedPrefsStore(this)
        findViewById<TextView>(R.id.screen_title).setText(R.string.settings_title)

        val list = findViewById<RecyclerView>(R.id.option_list)
        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)
        list.itemAnimator = null
        adapter = OptionAdapter(buildOptions()) { option -> onOptionClicked(option) }
        list.adapter = adapter
    }

    /**
     * The featured apps can change in the picker, which leaves this screen paused. The
     * values shown here have to be rebuilt when it comes back, or the count of featured
     * apps keeps the number it had before.
     */
    override fun onResume() {
        super.onResume()
        adapter.update(buildOptions())
    }

    private fun buildOptions(): List<Option> {
        val options = mutableListOf<Option>(
            Option.Item(
                R.string.opt_featured,
                getString(R.string.opt_featured_value, prefs.featuredPackages.size),
            ),
            Option.Item(
                R.string.opt_wallpaper,
                getString(
                    if (prefs.wallpaperMode == SharedPrefsStore.MODE_SOLID) {
                        R.string.wallpaper_solid
                    } else {
                        R.string.wallpaper_mountains
                    },
                ),
            ),
            Option.Item(
                R.string.opt_font_size,
                getString(fontScaleLabel(prefs.fontScale)),
            ),
            Option.Item(
                R.string.opt_featured_only,
                getString(if (prefs.showFeaturedOnly) R.string.yes else R.string.no),
            ),
        )
        if (prefs.wallpaperMode != SharedPrefsStore.MODE_MOUNTAINS) {
            options += Option.Action(R.string.opt_reset_wallpaper)
        }
        return options
    }

    private fun onOptionClicked(option: Option) {
        when (option) {
            is Option.Action -> {
                prefs.wallpaperMode = SharedPrefsStore.MODE_MOUNTAINS
                applySystemWallpaper()
                refresh()
            }

            is Option.Item -> when (option.titleRes) {
                R.string.opt_featured ->
                    startActivity(Intent(this, FeaturedPickerActivity::class.java))

                R.string.opt_wallpaper -> pickWallpaper()
                R.string.opt_font_size -> pickFontSize()
                R.string.opt_featured_only -> pickFeaturedOnly()
            }
        }
    }

    private fun pickFeaturedOnly() {
        val options = arrayOf<CharSequence>(
            getString(R.string.no),
            getString(R.string.yes),
        )
        val current = if (prefs.showFeaturedOnly) 1 else 0
        AlertDialog.Builder(this)
            .setTitle(R.string.opt_featured_only)
            .setSingleChoiceItems(options, current) { dialog, which ->
                prefs.showFeaturedOnly = which == 1
                dialog.dismiss()
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun pickWallpaper() {
        val modes = arrayOf<CharSequence>(
            getString(R.string.wallpaper_mountains),
            getString(R.string.wallpaper_solid),
        )
        val current = if (prefs.wallpaperMode == SharedPrefsStore.MODE_SOLID) 1 else 0
        AlertDialog.Builder(this)
            .setTitle(R.string.opt_wallpaper)
            .setSingleChoiceItems(modes, current) { dialog, which ->
                prefs.wallpaperMode =
                    if (which == 1) SharedPrefsStore.MODE_SOLID else SharedPrefsStore.MODE_MOUNTAINS
                if (which == 0) applySystemWallpaper()
                dialog.dismiss()
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun pickFontSize() {
        val labels = arrayOf<CharSequence>(
            getString(R.string.font_small),
            getString(R.string.font_medium),
            getString(R.string.font_large),
        )
        val scales = floatArrayOf(0.85f, 1.0f, 1.2f)
        val current = scales.indexOfFirst { it == prefs.fontScale }.coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(R.string.opt_font_size)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                prefs.fontScale = scales[which]
                dialog.dismiss()
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** Pushes the mountain image to the system wallpaper so the transition is seamless. */
    private fun applySystemWallpaper() {
        runCatching {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.wallpaper_mountains)
            if (bitmap != null) {
                WallpaperManager.getInstance(this).setBitmap(bitmap)
            }
        }
    }

    private fun fontScaleLabel(scale: Float): Int = when {
        scale < 0.95f -> R.string.font_small
        scale > 1.05f -> R.string.font_large
        else -> R.string.font_medium
    }

    private fun refresh() {
        adapter.update(buildOptions())
    }
}

/** A row in the settings list. */
sealed interface Option {
    val titleRes: Int

    /** A row that opens a choice. */
    data class Item(override val titleRes: Int, val value: String) : Option

    /** A row that performs an action straight away. */
    data class Action(override val titleRes: Int) : Option
}

/** Adapter for the settings list, with the same focus behaviour as the home screen. */
class OptionAdapter(
    options: List<Option>,
    private val onClick: (Option) -> Unit,
) : RecyclerView.Adapter<OptionAdapter.Holder>() {

    private var options: List<Option> = options

    /** Rebuilds the rows keeping the adapter, so the focus is not lost. */
    fun update(newOptions: List<Option>) {
        options = newOptions
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = options.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(options[position])
    }

    override fun onViewRecycled(holder: Holder) {
        FocusEffects.sync(holder.itemView, scale = false)
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.option_title)
        private val value: TextView = view.findViewById(R.id.option_value)

        init {
            view.setOnFocusChangeListener { v, hasFocus -> FocusEffects.apply(v, hasFocus, scale = false) }
        }

        fun bind(option: Option) {
            title.setText(option.titleRes)
            when (option) {
                is Option.Item -> {
                    value.text = option.value
                    value.visibility = View.VISIBLE
                }

                is Option.Action -> value.visibility = View.GONE
            }
            itemView.setOnClickListener { onClick(option) }
            FocusEffects.sync(itemView, scale = false)
        }
    }
}
