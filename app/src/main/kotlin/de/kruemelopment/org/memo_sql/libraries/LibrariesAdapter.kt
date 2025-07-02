package de.kruemelopment.org.memo_sql.libraries

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import de.kruemelopment.org.memo_sql.R
import es.dmoral.toasty.Toasty
import org.kruemelopment.de.bewecker.libraries.LibraryListe

class LibrariesAdapter(private val libraryListe: List<LibraryListe>, var context: Context?) :
    RecyclerView.Adapter<LibrariesAdapter.MyViewHolder>() {
    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: MaterialTextView = v.findViewById(R.id.name)
        var publisher: MaterialTextView = v.findViewById(R.id.publisher)
        var license: MaterialTextView = v.findViewById(R.id.license)

        var year: MaterialTextView = v.findViewById(R.id.year)
        var version: MaterialTextView = v.findViewById(R.id.version)
        var crissCrossLayout: ConstraintLayout = v.findViewById(R.id.wrapper)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = libraryListe[position]
        holder.setIsRecyclable(true)
        holder.name.text = item.name
        holder.publisher.text = item.publisher
        holder.license.text = item.getLicense()
        holder.year.text = item.year
        holder.version.text = item.getVersionName()
        holder.crissCrossLayout.setOnClickListener { openUrl(item.link) }
        holder.license.setOnClickListener {
            when (item.getLicense()) {
                "Apache 2.0", "Apache 2.0 BSD" -> openUrl("https://www.apache.org/licenses/LICENSE-2.0")
                "MIT" -> openUrl("https://opensource.org/licenses/MIT")
                "LGPL 3.0" -> openUrl("https://www.gnu.org/licenses/lgpl-3.0.html")
            }
        }
    }

    override fun getItemCount(): Int {
        return libraryListe.size
    }

    override fun getItemId(position: Int): Long {
        return libraryListe[position].hashCode().toLong()
    }

    private fun openUrl(url: String?) {
        if (context == null || url == null || url.trim { it <= ' ' }.isEmpty()) {
            if (context != null) {
                Toasty.error(context!!, "Ungültiger Link", Toast.LENGTH_SHORT).show()
            }
            return
        }
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url.trim { it <= ' ' }))
            if (browserIntent.resolveActivity(context!!.packageManager) != null) {
                context!!.startActivity(browserIntent)
            } else {
                Toasty.error(
                    context!!,
                    "Keine App zum Öffnen des Links gefunden",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.error(context!!, "Link konnte nicht geöffnet werden", Toast.LENGTH_SHORT).show()
        }
    }
}
