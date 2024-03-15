package de.kruemelopment.org.memo_sql

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Newmemo : Fragment() {
    private var shares = false
    private var locked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.neuesmemo, container, false)
        val editText = view.findViewById<AutoCompleteTextView>(R.id.editText2)
        val editText2 = view.findViewById<EditText>(R.id.editText)
        val editText3 = view.findViewById<EditText>(R.id.editText3)
        val password = view.findViewById<EditText>(R.id.editText4)
        val schloss = view.findViewById<ImageView>(R.id.imageView13)
        schloss.setOnClickListener {
            if (locked) {
                password.visibility = View.GONE
                schloss.setImageResource(R.drawable.lock_open_outline)
            } else {
                password.visibility = View.VISIBLE
                schloss.setImageResource(R.drawable.lock_outline)
            }
            locked = !locked
        }
        val myDB = DataBaseHelper(context)
        val cur = myDB.allDatesDESC
        val array = ArrayList<String>()
        if (cur.count > 0) {
            while (cur.moveToNext()) {
                val uname = cur.getString(2)
                if (!array.contains(uname)) array.add(uname)
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, array)
        editText.setAdapter(adapter)
        editText3.setText(inhalts)
        val save = view.findViewById<ImageView>(R.id.imageView4)
        val cancel = view.findViewById<ImageView>(R.id.imageView6)
        val share = view.findViewById<ImageView>(R.id.imageView8)
        save.setOnClickListener {
            if (editText.text.toString()
                    .isEmpty()
            ) editText.setText(getString(R.string.nocategorie))
            if (editText2.text.toString().isEmpty()) editText2.setText(getString(R.string.notitle))
            if (editText3.text.toString()
                    .isEmpty()
            ) editText3.setText(getString(R.string.nocontent))
            val titel = editText2.text.toString()
            val thema = editText.text.toString()
            val inhalt = editText3.text.toString()
            var passwor: String? = password.text.toString()
            if (passwor!!.isEmpty()) passwor = null
            if (!locked) passwor = null
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("dd.MM.yyyy,HH:mm:ss", Locale.GERMANY)
            val date = df.format(c.time)
            val result: Boolean = if (shares) myDB.insertData(
                thema,
                titel,
                inhalt,
                date,
                "true",
                passwor
            ) else myDB.insertData(thema, titel, inhalt, date, "false", passwor)
            if (result) {
                Toasty.success(
                    requireContext(),
                    getString(R.string.safe_sucessful),
                    Toast.LENGTH_SHORT
                ).show()
                editText.setText("")
                editText2.setText("")
                editText3.setText("")
                editText.clearFocus()
                editText2.clearFocus()
                editText3.clearFocus()
                val cur1 = myDB.allDatesDESC
                val array1 = ArrayList<String>()
                if (cur1.count > 0) {
                    while (cur1.moveToNext()) {
                        val uname = cur1.getString(2)
                        if (!array1.contains(uname)) array1.add(uname)
                    }
                }
                val adapter1 =
                    ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, array1)
                editText.setAdapter(adapter1)
                try {
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (getView() != null) imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                } catch (ignored: Exception) {
                }
                val widgetIDs = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(requireContext(), MemoListe::class.java))
                for (id in widgetIDs) AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
            } else {
                Toasty.error(requireContext(), getString(R.string.safe_failed), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        cancel.setOnClickListener {
            editText.setText("")
            editText2.setText("")
            editText3.setText("")
        }
        share.setOnClickListener {
            if (shares) share.setImageResource(R.drawable.stargrey) else share.setImageResource(R.drawable.staryellow)
            shares = !shares
        }
        return view
    }

    companion object {
        var inhalts: String? = null
        fun newInstance(s: String?): Newmemo {
            inhalts = s
            return Newmemo()
        }
    }
}