package de.kruemelopment.org.memo_sql

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WidgetAdapter(var context: Context, private var rowItems: List<Liste>, private var mAppWidgetId: Int) :
    RecyclerView.Adapter<WidgetAdapter.MyViewHolder>()  {


    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var textView: TextView
        var textView1: TextView
        var textView2: TextView
        var textView3: TextView
        var imageView: ImageView
        var imageView2: ImageView
        var relativeLayout: RelativeLayout

        init {
            textView = v.findViewById(R.id.textView201)
            textView1 = v.findViewById(R.id.textView301)
            textView2 = v.findViewById(R.id.textView401)
            textView3 = v.findViewById(R.id.textView501)
            imageView = v.findViewById(R.id.imageView9)
            imageView2 = v.findViewById(R.id.imageView10)
            relativeLayout = v.findViewById(R.id.widgetlayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.widgeteinzelnes_memo, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val rowItem = rowItems[position]
        holder.textView.text = rowItem.title
        holder.textView2.text = rowItem.thema
        holder.textView3.text = rowItem.datum
        holder.imageView2.setImageResource(0)
        if (rowItem.favo == "false") {
            holder.imageView.setImageResource(R.drawable.radiobox_blank)
        } else {
            holder.imageView.setImageResource(R.drawable.radiobox_marked)
        }
        if (rowItem.isLocked && rowItem.passwort != null) {
            val load = rowItem.inhalt
            val resultnew = StringBuilder()
            for (i in load.indices) {
                if (i + 1 <= load.length) {
                    if (load.startsWith("\n", i)) resultnew.append("\n") else resultnew.append("*")
                } else resultnew.append("*")
            }
            holder.textView1.text = resultnew.toString()
        } else {
            holder.textView1.text = rowItem.inhalt
        }
        holder.relativeLayout.setOnClickListener {
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
            notifyItemChanged(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return rowItems.size
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].id!!.toLong()
    }
}