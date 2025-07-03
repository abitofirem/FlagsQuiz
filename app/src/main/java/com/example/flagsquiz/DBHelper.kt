package com.example.flagsquiz

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper (context: Context)
    : SQLiteOpenHelper(context,"bayrakquiz.sqlite",null,1) {
    
    override fun onCreate(db: SQLiteDatabase?) {
        // Veritabanı zaten assets'ten kopyalandığı için burada tablo oluşturmaya gerek yok
        Log.d("DBHelper", "onCreate çağrıldı")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS bayraklar")
        onCreate(db)
    }
    
    fun veriEkle() {
        val db = this.writableDatabase
        
        // Önce tabloyu oluştur
        db.execSQL("CREATE TABLE IF NOT EXISTS bayraklar (bayrak_id INTEGER PRIMARY KEY AUTOINCREMENT, bayrak_ad TEXT, bayrak_resim TEXT)")
        
        // Verileri ekle
        val bayraklar = arrayOf(
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Türkiye', 'turkiye')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Almanya', 'almanya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Fransa', 'fransa')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('İtalya', 'italya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('İspanya', 'ispanya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Hollanda', 'hollanda')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Norveç', 'norvec')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Rusya', 'rusya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Yunanistan', 'yunanistan')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Bulgaristan', 'bulgaristan')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Romanya', 'romanya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Slovakya', 'slovakya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Slovenya', 'slovenya')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Bosna Hersek', 'bosnahersek')",
            "INSERT INTO bayraklar (bayrak_ad, bayrak_resim) VALUES ('Estonya', 'estonya')"
        )
        
        for (sorgu in bayraklar) {
            db.execSQL(sorgu)
        }
        
        Log.d("DBHelper", "Veriler eklendi")
        db.close()
    }
}