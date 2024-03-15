package de.kruemelopment.org.memo_sql

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.dmoral.toasty.Toasty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class Papierkorb : Fragment(), AdaptertoFragment {
    var adapter: Papierkorbadapter? = null
    private var backup = ArrayList<Liste>()
    var rowItems = ArrayList<Liste>()
    var listView: ListView? = null
    var myDB: PapierkorbHelper? = null
    private var filter = false
    var one: MenuItem? = null
    var two: MenuItem? = null
    var three: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.allemems, container, false)
        myDB = PapierkorbHelper(context)
        val res = myDB!!.allData
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
                rowItems.add(item)
            }
        }
        if (rowItems.isEmpty()) rowItems.add(
            Liste(
                "",
                getString(R.string.nodeletedmemos),
                "",
                "",
                "",
                "",
                "",
                false
            )
        )
        rowItems = sortieren(rowItems)
        setHasOptionsMenu(true)
        listView = view.findViewById(R.id.listview)
        adapter = Papierkorbadapter(context, rowItems)
        adapter!!.setAdaptertoFragment(this)
        listView!!.adapter = adapter
        val fab = view.findViewById<FloatingActionButton>(R.id.floating)
        fab.hide()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.allemems, menu)
        one = menu.findItem(R.id.suchleiste)
        two = menu.findItem(R.id.filtern)
        three = menu.findItem(R.id.losen)
        if (myDB!!.empty()) {
            one!!.setVisible(false)
            two!!.setVisible(false)
            three!!.setVisible(false)
        }
        val mSearchView = menu.findItem(R.id.suchleiste)
        mSearchView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                menu.findItem(R.id.filtern).setVisible(false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                adapter!!.reset()
                menu.findItem(R.id.filtern).setVisible(true)
                return true
            }
        })
        val searchView = mSearchView.actionView as SearchView?
        if (searchView != null) {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    adapter!!.search(query)
                    val imm =
                        (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter!!.search(newText)
                    return true
                }
            })
            searchView.setOnCloseListener(SearchView.OnCloseListener {
                adapter!!.reset()
                false
            })
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filtern) {
            if (!filter) {
                filter()
            } else reset()
        } else if (item.itemId == R.id.losen) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.confirmdeletion))
                .setMessage(getString(R.string.emptytrash))
                .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    try {
                        val pap = PapierkorbHelper(context)
                        pap.deleteAll()
                        pap.close()
                        val outtoRight: Animation = TranslateAnimation(
                            Animation.RELATIVE_TO_PARENT, 0.0f,
                            Animation.RELATIVE_TO_PARENT, +1.0f,
                            Animation.RELATIVE_TO_PARENT, 0.0f,
                            Animation.RELATIVE_TO_PARENT, 0.0f
                        )
                        outtoRight.duration = 200
                        outtoRight.interpolator = AccelerateInterpolator()
                        listView!!.startAnimation(outtoRight)
                        Toasty.success(
                            requireContext(),
                            getString(R.string.delete_succesful),
                            Toast.LENGTH_SHORT
                        ).show()
                        outtoRight.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {}
                            override fun onAnimationEnd(animation: Animation) {
                                rowItems.clear()
                                val item1 = Liste(
                                    "",
                                    getString(R.string.nodeletedmemos),
                                    "",
                                    "",
                                    "",
                                    "",
                                    null,
                                    false
                                )
                                rowItems.add(item1)
                                one!!.setVisible(false)
                                two!!.setVisible(false)
                                three!!.setVisible(false)
                                adapter!!.notifyDataSetChanged()
                                val outtoLeft: Animation = TranslateAnimation(
                                    Animation.RELATIVE_TO_PARENT, -1.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f
                                )
                                outtoLeft.duration = 200
                                outtoLeft.interpolator = AccelerateInterpolator()
                                listView!!.startAnimation(outtoLeft)
                                val widgetIDs = AppWidgetManager.getInstance(context)
                                    .getAppWidgetIds(
                                        ComponentName(
                                            requireActivity(),
                                            MemoListe::class.java
                                        )
                                    )
                                for (id in widgetIDs) AppWidgetManager.getInstance(context)
                                    .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                            }

                            override fun onAnimationRepeat(animation: Animation) {}
                        })
                    } catch (e: Exception) {
                        Toasty.error(
                            requireContext(),
                            getString(R.string.smtwentwrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(getString(R.string.no)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .show()
        }
        return true
    }

    override fun onResume() {
        if (!MainActivity.first) MainActivity.first = true else {
            if (MainActivity.againlocken) {
                for (a in rowItems) {
                    a.isLocked = true
                }
                adapter!!.notifyDataSetChanged()
            }
        }
        super.onResume()
    }

    private fun sortieren(a: ArrayList<Liste>): ArrayList<Liste> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        when (sharedPreferences.getString("order", "1")!!.toInt()) {
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
                            requireContext(),
                            getString(R.string.sorterror),
                            Toast.LENGTH_SHORT
                        ).show()
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
                            requireContext(),
                            getString(R.string.sorterror),
                            Toast.LENGTH_SHORT
                        ).show()
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

    private fun filter() {
        if (rowItems[0].title != getString(R.string.nodeletedmemos)) {
            val dialog = Dialog(requireContext(), R.style.AppDialog)
            dialog.setContentView(R.layout.filterlist)
            val listView = dialog.findViewById<ListView>(R.id.dynamic)
            val btn = dialog.findViewById<TextView>(R.id.textView42)
            val filterListes = ArrayList<FilterListe>()
            var passwortsecured = false
            for (a in rowItems) {
                if (a.passwort != null) passwortsecured = true
                filterListes.add(
                    FilterListe(
                        a.datum.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0], false))
                filterListes.add(FilterListe(a.thema, false))
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
            if (passwortsecured) noRepeat.add(FilterListe("Passwortgeschützte", false))
            val finalPasswortsecured = passwortsecured
            btn.setOnClickListener {
                dialog.dismiss()
                val help = ArrayList(rowItems)
                backup.clear()
                backup.addAll(help)
                filter = true
                rowItems.clear()
                for (i in noRepeat.indices) {
                    val a = noRepeat[i]
                    if (a.isSelected) {
                        if (noRepeat.size - i == 1) {
                            if (finalPasswortsecured) {
                                for (b in help) {
                                    if (b.passwort != null) {
                                        rowItems.add(b)
                                    }
                                }
                            } else {
                                for (b in help) {
                                    if (b.thema == a.name || b.datum.split(",".toRegex())
                                            .dropLastWhile { it.isEmpty() }
                                            .toTypedArray()[0] == a.name
                                    ) {
                                        rowItems.add(b)
                                    }
                                }
                            }
                        } else {
                            for (b in help) {
                                if (b.thema == a.name || b.datum.split(",".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0] == a.name
                                ) {
                                    rowItems.add(b)
                                }
                            }
                        }
                    }
                }
                if (rowItems.isEmpty()) rowItems.add(
                    Liste(
                        "",
                        getString(R.string.nomemoselected),
                        "",
                        "",
                        "",
                        "",
                        "",
                        false
                    )
                )
                rowItems = sortieren(rowItems)
                adapter!!.notifyDataSetChanged()
            }
            val adapt = FilterBaseAdapter(requireContext(), noRepeat, false, passwortsecured)
            listView.adapter = adapt
            listView.viewTreeObserver.addOnGlobalLayoutListener {
                val frameheight = listView.height
                val dp = frameheight / requireContext().resources.displayMetrics.density
                val setdp = 400 * requireContext().resources.displayMetrics.density
                if (dp > 400) {
                    val params = listView.layoutParams as RelativeLayout.LayoutParams
                    params.height = setdp.toInt()
                    listView.layoutParams = params
                }
            }
            dialog.show()
        }
    }

    private fun reset() {
        filter = false
        rowItems.clear()
        rowItems.addAll(sortieren(backup))
        adapter!!.notifyDataSetChanged()
    }

    override fun hidemenuitems() {
        one!!.setVisible(false)
        two!!.setVisible(false)
        three!!.setVisible(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter!!.unsetAdaptertoFragment()
    }

    companion object {
        fun newInstance(): Papierkorb {
            return Papierkorb()
        }
    }
}