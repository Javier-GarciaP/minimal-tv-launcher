package dev.minimaltv.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the home screen list: a header row, the apps and the settings entry.
 */
class LauncherAdapter(
    private val onAppClick: (AppEntry) -> Unit,
    private val onSettingsClick: () -> Unit,
    private val onFocusChanged: (AppEntry?) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var rows: List<Row> = emptyList()

    /** Multiplier applied to the label text size. */
    var fontScale: Float = 1.0f
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun submit(newRows: List<Row>) {
        rows = newRows
        notifyDataSetChanged()
    }

    /** Position of the given package, or -1 when it is not on the list. */
    fun positionOf(packageName: String): Int =
        rows.indexOfFirst { it is Row.App && it.entry.packageName == packageName }

    /** Position of the first app, or -1 when the list has none. */
    fun positionOfFirstApp(): Int = rows.indexOfFirst { it is Row.App }

    override fun getItemCount(): Int = rows.size

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is Row.Header -> TYPE_HEADER
        else -> TYPE_APP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderHolder(inflater.inflate(R.layout.item_header, parent, false))
        } else {
            AppHolder(inflater.inflate(R.layout.item_app, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Header -> (holder as HeaderHolder).bind(row)
            is Row.App -> (holder as AppHolder).bindApp(row.entry)
            Row.Settings -> (holder as AppHolder).bindSettings()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is AppHolder) FocusEffects.sync(holder.itemView)
    }

    private inner class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.header_title)

        fun bind(row: Row.Header) {
            title.text = row.title
        }
    }

    private inner class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.row_icon)
        private val label: TextView = view.findViewById(R.id.row_label)

        init {
            view.setOnFocusChangeListener { v, hasFocus ->
                FocusEffects.apply(v, hasFocus)
                if (hasFocus) onFocusChanged(currentEntry())
                else if (currentEntry() == null) onFocusChanged(null)
            }
        }

        /** The app this holder currently shows, or null for the settings row. */
        private fun currentEntry(): AppEntry? =
            (bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION })
                ?.let { rows.getOrNull(it) }
                ?.let { (it as? Row.App)?.entry }

        fun bindApp(entry: AppEntry) {
            label.text = entry.label
            label.textSize = BASE_TEXT_SP * fontScale
            icon.setImageDrawable(entry.icon)
            icon.visibility = if (entry.icon != null) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onAppClick(entry) }
            FocusEffects.sync(itemView)
        }

        fun bindSettings() {
            label.text = itemView.context.getString(R.string.settings_entry)
            label.textSize = BASE_TEXT_SP * fontScale
            icon.setImageResource(R.drawable.ic_gear)
            icon.visibility = View.VISIBLE
            itemView.setOnClickListener { onSettingsClick() }
            FocusEffects.sync(itemView)
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
        const val BASE_TEXT_SP = 24f
    }
}
