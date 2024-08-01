package com.example.myapplicationwmsystem.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

data class Bin(
    val id: String,
    val name: String,
    val imageRes: Int,
    val latitude: Double,
    val longitude: Double,
    var garbageLevel: Int = 0
)

data class GarbageLevelEntry(
    val binId: String,
    val garbageLevel: Int,
    val timestamp: Long
)

object BinContract {
    object BinEntry : BaseColumns {
        const val TABLE_NAME = "bins"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_IMAGE_RES = "image_res"
        const val COLUMN_NAME_LATITUDE = "latitude"
        const val COLUMN_NAME_LONGITUDE = "longitude"
        const val COLUMN_NAME_GARBAGE_LEVEL = "garbage_level"
    }
}

object GarbageLevelContract {
    object GarbageLevelEntry : BaseColumns {
        const val TABLE_NAME = "garbage_levels"
        const val COLUMN_NAME_BIN_ID = "bin_id"
        const val COLUMN_NAME_GARBAGE_LEVEL = "garbage_level"
        const val COLUMN_NAME_TIMESTAMP = "timestamp"
    }
}

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "BinDatabase.db"
    }

    private val SQL_CREATE_ENTRIES = """
        CREATE TABLE ${BinContract.BinEntry.TABLE_NAME} (
            ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${BinContract.BinEntry.COLUMN_NAME_ID} TEXT UNIQUE,
            ${BinContract.BinEntry.COLUMN_NAME_NAME} TEXT,
            ${BinContract.BinEntry.COLUMN_NAME_IMAGE_RES} INTEGER,
            ${BinContract.BinEntry.COLUMN_NAME_LATITUDE} REAL,
            ${BinContract.BinEntry.COLUMN_NAME_LONGITUDE} REAL,
            ${BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL} INTEGER
        )
    """.trimIndent()

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${BinContract.BinEntry.TABLE_NAME}"

    private val SQL_CREATE_GARBAGE_LEVEL_ENTRIES = """
        CREATE TABLE ${GarbageLevelContract.GarbageLevelEntry.TABLE_NAME} (
            ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID} TEXT,
            ${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL} INTEGER,
            ${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP} INTEGER
        )
    """.trimIndent()

    private val SQL_DELETE_GARBAGE_LEVEL_ENTRIES =
        "DROP TABLE IF EXISTS ${GarbageLevelContract.GarbageLevelEntry.TABLE_NAME}"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_GARBAGE_LEVEL_ENTRIES)
        Log.d("DatabaseHelper", "Database tables created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        db.execSQL(SQL_DELETE_GARBAGE_LEVEL_ENTRIES)
        onCreate(db)
        Log.d("DatabaseHelper", "Database upgraded from version $oldVersion to $newVersion")
    }

    fun insertBin(bin: Bin): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(BinContract.BinEntry.COLUMN_NAME_ID, bin.id)
            put(BinContract.BinEntry.COLUMN_NAME_NAME, bin.name)
            put(BinContract.BinEntry.COLUMN_NAME_IMAGE_RES, bin.imageRes)
            put(BinContract.BinEntry.COLUMN_NAME_LATITUDE, bin.latitude)
            put(BinContract.BinEntry.COLUMN_NAME_LONGITUDE, bin.longitude)
            put(BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL, bin.garbageLevel)
        }

        val newRowId = db.insertWithOnConflict(BinContract.BinEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        Log.d("DatabaseHelper", "Inserted bin with id: $newRowId")
        return newRowId
    }

    fun getAllBins(): List<Bin> {
        val bins = mutableListOf<Bin>()
        val db = readableDatabase
        val projection = arrayOf(
            BinContract.BinEntry.COLUMN_NAME_ID,
            BinContract.BinEntry.COLUMN_NAME_NAME,
            BinContract.BinEntry.COLUMN_NAME_IMAGE_RES,
            BinContract.BinEntry.COLUMN_NAME_LATITUDE,
            BinContract.BinEntry.COLUMN_NAME_LONGITUDE,
            BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL
        )

        val cursor: Cursor = db.query(
            BinContract.BinEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_ID))
                val name = getString(getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_NAME))
                val imageRes = getInt(getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_IMAGE_RES))
                val latitude = getDouble(getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_LONGITUDE))
                val garbageLevel = getInt(getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL))

                bins.add(Bin(id, name, imageRes, latitude, longitude, garbageLevel))
            }
        }
        cursor.close()
        db.close()
        Log.d("DatabaseHelper", "Retrieved bins: ${bins.size}")
        return bins
    }

    fun getBinById(id: String): Bin? {
        val db = readableDatabase
        val projection = arrayOf(
            BinContract.BinEntry.COLUMN_NAME_ID,
            BinContract.BinEntry.COLUMN_NAME_NAME,
            BinContract.BinEntry.COLUMN_NAME_IMAGE_RES,
            BinContract.BinEntry.COLUMN_NAME_LATITUDE,
            BinContract.BinEntry.COLUMN_NAME_LONGITUDE,
            BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL
        )
        val selection = "${BinContract.BinEntry.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(id)

        val cursor: Cursor = db.query(
            BinContract.BinEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            Bin(
                id = cursor.getString(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_NAME)),
                imageRes = cursor.getInt(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_IMAGE_RES)),
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_LATITUDE)),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_LONGITUDE)),
                garbageLevel = cursor.getInt(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL))
            )
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun getBinByName(name: String): Bin? {
        val db = readableDatabase
        val projection = arrayOf(
            BinContract.BinEntry.COLUMN_NAME_ID,
            BinContract.BinEntry.COLUMN_NAME_NAME,
            BinContract.BinEntry.COLUMN_NAME_IMAGE_RES,
            BinContract.BinEntry.COLUMN_NAME_LATITUDE,
            BinContract.BinEntry.COLUMN_NAME_LONGITUDE,
            BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL
        )
        val selection = "${BinContract.BinEntry.COLUMN_NAME_NAME} = ?"
        val selectionArgs = arrayOf(name)

        val cursor: Cursor = db.query(
            BinContract.BinEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            Bin(
                id = cursor.getString(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_NAME)),
                imageRes = cursor.getInt(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_IMAGE_RES)),
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_LATITUDE)),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_LONGITUDE)),
                garbageLevel = cursor.getInt(cursor.getColumnIndexOrThrow(BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL))
            )
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun insertGarbageLevel(entry: GarbageLevelEntry): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID, entry.binId)
            put(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL, entry.garbageLevel)
            put(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP, entry.timestamp)
        }
        val newRowId = db.insert(GarbageLevelContract.GarbageLevelEntry.TABLE_NAME, null, values)
        db.close()
        Log.d("DatabaseHelper", "Inserted garbage level with id: $newRowId")
        return newRowId
    }

    fun getGarbageLevelByBinId(binId: String): GarbageLevelEntry? {
        val db = readableDatabase
        val projection = arrayOf(
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID,
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL,
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP
        )
        val selection = "${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID} = ?"
        val selectionArgs = arrayOf(binId)
        val cursor: Cursor = db.query(
            GarbageLevelContract.GarbageLevelEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            "${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP} DESC",
            "1"
        )

        var entry: GarbageLevelEntry? = null
        with(cursor) {
            if (moveToFirst()) {
                val garbageLevel = getInt(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL))
                val timestamp = getLong(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP))
                entry = GarbageLevelEntry(binId, garbageLevel, timestamp)
            }
        }
        cursor.close()
        db.close()
        Log.d("DatabaseHelper", "Retrieved garbage level for binId $binId: $entry")
        return entry
    }

    fun getGarbageLevelsByBinId(binId: String): List<GarbageLevelEntry> {
        val db = readableDatabase

        val projection = arrayOf(
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID,
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL,
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP
        )

        val selection = "${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID} = ?"
        val selectionArgs = arrayOf(binId)

        val cursor = db.query(
            GarbageLevelContract.GarbageLevelEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            "${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP} ASC"
        )

        val entries = mutableListOf<GarbageLevelEntry>()
        with(cursor) {
            while (moveToNext()) {
                val entryBinId = getString(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID))
                val garbageLevel = getInt(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL))
                val timestamp = getLong(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP))
                entries.add(GarbageLevelEntry(entryBinId, garbageLevel, timestamp))
            }
        }
        cursor.close()

        return entries
    }

    fun getGarbageLevelsByBinIdAndTimeRange(binId: String, startTime: Long, endTime: Long): List<GarbageLevelEntry> {
        val garbageLevels = mutableListOf<GarbageLevelEntry>()
        val db = readableDatabase
        val projection = arrayOf(
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID,
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL,
            GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP
        )
        val selection = "${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID} = ? AND ${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(binId, startTime.toString(), endTime.toString())
        val cursor: Cursor = db.query(
            GarbageLevelContract.GarbageLevelEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val binId = getString(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID))
                val garbageLevel = getInt(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_GARBAGE_LEVEL))
                val timestamp = getLong(getColumnIndexOrThrow(GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_TIMESTAMP))

                garbageLevels.add(GarbageLevelEntry(binId, garbageLevel, timestamp))
            }
        }
        cursor.close()
        db.close()
        Log.d("DatabaseHelper", "Retrieved garbage levels: ${garbageLevels.size}")
        return garbageLevels
    }

    fun deleteBin(id: String): Int {
        val db = writableDatabase
        val selection = "${BinContract.BinEntry.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(id)
        val deletedRows = db.delete(BinContract.BinEntry.TABLE_NAME, selection, selectionArgs)

        db.delete(GarbageLevelContract.GarbageLevelEntry.TABLE_NAME, "${GarbageLevelContract.GarbageLevelEntry.COLUMN_NAME_BIN_ID} = ?", arrayOf(id))

        db.close()
        Log.d("DatabaseHelper", "Deleted bin with id: $id, rows affected: $deletedRows")
        return deletedRows
    }

    fun updateBin(bin: Bin): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(BinContract.BinEntry.COLUMN_NAME_NAME, bin.name)
            put(BinContract.BinEntry.COLUMN_NAME_IMAGE_RES, bin.imageRes)
            put(BinContract.BinEntry.COLUMN_NAME_LATITUDE, bin.latitude)
            put(BinContract.BinEntry.COLUMN_NAME_LONGITUDE, bin.longitude)
            put(BinContract.BinEntry.COLUMN_NAME_GARBAGE_LEVEL, bin.garbageLevel)
        }
        val selection = "${BinContract.BinEntry.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(bin.id)
        val updatedRows = db.update(BinContract.BinEntry.TABLE_NAME, values, selection, selectionArgs)

        db.close()
        Log.d("DatabaseHelper", "Updated bin with id: ${bin.id}, rows affected: $updatedRows")
        return updatedRows
    }

}
