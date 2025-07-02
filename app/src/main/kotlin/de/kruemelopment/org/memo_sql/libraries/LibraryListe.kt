package org.kruemelopment.de.bewecker.libraries

class LibraryListe(
    @JvmField var name: String,
    @JvmField var publisher: String,
    license: String,
    var version: String?,
    @JvmField var year: String,
    @JvmField var link: String
) {
    private var license: String? = license

    fun getLicense(): String {
        return if (license != null) license!! else ""
    }

    fun getVersionName(): String {
        return if (version != null) "Version: $version" else ""
    }
}

