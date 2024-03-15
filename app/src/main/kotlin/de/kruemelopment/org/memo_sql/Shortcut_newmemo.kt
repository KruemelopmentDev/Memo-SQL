package de.kruemelopment.org.memo_sql

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Shortcut_newmemo : AppCompatActivity() {
    private var shares = false
    private var locked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val lol = PreferenceManager.getDefaultSharedPreferences(this)
            val nacht = lol.getBoolean("nightmode", false)
            if (nacht) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        setContentView(R.layout.shortcut_newmemo)
        val editText = findViewById<AutoCompleteTextView>(R.id.editText2)
        val editText2 = findViewById<EditText>(R.id.editText)
        val editText3 = findViewById<EditText>(R.id.editText3)
        val password = findViewById<EditText>(R.id.editText4)
        val schloss = findViewById<ImageView>(R.id.imageView13)
        var text = intent.getStringExtra("copytext")
        if (text == null) text = ""
        editText3.setText(text)
        password.visibility = View.GONE
        schloss.setImageResource(R.drawable.lock_open_outline)
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
        val myDB = DataBaseHelper(this)
        val cur = myDB.allDatesDESC
        val array = ArrayList<String>()
        if (cur.count > 0) {
            while (cur.moveToNext()) {
                val uname = cur.getString(2)
                if (!array.contains(uname)) array.add(uname)
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, array)
        editText.setAdapter(adapter)
        val save = findViewById<ImageView>(R.id.imageView4)
        val cancel = findViewById<ImageView>(R.id.imageView6)
        val share = findViewById<ImageView>(R.id.imageView8)
        save.setImageResource(R.drawable.checkbox_marked_circle_outline)
        cancel.setImageResource(R.drawable.close_circle_outline)
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
                    this@Shortcut_newmemo,
                    getString(R.string.safe_sucessful),
                    Toast.LENGTH_SHORT
                ).show()
                val widgetIDs = AppWidgetManager.getInstance(this@Shortcut_newmemo)
                    .getAppWidgetIds(ComponentName(this@Shortcut_newmemo, MemoListe::class.java))
                for (id in widgetIDs) AppWidgetManager.getInstance(this@Shortcut_newmemo)
                    .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                finishAndRemoveTask()
            } else {
                Toasty.error(
                    this@Shortcut_newmemo,
                    getString(R.string.safe_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        cancel.setOnClickListener { finishAndRemoveTask() }
        share.setImageResource(R.drawable.stargrey)
        share.setOnClickListener {
            if (shares) share.setImageResource(R.drawable.stargrey) else share.setImageResource(R.drawable.staryellow)
            shares = !shares
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAndRemoveTask()
        }
        return super.onKeyDown(keyCode, event)
    }
}
