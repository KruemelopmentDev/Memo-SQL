package de.kruemelopment.org.memo_sql

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context?) : SQLiteOpenHelper(context, Database_Name, null, 2) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("Create Table $Table_Name (ID INTEGER PRIMARY KEY AUTOINCREMENT, Titel TEXT,Thema TEXT,Inhalt TEXT,Datum TEXT,Favorit TEXT,Passwort TEXT)")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        if (i1 > i) {
            sqLiteDatabase.execSQL("ALTER TABLE $Table_Name ADD COLUMN Passwort TEXT DEFAULT null")
        } else sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $Table_Name")
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
        val result = database.insert(Table_Name, null, contentValues)
        database.close()
        return result != -1L
    }

    val allData: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery("Select * from $Table_Name", null)
        }

    fun deleteData(id: String?) {
        val db = this.writableDatabase
        db.delete(Table_Name, "ID=?", arrayOf(id))
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
        val result = db.update(Table_Name, contentValues, "ID=?", arrayOf(id))
        return result > 0
    }

    val allDatesASC: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery(
                "Select * from $Table_Name ORDER BY Datum ASC",
                null
            )
        }
    val allDatesDESC: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery(
                "Select * from $Table_Name ORDER BY Datum DESC",
                null
            )
        }

    fun deleteAll() {
        val db = this.writableDatabase
        db.execSQL("delete from $Table_Name")
    }

    fun getData(id: String?): Cursor {
        val sqLiteDatabase = this.writableDatabase
        val cursor = sqLiteDatabase.rawQuery("Select * from $Table_Name Where ID=$id", null)
        cursor.moveToFirst()
        return cursor
    }

    fun empty(): Boolean {
        var empty = true
        val db = this.writableDatabase
        val cur = db.rawQuery("SELECT COUNT(*) FROM $Table_Name", null)
        if (cur != null && cur.moveToFirst()) {
            empty = cur.getInt(0) == 0
        }
        cur.close()
        return empty
    }

    fun getlastid(): String {
        val sqLiteDatabase = this.writableDatabase
        val r = sqLiteDatabase.rawQuery("Select * from $Table_Name", null)
        r.moveToLast()
        val re = r.getString(0)
        r.close()
        return re
    }

    companion object {
        private const val Database_Name = "Memo.db"
        private const val Table_Name = "default_table"
    }
}
