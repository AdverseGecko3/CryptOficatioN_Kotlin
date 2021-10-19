package com.kotlin.cryptofication.data

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.lang.Exception

class DatabaseClass(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    private val createSQLiteTableFavorites =
        "CREATE TABLE Favorites (SYMBOL TEXT PRIMARY KEY, DATE_ADDED TEXT)"
    private val dropSQLiteTableFavorites = "DROP TABLE IF EXISTS 'Favorites'"
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createSQLiteTableFavorites)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(dropSQLiteTableFavorites)
        onCreate(db)
    }

    fun insertToFavorites(symbol: String, date: String): Int {
        val writable = this.writableDatabase
        val readable = this.readableDatabase
        // Insert both entered fields into the database
        val query = "INSERT INTO Favorites VALUES ('$symbol' , '$date')"
        var unique = true
        try {
            writable.execSQL(query)
        } catch (e: Exception) {
            e.stackTrace
            unique = false
        }
        val fields = arrayOf("symbol")
        val args = arrayOf(symbol)
        val cursor = readable.query(
            "Favorites", fields,  //Search in database
            "symbol = ?", args, null, null, null
        )
        val i = cursor.count
        writable.close()
        readable.close()
        cursor.close()
        Log.d("insertFavorites", "Cryptos with that symbol in favorites: $i")
        return if (!unique) {
            -1
        } else if (i == 1) {
            1
        } else {
            0
        }
    }

    fun deleteFromFavorites(symbol: String): Int {
        val writable = this.writableDatabase
        val `val` = arrayOf(symbol)
        val sol = writable.delete("Favorites", "symbol = ?", `val`) //Delete from table
        writable.close()
        return sol
    }

    fun searchInFavorites(username: String): Float {
        val readable = this.readableDatabase
        val selectQuery = "SELECT * FROM Rates WHERE username = '$username'"
        return try {
            val cursor = readable.rawQuery(selectQuery, null)
            if (cursor.count >= 1) {
                Log.d("ratingQuery", "User already voted")
                cursor.moveToNext()
                val sol = cursor.getFloat(1)
                readable.close()
                cursor.close()
                sol
            } else {
                Log.d("ratingQuery", "User did not vote")
                readable.close()
                cursor.close()
                (-5).toFloat()
            }
        } catch (ex: SQLException) {
            Log.d("ratingQuery", "Exception")
            (-5).toFloat()
        }
    }
}