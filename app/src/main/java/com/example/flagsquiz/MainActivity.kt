package com.example.flagsquiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.flagsquiz.databinding.ActivityMainBinding
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Veritabanını kopyala
        try {
            Log.d("MainActivity", "Veritabanı kopyalama başlıyor...")
            val copyHelper = DatabaseCopyHelper(this)
            copyHelper.createDataBase()
            copyHelper.close()
            Log.d("MainActivity", "Veritabanı kopyalama tamamlandı")
        } catch (e: Exception) {
            Log.e("MainActivity", "Veritabanı kopyalama hatası: ${e.message}")
            e.printStackTrace()
        }
        
        // Manuel veri ekleme (eğer assets'ten kopyalama çalışmazsa)
        try {
            val dbHelper = DBHelper(this)
            dbHelper.veriEkle()
            Log.d("MainActivity", "Manuel veri ekleme tamamlandı")
        } catch (e: Exception) {
            Log.e("MainActivity", "Manuel veri ekleme hatası: ${e.message}")
            e.printStackTrace()
        }

        binding.btnStart.setOnClickListener {
            startActivity(Intent(this@MainActivity, QuizActivity::class.java))
        }
    }
}