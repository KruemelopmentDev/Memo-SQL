package de.kruemelopment.org.memo_sql

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PapierkorbHelper internal constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, 2) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("Create Table $TABLE_NAME (ID INTEGER PRIMARY KEY AUTOINCREMENT, Titel TEXT,Thema TEXT,Inhalt TEXT,Datum TEXT,Favorit TEXT,Passwort TEXT)")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        if (i1 > i) {
            sqLiteDatabase.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN Passwort TEXT DEFAULT null")
        } else sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun insertData(
        thema: String?,
        titel: String?,
        inhalt: String?,
        datum: String?,
        passwort: String?,
        favo:String?
    ) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("Titel", titel)
        contentValues.put("Thema", thema)
        contentValues.put("Inhalt", inhalt)
        contentValues.put("Datum", datum)
        contentValues.put("Passwort", passwort)
        contentValues.put("Favorit", favo)
        database.insert(TABLE_NAME, null, contentValues)
        database.close()
    }

    val allData: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery(
                "Select * from $TABLE_NAME ORDER BY Datum DESC",
                null
            )
        }

    fun deleteData(id: String?) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "ID=?", arrayOf(id))
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.execSQL("delete from $TABLE_NAME")
    }

    fun empty(): Boolean {
        var empty = true
        val db = this.writableDatabase
        val cur = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        if (cur.moveToFirst()) {
            empty = cur.getInt(0) == 0
        }
        cur.close()
        return empty
    }

    companion object {
        private const val DATABASE_NAME = "Papierkorb.db"
        private const val TABLE_NAME = "default_table"
    }
}
