package de.kruemelopment.org.memo_sql

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import es.dmoral.toasty.Toasty
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Objects

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var last: Fragment = Allmems.newInstance("")
    private var lastitem: MenuItem? = null
    private var lastzahl = R.id.allmemo
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val lol = PreferenceManager.getDefaultSharedPreferences(this)
            val nacht = lol.getBoolean("nightmode", false)
            if (nacht) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        againlocken = sharedPreferences.getBoolean("devicelock", false)
        usefingerprint = sharedPreferences.getBoolean("fingerprint", false)
        val sese = getSharedPreferences("Start", 0)
        val web = sese.getBoolean("agbs", false)
        if (!web) {
            val dialog = Dialog(this, R.style.AppDialog)
            dialog.setContentView(R.layout.webdialog)
            val ja = dialog.findViewById<TextView>(R.id.textView5)
            val nein = dialog.findViewById<TextView>(R.id.textView8)
            ja.setOnClickListener {
                val sp8 = getSharedPreferences("Start", 0)
                val ed = sp8.edit()
                ed.putBoolean("agbs", true)
                ed.apply()
                dialog.dismiss()
            }
            nein.setOnClickListener { finishAndRemoveTask() }
            val textView = dialog.findViewById<TextView>(R.id.textView4)
            textView.text = Html.fromHtml(getString(R.string.agreement),Html.FROM_HTML_MODE_LEGACY)
            textView.movementMethod = LinkMovementMethod.getInstance()
            dialog.setCancelable(false)
            dialog.show()
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView = findViewById(R.id.nav_view)
        navigationView!!.setNavigationItemSelectedListener(this)
        lastitem = navigationView!!.checkedItem
        try {
            if (Objects.requireNonNull<String?>(intent.getStringExtra("Hallo")).isNotEmpty()) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(
                    R.id.frame_layout,
                    Allmems.newInstance(intent.getStringExtra("Hallo"))
                )
                transaction.commit()
                handlelink(intent)
            } else {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, Allmems.newInstance(""))
                transaction.commit()
                handlelink(intent)
            }
        } catch (e: Exception) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, Allmems.newInstance(""))
            transaction.commit()
            handlelink(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (lastzahl != R.id.allmemo) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, Allmems.newInstance(""), "Allems")
            transaction.commit()
            lastitem!!.setChecked(false)
            navigationView!!.setCheckedItem(R.id.allmemo)
            last = Allmems.newInstance("")
            lastzahl = R.id.allmemo
            supportActionBar!!.title = getString(R.string.memos)
        } else super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when {
            id == R.id.newmemo && lastzahl != id -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, Newmemo.newInstance(""))
                transaction.commit()
                lastitem!!.setChecked(false)
                item.setChecked(true)
                last = Newmemo.newInstance("")
                supportActionBar!!.title = getString(R.string.newmemo)
            }
            id == R.id.allmemo && lastzahl != id -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, Allmems.newInstance(""), "Allems")
                transaction.commit()
                lastitem!!.setChecked(false)
                item.setChecked(true)
                last = Allmems.newInstance("")
                supportActionBar!!.title = getString(R.string.memos)
            }
            id == R.id.papierkorbanz && lastzahl != id -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, Papierkorb.newInstance())
                transaction.commit()
                lastitem!!.setChecked(false)
                item.setChecked(true)
                last = Papierkorb.newInstance()
                supportActionBar!!.title = getString(R.string.papierkorb)
            }
            id == R.id.settings && lastzahl != id -> {
                lastitem!!.setChecked(false)
                item.setChecked(true)
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, SettingsFragment())
                transaction.commit()
                last = SettingsFragment()
                supportActionBar!!.title = getString(R.string.settings)
            }
            id == R.id.nutz && lastzahl != id -> {
                val uri = Uri.parse(getString(R.string.nutzungsbedingungen))
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            id == R.id.daten && lastzahl != id -> {
                val uri = Uri.parse(getString(R.string.datens))
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            id == R.id.feed && lastzahl != id -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("mailto:kontakt@kruemelopment-dev.de"))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            id == R.id.sources && lastzahl != id -> {
                lastitem!!.setChecked(false)
                item.setChecked(true)
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, Libraries())
                transaction.commit()
                last = Libraries()
                supportActionBar!!.title = getString(R.string.libraries)
            }
        }
        if (id != R.id.nutz && id != R.id.daten && id != R.id.feed) lastzahl = id
        lastitem = item
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlelink(intent)
    }

    private fun handlelink(intent: Intent) {
        val appLinkAction = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == appLinkAction && type != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(
                R.id.frame_layout,
                Newmemo.newInstance(intent.getStringExtra(Intent.EXTRA_TEXT))
            )
            transaction.commit()
        } else {
            if (Intent.ACTION_VIEW == appLinkAction) {
                val mimetype = getFileName(intent.data!!)
                if (mimetype!!.contains("membck")) {
                    if (intent.data == null) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.restored_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    try {
                        val cr = contentResolver
                        val `is` = cr.openInputStream(intent.data!!) ?: return
                        val buf = StringBuilder()
                        val reader = BufferedReader(InputStreamReader(`is`))
                        var str: String?
                        while (reader.readLine().also { str = it } != null) {
                            buf.append(str).append("\n")
                        }
                        `is`.close()
                        val help =
                            buf.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val myDB = DataBaseHelper(this@MainActivity)
                        var w = true
                        for (s in help) {
                            val array = s.split("§%21/".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            val a = myDB.insertData(
                                array[0],
                                array[1],
                                array[2].replace("%20leerzeichen", "\n"),
                                array[3],
                                "false",
                                null
                            )
                            if (!a) w = false
                        }
                        if (w) {
                            Toasty.success(
                                this@MainActivity,
                                getString(R.string.restored_sucessful),
                                Toast.LENGTH_SHORT
                            ).show()
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.replace(
                                R.id.frame_layout,
                                Allmems.newInstance("")
                            )
                            transaction.commit()
                        } else {
                            Toasty.info(
                                this@MainActivity,
                                getString(R.string.restored_partially_sucessful),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toasty.error(this, getString(R.string.restored_failed), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else if (mimetype.contains("mem")) {
                    try {
                        if (intent.data == null) {
                            Toasty.info(this, getString(R.string.notloaded), Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                        val cr = contentResolver
                        val `is` = cr.openInputStream(intent.data!!) ?: return
                        val buf = StringBuilder()
                        val reader = BufferedReader(InputStreamReader(`is`))
                        var str: String?
                        while (reader.readLine().also { str = it } != null) {
                            buf.append(str)
                        }
                        `is`.close()
                        val array =
                            buf.toString().split("§%21/".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val dialog = Dialog(this, R.style.AppDialog)
                        dialog.setContentView(R.layout.memoerhalten)
                        val titel = dialog.findViewById<TextView>(R.id.editText09)
                        val inahlt = dialog.findViewById<TextView>(R.id.editText32)
                        inahlt.movementMethod = ScrollingMovementMethod()
                        val kategorie = dialog.findViewById<TextView>(R.id.editText209)
                        val save = dialog.findViewById<ImageView>(R.id.imageView42)
                        val cancel = dialog.findViewById<ImageView>(R.id.imageView62)
                        inahlt.movementMethod = ScrollingMovementMethod()
                        titel.text = array[1]
                        inahlt.text = array[2].replace("%20leerzeichen", "\n")
                        kategorie.text = array[0]
                        save.setOnClickListener {
                            dialog.dismiss()
                            val myDB = DataBaseHelper(this@MainActivity)
                            val reult = myDB.insertData(
                                array[0],
                                array[1],
                                array[2].replace("%20leerzeichen", "\n"),
                                array[3],
                                "false",
                                null
                            )
                            myDB.close()
                            if (reult) {
                                Toasty.success(
                                    this@MainActivity,
                                    getString(R.string.safed_succesful),
                                    Toast.LENGTH_SHORT
                                ).show()
                                val transaction = supportFragmentManager.beginTransaction()
                                transaction.replace(
                                    R.id.frame_layout,
                                    Allmems.newInstance("")
                                )
                                transaction.commit()
                            } else {
                                Toasty.error(
                                    this@MainActivity,
                                    getString(R.string.safe_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        cancel.setOnClickListener { dialog.dismiss() }
                        dialog.show()
                    } catch (e: Exception) {
                        Toasty.error(this, getString(R.string.notloaded), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toasty.error(this, getString(R.string.nofile), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = if (result != null) result!!.lastIndexOf('/') else 0
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result
    }

    companion object {
        var first = false
        var againlocken = false
        var usefingerprint = false
        var navigationView: NavigationView? = null
    }
}