package de.kruemelopment.org.memo_sql

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class WidgetAdapter(var context: Context, private var rowItems: List<Liste>, private var mAppWidgetId: Int) :
    BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null) {
            val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = vi.inflate(R.layout.widgeteinzelnes_memo, parent, false)
        }
        val textView = v!!.findViewById<TextView>(R.id.textView201)
        val textView1 = v.findViewById<TextView>(R.id.textView301)
        val textView2 = v.findViewById<TextView>(R.id.textView401)
        val textView3 = v.findViewById<TextView>(R.id.textView501)
        val imageView = v.findViewById<ImageView>(R.id.imageView9)
        val imageView2 = v.findViewById<ImageView>(R.id.imageView10)
        val relativeLayout = v.findViewById<RelativeLayout>(R.id.widgetlayout)
        val rowItem = getItem(position) as Liste
        textView.text = rowItem.title
        textView2.text = rowItem.thema
        textView3.text = rowItem.datum
        imageView2!!.setImageResource(0)
        if (rowItem.favo == "false") {
            imageView!!.setImageResource(R.drawable.radiobox_blank)
        } else {
            imageView!!.setImageResource(R.drawable.radiobox_marked)
        }
        if (rowItem.isLocked && rowItem.passwort != null) {
            val load = rowItem.inhalt
            val resultnew = StringBuilder()
            for (i in load.indices) {
                if (i + 1 <= load.length) {
                    if (load.startsWith("\n", i)) resultnew.append("\n") else resultnew.append("*")
                } else resultnew.append("*")
            }
            textView1!!.text = resultnew.toString()
        } else {
            textView1!!.text = rowItem.inhalt
        }
        relativeLayout!!.setOnClickListener {
            if (rowItem.favo == "false") {
                var i = 0
                while (rowItems.size > i) {
                    rowItems[i].favo = "false"
                    i++
                }
                rowItem.favo = "true"
                val sp3 = context.getSharedPreferences("Widget", 0)
                val ede = sp3.edit()
                ede.putString("single$mAppWidgetId", rowItem.id.toString())
                ede.apply()
            } else {
                val sp3 = context.getSharedPreferences("Widget", 0)
                val ede = sp3.edit()
                ede.putString("single$mAppWidgetId", "")
                ede.apply()
                rowItem.favo = "false"
            }
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
        return rowItems[position].id!!.toLong()
    }
}