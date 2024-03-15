package de.kruemelopment.org.memo_sql

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import android.widget.Toast
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class WidgetRemoteViewsFactory internal constructor(private val context: Context, intent: Intent) :
    RemoteViewsFactory {
    private var count = 0
    private var widgetlist = ArrayList<Liste>()
    private val dataBaseHelper: DataBaseHelper = DataBaseHelper(context)
    private var mAppWidgetId: Int

    init {
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        updateWidgetListview()
    }

    private fun updateWidgetListview() {
        count = 0
        widgetlist.clear()
        if (mAppWidgetId == -1) return
        val sp7 = context.getSharedPreferences("WidgetListe", 0)
        val was = sp7.getString("single$mAppWidgetId", "ALL§%21/")
        val res = dataBaseHelper.allData
        if (res.count > 0) {
            while (res.moveToNext()) {
                val item = Liste(
                    res.getString(0),
                    res.getString(1),
                    res.getString(2),
                    res.getString(3),
                    res.getString(4),
                    res.getString(5),
                    res.getString(6),
                    true
                )
                widgetlist.add(item)
            }
        }
        val help = ArrayList<Liste>()
        widgetlist = if (was != "ALL§%21/") {
            for (a in was!!.split("§%21/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
                when (a) {
                    "passwordprotected/643%" -> {
                        for (n in widgetlist) {
                            if (n.passwort != null) {
                                if (!help.contains(n)) help.add(n)
                            }
                        }
                    }
                    "markedmemo/643%" -> {
                        for (n in widgetlist) {
                            if (n.favo == "true") if (!help.contains(n)) help.add(n)
                        }
                    }
                    else -> {
                        for (n in widgetlist) {
                            if (n.thema == a || n.datum.split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()[0] == a
                            ) {
                                if (!help.contains(n)) help.add(n)
                            }
                        }
                    }
                }
            }
            widgetlist.clear()
            sortieren(help)
        } else {
            sortieren(widgetlist)
        }
        count = widgetlist.size
        if (widgetlist.isEmpty()) {
            val item = Liste(
                "",
                "Keine Memos",
                "",
                "Du hast noch keine Memos gespeichert",
                "",
                "",
                null,
                false
            )
            widgetlist.add(item)
            count = 1
        }
    }

    override fun onCreate() {
        updateWidgetListview()
    }

    override fun onDataSetChanged() {
        updateWidgetListview()
    }

    override fun onDestroy() {
        widgetlist.clear()
        dataBaseHelper.close()
    }

    override fun getCount(): Int {
        return widgetlist.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widgetallememoscard)
        val load = widgetlist[i].passwort
        var inhalt = widgetlist[i].inhalt
        if (load != null) {
            val resultnew = StringBuilder()
            for (`in` in inhalt.indices) {
                if (`in` + 1 <= inhalt.length) {
                    if (inhalt.startsWith("\n", `in`)) resultnew.append("\n") else resultnew.append(
                        "*"
                    )
                } else resultnew.append("*")
            }
            inhalt = resultnew.toString()
        }
        remoteViews.setTextViewText(R.id.textView30, inhalt)
        remoteViews.setTextViewText(R.id.textView20, widgetlist[i].title)
        remoteViews.setTextViewText(R.id.textView40, widgetlist[i].thema)
        remoteViews.setTextViewText(R.id.textView50, widgetlist[i].datum)
        val `in` = Intent()
        val extras = Bundle()
        extras.putString("Hallo", widgetlist[i].inhalt)
        `in`.putExtras(extras)
        remoteViews.setOnClickFillInIntent(R.id.textView30, `in`)
        remoteViews.setOnClickFillInIntent(R.id.textView20, `in`)
        remoteViews.setOnClickFillInIntent(R.id.textView40, `in`)
        remoteViews.setOnClickFillInIntent(R.id.textView50, `in`)
        remoteViews.setOnClickFillInIntent(R.id.tallayout0, `in`)
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    private fun sortieren(a: ArrayList<Liste>): ArrayList<Liste> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            context
        )
        when (sharedPreferences.getString("orderwidget", "1")!!.toInt()) {
            1 -> {
                val formatter = SimpleDateFormat("dd.MM.yyyy,hh:mm:ss", Locale.GERMAN)
                a.sortWith { o1: Liste, o2: Liste ->
                    val a1 = o1.datum
                    val b1 = o2.datum
                    try {
                        val date = formatter.parse(a1)
                        val date2 = formatter.parse(b1)!!
                        date2.compareTo(date)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        Toasty.error(
                            context,
                            context.getString(R.string.sorterror),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        0
                    }
                }
            }
            2 -> {
                val formatter = SimpleDateFormat("dd.MM.yyyy,hh:mm:ss", Locale.GERMAN)
                a.sortWith { o1: Liste, o2: Liste ->
                    val c = o1.datum
                    val d = o2.datum
                    try {
                        val date = formatter.parse(c)
                        val date2 = formatter.parse(d)
                        assert(date != null)
                        date!!.compareTo(date2)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        Toasty.error(
                            context,
                            context.getString(R.string.sorterror),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        0
                    }
                }
            }
            3 -> {
                return sortlang(a)
            }
            4 -> {
                return sortkurz(a)
            }
            5 -> {
                a.sortWith { o1: Liste, o2: Liste ->
                    o1.title.uppercase(Locale.getDefault()).compareTo(
                        o2.title.uppercase(
                            Locale.getDefault()
                        )
                    )
                }
            }
            6 -> {
                a.sortWith { o1: Liste, o2: Liste ->
                    o2.title.uppercase(Locale.getDefault()).compareTo(
                        o1.title.uppercase(
                            Locale.getDefault()
                        )
                    )
                }
            }
        }
        return a
    }

    private fun sortlang(sorr: ArrayList<Liste>): ArrayList<Liste> {
        var k: Liste
        for (i in 1 until sorr.size) {
            var j = 0
            while (j < sorr.size - i) {
                if (sorr[j].inhalt.length < sorr[j + 1].inhalt.length) {
                    k = sorr[j]
                    sorr.removeAt(j)
                    sorr.add(j + 1, k)
                    j--
                }
                j++
            }
        }
        return sorr
    }

    private fun sortkurz(sorr: ArrayList<Liste>): ArrayList<Liste> {
        var k: Liste
        for (i in 1 until sorr.size) {
            var j = 0
            while (j < sorr.size - i) {
                if (sorr[j].inhalt.length > sorr[j + 1].inhalt.length) {
                    k = sorr[j]
                    sorr.removeAt(j)
                    sorr.add(j + 1, k)
                    j--
                }
                j++
            }
        }
        return sorr
    }
}
