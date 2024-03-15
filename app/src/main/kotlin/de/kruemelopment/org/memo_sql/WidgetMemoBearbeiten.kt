package de.kruemelopment.org.memo_sql

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class WidgetMemoBearbeiten : AppCompatActivity() {
    private var locked = false
    private var shares = false
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
        val id = intent.getStringExtra("id")
        val titel = intent.getStringExtra("titel")
        val thema = intent.getStringExtra("thema")
        val passwort = intent.getStringExtra("passwort")
        val inhalt = intent.getStringExtra("inhalt")
        editText.setText(thema)
        editText2.setText(titel)
        editText3.setText(inhalt)
        if (passwort != null) {
            password.setText(passwort)
            password.visibility = View.VISIBLE
            schloss.setImageResource(R.drawable.lock_outline)
        } else {
            password.visibility = View.GONE
            schloss.setImageResource(R.drawable.lock_open_outline)
        }
        locked = true
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
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (editText.length() > 40) {
                    editText.setText(editText.text.toString().substring(0, editText.length() - 1))
                    editText.clearFocus()
                    Toasty.warning(
                        this@WidgetMemoBearbeiten,
                        "Zeichenlimit (40) erreicht",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        editText2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (editText2.length() > 30) {
                    editText2.setText(editText2.text.toString().substring(0, 30))
                    editText2.clearFocus()
                    Toasty.warning(
                        this@WidgetMemoBearbeiten,
                        "Zeichenlimit (30) erreicht",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
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
        save.setImageResource(R.drawable.checkbox_marked_circle_outline)
        cancel.setImageResource(R.drawable.close_circle_outline)
        save.setOnClickListener {
            if (editText.text.toString().isEmpty()) editText.setText(getString(R.string.keine_kategorie))
            if (editText2.text.toString().isEmpty()) editText2.setText(getString(R.string.kein_titel))
            if (editText3.text.toString().isEmpty()) editText3.setText(getString(R.string.kein_inhalt))
            val titeltext = editText2.text.toString()
            val thematext = editText.text.toString()
            val inhalttext = editText3.text.toString()
            var passwor: String? = password.text.toString()
            if (passwor!!.isEmpty()) passwor = null
            if (!locked) passwor = null
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("dd.MM.yyyy,HH:mm:ss", Locale.GERMANY)
            val date = df.format(c.time)
            val result: Boolean = if (shares) myDB.updateData(
                id,
                thematext,
                titeltext,
                inhalttext,
                date,
                "true",
                passwor
            ) else myDB.updateData(id, thematext, titeltext, inhalttext, date, "false", passwor)
            if (result) {
                Toasty.success(
                    this@WidgetMemoBearbeiten,
                    "Erfolgreich geändert",
                    Toast.LENGTH_SHORT
                ).show()
                val widgetIDs = AppWidgetManager.getInstance(this@WidgetMemoBearbeiten)
                    .getAppWidgetIds(
                        ComponentName(
                            this@WidgetMemoBearbeiten,
                            MemoListe::class.java
                        )
                    )
                for (widgetId in widgetIDs) AppWidgetManager.getInstance(this@WidgetMemoBearbeiten)
                    .notifyAppWidgetViewDataChanged(widgetId, R.id.listewidget)
                val intent = Intent(this@WidgetMemoBearbeiten, WidgeteinzelnesMemo::class.java)
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                val ids = AppWidgetManager.getInstance(this@WidgetMemoBearbeiten).getAppWidgetIds(
                    ComponentName(
                        this@WidgetMemoBearbeiten,
                        WidgeteinzelnesMemo::class.java
                    )
                )
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(intent)
            } else {
                Toasty.error(
                    this@WidgetMemoBearbeiten,
                    "Bearbeiten fehlgeschlagen",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finishAndRemoveTask()
        }
        cancel.setOnClickListener { finishAndRemoveTask() }
        val share = findViewById<ImageView>(R.id.imageView8)
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