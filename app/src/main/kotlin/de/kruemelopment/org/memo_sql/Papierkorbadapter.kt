package de.kruemelopment.org.memo_sql

import android.app.AlertDialog
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty
import java.util.Locale
import java.util.concurrent.Executors

class Papierkorbadapter internal constructor(
    var context: Context?,
    var rowItems: MutableList<Liste>
) : BaseAdapter() {
    private var klappen = true
    var alleitems = ArrayList<Liste>()
    var a: Boolean
    var b: Boolean
    var geklicktes = 0
    private var myBiometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null
    private var adaptertoFragment: AdaptertoFragment? = null

    init {
        alleitems.addAll(rowItems)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            context!!
        )
        a = sharedPreferences.getBoolean("fingerprint", false)
        b = sharedPreferences.getBoolean("allwithfinger", false)
        if (a) fingerprintinit()
    }

    /*private view holder class*/
    private class ViewHolder {
        var textView: TextView? = null
        var textView1: TextView? = null
        var textView2: TextView? = null
        var textView3: TextView? = null
        var id: String? = null
        var imageView: ImageView? = null
        var imageView2: ImageView? = null
        var imageView3: ImageView? = null
        var imageView4: ImageView? = null
        var imageView5: ImageView? = null
        var relativeLayout: RelativeLayout? = null
        var schloss: ImageView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder = ViewHolder()
        if (v == null) {
            val vi = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = vi.inflate(R.layout.allememoscard, parent, false)
            holder.textView = v.findViewById(R.id.textView2)
            holder.textView1 = v.findViewById(R.id.textView3)
            holder.textView2 = v.findViewById(R.id.textView4)
            holder.textView3 = v.findViewById(R.id.textView5)
            holder.imageView = v.findViewById(R.id.imageView2)
            holder.imageView2 = v.findViewById(R.id.imageView3)
            holder.imageView3 = v.findViewById(R.id.imageView5)
            holder.imageView4 = v.findViewById(R.id.imageView7)
            holder.imageView5 = v.findViewById(R.id.imageView10)
            holder.relativeLayout = v.findViewById(R.id.tallayout)
            holder.schloss = v.findViewById(R.id.imageView11)
        }
        val rowItem = getItem(position) as Liste
        if (rowItem.title == context!!.getString(R.string.nodeletedmemos) || rowItem.title == context!!.getString(
                R.string.nomemoselected
            ) || rowItem.title == context!!.getString(R.string.nomemochars)
        ) {
            holder.textView1!!.gravity = Gravity.CENTER
            holder.textView1!!.text = rowItem.title
            holder.textView!!.text = context!!.getString(R.string.no_memos)
            holder.textView2!!.text = ""
            holder.textView3!!.text = ""
            holder.imageView!!.setImageResource(0)
            holder.imageView2!!.setImageResource(0)
            holder.imageView3!!.setImageResource(0)
            holder.imageView4!!.setImageResource(0)
            holder.imageView5!!.setImageResource(0)
            holder.schloss!!.setImageResource(0)
        } else {
            var showicon = true
            if (rowItem.passwort == null) {
                showicon = false
                rowItem.isLocked = false
            }
            if (!showicon) {
                holder.schloss!!.visibility = View.GONE
                holder.textView1!!.text = rowItem.inhalt
            } else {
                holder.schloss!!.visibility = View.VISIBLE
                if (rowItem.isLocked) {
                    val load = rowItem.inhalt
                    val resultnew = StringBuilder()
                    for (i in load.indices) {
                        if (i + 1 <= load.length) {
                            if (load.startsWith(
                                    "\n",
                                    i
                                )
                            ) resultnew.append("\n") else resultnew.append("*")
                        } else resultnew.append("*")
                    }
                    holder.textView1!!.text = resultnew.toString()
                    holder.schloss!!.setImageResource(R.drawable.lock_outline)
                } else {
                    holder.textView1!!.text = rowItem.inhalt
                    holder.schloss!!.setImageResource(R.drawable.lock_open_outline)
                }
            }
            holder.imageView3!!.setImageResource(0)
            holder.textView!!.text = rowItem.title
            holder.textView2!!.text = rowItem.thema
            holder.textView3!!.text = rowItem.datum
            holder.imageView4!!.setImageResource(0)
            holder.id = rowItem.id
            holder.imageView!!.setImageResource(R.drawable.open_in_app)
            holder.imageView2!!.setImageResource(R.drawable.delete_forever_outline2)
            holder.schloss!!.setOnClickListener {
                if (rowItem.isLocked) {
                    if (a) {
                        geklicktes = position
                        authenticate()
                    } else {
                        val dialog = Dialog(context!!, R.style.AppDialog)
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
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    holder.textView1!!.text = rowItem.inhalt
                                    holder.schloss!!.setImageResource(R.drawable.lock_open_outline)
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    holder.textView1!!.text = rowItem.inhalt
                                    holder.schloss!!.setImageResource(R.drawable.lock_open_outline)
                                    dialog.dismiss()
                                } else {
                                    falsch.visibility = View.VISIBLE
                                }
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                        dialog.show()
                    }
                } else {
                    holder.schloss!!.setImageResource(R.drawable.lock_outline)
                    val load = rowItem.inhalt
                    val resultnew = StringBuilder()
                    for (i in load.indices) {
                        if (i + 1 <= load.length) {
                            if (load.startsWith(
                                    "\n",
                                    i
                                )
                            ) resultnew.append("\n") else resultnew.append("*")
                        } else resultnew.append("*")
                    }
                    holder.textView1!!.text = resultnew.toString()
                    holder.imageView5!!.rotation = 0f
                    rowItem.isLocked = true
                    notifyDataSetChanged()
                }
            }
            holder.textView1!!.setOnLongClickListener {
                if (!rowItem.isLocked) {
                    val clipboard =
                        context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(
                        context!!.getString(R.string.app_name),
                        rowItem.inhalt
                    )
                    clipboard.setPrimaryClip(clip)
                    Toasty.info(
                        context!!,
                        context!!.getString(R.string.copyclipboard),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
            holder.textView1!!.setOnClickListener {
                if (holder.imageView5!!.visibility == View.VISIBLE) {
                    if (!rowItem.isLocked) {
                        if (holder.textView1!!.text.toString() == rowItem.inhalt) {
                            holder.imageView5!!.rotation = 0f
                            val lineEndIndex = holder.textView1!!.layout.getLineEnd(9)
                            val text =
                                holder.textView1!!.text.subSequence(0, lineEndIndex - 3)
                                    .toString() + "..."
                            holder.textView1!!.text = text
                            klappen = true
                        } else {
                            holder.imageView5!!.rotation = 180f
                            holder.textView1!!.text = rowItem.inhalt
                            klappen = false
                        }
                    } else Toasty.warning(
                        context!!,
                        context!!.getString(R.string.firstunlock),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            holder.textView1!!.post {
                val lineCnt = holder.textView1!!.lineCount
                if (lineCnt > 10) {
                    if (holder.imageView5!!.rotation == 0f) holder.imageView5!!.setImageResource(R.drawable.chevron_down) else {
                        holder.imageView5!!.setImageResource(R.drawable.chevron_down)
                        holder.imageView5!!.rotation = 180f
                    }
                    val lineEndIndex = holder.textView1!!.layout.getLineEnd(9)
                    val text =
                        holder.textView1!!.text.subSequence(0, lineEndIndex - 3).toString() + "..."
                    holder.textView1!!.text = text
                } else {
                    holder.imageView5!!.setImageResource(0)
                }
            }
            holder.imageView!!.setOnClickListener {
                val myDb = PapierkorbHelper(context)
                val res = myDb.allData
                myDb.close()
                var titel: String? = ""
                var thema: String? = ""
                var datum: String? = ""
                var inhalt: String? = ""
                var passwort: String? = ""
                if (res.count > 0) {
                    while (res.moveToNext()) {
                        if (res.getString(0) == holder.id) {
                            titel = res.getString(1)
                            thema = res.getString(2)
                            datum = res.getString(4)
                            inhalt = res.getString(3)
                            passwort = res.getString(6)
                        }
                    }
                }
                val myDB = DataBaseHelper(context)
                val result = myDB.insertData(thema, titel, inhalt, datum, "false", passwort)
                myDB.close()
                if (result) {
                    Toasty.success(
                        context!!,
                        context!!.getString(R.string.restored_sucessful),
                        Toast.LENGTH_SHORT
                    ).show()
                    myDb.deleteData(holder.id)
                    val outtoRight: Animation = TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f
                    )
                    outtoRight.duration = 300
                    outtoRight.interpolator = AccelerateInterpolator()
                    holder.relativeLayout!!.startAnimation(outtoRight)
                    outtoRight.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            rowItems.removeAt(position)
                            alleitems.removeAt(position)
                            if (rowItems.isEmpty()) {
                                rowItems.add(
                                    0,
                                    Liste(
                                        "",
                                        context!!.getString(R.string.nodeletedmemos),
                                        "",
                                        "",
                                        "",
                                        "",
                                        null,
                                        false
                                    )
                                )
                                if (adaptertoFragment != null) adaptertoFragment!!.hidemenuitems()
                                notifyDataSetChanged()
                                val infromRight: Animation = TranslateAnimation(
                                    Animation.RELATIVE_TO_PARENT, 1.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f
                                )
                                infromRight.duration = 300
                                infromRight.interpolator = AccelerateInterpolator()
                                holder.relativeLayout!!.startAnimation(infromRight)
                            } else notifyDataSetChanged()
                            val widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(
                                ComponentName(
                                    context!!, MemoListe::class.java
                                )
                            )
                            for (id in widgetIDs) AppWidgetManager.getInstance(context)
                                .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                } else {
                    Toasty.error(
                        context!!,
                        context!!.getString(R.string.restored_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            holder.imageView2!!.setOnClickListener {
                val builder = AlertDialog.Builder(
                    context
                )
                builder.setTitle(context!!.getString(R.string.confirmdeletion))
                    .setMessage(context!!.getString(R.string.deleteforever)).setPositiveButton(
                    context!!.getString(R.string.yes)
                ) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    val myDb = PapierkorbHelper(context)
                    myDb.deleteData(holder.id)
                    myDb.close()
                    Toasty.success(
                        context!!,
                        context!!.getString(R.string.delete_succesful),
                        Toast.LENGTH_SHORT
                    ).show()
                    val outtoRight: Animation = TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f
                    )
                    outtoRight.duration = 300
                    outtoRight.interpolator = AccelerateInterpolator()
                    holder.relativeLayout!!.startAnimation(outtoRight)
                    outtoRight.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            rowItems.removeAt(position)
                            alleitems.removeAt(position)
                            if (rowItems.isEmpty()) {
                                rowItems.add(
                                    0,
                                    Liste(
                                        "",
                                        context!!.getString(R.string.nodeletedmemos),
                                        "",
                                        "",
                                        "",
                                        "",
                                        null,
                                        false
                                    )
                                )
                                if (adaptertoFragment != null) adaptertoFragment!!.hidemenuitems()
                                notifyDataSetChanged()
                                val infromLeft: Animation = TranslateAnimation(
                                    Animation.RELATIVE_TO_PARENT, -1.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f,
                                    Animation.RELATIVE_TO_PARENT, 0.0f
                                )
                                infromLeft.duration = 300
                                infromLeft.interpolator = AccelerateInterpolator()
                                holder.relativeLayout!!.startAnimation(infromLeft)
                            } else notifyDataSetChanged()
                            val widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(
                                ComponentName(
                                    context!!, MemoListe::class.java
                                )
                            )
                            for (id in widgetIDs) AppWidgetManager.getInstance(context)
                                .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                }
                    .setNegativeButton(context!!.getString(R.string.no)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                    .show()
            }
            holder.imageView5!!.setOnClickListener {
                if (!rowItem.isLocked) {
                    if (holder.textView1!!.text.toString() == rowItem.inhalt) {
                        holder.imageView5!!.rotation = 0f
                        val lineEndIndex = holder.textView1!!.layout.getLineEnd(9)
                        val text =
                            holder.textView1!!.text.subSequence(0, lineEndIndex - 3)
                                .toString() + "..."
                        holder.textView1!!.text = text
                        klappen = true
                    } else {
                        holder.imageView5!!.rotation = 180f
                        holder.textView1!!.text = rowItem.inhalt
                        klappen = false
                    }
                } else Toasty.warning(
                    context!!,
                    context!!.getString(R.string.firstunlock),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return v!!
    }

    override fun getCount(): Int {
        return rowItems.size
    }

    override fun getItem(position: Int): Any {
        return rowItems[position]
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].id!!.toLong()
    }

    fun reset() {
        rowItems.clear()
        rowItems.addAll(alleitems)
        notifyDataSetChanged()
    }

    fun search(charText: String) {
        rowItems.clear()
        if (charText.isEmpty()) rowItems.addAll(alleitems) else {
            for (wp in alleitems) {
                if (wp.title.contains(charText) || wp.inhalt.contains(charText) || wp.thema.contains(
                        charText
                    ) || wp.datum.contains(charText)
                ) {
                    rowItems.add(wp)
                } else {
                    if (wp.title.lowercase(Locale.getDefault())
                            .contains(charText.lowercase(Locale.getDefault())) || wp.inhalt.lowercase(
                            Locale.getDefault()
                        ).contains(charText.lowercase(Locale.getDefault())) || wp.datum.lowercase(
                            Locale.getDefault()
                        ).contains(charText.lowercase(Locale.getDefault())) || wp.thema.lowercase(
                            Locale.getDefault()
                        ).contains(charText.lowercase(Locale.getDefault()))
                    ) rowItems.add(wp)
                }
            }
            if (rowItems.isEmpty()) {
                val item =
                    Liste("", context!!.getString(R.string.nomemochars), "", "", "", "", "", false)
                rowItems.add(item)
            }
        }
        notifyDataSetChanged()
    }

    private fun fingerprintinit() {
        promptInfo = PromptInfo.Builder()
            .setTitle("Memo")
            .setDescription("Nutz' deinen Fingerabdruck um deine Memos entsperren")
            .setConfirmationRequired(true)
            .setNegativeButtonText("Passwort verwenden")
            .build()
        myBiometricPrompt = BiometricPrompt(
            (context as FragmentActivity?)!!,
            Executors.newSingleThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Handler(Looper.getMainLooper()).post {
                        val dialog = Dialog(context!!, R.style.AppDialog)
                        dialog.setContentView(R.layout.insertpasswort)
                        val rowItem = rowItems[geklicktes]
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
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    notifyDataSetChanged()
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    notifyDataSetChanged()
                                    dialog.dismiss()
                                } else {
                                    falsch.visibility = View.VISIBLE
                                    falsch.text = context!!.getString(R.string.wrongpassword)
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
                    if (b) {
                        Handler(Looper.getMainLooper()).post {
                            Toasty.success(context!!, "Memos entsperrt", Toast.LENGTH_SHORT).show()
                            for (i in rowItems.indices) {
                                rowItems[i].isLocked = false
                            }
                            notifyDataSetChanged()
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toasty.success(context!!, "Memo entsperrt", Toast.LENGTH_SHORT).show()
                            rowItems[geklicktes].isLocked = false
                            notifyDataSetChanged()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Handler(Looper.getMainLooper()).post {
                        val dialog = Dialog(context!!, R.style.AppDialog)
                        dialog.setContentView(R.layout.insertpasswort)
                        val rowItem = rowItems[geklicktes]
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
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    notifyDataSetChanged()
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    notifyDataSetChanged()
                                    dialog.dismiss()
                                } else {
                                    falsch.visibility = View.VISIBLE
                                    falsch.text = context!!.getString(R.string.wrongpassword)
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

    fun setAdaptertoFragment(adaptertoFragment: AdaptertoFragment?) {
        this.adaptertoFragment = adaptertoFragment
    }

    fun unsetAdaptertoFragment() {
        adaptertoFragment = null
    }
}