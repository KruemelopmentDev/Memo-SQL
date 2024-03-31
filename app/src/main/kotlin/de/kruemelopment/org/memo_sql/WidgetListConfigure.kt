package de.kruemelopment.org.memo_sql

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.dmoral.toasty.Toasty

/**
 * The configuration screen for the [WidgeteinzelnesMemo][MemoListe] AppWidget.
 */
class WidgetListConfigure : Activity() {
    private var markierte = false
    private var passwortsecured = false
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val lol = PreferenceManager.getDefaultSharedPreferences(this)
            val nacht = lol.getBoolean("nightmode", false)
            if (nacht) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setResult(RESULT_CANCELED)
        setContentView(R.layout.filterlist)
        val filterListes = ArrayList<FilterListe>()
        val myDB = DataBaseHelper(this)
        val rese = myDB.allDatesASC
        if (rese.count > 0) {
            while (rese.moveToNext()) {
                if (rese.getString(6) != null) passwortsecured = true
                if (rese.getString(5) == "true") markierte = true
                filterListes.add(
                    FilterListe(
                        rese.getString(4).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0], false))
                filterListes.add(FilterListe(rese.getString(2), false))
            }
        } else {
            Toasty.info(this, getString(R.string.nomemossafed), Toast.LENGTH_SHORT).show()
            finish()
        }
        val noRepeat: MutableList<FilterListe> = ArrayList()
        for (event in filterListes) {
            var isFound = false
            for (e in noRepeat) {
                if (e.name == event.name) {
                    isFound = true
                    break
                }
            }
            if (!isFound) noRepeat.add(event)
        }
        if (markierte) noRepeat.add(FilterListe("Markierte", false))
        if (passwortsecured) noRepeat.add(FilterListe("Passwortgeschützte", false))
        val listViewe = findViewById<RecyclerView>(R.id.dynamic)
        val btn = findViewById<TextView>(R.id.textView42)
        val adapt = FilterBaseAdapter(this, noRepeat, markierte, passwortsecured)
        listViewe!!.setHasFixedSize(false)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.recycleChildrenOnDetach = true
        listViewe.layoutManager = linearLayoutManager
        listViewe.adapter = adapt
        listViewe.viewTreeObserver.addOnGlobalLayoutListener {
            val frameheight = listViewe.height
            val dp = frameheight / resources.displayMetrics.density
            val setdp = 400 * resources.displayMetrics.density
            if (dp > 400) {
                val params = listViewe.layoutParams as RelativeLayout.LayoutParams
                params.height = setdp.toInt()
                listViewe.layoutParams = params
            }
        }
        btn.setOnClickListener {
            var selected = false
            val ausgewahlt = StringBuilder()
            for (i in noRepeat.indices) {
                val a = noRepeat[i]
                if (a.isSelected) {
                    selected = true
                    if (noRepeat.size - i == 1) {
                        if (passwortsecured) {
                            ausgewahlt.append("passwordprotected/643%").append("§%21/")
                        } else if (markierte) {
                            ausgewahlt.append("markedmemo/643%").append("§%21/")
                        } else ausgewahlt.append(a.name).append("§%21/")
                    } else if (noRepeat.size - i == 2) {
                        if (passwortsecured && markierte) {
                            ausgewahlt.append("markedmemo/643%").append("§%21/")
                        } else ausgewahlt.append(a.name).append("§%21/")
                    } else {
                        ausgewahlt.append(a.name).append("§%21/")
                    }
                }
            }
            if (!selected) ausgewahlt.append("ALL§%21/") else ausgewahlt.substring(
                0,
                ausgewahlt.length - 5
            )
            val sp3 = getSharedPreferences("WidgetListe", 0)
            val ede = sp3.edit()
            ede.putString("single$mAppWidgetId", ausgewahlt.toString())
            ede.apply()
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val ids =
                appWidgetManager.getAppWidgetIds(ComponentName(application, MemoListe::class.java))
            val myWidget = MemoListe()
            myWidget.onUpdate(applicationContext, appWidgetManager, ids)
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    companion object {
        var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }
}
