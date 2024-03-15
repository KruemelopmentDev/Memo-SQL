package de.kruemelopment.org.memo_sql

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.franmontiel.attributionpresenter.AttributionPresenter
import com.franmontiel.attributionpresenter.entities.Attribution
import com.franmontiel.attributionpresenter.entities.License

class Libraries : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.libraries, container, false)
        setHasOptionsMenu(true)
        val listView = view.findViewById<ListView>(R.id.listesources)
        val attributionPresenter = AttributionPresenter.Builder(context)
            .addAttributions(
                Attribution.Builder("AndroidX Appcompat Library")
                    .addCopyrightNotice("The Android Open Source Project")
                    .addLicense(License.APACHE)
                    .build()
            )
            .addAttributions(
                Attribution.Builder("AndroidX Preference Library")
                    .addCopyrightNotice("The Android Open Source Project")
                    .addLicense(License.APACHE)
                    .build()
            )
            .addAttributions(
                Attribution.Builder("AndroidX Legacy-Preference Library")
                    .addCopyrightNotice("The Android Open Source Project")
                    .addLicense(License.APACHE)
                    .build()
            )
            .addAttributions(
                Attribution.Builder("Material Components For Android")
                    .addCopyrightNotice("The Android Open Source Project")
                    .addLicense(License.APACHE)
                    .build()
            )
            .addAttributions(
                Attribution.Builder("AndroidX Biometric")
                    .addCopyrightNotice("Google")
                    .addLicense(License.APACHE)
                    .build()
            )
            .addAttributions(
                Attribution.Builder("Toasty")
                    .addCopyrightNotice("Daniel Morales")
                    .addLicense(License.LGPL_3)
                    .setWebsite("https://github.com/GrenderG/Toasty")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("AttributionPresenter")
                    .addCopyrightNotice("Francisco José Montiel Navarro", "2017")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                    .build()
            )
            .build()
        listView.adapter = attributionPresenter.adapter
        listView.itemsCanFocus = false
        return view
    }
}
