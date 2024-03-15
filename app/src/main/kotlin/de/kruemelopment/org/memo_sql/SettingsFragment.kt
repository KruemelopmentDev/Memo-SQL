package de.kruemelopment.org.memo_sql

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import es.dmoral.toasty.Toasty
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {
    private var fingerabdruck: SwitchPreference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val mCategory = findPreference<PreferenceCategory>("safety")
        fingerabdruck = findPreference("fingerprint")
        val all = findPreference<SwitchPreference>("allwithfinger")
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED, BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED, BiometricManager.BIOMETRIC_STATUS_UNKNOWN, BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                assert(mCategory != null)
                mCategory!!.removePreference(fingerabdruck!!)
                if (all != null) {
                    mCategory.removePreference(all)
                }
            }

            BiometricManager.BIOMETRIC_SUCCESS -> fingerabdruck!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, _: Any? -> true }
        }
        val switchPreference = findPreference<SwitchPreference>("devicelock")!!
        switchPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                MainActivity.againlocken = newValue as Boolean
                true
            }
        val myPref2 = findPreference<Preference>("makebackup")!!
        myPref2.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val c = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
                val titel = sdf.format(c.time)
                val name = "Memo_$titel.membck"
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("application/membck")
                intent.putExtra(Intent.EXTRA_TITLE, name)
                startActivityForResult(intent, 30)
                true
            }
        val myPref = findPreference<Preference>("openbackup")!!
        myPref.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                chooseFile.setType("*/*")
                startActivityForResult(
                    Intent.createChooser(chooseFile, "Datei auswählen"),
                    20
                )
                true
            }
        val delete = findPreference<Preference>("deleteforeverall")!!
        delete.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val builder = AlertDialog.Builder(
                    context
                )
                builder.setTitle(getString(R.string.confirmdeletion))
                    .setMessage(getString(R.string.deleteall))
                    .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                        DataBaseHelper(context).deleteAll()
                        PapierkorbHelper(context).deleteAll()
                        val widgetIDs = AppWidgetManager.getInstance(context)
                            .getAppWidgetIds(ComponentName(requireContext(), MemoListe::class.java))
                        for (id in widgetIDs) AppWidgetManager.getInstance(context)
                            .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
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
                true
            }
        val listPreference = findPreference<ListPreference>("orderwidget")!!
        listPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                val widgetIDs = AppWidgetManager.getInstance(
                    context
                ).getAppWidgetIds(ComponentName(requireContext(), MemoListe::class.java))
                for (id in widgetIDs) AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(id, R.id.listewidget)
                true
            }
        val darkmode = findPreference<SwitchPreference>("nightmode")
        val categ = findPreference<PreferenceCategory>("appsettings")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            assert(categ != null)
            if (darkmode != null) {
                categ!!.removePreference(darkmode)
            }
        } else {
            assert(darkmode != null)
            darkmode!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    if (newValue as Boolean) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                    MainActivity.navigationView!!.setCheckedItem(R.id.allmemo)
                    true
                }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 20) {
            try {
                if (data!!.data == null) {
                    Toasty.error(
                        requireContext(),
                        getString(R.string.restored_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (!getFileName(data.data)!!.contains("membck")) {
                    Toasty.info(requireContext(), getString(R.string.nobackup), Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                val cr = requireContext().contentResolver
                val `is` = cr.openInputStream(data.data!!) ?: return
                val buf = StringBuilder()
                val reader = BufferedReader(InputStreamReader(`is`))
                var str: String?
                while (reader.readLine().also { str = it } != null) {
                    buf.append(str).append("\n")
                }
                `is`.close()
                val help = buf.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val myDB = DataBaseHelper(requireContext())
                for (s in help) {
                    val array =
                        s.split("§%21/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (array.size == 5) myDB.insertData(
                        array[0],
                        array[1],
                        array[2].replace("%20leerzeichen", "\n"),
                        array[3],
                        "false",
                        array[4]
                    ) else myDB.insertData(
                        array[0],
                        array[1],
                        array[2].replace("%20leerzeichen", "\n"),
                        array[3],
                        "false",
                        null
                    )
                }
                Toasty.success(
                    requireContext(),
                    getString(R.string.restored_sucessful),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toasty.error(
                    requireContext(),
                    getString(R.string.restored_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == 30) {
            if (data!!.data == null) {
                Toasty.info(
                    requireContext(),
                    getString(R.string.nomemoscreated),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val myDB = DataBaseHelper(context)
            val res = myDB.allDatesDESC
            val memos: MutableList<String> = ArrayList()
            if (res.count > 0) {
                while (res.moveToNext()) {
                    var text =
                        res.getString(2) + "§%21/" + res.getString(1) + "§%21/" + res.getString(3)
                            .replace(
                                "\n",
                                "%20leerzeichen"
                            ) + "§%21/" + res.getString(4) + "§%21/" + res.getString(6)
                    if (text.endsWith("null")) text = text.substring(0, text.length - 9)
                    memos.add(text)
                }
                res.close()
            }
            val builder = StringBuilder()
            for (a in memos) builder.append(a).append("\n")
            var writing = builder.toString()
            writing = writing.substring(0, writing.length - 1)
            try {
                val cr = requireContext().contentResolver
                val os = cr.openOutputStream(data.data!!)
                if (os == null) {
                    Toasty.info(
                        requireContext(),
                        getString(R.string.nomemoscreated),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                os.write(writing.toByteArray())
                os.close()
                Toasty.success(
                    requireContext(),
                    getString(R.string.allsafedsucessful),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toasty.info(
                    requireContext(),
                    getString(R.string.nomemoscreated),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri?): String? {
        var result: String? = null
        if (uri!!.scheme == "content") {
            requireContext().contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = if (result != null) result!!.lastIndexOf('/') else 0
            if (cut != -1) {
                result = if (result != null) result!!.substring(cut + 1) else null
            }
        }
        return result
    }
}