package cl.inacap.dispensador

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLException

data class Lectura(val id: Int, val temporizador: Int, val comentario: String, val fecha: Int)

class DBHelper(context: Context) : SQLiteOpenHelper(context, "dispensador.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT UNIQUE NOT NULL, password TEXT NOT NULL)")

        db.execSQL("CREATE TABLE lecturas (id INTEGER PRIMARY KEY AUTOINCREMENT, temporizador INTEGER, comentario TEXT, fecha INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        db?.execSQL("DROP TABLE IF EXISTS lecturas")
    }

    fun registrarUsuarios(usuario: String, password: String): Boolean {
        val db = this.writableDatabase

        return try {
            db.execSQL("INSERT INTO usuarios (usuario, password) VALUES ('$usuario', '$password')")
            true

        } catch (e: SQLException) {
            false
        }
    }

    fun validarLogin(usuario: String, password: String): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT id FROM usuarios WHERE usuario = '$usuario' AND password = '$password'", null)
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun insertarLectura(temporizador: Int, comentario: String) {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put("temporizador", temporizador)
            put("comentario", comentario)
            put("fecha", System.currentTimeMillis()) // Guarda la fecha/hora actual en milisegundos
        }
        db.insert("lecturas", null, values)
        db.close()
    }


    fun obtenerLecturas(): List<String> {
        val lista = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT temporizador, comentario FROM lecturas ORDER BY id DESC LIMIT 5", null)

        if (cursor.moveToFirst()) {
            do {
                val tiempo = cursor.getInt(0)
                val comentario = cursor.getString(1)
                lista.add("$comentario: ${tiempo}s")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


}

