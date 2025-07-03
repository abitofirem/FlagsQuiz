package com.example.flagsquiz

import android.annotation.SuppressLint
import android.util.Log

class Flagsdao {

    //rastgele 10 bayrak getir

    @SuppressLint("Range")
    fun rasgele10BayrakGetir(vt:DBHelper) : ArrayList<Flags> {
        val bayraklarListe = ArrayList<Flags>()
        val db = vt.writableDatabase
        
        Log.d("Flagsdao", "Veritabanı açıldı: ${db.path}")
        
        // Önce tüm bayrakları al
        val tumBayraklar = ArrayList<Flags>()
        val c = db.rawQuery("SELECT * FROM bayraklar", null)
        
        while(c.moveToNext()){
            val bayrak = Flags(c.getInt(c.getColumnIndex("bayrak_id"))
                ,c.getString(c.getColumnIndex("bayrak_ad"))
                ,c.getString(c.getColumnIndex("bayrak_resim")))
            tumBayraklar.add(bayrak)
        }
        c.close()
        
        // Tüm bayrakları karıştır
        tumBayraklar.shuffle()
        
        // İlk 10 bayrağı al (veya tüm bayrakları eğer 10'dan azsa)
        val alinacakSayi = minOf(10, tumBayraklar.size)
        for (i in 0 until alinacakSayi) {
            bayraklarListe.add(tumBayraklar[i])
            Log.d("Flagsdao", "Bayrak eklendi: ${tumBayraklar[i].bayrak_ad}")
        }
        
        Log.d("Flagsdao", "Toplam bayrak sayısı: ${bayraklarListe.size}")

        return bayraklarListe
    }
    @SuppressLint("Range")
    fun rasgele3YanlisSecenekGetir(vt:DBHelper, bayrak_id:Int) : ArrayList<Flags> {
        val bayraklarListe = ArrayList<Flags>()
        val db = vt.writableDatabase
        
        // Önce doğru cevap hariç tüm bayrakları al
        val tumYanlisBayraklar = ArrayList<Flags>()
        val c = db.rawQuery("SELECT * FROM bayraklar WHERE bayrak_id != $bayrak_id", null)

        while(c.moveToNext()){
            val bayrak = Flags(c.getInt(c.getColumnIndex("bayrak_id"))
                ,c.getString(c.getColumnIndex("bayrak_ad"))
                ,c.getString(c.getColumnIndex("bayrak_resim")))
            tumYanlisBayraklar.add(bayrak)
        }
        c.close()
        
        // Yanlış bayrakları karıştır
        tumYanlisBayraklar.shuffle()
        
        // İlk 3 yanlış bayrağı al
        val alinacakSayi = minOf(3, tumYanlisBayraklar.size)
        for (i in 0 until alinacakSayi) {
            bayraklarListe.add(tumYanlisBayraklar[i])
        }

        return bayraklarListe
    }
}

