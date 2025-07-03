package com.example.flagsquiz

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.flagsquiz.databinding.ActivityQuizBinding
import com.example.flagsquiz.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sonuçları hemen göster
        val dogruSayac = intent.getIntExtra("dogruSayac", 0)
        val saniye = intent.getIntExtra("sure", 0)

        val dakika = saniye / 60
        val kalanSaniye = saniye % 60

        binding.tvCorrectAnswers.text = "Doğru:$dogruSayac"
        binding.tvScoreValue.text = "Skor:$dogruSayac/10"
        binding.tvIncorrectAnswers.text = "Yanlış:${10-dogruSayac}"

        binding.tvTimeTaken.text = "Süre:$dakika:$kalanSaniye"

        binding.btnPlayAgain.setOnClickListener {
            startActivity(Intent(this@ResultActivity, QuizActivity::class.java))
            finish()
        }
    }
}