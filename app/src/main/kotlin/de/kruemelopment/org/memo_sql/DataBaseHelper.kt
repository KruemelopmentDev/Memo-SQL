package de.kruemelopment.org.memo_sql

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 2) {
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
        fav: String?,
        passwort: String?
    ): Boolean {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("Titel", titel)
        contentValues.put("Thema", thema)
        contentValues.put("Inhalt", inhalt)
        contentValues.put("Datum", datum)
        contentValues.put("Favorit", fav)
        contentValues.put("Passwort", passwort)
        val result = database.insert(TABLE_NAME, null, contentValues)
        database.close()
        return result != -1L
    }

    val allData: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery("Select * from $TABLE_NAME", null)
        }

    fun deleteData(id: String?) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "ID=?", arrayOf(id))
    }

    fun updateData(
        id: String?,
        thema: String?,
        titel: String?,
        inhalt: String?,
        datum: String?,
        fav: String?,
        passwort: String?
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("Titel", titel)
        contentValues.put("Thema", thema)
        contentValues.put("Inhalt", inhalt)
        contentValues.put("Datum", datum)
        contentValues.put("Favorit", fav)
        contentValues.put("Passwort", passwort)
        val result = db.update(TABLE_NAME, contentValues, "ID=?", arrayOf(id))
        return result > 0
    }

    val allDatesASC: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery(
                "Select * from $TABLE_NAME ORDER BY Datum ASC",
                null
            )
        }
    val allDatesDESC: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery(
                "Select * from $TABLE_NAME ORDER BY Datum DESC",
                null
            )
        }

    fun deleteAll() {
        val db = this.writableDatabase
        db.execSQL("delete from $TABLE_NAME")
    }

    fun getData(id: String?): Cursor {
        val sqLiteDatabase = this.writableDatabase
        val cursor = sqLiteDatabase.rawQuery("Select * from $TABLE_NAME Where ID=$id", null)
        cursor.moveToFirst()
        return cursor
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

    fun getlastid(): String {
        val sqLiteDatabase = this.writableDatabase
        val r = sqLiteDatabase.rawQuery("Select * from $TABLE_NAME", null)
        r.moveToLast()
        val re = r.getString(0)
        r.close()
        return re
    }

    companion object {
        private const val DATABASE_NAME = "Memo.db"
        private const val TABLE_NAME = "default_table"
    }
}
