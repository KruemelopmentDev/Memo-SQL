package de.kruemelopment.org.memo_sql

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
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
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class Allmems : Fragment(), AdaptertoFragment {
    var rowItems = ArrayList<Liste>()
    private var backup = ArrayList<Liste>()
    var adapter: CustomBaseAdapter? = null
    var listView: ListView? = null
    var searchView: SearchView? = null
    var myDB: DataBaseHelper? = null
    private var filter = false
    private var fab: FloatingActionButton? = null
    private var downY = 0f
    private var showfab = true
    var one: MenuItem? = null
    var two: MenuItem? = null
    var three: MenuItem? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.allemems, container, false)
        myDB = DataBaseHelper(context)
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
                getString(R.string.nomemossafed),
                "",
                "",
                "",
                "",
                "",
                false
            )
        )
        listView = view.findViewById(R.id.listview)
        rowItems = sortieren(rowItems)
        setHasOptionsMenu(true)
        adapter = CustomBaseAdapter(context, rowItems, myDB!!)
        adapter!!.setAdaptertoFragment(this)
        listView!!.adapter = adapter
        for (i in rowItems.indices) {
            if (rowItems[i].id == focus) {
                listView!!.setSelection(i)
            }
        }
        fab = view.findViewById(R.id.floating)
        fab!!.setOnClickListener {
            val dialog = Dialog(requireContext(), R.style.AppDialog)
            dialog.setContentView(R.layout.neuesmemo)
            val relativeLayout = dialog.findViewById<RelativeLayout>(R.id.layoutmain)
            relativeLayout.setBackgroundColor(android.R.attr.colorBackground)
            relativeLayout.gravity = Gravity.CENTER
            val params = relativeLayout.layoutParams as FrameLayout.LayoutParams
            params.setMargins(5, 5, 5, 5)
            relativeLayout.layoutParams = params
            val passwort = dialog.findViewById<EditText>(R.id.editText4)
            val lock = dialog.findViewById<ImageView>(R.id.imageView13)
            val editText = dialog.findViewById<AutoCompleteTextView>(R.id.editText2)
            val editText2 = dialog.findViewById<EditText>(R.id.editText)
            val editText3 = dialog.findViewById<EditText>(R.id.editText3)
            val locked2 = BooleanArray(1)
            lock.setOnClickListener {
                if (locked2[0]) {
                    passwort.visibility = View.GONE
                    lock.setImageResource(R.drawable.lock_open_outline)
                } else {
                    passwort.visibility = View.VISIBLE
                    lock.setImageResource(R.drawable.lock_outline)
                }
                locked2[0] = !locked2[0]
            }
            val save = dialog.findViewById<ImageView>(R.id.imageView4)
            val cancel = dialog.findViewById<ImageView>(R.id.imageView6)
            val shares = dialog.findViewById<ImageView>(R.id.imageView8)
            shares.visibility = View.GONE
            val load = lock.layoutParams as RelativeLayout.LayoutParams
            val param = RelativeLayout.LayoutParams(load.width, load.height)
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            param.addRule(RelativeLayout.BELOW, R.id.imageView6)
            lock.layoutParams = param
            dialog.show()
            val cur = myDB!!.allData
            val array = ArrayList<String>()
            if (cur.count > 0) {
                while (cur.moveToNext()) {
                    val uname = cur.getString(2)
                    if (!array.contains(uname)) array.add(uname)
                }
            }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, array)
            editText.setAdapter(adapter)
            save.setOnClickListener {
                if (editText.text.toString()
                        .isEmpty()
                ) editText.setText(getString(R.string.nocategorie))
                if (editText2.text.toString()
                        .isEmpty()
                ) editText2.setText(getString(R.string.notitle))
                if (editText3.text.toString()
                        .isEmpty()
                ) editText3.setText(getString(R.string.nocontent))
                val titell = editText2.text.toString()
                val themaa = editText.text.toString()
                val inhaltt = editText3.text.toString()
                var passworr: String? = passwort.text.toString()
                if (passworr!!.isEmpty()) passworr = null
                if (passwort.visibility == View.GONE) passworr = null
                val c = Calendar.getInstance()
                val df = SimpleDateFormat("dd.MM.yyyy,HH:mm:ss", Locale.GERMANY)
                val date = df.format(c.time)
                val result = myDB!!.insertData(themaa, titell, inhaltt, date, "false", passworr)
                if (result) {
                    Toasty.success(
                        requireContext(),
                        getString(R.string.safed_succesful),
                        Toast.LENGTH_SHORT
                    ).show()
                    val widgetIDs = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(requireActivity(), MemoListe::class.java))
                    for (id in widgetIDs) AppWidgetManager.getInstance(context)
                        .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                    val intent = Intent(context, WidgeteinzelnesMemo::class.java)
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                        ComponentName(
                            requireActivity(),
                            WidgeteinzelnesMemo::class.java
                        )
                    )
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    requireActivity().sendBroadcast(intent)
                    dialog.dismiss()
                    if (rowItems[0].title == getString(R.string.nomemossafed)) rowItems.clear()
                    rowItems.add(
                        Liste(
                            myDB!!.getlastid(),
                            titell,
                            themaa,
                            inhaltt,
                            date,
                            "false",
                            passworr,
                            true
                        )
                    )
                    rowItems = sortieren(rowItems)
                    adapter.notifyDataSetChanged()
                    one!!.setVisible(true)
                    two!!.setVisible(true)
                    three!!.setVisible(true)
                } else Toasty.error(
                    requireContext(),
                    getString(R.string.safed_couldnt),
                    Toast.LENGTH_SHORT
                ).show()
            }
            cancel.setOnClickListener { dialog.dismiss() }
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        showfab = sharedPreferences.getBoolean("fabshow", true)
        if (!showfab) fab!!.hide() else {
            listView!!.setOnTouchListener { v: View, event: MotionEvent ->
                v.performClick()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        run { downY = event.y }
                        run {
                            val upY = event.y
                            val deltaY = downY - upY
                            if (abs(deltaY) > 0) {
                                if (deltaY >= 0) {
                                    fab!!.hide()
                                } else {
                                    fab!!.show()
                                }
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        val upY = event.y
                        val deltaY = downY - upY
                        if (abs(deltaY) > 0) {
                            if (deltaY >= 0) {
                                fab!!.hide()
                            } else {
                                fab!!.show()
                            }
                        }
                    }
                }
                false
            }
        }
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
        searchView = mSearchView.actionView as SearchView?
        if (searchView != null) {
            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    adapter!!.search(query)
                    val imm =
                        requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchView!!.windowToken, 0)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter!!.search(newText)
                    return true
                }
            })
            searchView!!.setOnCloseListener {
                adapter!!.reset()
                false
            }
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
                .setMessage(getString(R.string.suredeletmemo))
                .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    try {
                        val pap = DataBaseHelper(context)
                        val pb = PapierkorbHelper(context)
                        val res = pap.allData
                        if (res.count > 0) {
                            while (res.moveToNext()) {
                                pb.insertData(
                                    res.getString(2),
                                    res.getString(1),
                                    res.getString(3),
                                    res.getString(4),
                                    res.getString(6)
                                )
                            }
                        }
                        pap.deleteAll()
                        pap.close()
                        pb.close()
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
                                    getString(R.string.nomemossafed),
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
                                            requireContext(),
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
                    val intent = Intent(context, WidgeteinzelnesMemo::class.java)
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                        ComponentName(
                            requireContext(),
                            WidgeteinzelnesMemo::class.java
                        )
                    )
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    requireContext().sendBroadcast(intent)
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
        if (rowItems[0].title != getString(R.string.nomemossafed)) {
            val dialog = Dialog(requireContext(), R.style.AppDialog)
            dialog.setContentView(R.layout.filterlist)
            val listViewe = dialog.findViewById<ListView>(R.id.dynamic)
            val btn = dialog.findViewById<TextView>(R.id.textView42)
            val filterListes = ArrayList<FilterListe>()
            var markierte = false
            var passwortsecured = false
            for (a in rowItems) {
                if (a.passwort != null) passwortsecured = true
                if (a.favo == "true") markierte = true
                filterListes.add(
                    FilterListe(
                        a.datum.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0], false))
                filterListes.add(FilterListe(a.thema, false))
            }
            val noRepeat = ArrayList<FilterListe>()
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
            val finalMarkierte = markierte
            val finalPasswortsecured = passwortsecured
            btn.setOnClickListener {
                dialog.dismiss()
                val help = ArrayList(rowItems)
                backup.clear()
                backup.addAll(help)
                rowItems.clear()
                filter = true
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
                            } else if (finalMarkierte) {
                                for (b in help) {
                                    if (b.favo == "true") {
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
                        } else if (noRepeat.size - i == 2) {
                            if (finalPasswortsecured && finalMarkierte) {
                                for (b in help) {
                                    if (b.favo == "true") {
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
            val adapt = FilterBaseAdapter(requireContext(), noRepeat, markierte, passwortsecured)
            listViewe.adapter = adapt
            listViewe.viewTreeObserver.addOnGlobalLayoutListener {
                val frameheight = listViewe.height
                val dp = frameheight / requireContext().resources.displayMetrics.density
                val setdp = 400 * requireContext().resources.displayMetrics.density
                if (dp > 400) {
                    val params = listViewe.layoutParams as RelativeLayout.LayoutParams
                    params.height = setdp.toInt()
                    listViewe.layoutParams = params
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
        var focus: String? = ""
        fun newInstance(s: String?): Allmems {
            focus = s
            return Allmems()
        }
    }
}