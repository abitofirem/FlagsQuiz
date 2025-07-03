package com.example.flagsquiz

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.flagsquiz.databinding.ActivityQuizBinding
import java.util.Timer
import java.util.TimerTask

class QuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizBinding

    private lateinit var sorular:ArrayList<Flags>
    private lateinit var yanlisSecenekler:ArrayList<Flags>
    private lateinit var dogruSoru:Flags
    private lateinit var tumSecenekler:HashSet<Flags>
    private lateinit var vt:DBHelper

    private var soruSayac = 0
    private var dogruSayac = 0
    private var yanlisSayac = 0
    private var saniye = 0
    private var timer: Timer? = null
    private var zamanlayiciBasladiMi = false

    // Kullanılan bayrakları takip etmek için
    private var kullanilanBayraklar = HashSet<Int>() // bayrak_id'leri takip eder
    private var kullanilanSecenekler = HashSet<String>() // bayrak_ad'larını takip eder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vt = DBHelper(this)

        // Kullanılan bayrakları temizle (yeni quiz başlangıcı)
        kullanilanBayraklar.clear()
        kullanilanSecenekler.clear()

        sorular = Flagsdao().rasgele10BayrakGetir(vt)

        // Sorular listesi boş mu kontrol et
        if (sorular.isEmpty()) {
            // Eğer soru yoksa kullanıcıya mesaj göster ve geri dön
            android.widget.Toast.makeText(this, "Veritabanında soru bulunamadı!", android.widget.Toast.LENGTH_LONG).show()
            finish()
            return
        }

        soruYukle()

        if (!zamanlayiciBasladiMi) {
            zamanlayiciBasladiMi = true
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    saniye++
                    val dakika = saniye / 60
                    val kalanSaniye = saniye % 60

                    // UI güncellemesi yapmak için main thread'e geç
                    runOnUiThread {
                        binding.tvTimer.text = String.format("%02d:%02d", dakika, kalanSaniye)
                    }
                }
            }, 0, 1000)
        }




        // Tüm butonlar için click listener'ları ekle
        binding.btnOption1.setOnClickListener {
            dogruKontrol(binding.btnOption1)
        }
        
        binding.btnOption2.setOnClickListener {
            dogruKontrol(binding.btnOption2)
        }
        
        binding.btnOption3.setOnClickListener {
            dogruKontrol(binding.btnOption3)
        }
        
        binding.btnOption4.setOnClickListener {
            dogruKontrol(binding.btnOption4)
        }
    }


    fun soruYukle(){
        binding.star.setImageResource(R.drawable.ic_star) //

        // Butonların renklerini varsayılan (beyaz) hale getir
        resetButtonColors() //

        binding.tvScore.text = "${soruSayac+1}. Soru"

        dogruSoru = sorular.get(soruSayac)
        
        // Kullanılan bayrakları takip et
        kullanilanBayraklar.add(dogruSoru.bayrak_id)

        // Debug: Bayrak resim adını logla
        val resimAdi = dogruSoru.bayrak_resim
        val resimId = resources.getIdentifier(resimAdi, "drawable", packageName)
        android.util.Log.d("QuizActivity", "Bayrak resim adı: $resimAdi, Resim ID: $resimId")
        
        if (resimId != 0) {
            binding.ivFlag.setImageResource(resimId)
            android.util.Log.d("QuizActivity", "Bayrak resmi yüklendi: $resimAdi")
        } else {
            android.util.Log.e("QuizActivity", "Bayrak resmi bulunamadı: $resimAdi")
            // Varsayılan bir resim göster
            binding.ivFlag.setImageResource(R.drawable.flags)
        }

        // Yanlış seçenekleri al ve tekrar kontrolü yap
        yanlisSecenekler = getUniqueYanlisSecenekler(dogruSoru.bayrak_id)

        // Tüm seçenekleri birleştir ve benzersizlik kontrolü yap
        val tumSeceneklerList = ArrayList<Flags>()
        tumSeceneklerList.add(dogruSoru)
        tumSeceneklerList.addAll(yanlisSecenekler)
        
        // Benzersizlik kontrolü
        val benzersizSecenekler = tumSeceneklerList.distinctBy { it.bayrak_ad }
        if (benzersizSecenekler.size != 4) {
            android.util.Log.e("QuizActivity", "TEKRAR EDEN SEÇENEK BULUNDU! Toplam: ${tumSeceneklerList.size}, Benzersiz: ${benzersizSecenekler.size}")
            android.util.Log.e("QuizActivity", "Tüm seçenekler: ${tumSeceneklerList.map { it.bayrak_ad }}")
            android.util.Log.e("QuizActivity", "Benzersiz seçenekler: ${benzersizSecenekler.map { it.bayrak_ad }}")
            
            // Eğer yeterli benzersiz seçenek yoksa, eksik olanları tamamla
            if (benzersizSecenekler.size < 4) {
                val eksikSayi = 4 - benzersizSecenekler.size
                android.util.Log.w("QuizActivity", "Eksik seçenek sayısı: $eksikSayi")
                
                // Eksik seçenekleri tamamla
                val tamamlanmisSecenekler = ArrayList(benzersizSecenekler)
                val tumBayraklar = getAllBayraklar()
                
                for (bayrak in tumBayraklar) {
                    if (tamamlanmisSecenekler.size >= 4) break
                    if (!tamamlanmisSecenekler.any { it.bayrak_ad == bayrak.bayrak_ad }) {
                        tamamlanmisSecenekler.add(bayrak)
                        android.util.Log.d("QuizActivity", "Eksik seçenek tamamlandı: ${bayrak.bayrak_ad}")
                    }
                }
                
                // Seçenekleri karıştır
                val karisikSecenekler = tamamlanmisSecenekler.shuffled()
                
                binding.btnOption1.text = karisikSecenekler[0].bayrak_ad
                binding.btnOption2.text = karisikSecenekler[1].bayrak_ad
                binding.btnOption3.text = karisikSecenekler[2].bayrak_ad
                binding.btnOption4.text = karisikSecenekler[3].bayrak_ad
                
                // Kullanılan seçenekleri takip et
                kullanilanSecenekler.clear()
                kullanilanSecenekler.addAll(karisikSecenekler.map { it.bayrak_ad })
                
                android.util.Log.d("QuizActivity", "Tamamlanmış seçenekler: ${karisikSecenekler.map { it.bayrak_ad }}")
                return
            }
        }

        // Normal durum - seçenekleri karıştır
        val karisikSecenekler = benzersizSecenekler.shuffled()

        binding.btnOption1.text = karisikSecenekler[0].bayrak_ad
        binding.btnOption2.text = karisikSecenekler[1].bayrak_ad
        binding.btnOption3.text = karisikSecenekler[2].bayrak_ad
        binding.btnOption4.text = karisikSecenekler[3].bayrak_ad
        
        // Kullanılan seçenekleri takip et
        kullanilanSecenekler.clear() // Her soru için temizle
        kullanilanSecenekler.addAll(karisikSecenekler.map { it.bayrak_ad })
        
        android.util.Log.d("QuizActivity", "Final seçenekler: ${karisikSecenekler.map { it.bayrak_ad }}")
    }

    private fun resetButtonColors() {

        val defaultColor = resources.getColor(R.color.white, theme) //Varsayılan rengi al
        //Her bir butonun rengini ayrı ayrı varsayılan renge ayarla
        binding.btnOption1.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btnOption2.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btnOption3.backgroundTintList = ColorStateList.valueOf(defaultColor)
        binding.btnOption4.backgroundTintList = ColorStateList.valueOf(defaultColor)

    }

    fun soruSayacKontrol(){
        soruSayac++

        if(soruSayac != 10){
            soruYukle()
        }else{
            timer?.cancel() // zamanlayıcıyı durdur
            val intent = Intent(this@QuizActivity,ResultActivity::class.java)
            intent.putExtra("dogruSayac",dogruSayac)
            intent.putExtra("sure", saniye) // saniyeyi gönderiyoruz
            startActivity(intent)
            finish()
        }
    }

    fun dogruKontrol(button: Button){
        val buttonYazi = button.text.toString()
        val dogruCevap = dogruSoru.bayrak_ad

        // Butonları devre dışı bırak ki kullanıcı tekrar tıklamasın
        setButtonsEnabled(false)

        if(buttonYazi == dogruCevap){
            //⭐ Yıldızı değiştir
            binding.star.setImageResource(R.drawable.ic_y_star)
            // Doğru butonu yeşil yap
            button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.correct_answer_green, theme))
            dogruSayac++

        }else{
            yanlisSayac++
            button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.wrong_answer_red, theme))
            //Doğru cevabı bul ve yeşil yap
            if (binding.btnOption1.text.toString() == dogruCevap) {
                binding.btnOption1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.correct_answer_green, theme))
            } else if (binding.btnOption2.text.toString() == dogruCevap) {
                binding.btnOption2.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.correct_answer_green, theme))
            } else if (binding.btnOption3.text.toString() == dogruCevap) {
                binding.btnOption3.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.correct_answer_green, theme))
            } else if (binding.btnOption4.text.toString() == dogruCevap) {
                binding.btnOption4.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.correct_answer_green, theme))
            }
        }

        // Kısa bir gecikme sonrası sonraki soruya geç
        Handler(Looper.getMainLooper()).postDelayed({
            soruSayacKontrol()
            setButtonsEnabled(true) // Butonları tekrar etkinleştir
        }, 800) // 800 milisaniye (0.8 saniye) beklesin. Bu süreyi ayarlayabilirsin.
    }

    // Butonları etkinleştirmek/devre dışı bırakmak için yardımcı fonksiyon
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnOption1.isEnabled = enabled
        binding.btnOption2.isEnabled = enabled
        binding.btnOption3.isEnabled = enabled
        binding.btnOption4.isEnabled = enabled
    }

    // Benzersiz yanlış seçenekler almak için fonksiyon
    private fun getUniqueYanlisSecenekler(dogruBayrakId: Int): ArrayList<Flags> {
        val yanlisSecenekler = ArrayList<Flags>()
        val db = vt.writableDatabase
        
        // Önce doğru cevap hariç tüm bayrakları al
        val tumYanlisBayraklar = ArrayList<Flags>()
        val c = db.rawQuery("SELECT * FROM bayraklar WHERE bayrak_id != $dogruBayrakId", null)

        while(c.moveToNext()){
            val bayrak = Flags(c.getInt(c.getColumnIndex("bayrak_id"))
                ,c.getString(c.getColumnIndex("bayrak_ad"))
                ,c.getString(c.getColumnIndex("bayrak_resim")))
            tumYanlisBayraklar.add(bayrak)
        }
        c.close()
        
        // Yanlış bayrakları karıştır
        tumYanlisBayraklar.shuffle()
        
        // Kullanılmamış bayrakları seç
        var secilenSayi = 0
        var index = 0
        val secilenBayrakAdlari = HashSet<String>() // Seçilen bayrak adlarını takip et
        
        while (secilenSayi < 3 && index < tumYanlisBayraklar.size) {
            val bayrak = tumYanlisBayraklar[index]
            
            // Bu bayrak daha önce kullanılmamışsa ve adı tekrar etmiyorsa ekle
            if (!kullanilanBayraklar.contains(bayrak.bayrak_id) && 
                !secilenBayrakAdlari.contains(bayrak.bayrak_ad)) {
                yanlisSecenekler.add(bayrak)
                kullanilanBayraklar.add(bayrak.bayrak_id)
                secilenBayrakAdlari.add(bayrak.bayrak_ad)
                secilenSayi++
                android.util.Log.d("QuizActivity", "Benzersiz yanlış seçenek eklendi: ${bayrak.bayrak_ad}")
            }
            index++
        }
        
        // Eğer yeterli benzersiz bayrak bulunamazsa, kullanılmış olanlardan da seç
        if (yanlisSecenekler.size < 3) {
            android.util.Log.w("QuizActivity", "Yeterli benzersiz bayrak bulunamadı, kullanılmış olanlardan seçiliyor")
            for (bayrak in tumYanlisBayraklar) {
                if (yanlisSecenekler.size >= 3) break
                // Hem ID hem de ad kontrolü yap
                if (!yanlisSecenekler.any { it.bayrak_id == bayrak.bayrak_id } && 
                    !secilenBayrakAdlari.contains(bayrak.bayrak_ad)) {
                    yanlisSecenekler.add(bayrak)
                    secilenBayrakAdlari.add(bayrak.bayrak_ad)
                    android.util.Log.d("QuizActivity", "Kullanılmış bayraktan seçenek eklendi: ${bayrak.bayrak_ad}")
                }
            }
        }
        
        // Son kontrol: Eğer hala yeterli seçenek yoksa, tekrar eden olmayacak şekilde ekle
        if (yanlisSecenekler.size < 3) {
            android.util.Log.e("QuizActivity", "Kritik durum: Yeterli seçenek bulunamadı!")
            for (bayrak in tumYanlisBayraklar) {
                if (yanlisSecenekler.size >= 3) break
                if (!secilenBayrakAdlari.contains(bayrak.bayrak_ad)) {
                    yanlisSecenekler.add(bayrak)
                    secilenBayrakAdlari.add(bayrak.bayrak_ad)
                    android.util.Log.d("QuizActivity", "Son çare seçenek eklendi: ${bayrak.bayrak_ad}")
                }
            }
        }
        
        // Final kontrol
        val benzersizYanlisSecenekler = yanlisSecenekler.distinctBy { it.bayrak_ad }
        if (benzersizYanlisSecenekler.size != yanlisSecenekler.size) {
            android.util.Log.e("QuizActivity", "YANLIŞ SEÇENEKLERDE TEKRAR VAR!")
            android.util.Log.e("QuizActivity", "Orijinal: ${yanlisSecenekler.map { it.bayrak_ad }}")
            android.util.Log.e("QuizActivity", "Benzersiz: ${benzersizYanlisSecenekler.map { it.bayrak_ad }}")
            return ArrayList(benzersizYanlisSecenekler)
        }
        
        return yanlisSecenekler
    }

    // Tüm bayrakları getirmek için fonksiyon
    private fun getAllBayraklar(): ArrayList<Flags> {
        val tumBayraklar = ArrayList<Flags>()
        val db = vt.writableDatabase
        val c = db.rawQuery("SELECT * FROM bayraklar", null)

        while(c.moveToNext()){
            val bayrak = Flags(c.getInt(c.getColumnIndex("bayrak_id"))
                ,c.getString(c.getColumnIndex("bayrak_ad"))
                ,c.getString(c.getColumnIndex("bayrak_resim")))
            tumBayraklar.add(bayrak)
        }
        c.close()
        
        return tumBayraklar
    }

}
