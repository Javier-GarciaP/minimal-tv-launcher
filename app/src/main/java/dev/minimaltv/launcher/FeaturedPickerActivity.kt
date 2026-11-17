package dev.minimaltv.launcher

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Lets the user pick which apps are pinned to the top of the home screen.
 */
class FeaturedPickerActivity : Activity() {

    private lateinit var prefs: SharedPrefsStore
    private lateinit var adapter: PickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = SharedPrefsStore(this)
        findViewById<TextView>(R.id.screen_title).setText(R.string.featured_picker_title)

        val apps = AppListBuilder.defaultCollator().let { collator ->
            AppsRepository(this).loadApps()
                .filter { it.packageName != packageName }
                .sortedWith { a, b -> collator.compare(a.label, b.label) }
        }

        adapter = PickerAdapter(apps, prefs.featuredPackages) { entry, updated ->
            prefs.featuredPackages = updated
        }

        val list = findViewById<RecyclerView>(R.id.option_list)
        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)
        list.itemAnimator = null
        list.adapter = adapter
    }
}

/** Adapter for the featured apps picker: label plus a check mark when selected. */
class PickerAdapter(
    private val apps: List<AppEntry>,
    featured: Set<String>,
    private val onToggle: (AppEntry, Set<String>) -> Unit,
) : RecyclerView.Adapter<PickerAdapter.Holder>() {

    private var featured: Set<String> = featured

    override fun getItemCount(): Int = apps.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pick, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(apps[position])
    }

    override fun onViewRecycled(holder: Holder) {
        FocusEffects.reset(holder.itemView, scale = false)
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.pick_icon)
        private val label: TextView = view.findViewById(R.id.pick_label)
        private val check: ImageView = view.findViewById(R.id.pick_check)

        init {
            view.setOnFocusChangeListener { v, hasFocus -> FocusEffects.apply(v, hasFocus, scale = false) }
        }

        fun bind(entry: AppEntry) {
            label.text = entry.label
            icon.setImageDrawable(entry.icon)
            icon.visibility = if (entry.icon != null) View.VISIBLE else View.GONE
            val selected = entry.packageName in featured
            check.visibility = if (selected) View.VISIBLE else View.INVISIBLE
            itemView.setOnClickListener {
                featured = if (entry.packageName in featured) {
                    featured - entry.packageName
                } else {
                    featured + entry.packageName
                }
                onToggle(entry, featured)
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) notifyItemChanged(position)
            }
            FocusEffects.reset(itemView, scale = false)
        }
    }
}
