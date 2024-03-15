package de.kruemelopment.org.memo_sql

class Liste(
    var id: String?,
    var title: String,
    var thema: String,
    var inhalt: String,
    var datum: String,
    var favo: String,
    var passwort: String?,
    var isLocked: Boolean
) {

    override fun toString(): String {
        return title
    }
}
