package de.kruemelopment.org.memo_sql

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class FilterBaseAdapter(
    var context: Context,
    private var rowItems: List<FilterListe>,
    private var markierte: Boolean,
    private var passwordsafed: Boolean
) : BaseAdapter() {
    var color: Int

    init {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        val colorRes = if (typedValue.resourceId != 0) typedValue.resourceId else typedValue.data
        color = ContextCompat.getColor(context, colorRes)
    }

    /*private view holder class*/
    private class ViewHolder {
        var relativeLayout: RelativeLayout? = null
        var image: ImageView? = null
        var textView: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder = ViewHolder()
        if (v == null) {
            val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = vi.inflate(R.layout.widgeteinzelnes_memo, parent, false)
            holder.relativeLayout = v!!.findViewById(R.id.relop)
            holder.image = v.findViewById(R.id.check)
            holder.textView = v.findViewById(R.id.textView13)
        }
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
            notifyDataSetChanged()
        }
        return v
    }

    override fun getCount(): Int {
        return rowItems.size
    }

    override fun getItem(position: Int): Any {
        return rowItems[position]
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].hashCode().toLong()
    }
}