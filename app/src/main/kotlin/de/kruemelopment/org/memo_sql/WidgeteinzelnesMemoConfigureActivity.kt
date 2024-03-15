package de.kruemelopment.org.memo_sql

import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty
import java.util.concurrent.Executors

/**
 * The configuration screen for the [WidgeteinzelnesMemo] AppWidget.
 */
class WidgeteinzelnesMemoConfigureActivity : AppCompatActivity() {
    private var myBiometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null
    var passwort: String? = null
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val lol = PreferenceManager.getDefaultSharedPreferences(this)
            val nacht = lol.getBoolean("nightmode", false)
            if (nacht) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val a = sharedPreferences.getBoolean("fingerprint", false)
        if (a) {
            fingerprintinit()
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
        setContentView(R.layout.widgeteinzelnes_memo_configure)
        val listView = findViewById<ListView>(R.id.widgeteinzeln)
        val btn = findViewById<Button>(R.id.add_button)
        val rowItems: MutableList<Liste> = ArrayList()
        val myDB = DataBaseHelper(this)
        val res = myDB.allDatesDESC
        myDB.close()
        if (res.count > 0) {
            while (res.moveToNext()) {
                val item = Liste(
                    res.getString(0),
                    res.getString(1),
                    res.getString(2),
                    res.getString(3),
                    res.getString(4),
                    "false",
                    res.getString(6),
                    true
                )
                rowItems.add(item)
            }
        } else {
            finish()
            Toasty.info(this, getString(R.string.nomemossafed), Toast.LENGTH_SHORT).show()
        }
        val adapter = WidgetAdapter(this, rowItems, mAppWidgetId)
        listView.adapter = adapter
        btn.setOnClickListener {
            val sp7 = applicationContext.getSharedPreferences("Widget", 0)
            val was = sp7.getString("single$mAppWidgetId", "")
            if (was != "") {
                val db = DataBaseHelper(applicationContext)
                val res1 = db.getData(was)
                db.close()
                passwort = res1.getString(6)
                if (passwort != null) {
                    if (a) {
                        authenticate()
                    } else {
                        setContentView(R.layout.insertpasswort)
                        val falsch = findViewById<TextView>(R.id.textView12)
                        falsch.visibility = View.GONE
                        val pass = findViewById<EditText>(R.id.editText5)
                        pass.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                falsch.visibility = View.GONE
                                if (passwort == pass.text.toString()) {
                                    val appWidgetManager =
                                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                                    WidgeteinzelnesMemo.updateAppWidget(
                                        this@WidgeteinzelnesMemoConfigureActivity,
                                        appWidgetManager,
                                        mAppWidgetId
                                    )
                                    val resultValue = Intent()
                                    resultValue.putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        mAppWidgetId
                                    )
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (passwort == pass.text.toString()) {
                                    val appWidgetManager =
                                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                                    WidgeteinzelnesMemo.updateAppWidget(
                                        this@WidgeteinzelnesMemoConfigureActivity,
                                        appWidgetManager,
                                        mAppWidgetId
                                    )
                                    val resultValue = Intent()
                                    resultValue.putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        mAppWidgetId
                                    )
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                } else {
                                    falsch.visibility = View.VISIBLE
                                }
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                    }
                } else {
                    val appWidgetManager =
                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                    WidgeteinzelnesMemo.updateAppWidget(
                        this@WidgeteinzelnesMemoConfigureActivity,
                        appWidgetManager,
                        mAppWidgetId
                    )
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    setResult(RESULT_OK, resultValue)
                    finish()
                }
            } else {
                finish()
                Toasty.warning(
                    this@WidgeteinzelnesMemoConfigureActivity,
                    getString(R.string.nomemoselected),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun fingerprintinit() {
        promptInfo = PromptInfo.Builder()
            .setTitle("Memo")
            .setDescription("Nutz' deinen Fingerabdruck um deine Memos entsperren")
            .setConfirmationRequired(true)
            .setNegativeButtonText("Passwort verwenden")
            .build()
        myBiometricPrompt = BiometricPrompt(
            this,
            Executors.newSingleThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread {
                        val dialog =
                            Dialog(this@WidgeteinzelnesMemoConfigureActivity, R.style.AppDialog)
                        dialog.setContentView(R.layout.insertpasswort)
                        val falsch = dialog.findViewById<TextView>(R.id.textView12)
                        falsch.visibility = View.GONE
                        val pass = dialog.findViewById<EditText>(R.id.editText5)
                        pass.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                falsch.visibility = View.GONE
                                if (passwort == pass.text.toString()) {
                                    val appWidgetManager =
                                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                                    WidgeteinzelnesMemo.updateAppWidget(
                                        this@WidgeteinzelnesMemoConfigureActivity,
                                        appWidgetManager,
                                        mAppWidgetId
                                    )
                                    val resultValue = Intent()
                                    resultValue.putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        mAppWidgetId
                                    )
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (passwort == pass.text.toString()) {
                                    val appWidgetManager =
                                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                                    WidgeteinzelnesMemo.updateAppWidget(
                                        this@WidgeteinzelnesMemoConfigureActivity,
                                        appWidgetManager,
                                        mAppWidgetId
                                    )
                                    val resultValue = Intent()
                                    resultValue.putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        mAppWidgetId
                                    )
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                    dialog.dismiss()
                                } else {
                                    falsch.visibility = View.VISIBLE
                                    falsch.text =
                                        this@WidgeteinzelnesMemoConfigureActivity.getString(R.string.wrongpassword)
                                }
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                        dialog.show()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val appWidgetManager =
                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                    WidgeteinzelnesMemo.updateAppWidget(
                        this@WidgeteinzelnesMemoConfigureActivity,
                        appWidgetManager,
                        mAppWidgetId
                    )
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    setResult(RESULT_OK, resultValue)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {
                        val dialog =
                            Dialog(this@WidgeteinzelnesMemoConfigureActivity, R.style.AppDialog)
                        dialog.setContentView(R.layout.insertpasswort)
                        val falsch = dialog.findViewById<TextView>(R.id.textView12)
                        falsch.visibility = View.GONE
                        val pass = dialog.findViewById<EditText>(R.id.editText5)
                        pass.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                falsch.visibility = View.GONE
                                if (passwort == pass.text.toString()) {
                                    val appWidgetManager =
                                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                                    WidgeteinzelnesMemo.updateAppWidget(
                                        this@WidgeteinzelnesMemoConfigureActivity,
                                        appWidgetManager,
                                        mAppWidgetId
                                    )
                                    val resultValue = Intent()
                                    resultValue.putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        mAppWidgetId
                                    )
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (passwort == pass.text.toString()) {
                                    val appWidgetManager =
                                        AppWidgetManager.getInstance(this@WidgeteinzelnesMemoConfigureActivity)
                                    WidgeteinzelnesMemo.updateAppWidget(
                                        this@WidgeteinzelnesMemoConfigureActivity,
                                        appWidgetManager,
                                        mAppWidgetId
                                    )
                                    val resultValue = Intent()
                                    resultValue.putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        mAppWidgetId
                                    )
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                    dialog.dismiss()
                                } else {
                                    falsch.visibility = View.VISIBLE
                                    falsch.text =
                                        this@WidgeteinzelnesMemoConfigureActivity.getString(R.string.wrongpassword)
                                }
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                        dialog.show()
                    }
                }
            })
    }

    private fun authenticate() {
        myBiometricPrompt!!.authenticate(promptInfo!!)
    }

    companion object {
        var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }
}
