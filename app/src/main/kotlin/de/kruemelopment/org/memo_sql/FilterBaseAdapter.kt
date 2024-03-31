package de.kruemelopment.org.memo_sql

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class FilterBaseAdapter(
    var context: Context,
    private var rowItems: List<FilterListe>,
    private var markierte: Boolean,
    private var passwordsafed: Boolean
) : RecyclerView.Adapter<FilterBaseAdapter.MyViewHolder>() {
    var color: Int

    init {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        val colorRes = if (typedValue.resourceId != 0) typedValue.resourceId else typedValue.data
        color = ContextCompat.getColor(context, colorRes)
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var relativeLayout: RelativeLayout? = null
        var image: ImageView? = null
        var textView: TextView? = null

        init {
            relativeLayout = v.findViewById(R.id.relop)
            image = v.findViewById(R.id.check)
            textView = v.findViewById(R.id.textView13)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.filteritem, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val rowItem = rowItems[position]
        if (rowItem.isSelected) holder.image!!.setImageResource(R.drawable.checkbox_marked_circle_outline) else holder.image!!.setImageResource(
            R.drawable.checkbox_blank_circle_outline
        )
        holder.textView!!.text = rowItem.name
        if (rowItems.size - position == 1) {
            if (passwordsafed) holder.textView!!.setTextColor(ResourcesCompat.getColor(context.resources,R.color.colorPrimary,context.theme))
            if (markierte) holder.textView!!.setTextColor(ResourcesCompat.getColor(context.resources,R.color.colorPrimary,context.theme))
        } else if (markierte && passwordsafed && rowItems.size - position == 2) {
            holder.textView!!.setTextColor(ResourcesCompat.getColor(context.resources,R.color.colorPrimary,context.theme))
        } else holder.textView!!.setTextColor(color)
        holder.relativeLayout!!.setOnClickListener {
            rowItem.isSelected = !rowItem.isSelected
            notifyItemChanged(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return rowItems.size
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].hashCode().toLong()
    }
}