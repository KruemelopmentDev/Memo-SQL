package de.kruemelopment.org.memo_sql.libraries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.kruemelopment.org.memo_sql.R
import org.kruemelopment.de.bewecker.libraries.LibraryListe

class LibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val licenseList: MutableList<LibraryListe> = ArrayList()
        licenseList.add(
            LibraryListe(
                "AppCompat",
                "Google",
                "Apache 2.0",
                "1.7.0",
                "29.05.2024",
                "https://developer.android.com/jetpack/androidx/releases/appcompat#1.7.0"
            )
        )
        licenseList.add(
            LibraryListe(
                "Material Components For Android",
                "Google",
                "Apache 2.0",
                "Apache 2.0",
                "02.05.2024",
                "https://github.com/material-components/material-components-android"
            )
        )
        licenseList.add(
            LibraryListe(
                "ConstraintLayout",
                "Google",
                "Apache 2.0",
                "2.2.1",
                "26.02.2025",
                "https://developer.android.com/jetpack/androidx/releases/constraintlayout#2.2.1"
            )
        )
        licenseList.add(
            LibraryListe(
                "Preference",
                "Google",
                "Apache 2.0",
                "1.2.1",
                "26.07.2023",
                "https://developer.android.com/jetpack/androidx/releases/preference#1.2.1"
            )
        )
        licenseList.add(
            LibraryListe(
                "RecyclerView",
                "The Android Open Source Project",
                "Apache 2.0",
                "1.4.0",
                "18.10.2023",
                "https://developer.android.com/jetpack/androidx/releases/recyclerview#1.4.0"
            )
        )
        licenseList.add(
            LibraryListe(
                "Toasty",
                "GrenderG",
                "LGPL 3.0",
                "1.5.2",
                "17.06.2022",
                "https://github.com/GrenderG/Toasty"
            )
        )

        licenseList.add(
            LibraryListe(
                "Flexbox-Layout",
                "Google",
                "Apache 2.0",
                "3.0.0",
                "21.05.2021",
                "https://github.com/google/flexbox-layout"
            )
        )

        licenseList.add(
            LibraryListe(
                "AndroidX Biometric",
                "The Android Open Source Project",
                "Apache 2.0",
                "1.1.0",
                "20.05.2025",
                "https://github.com/rengwuxian/MaterialEditText"
            )
        )

        licenseList.sortWith { o1: LibraryListe, o2: LibraryListe ->
            o1.name.compareTo(
                o2.name,
                ignoreCase = true
            )
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.recycleChildrenOnDetach = true
        recyclerView.layoutManager = linearLayoutManager
        val customDivider =
            ContextCompat.getDrawable(requireContext(), R.drawable.divider_library)
                ?.let { CustomDivider(it) }
        if (customDivider != null) {
            recyclerView.addItemDecoration(customDivider)
        }
        val librariesAdapter = LibrariesAdapter(licenseList, requireContext())
        recyclerView.adapter = librariesAdapter
    }
}