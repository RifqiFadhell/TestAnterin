package id.fadhell.testanterin.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class AddressDbManager {

    private val dbName = "JSANotes"
    private val dbTable = "Notes"
    private val columnId = "Id"
    private val columnName = "Name"
    private val columnAddress = "Address"
    private val columnDescription = "Description"
    private val columnCoordinate = "Coordinate"
    private val dbVersion = 1

    private val CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS " + dbTable + " (" + columnId + " " + "INTEGER PRIMARY KEY," + columnName + " TEXT, " + columnAddress + " TEXT, " + columnDescription + " TEXT, " + columnCoordinate + "TEXT);"
    private var database: SQLiteDatabase? = null

    constructor(context: Context) {
        val dbHelper = DatabaseHelper(context)
        database = dbHelper.writableDatabase
    }

    fun insert(values: ContentValues): Long {

        val id = database!!.insert(dbTable, "", values)
        return id
    }

    fun queryAll(): Cursor {

        return database!!.rawQuery("select * from " + dbTable, null)
    }

    fun delete(selection: String, selectionArgs: Array<String>): Int {

        val count = database!!.delete(dbTable, selection, selectionArgs)
        return count
    }

    fun update(values: ContentValues, selection: String, selectionArgs: Array<String>): Int {

        val count = database!!.update(dbTable, values, selection, selectionArgs)
        return count
    }

    inner class DatabaseHelper : SQLiteOpenHelper {

        var context: Context? = null

        constructor(context: Context) : super(context, dbName, null, dbVersion) {
            this.context = context
        }

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(CREATE_TABLE_SQL)
            Toast.makeText(this.context, " database is created", Toast.LENGTH_LONG).show()
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("Drop table IF EXISTS " + dbTable)
        }
    }
}