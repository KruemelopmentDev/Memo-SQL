package de.kruemelopment.org.memo_sql

import android.app.AlertDialog
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import es.dmoral.toasty.Toasty
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class CustomBaseAdapter(
    var context: Context?,
    var rowItems: MutableList<Liste>,
    myDB: DataBaseHelper
) : RecyclerView.Adapter<CustomBaseAdapter.MyViewHolder>() {
    private var resul = false
    private var klappen = true
    private var outtoRight: Animation? = null
    var alleitems = ArrayList<Liste>()
    var a: Boolean
    var b: Boolean
    var geklicktes = 0
    var myDB: DataBaseHelper
    private var myBiometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null
    var adapterFragment: AdaptertoFragment? = null

    init {
        alleitems.addAll(rowItems)
        this.myDB = myDB
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            context!!
        )
        a = sharedPreferences.getBoolean("fingerprint", false)
        b = sharedPreferences.getBoolean("allwithfinger", false)
        if (a) {
            fingerprintinit()
        }
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
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

        init {
            textView = v.findViewById(R.id.textView2)
            textView1 = v.findViewById(R.id.textView3)
            textView2 = v.findViewById(R.id.textView4)
            textView3 = v.findViewById(R.id.textView5)
            imageView = v.findViewById(R.id.imageView2)
            imageView2 = v.findViewById(R.id.imageView3)
            imageView3 = v.findViewById(R.id.imageView5)
            imageView4 = v.findViewById(R.id.imageView7)
            imageView5 = v.findViewById(R.id.imageView10)
            relativeLayout = v.findViewById(R.id.tallayout)
            schloss = v.findViewById(R.id.imageView11)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.allememoscard, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val rowItem = rowItems[position]
        if (rowItem.title == context!!.getString(R.string.nomemossafed) || rowItem.title == context!!.getString(
                R.string.nomemochars
            ) || rowItem.title == context!!.getString(R.string.nomemoselected)
        ) {
            holder.textView1!!.gravity = Gravity.CENTER
            holder.textView1!!.text = rowItem.title
            holder.textView!!.text = context!!.getString(R.string.nomemo)
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
                holder.textView1!!.text = rowItem.inhalt
                holder.schloss!!.visibility = View.GONE
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
            holder.textView!!.text = rowItem.title
            holder.textView2!!.text = rowItem.thema
            holder.textView3!!.text = rowItem.datum
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
                                    falsch.text = context!!.getString(R.string.wrongpassword)
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
                    rowItem.isLocked = true
                    holder.imageView5!!.rotation = 0f
                    notifyItemChanged(holder.adapterPosition)
                }
            }
            holder.imageView!!.setImageResource(R.drawable.pencil_outline)
            holder.imageView2!!.setImageResource(R.drawable.delete_outline)
            holder.imageView3!!.setImageResource(R.drawable.share_variant)
            if (rowItem.favo == "true") holder.imageView4!!.setImageResource(R.drawable.staryellow) else holder.imageView4!!.setImageResource(
                R.drawable.stargrey
            )
            holder.id = rowItem.id
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
            holder.imageView5!!.visibility = View.INVISIBLE
            holder.textView1!!.post {
                val lineCnt = holder.textView1!!.lineCount
                if (lineCnt > 10) {
                    holder.imageView5!!.visibility = View.VISIBLE
                    if (holder.imageView5!!.rotation == 0f) holder.imageView5!!.setImageResource(R.drawable.chevron_down) else {
                        holder.imageView5!!.setImageResource(R.drawable.chevron_down)
                        holder.imageView5!!.rotation = 180f
                    }
                    val lineEndIndex = holder.textView1!!.layout.getLineEnd(9)
                    val text =
                        holder.textView1!!.text.subSequence(0, lineEndIndex - 3).toString() + "..."
                    holder.textView1!!.text = text
                } else {
                    holder.imageView5!!.visibility = View.GONE
                }
            }
            holder.imageView!!.setOnClickListener {
                if (!rowItem.isLocked) {
                    val dialog = Dialog(context!!, R.style.AppDialog)
                    dialog.setContentView(R.layout.neuesmemo)
                    val relativeLayout = dialog.findViewById<RelativeLayout>(R.id.layoutmain)
                    relativeLayout.gravity = Gravity.CENTER
                    val params = relativeLayout.layoutParams as FrameLayout.LayoutParams
                    params.setMargins(5, 5, 5, 5)
                    relativeLayout.layoutParams = params
                    val passwort = dialog.findViewById<EditText>(R.id.editText4)
                    val lock = dialog.findViewById<ImageView>(R.id.imageView13)
                    val editText = dialog.findViewById<AutoCompleteTextView>(R.id.editText2)
                    val editText2 = dialog.findViewById<EditText>(R.id.editText)
                    val editText3 = dialog.findViewById<EditText>(R.id.editText3)
                    editText.setText(rowItem.thema)
                    editText2.setText(rowItem.title)
                    editText3.setText(rowItem.inhalt)
                    val locked2 = BooleanArray(1)
                    if (rowItem.passwort != null) {
                        passwort.visibility = View.VISIBLE
                        passwort.setText(rowItem.passwort)
                        lock.setImageResource(R.drawable.lock_outline)
                        locked2[0] = true
                    } else {
                        passwort.visibility = View.GONE
                        lock.setImageResource(R.drawable.lock_open_outline)
                    }
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
                    val myDB = DataBaseHelper(context)
                    val cur = myDB.allData
                    val array = ArrayList<String>()
                    if (cur.count > 0) {
                        while (cur.moveToNext()) {
                            val uname = cur.getString(2)
                            if (!array.contains(uname)) array.add(uname)
                        }
                    }
                    val adapter =
                        ArrayAdapter(context!!, android.R.layout.simple_list_item_1, array)
                    editText.setAdapter(adapter)
                    save.setOnClickListener {
                        if (editText.text.toString().isEmpty()) editText.setText(
                            context!!.getString(R.string.nocategorie)
                        )
                        if (editText2.text.toString()
                                .isEmpty()
                        ) editText2.setText(context!!.getString(R.string.notitle))
                        if (editText3.text.toString()
                                .isEmpty()
                        ) editText3.setText(context!!.getString(R.string.nocontent))
                        val titell = editText2.text.toString()
                        val themaa = editText.text.toString()
                        val inhaltt = editText3.text.toString()
                        var passworr: String? = passwort.text.toString()
                        if (passworr!!.isEmpty()) passworr = null
                        if (passwort.visibility == View.GONE) passworr = null
                        val c = Calendar.getInstance()
                        val df = SimpleDateFormat("dd.MM.yyyy,HH:mm:ss", Locale.GERMAN)
                        val date = df.format(c.time)
                        val result: Boolean = if (rowItem.favo == "true") myDB.updateData(
                            holder.id,
                            themaa,
                            titell,
                            inhaltt,
                            date,
                            "true",
                            passworr
                        ) else myDB.updateData(
                            holder.id,
                            themaa,
                            titell,
                            inhaltt,
                            date,
                            "false",
                            passworr
                        )
                        if (result) {
                            Toasty.success(
                                context!!,
                                context!!.getString(R.string.changed_succesful),
                                Toast.LENGTH_SHORT
                            ).show()
                            val widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(
                                ComponentName(
                                    context!!, MemoListe::class.java
                                )
                            )
                            for (id in widgetIDs) AppWidgetManager.getInstance(context)
                                .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                            val intent = Intent(context, WidgeteinzelnesMemo::class.java)
                            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                                ComponentName(
                                    context!!, WidgeteinzelnesMemo::class.java
                                )
                            )
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                            context!!.sendBroadcast(intent)
                            rowItem.title = titell
                            rowItem.inhalt = inhaltt
                            rowItem.datum = date
                            rowItem.thema = themaa
                            rowItem.passwort = passworr
                            if (passworr != null) rowItem.isLocked = true
                            alleitems.clear()
                            rowItems = sortieren(rowItems)
                            alleitems.addAll(rowItems)
                            dialog.dismiss()
                            holder.imageView5!!.rotation = 0f
                            notifyItemChanged(holder.adapterPosition)
                        } else Toasty.error(
                            context!!,
                            context!!.getString(R.string.change_couldnt),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    cancel.setOnClickListener { dialog.dismiss() }
                } else Toasty.warning(
                    context!!,
                    context!!.getString(R.string.firstunlock),
                    Toast.LENGTH_SHORT
                ).show()
                myDB.close()
            }
            holder.imageView2!!.setOnClickListener {
                val builder = AlertDialog.Builder(
                    context
                )
                builder.setTitle(context!!.getString(R.string.confirmdeletion))
                    .setMessage(context!!.getString(R.string.deletethismemo)).setPositiveButton(
                        context!!.getString(R.string.yes)
                    ) { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                        val pb = PapierkorbHelper(context)
                        pb.insertData(
                            rowItem.thema,
                            rowItem.title,
                            rowItem.inhalt,
                            rowItem.datum,
                            rowItem.passwort,
                            rowItem.favo
                        )
                        val myDb = DataBaseHelper(context)
                        myDb.deleteData(holder.id)
                        myDb.close()
                        pb.close()
                        Toasty.success(
                            context!!,
                            context!!.getString(R.string.delete_succesful),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(context, WidgeteinzelnesMemo::class.java)
                        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                            ComponentName(
                                context!!, WidgeteinzelnesMemo::class.java
                            )
                        )
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                        context!!.sendBroadcast(intent)
                        outtoRight = TranslateAnimation(
                            Animation.RELATIVE_TO_PARENT, 0.0f,
                            Animation.RELATIVE_TO_PARENT, +1.0f,
                            Animation.RELATIVE_TO_PARENT, 0.0f,
                            Animation.RELATIVE_TO_PARENT, 0.0f
                        )
                        outtoRight!!.duration = 300
                        outtoRight!!.interpolator = AccelerateInterpolator()
                        holder.relativeLayout!!.startAnimation(outtoRight)
                        outtoRight!!.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {}
                            override fun onAnimationEnd(animation: Animation) {
                                rowItems.remove(rowItem)
                                alleitems.remove(rowItem)
                                if (rowItems.isEmpty()) {
                                    rowItems.add(
                                        Liste(
                                            "",
                                            context!!.getString(R.string.nomemossafed),
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            false
                                        )
                                    )
                                    if (adapterFragment != null) adapterFragment!!.hidemenuitems()
                                    notifyItemChanged(holder.adapterPosition)
                                    val outtoRight2: Animation = TranslateAnimation(
                                        Animation.RELATIVE_TO_PARENT, -1.0f,
                                        Animation.RELATIVE_TO_PARENT, 0.0f,
                                        Animation.RELATIVE_TO_PARENT, 0.0f,
                                        Animation.RELATIVE_TO_PARENT, 0.0f
                                    )
                                    outtoRight2.duration = 300
                                    outtoRight2.interpolator = AccelerateInterpolator()
                                    holder.relativeLayout!!.startAnimation(outtoRight2)
                                } else notifyItemRemoved(holder.adapterPosition)
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
            holder.imageView3!!.setOnClickListener {
                if (!rowItem.isLocked) {
                    val builder = AlertDialog.Builder(
                        context
                    )
                    builder.setTitle(context!!.getString(R.string.share))
                        .setMessage(context!!.getString(R.string.share_how)).setPositiveButton(
                            context!!.getString(R.string.asfile)
                        ) { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                            val text =
                                rowItem.thema + "§%21/" + rowItem.title + "§%21/" + rowItem.inhalt.replace(
                                    "\n",
                                    "%20leerzeichen"
                                ) + "§%21/" + rowItem.datum
                            val memo = File(
                                context!!.getExternalFilesDir(null),
                                rowItem.title.replace(".", "-") + ".mem"
                            )
                            if (memo.exists()) memo.delete()
                            try {
                                memo.createNewFile()
                                val writer = BufferedWriter(FileWriter(memo, true /*append*/))
                                writer.write(text)
                                writer.close()
                                val intentShareFile = Intent(Intent.ACTION_SEND)
                                intentShareFile.setType("application/mem")
                                val uri = FileProvider.getUriForFile(
                                    context!!,
                                    "de.kruemelopment.org.memo_sql.provider",
                                    memo
                                )
                                intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
                                context!!.startActivity(
                                    Intent.createChooser(
                                        intentShareFile,
                                        context!!.getString(R.string.sharevia)
                                    )
                                )
                            } catch (e: IOException) {
                                Toasty.error(
                                    context!!,
                                    context!!.getString(R.string.smtwentwrong),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .setNegativeButton(context!!.getString(R.string.astext)) { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                            val send = """
                        ${rowItem.title}
                        
                        ${rowItem.inhalt}
                        ${context!!.getString(R.string.categorie)}:${rowItem.thema}
                        """.trimIndent()
                            val shareIntent = Intent()
                            shareIntent.setAction(Intent.ACTION_SEND)
                            shareIntent.setType("text/plain")
                            shareIntent.putExtra(Intent.EXTRA_TEXT, send)
                            context!!.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    context!!.getString(R.string.sharevia)
                                )
                            )
                        }
                        .show()
                } else Toasty.warning(
                    context!!,
                    context!!.getString(R.string.firstunlock),
                    Toast.LENGTH_SHORT
                ).show()
            }
            holder.imageView4!!.setOnClickListener {
                val result: Boolean
                val myDb = DataBaseHelper(context)
                if (resul) {
                    result = myDb.updateData(
                        holder.id,
                        rowItem.thema,
                        rowItem.title,
                        rowItem.inhalt,
                        rowItem.datum,
                        "false",
                        rowItem.passwort
                    )
                    if (result) {
                        holder.imageView4!!.setImageResource(R.drawable.stargrey)
                        resul = false
                        rowItem.favo = "false"
                        alleitems[position].favo = "false"
                    } else Toasty.error(
                        context!!,
                        context!!.getString(R.string.smtwentwrong),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    result = myDb.updateData(
                        holder.id,
                        rowItem.thema,
                        rowItem.title,
                        rowItem.inhalt,
                        rowItem.datum,
                        "true",
                        rowItem.passwort
                    )
                    if (result) {
                        resul = true
                        rowItem.favo = "true"
                        alleitems[position].favo = "true"
                        holder.imageView4!!.setImageResource(R.drawable.staryellow)
                    } else Toasty.error(
                        context!!,
                        context!!.getString(R.string.smtwentwrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                myDb.close()
                val widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(
                        context!!, MemoListe::class.java
                    )
                )
                for (id in widgetIDs) AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
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
    }

    override fun getItemCount(): Int {
        return rowItems.size
    }


    override fun getItemId(position: Int): Long {
        return rowItems[position].hashCode().toLong()
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
                                    notifyItemChanged(geklicktes)
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    notifyItemChanged(geklicktes)
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
                            notifyItemRangeChanged(0,rowItems.size)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toasty.success(context!!, "Memo entsperrt", Toast.LENGTH_SHORT).show()
                            rowItems[geklicktes].isLocked = false
                            notifyItemChanged(geklicktes)
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
                                    notifyItemChanged(geklicktes)
                                    dialog.dismiss()
                                }
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        pass.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                if (rowItem.passwort == pass.text.toString()) {
                                    rowItem.isLocked = false
                                    notifyItemChanged(geklicktes)
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

    private fun sortieren(a: MutableList<Liste>): MutableList<Liste> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            context!!
        )
        when (sharedPreferences.getString("order", "1")!!.toInt()) {
            1 -> {
                val formatter = SimpleDateFormat("dd.MM.yyyy,hh:mm:ss", Locale.GERMAN)
                a.sortWith { o1: Liste, o2: Liste ->
                    val a1 = o1.datum
                    val b1 = o2.datum
                    try {
                        val date = formatter.parse(a1)
                        val date2 = formatter.parse(b1)
                        assert(date != null)
                        assert(date2 != null)
                        date!!.compareTo(date2)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        Toasty.error(
                            context!!,
                            context!!.getString(R.string.sorterror),
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
                        assert(date2 != null)
                        date!!.compareTo(date2)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        Toasty.error(
                            context!!,
                            context!!.getString(R.string.sorterror),
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
                a.sortWith { o1: Liste, o2: Liste -> o1.title.compareTo(o2.title) }
            }
            6 -> {
                a.sortWith { o1: Liste, o2: Liste -> o2.title.compareTo(o1.title) }
            }
        }
        return a
    }

    private fun sortlang(sorr: MutableList<Liste>): MutableList<Liste> {
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

    private fun sortkurz(sorr: MutableList<Liste>): MutableList<Liste> {
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

    fun setAdaptertoFragment(adaptertoFragment: AdaptertoFragment?) {
        this.adapterFragment = adaptertoFragment
    }

    fun unsetAdaptertoFragment() {
        adapterFragment = null
    }
}