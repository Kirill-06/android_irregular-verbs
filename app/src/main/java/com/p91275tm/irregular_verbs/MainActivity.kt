package com.p91275tm.irregular_verbs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.p91275tm.irregular_verbs.databinding.ActivityMainBinding
import com.yandex.metrica.impl.ob.db
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.InitializationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import com.yandex.mobile.ads.common.MobileAds as YandexMobileAds
import com.yandex.mobile.ads.common.AdRequest as YandexAdRequst

//import com.yandex.mobile.ads.common.MobileAds


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var adapter = WerbsAdapter()
    private val YANDEX_MOBILE_ADS_TAG = "YandexMobileAds"

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val sharedPref = getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)
        val language = sharedPref.getString("language", "en")
        val newLocale = Locale(language)
        Locale.setDefault(newLocale)
        val configuration = resources.configuration
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        var isFirstRun = sharedPrefs.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            CoroutineScope(Dispatchers.IO).launch {
                completion()
            }

            val editor = sharedPrefs.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(language == "en") {
            binding.translationInfo.visibility = View.GONE
        }
        else{
            binding.translationInfo.visibility = View.VISIBLE
        }
        var languge = Languge(language.toString())
        init(languge)
        initserch()
        initadMob()
        initadYandex()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.english -> {
                val newLocale = Locale("en")
                Locale.setDefault(newLocale)
                val configuration = resources.configuration
                configuration.setLocale(newLocale)
                resources.updateConfiguration(configuration, resources.displayMetrics)
                saveLanguage("en")
                finish()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.russian -> {
                val newLocale = Locale("ru")
                Locale.setDefault(newLocale)
                val configuration = resources.configuration
                configuration.setLocale(newLocale)
                resources.updateConfiguration(configuration, resources.displayMetrics)
                saveLanguage("ru")
                finish()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            /*R.id.chinese -> {
                val newLocale = Locale("zh")
                Locale.setDefault(newLocale)
                val configuration = resources.configuration
                configuration.setLocale(newLocale)
                resources.updateConfiguration(configuration, resources.displayMetrics)
                saveLanguage("zh")
                recreate()
                return true
            }*/
            R.id.card -> {
                val intent = Intent(this, CardActivity::class.java)
                intent.putExtra("language", Locale.getDefault().language)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun saveLanguage(language: String) {
        val sharedPref = getSharedPreferences("my_app_preferences", MODE_PRIVATE)
        sharedPref.edit().putString("language", language).apply()
    }
    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.adView.destroy()
        binding.adViewyan.destroy()
    }

    private fun initadYandex() {
        YandexMobileAds.initialize(this) { Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized") }
        binding.adViewyan.setAdUnitId("R-M-2262396-1")
        binding.adViewyan.setAdSize(AdSize.BANNER_320x50)
        val adRequest = YandexAdRequst.Builder().build()
        binding.adViewyan.loadAd(adRequest)
    }

    private fun initadMob()
    {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun init(language: Languge) = with(binding) {
        Rview.layoutManager = LinearLayoutManager(this@MainActivity)
        Rview.adapter = adapter
        adapter.languch(language)
        val db = AppDatabase.irregularVerbsDao(this@MainActivity)
        db.irregularVerbsDao().getAllWords().asLiveData().observe(this@MainActivity) { it ->
            it.forEach{
                val text = Word(it.base_form, it.past_simple, it.past_participle, it.translation)
                adapter.addword(text)
            }
        }
    }
    fun initserch()
    {
       binding.serch.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
           override fun onQueryTextSubmit(query: String?): Boolean {
               return true
           }

           override fun onQueryTextChange(newText: String?): Boolean {
               if (newText != null) {
                   adapter.clearList()
                   val dao = AppDatabase.getIrregularVerbsDao(this@MainActivity)
                   val searchQuery = "%${newText}%"
                   dao.searchWords(searchQuery).asLiveData().observe(this@MainActivity) { it ->
                       it.forEach {
                           val text = Word(
                               it.base_form,
                               it.past_simple,
                               it.past_participle,
                               it.translation
                           )
                           adapter.addword(text)
                       }
                   }
               }
               return true
           }
       })
    }

    private fun completion() {
        val db = AppDatabase.irregularVerbsDao(this)
        var word = Words(1, "arise", "arose", "arisen", "возникать, появляться")
        db.irregularVerbsDao().insert(word)
        word = Words(2, "awake", "awakened / awoke", "awakened / awoken", "будить, проснуться")
        db.irregularVerbsDao().insert(word)
        word = Words(3, "backslide", "backslid", "backslidden / backslid", "отказываться от прежних убеждений")
        db.irregularVerbsDao().insert(word)
        word = Words(4, "be", "was, were", "been", "быть")
        db.irregularVerbsDao().insert(word)
        word = Words(5, "bear", "bore", "born / borne", "родить")
        db.irregularVerbsDao().insert(word)
        word = Words(6, "beat", "beat", "beaten / beat", "бить")
        db.irregularVerbsDao().insert(word)
        word = Words(7, "become", "became", "become", "становиться, делаться")
        db.irregularVerbsDao().insert(word)
        word = Words(8, "begin", "began", "begun", "начинать")
        db.irregularVerbsDao().insert(word)
        word = Words(9, "bend", "bent", "bent", "сгибать, гнуть")
        db.irregularVerbsDao().insert(word)
        word = Words(10, "bet", "bet / betted ", "bet / betted", "держать пари")
        db.irregularVerbsDao().insert(word)
        word = Words(11, "bind", "bound", "bound", "связать")
        db.irregularVerbsDao().insert(word)
        word = Words(12, "bite", "bit", "bitten", "кусать")
        db.irregularVerbsDao().insert(word)
        word = Words(13, "bleed", "bled", "bled", "кровоточить")
        db.irregularVerbsDao().insert(word)
        word = Words(14, "blow", "blew", "blown", "дуть")
        db.irregularVerbsDao().insert(word)
        word = Words(15, "break", "broke", "broken", "ломать")
        db.irregularVerbsDao().insert(word)
        word = Words(16, "breed", "bred", "bred", "выращивать")
        db.irregularVerbsDao().insert(word)
        word = Words(17, "bring", "brought", "brought", "приносить")
        db.irregularVerbsDao().insert(word)
        word = Words(18, "broadcast", "broadcast / broadcasted", "broadcast / broadcasted", "распространять, разбрасывать")
        db.irregularVerbsDao().insert(word)
        word = Words(19, "browbeat", "browbeat", "browbeaten / browbeat", "запугивать")
        db.irregularVerbsDao().insert(word)
        word = Words(20, "build", "built", "built", "строить")
        db.irregularVerbsDao().insert(word)
        word = Words(21, "burn", "burned / burnt", "burned / burnt", "гореть, жечь")
        db.irregularVerbsDao().insert(word)
        word = Words(22, "burst", "burst", "burst", "взрываться, прорываться")
        db.irregularVerbsDao().insert(word)
        word = Words(23, "bust", "busted / bust", "busted / bust", "разжаловать")
        db.irregularVerbsDao().insert(word)
        word = Words(24, "buy", "bought", "bought", "покупать")
        db.irregularVerbsDao().insert(word)
        word = Words(25, "can", "could", "could", "мочь, уметь")
        db.irregularVerbsDao().insert(word)
        word = Words(26, "cast", "cast", "cast", "бросить, кинуть, вышвырнуть")
        db.irregularVerbsDao().insert(word)
        word = Words(27, "catch", "caught", "caught", "ловить, хватать, успеть")
        db.irregularVerbsDao().insert(word)
        word = Words(28, "choose", "chose", "chosen", "выбирать")
        db.irregularVerbsDao().insert(word)
        word = Words(29, "cling", "clung", "clung", "цепляться, льнуть")
        db.irregularVerbsDao().insert(word)
        word = Words(30, "clothe", "clothed / clad", "clothed / clad", "одевать (кого-либо)")
        db.irregularVerbsDao().insert(word)
        word = Words(31, "come", "came", "come", "приходить")
        db.irregularVerbsDao().insert(word)
        word = Words(32, "cost", "cost", "cost", "стоить, обходиться (в какую-либо сумму)")
        db.irregularVerbsDao().insert(word)
        word = Words(33, "creep", "crept", "crept", "ползать")
        db.irregularVerbsDao().insert(word)
        word = Words(34, "cut", "cut", "cut", "резать, разрезать")
        db.irregularVerbsDao().insert(word)
        word = Words(35, "deal", "dealt", "dealt", "иметь дело")
        db.irregularVerbsDao().insert(word)
        word = Words(36, "dig", "dug", "dug", "копать")
        db.irregularVerbsDao().insert(word)
        word = Words(37, "dive", "dove / dived", "dived", "нырять, погружаться")
        db.irregularVerbsDao().insert(word)
        word = Words(38, "do", "did", "done", "делать, выполнять")
        db.irregularVerbsDao().insert(word)
        word = Words(39, "draw", "drew", "drawn", "рисовать, чертить")
        db.irregularVerbsDao().insert(word)
        word = Words(40, "dream", "dreamed / dreamt", "dreamed / dreamt", "грезить, мечтать")
        db.irregularVerbsDao().insert(word)
        word = Words(41, "drink", "drank", "drunk", "пить")
        db.irregularVerbsDao().insert(word)
        word = Words(42, "drive", "drove", "driven", "управлять (авто)")
        db.irregularVerbsDao().insert(word)
        word = Words(43, "dwell", "dwelt / dwelled", "dwelt / dwelled", "обитать, находиться")
        db.irregularVerbsDao().insert(word)
        word = Words(44, "eat", "ate", "eaten", "есть, кушать")
        db.irregularVerbsDao().insert(word)
        word = Words(45, "fall", "fell", "fallen", "падать")
        db.irregularVerbsDao().insert(word)
        word = Words(46, "feed", "fed", "fed", "кормить")
        db.irregularVerbsDao().insert(word)
        word = Words(47, "feel", "felt", "felt", "чувствовать")
        db.irregularVerbsDao().insert(word)
        word = Words(48, "fight", "fought", "fought", "драться, сражаться, бороться")
        db.irregularVerbsDao().insert(word)
        word = Words(49, "find", "found", "found", "находить")
        db.irregularVerbsDao().insert(word)
        word = Words(50, "fit", "fit", "fit", "подходить по размеру")
        db.irregularVerbsDao().insert(word)
        word = Words(51, "flee", "fled", "fled", "убегать, спасаться")
        db.irregularVerbsDao().insert(word)
        word = Words(52, "fling", "flung", "flung", "бросаться, ринуться")
        db.irregularVerbsDao().insert(word)
        word = Words(53, "fly", "flew", "flown", "летать")
        db.irregularVerbsDao().insert(word)
        word = Words(54, "forbid", "forbade", "forbidden", "запрещать")
        db.irregularVerbsDao().insert(word)
        word = Words(55, "forecast", "forecast", "forecast", "предсказывать, предвосхищать")
        db.irregularVerbsDao().insert(word)
        word = Words(56, "foresee", "foresaw", "foreseen", "предвидеть")
        db.irregularVerbsDao().insert(word)
        word = Words(57, "foretell", "foretold", "foretold", "предсказывать, прогнозировать")
        db.irregularVerbsDao().insert(word)
        word = Words(58, "forget", "forgot", "forgotten", "забывать")
        db.irregularVerbsDao().insert(word)
        word = Words(59, "forgive", "forgave", "forgiven", "прощать")
        db.irregularVerbsDao().insert(word)
        word = Words(60, "forsake", "forsook", "forsaken", "покидать")
        db.irregularVerbsDao().insert(word)
        word = Words(61, "freeze", "froze", "frozen", "замерзать")
        db.irregularVerbsDao().insert(word)
        word = Words(62, "get", "got", "gotten / got", "получать, достигать")
        db.irregularVerbsDao().insert(word)
        word = Words(63, "give", "gave", "given", "давать")
        db.irregularVerbsDao().insert(word)
        word = Words(64, "go", "went", "gone", "идти, ехать")
        db.irregularVerbsDao().insert(word)
        word = Words(65, "grind", "ground", "ground", "молоть, толочь")
        db.irregularVerbsDao().insert(word)
        word = Words(66, "grow", "grew", "grown", "расти")
        db.irregularVerbsDao().insert(word)
        word = Words(67, "hang", "hung / hanged", "hung / hanged", "вешать, развешивать")
        db.irregularVerbsDao().insert(word)
        word = Words(68, "have, has", "had", "had", "иметь")
        db.irregularVerbsDao().insert(word)
        word = Words(69, "hear", "heard", "heard", "слышать")
        db.irregularVerbsDao().insert(word)
        word = Words(70, "hew", "hewed", "hewn / hewed", "рубить")
        db.irregularVerbsDao().insert(word)
        word = Words(71, "hide", "hid", "hidden", "прятаться, скрываться")
        db.irregularVerbsDao().insert(word)
        word = Words(72, "hit", "hit", "hit", "ударять, поражать")
        db.irregularVerbsDao().insert(word)
        word = Words(73, "hold", "held", "held", "держать, удерживать, фиксировать")
        db.irregularVerbsDao().insert(word)
        word = Words(74, "hurt", "hurt", "hurt", "ранить, причинить боль")
        db.irregularVerbsDao().insert(word)
        word = Words(75, "inlay", "inlaid", "inlaid", "вкладывать, вставлять, выстилать")
        db.irregularVerbsDao().insert(word)
        word = Words(76, "input", "input / inputted", "input / inputted", "входить")
        db.irregularVerbsDao().insert(word)
        word = Words(77, "interweave", "interwove", "interwoven", "воткать")
        db.irregularVerbsDao().insert(word)
        word = Words(78, "keep", "kept", "kept", "держать, хранить")
        db.irregularVerbsDao().insert(word)
        word = Words(79, "kneel", "knelt / kneeled", "knelt / kneeled", "становиться на колени")
        db.irregularVerbsDao().insert(word)
        word = Words(80, "knit", "knitted / knit", "knitted / knit", "вязать")
        db.irregularVerbsDao().insert(word)
        word = Words(81, "know", "knew", "known", "знать, иметь представление (о чем-либо)")
        db.irregularVerbsDao().insert(word)
        word = Words(82, "lay", "laid", "laid", "класть, положить")
        db.irregularVerbsDao().insert(word)
        word = Words(83, "lead", "led", "led", "вести, руководить, управлять")
        db.irregularVerbsDao().insert(word)
        word = Words(84, "lean", "leaned / leant", "leaned / leant", "опираться, прислоняться")
        db.irregularVerbsDao().insert(word)
        word = Words(85, "leap", "leaped / leapt ", "leaped / leapt", "прыгать, скакать")
        db.irregularVerbsDao().insert(word)
        word = Words(86, "learn", "learnt / learned", "learnt / learned", "учить")
        db.irregularVerbsDao().insert(word)
        word = Words(87, "leave", "left", "left", "покидать, оставлять")
        db.irregularVerbsDao().insert(word)
        word = Words(88, "lend", "lent", "lent", "одалживать, давать взаймы")
        db.irregularVerbsDao().insert(word)
        word = Words(89, "let", "let", "let", "позволять, предполагать")
        db.irregularVerbsDao().insert(word)
        word = Words(90, "lie", "lay", "lain", "лежать")
        db.irregularVerbsDao().insert(word)
        word = Words(91, "light", "lit / lighted", "lit / lighted", "освещать")
        db.irregularVerbsDao().insert(word)
        word = Words(92, "lose", "lost", "lost", "терять")
        db.irregularVerbsDao().insert(word)
        word = Words(93, "make", "made", "made", "делать, производить, создавать")
        db.irregularVerbsDao().insert(word)
        word = Words(94, "may", "might", "might", "мочь, иметь возможность")
        db.irregularVerbsDao().insert(word)
        word = Words(95, "mean", "meant", "meant", "значить, иметь ввиду")
        db.irregularVerbsDao().insert(word)
        word = Words(96, "meet", "met", "met", "встречать")
        db.irregularVerbsDao().insert(word)
        word = Words(97, "miscast", "miscast", "miscast", "неправильно распределять роли")
        db.irregularVerbsDao().insert(word)
        word = Words(98, "misdeal", "misdealt", "misdealt", "поступать неправильно")
        db.irregularVerbsDao().insert(word)
        word = Words(99, "misdo", "misdid", "misdone", "делать что-либо неправильно или небрежно")
        db.irregularVerbsDao().insert(word)
        word = Words(100, "misgive", "misgave", "misgiven", "внушать недоверия, опасения")
        db.irregularVerbsDao().insert(word)
        word = Words(101, "mishear", "misheard", "misheard", "ослышаться")
        db.irregularVerbsDao().insert(word)
        word = Words(102, "mishit", "mishit", "mishit", "промахнуться")
        db.irregularVerbsDao().insert(word)
        word = Words(103, "mislay", "mislaid", "mislaid", "класть не на место")
        db.irregularVerbsDao().insert(word)
        word = Words(104, "mislead", "misled", "misled", "ввести в заблуждение")
        db.irregularVerbsDao().insert(word)
        word = Words(105, "misread", "misread", "misread", "неправильно истолковывать")
        db.irregularVerbsDao().insert(word)
        word = Words(106,"misspell","misspelled / misspelt ","misspelled / misspelt","писать с ошибками")
        db.irregularVerbsDao().insert(word)
        word = Words(107, "misspend", "misspent", "misspent", "неразумно, зря тратить")
        db.irregularVerbsDao().insert(word)
        word = Words(108, "mistake", "mistook", "mistaken", "ошибаться")
        db.irregularVerbsDao().insert(word)
        word = Words(109, "misunderstand", "misunderstood", "misunderstood", "неправильно понимать")
        db.irregularVerbsDao().insert(word)
        word = Words(110, "mow", "mowed", "mowed / mown", "косить")
        db.irregularVerbsDao().insert(word)
        word = Words(111, "offset", "offset", "offset", "возмещать, вознаграждать, компенсировать")
        db.irregularVerbsDao().insert(word)
        word = Words(112, "outbid", "outbid", "outbid", "перебивать цену")
        db.irregularVerbsDao().insert(word)
        word = Words(113, "outdo", "outdid", "outdone", "превосходить")
        db.irregularVerbsDao().insert(word)
        word = Words(114, "outfight", "outfought", "outfought", "побеждать в бою")
        db.irregularVerbsDao().insert(word)
        word = Words(115, "outgrow", "outgrew", "outgrown", "вырастать из")
        db.irregularVerbsDao().insert(word)
        word = Words(116, "output", "output / outputted", "output / outputted", "выходить")
        db.irregularVerbsDao().insert(word)
        word = Words(117, "outrun", "outran", "outrun", "перегонять, опережать")
        db.irregularVerbsDao().insert(word)
        word = Words(118, "outsell", "outsold", "outsold", "продавать лучше или дороже")
        db.irregularVerbsDao().insert(word)
        word = Words(119, "outshine", "outshone", "outshone", "затмевать")
        db.irregularVerbsDao().insert(word)
        word = Words(120, "overbid", "overbid", "overbid", "повелевать")
        db.irregularVerbsDao().insert(word)
        word = Words(121, "overcome", "overcame", "overcome", "компенсировать")
        db.irregularVerbsDao().insert(word)
        word = Words(122, "overdo", "overdid", "overdone", "пережари(ва)ть")
        db.irregularVerbsDao().insert(word)
        word = Words(123, "overdraw", "overdrew", "overdrawn", "превышать")
        db.irregularVerbsDao().insert(word)
        word = Words(124, "overeat", "overate", "overeaten", "объедаться")
        db.irregularVerbsDao().insert(word)
        word = Words(125, "overfly", "overflew", "overflown", "перелетать")
        db.irregularVerbsDao().insert(word)
        word = Words(126, "overhang", "overhung", "overhung", "нависать")
        db.irregularVerbsDao().insert(word)
        word = Words(127, "overhear", "overheard", "overheard", "подслуш(ив)ать")
        db.irregularVerbsDao().insert(word)
        word = Words(128, "overlay", "overlaid", "overlaid", "покры(ва)ть")
        db.irregularVerbsDao().insert(word)
        word = Words(129, "overpay", "overpaid", "overpaid", "переплачивать")
        db.irregularVerbsDao().insert(word)
        word = Words(130, "override", "overrode", "overridden", "отменять, аннулировать ")
        db.irregularVerbsDao().insert(word)
        word = Words(131, "overrun", "overran", "overrun", "переливаться через край")
        db.irregularVerbsDao().insert(word)
        word = Words(132, "oversee", "oversaw", "overseen", "надзирать за")
        db.irregularVerbsDao().insert(word)
        word = Words(133, "overshoot", "overshot", "overshot", "расстрелять")
        db.irregularVerbsDao().insert(word)
        word = Words(134, "oversleep", "overslept", "overslept", "проспать, заспаться")
        db.irregularVerbsDao().insert(word)
        word = Words(135, "overtake", "overtook", "overtaken", "догонять")
        db.irregularVerbsDao().insert(word)
        word = Words(136, "overthrow", "overthrew", "overthrown", "свергать")
        db.irregularVerbsDao().insert(word)
        word = Words(137, "partake", "partook", "partaken", "принимать участие")
        db.irregularVerbsDao().insert(word)
        word = Words(138, "pay", "paid", "paid", "платить")
        db.irregularVerbsDao().insert(word)
        word = Words(139, "plead", "pleaded / pled", "pleaded / pled", "обращаться к суду")
        db.irregularVerbsDao().insert(word)
        word = Words(140, "prepay", "prepaid", "prepaid", "платить вперед")
        db.irregularVerbsDao().insert(word)
        word = Words(141, "prove", "proved", "proven / proved", "доказывать")
        db.irregularVerbsDao().insert(word)
        word = Words(142, "put", "put", "put", "класть, ставить, размещать")
        db.irregularVerbsDao().insert(word)
        word = Words(143, "quit", "quit / quitted", "quit / quitted", "выходить, покидать, оставлять")
        db.irregularVerbsDao().insert(word)
        word = Words(144, "read", "read", "read", "читать")
        db.irregularVerbsDao().insert(word)
        word = Words(145, "rebind", "rebound", "rebound", "перевязывать")
        db.irregularVerbsDao().insert(word)
        word = Words(146, "rebuild", "rebuilt", "rebuilt", "перестроить")
        db.irregularVerbsDao().insert(word)
        word = Words(147, "recast", "recast", "recast", "изменять, перестраивать")
        db.irregularVerbsDao().insert(word)
        word = Words(148, "redo", "redid", "redone", "делать вновь, переделывать")
        db.irregularVerbsDao().insert(word)
        word = Words(149, "rehear", "reheard", "reheard", "слушать вторично")
        db.irregularVerbsDao().insert(word)
        word = Words(150, "remake", "remade", "remade", "переделывать")
        db.irregularVerbsDao().insert(word)
        word = Words(151, "rend", "rent", "rent", "раздирать")
        db.irregularVerbsDao().insert(word)
        word = Words(152, "repay", "repaid", "repaid", "отдавать долг")
        db.irregularVerbsDao().insert(word)
        word = Words(153, "rerun", "reran", "rerun", "выполнять повторно")
        db.irregularVerbsDao().insert(word)
        word = Words(154, "resell", "resold", "resold", "перепродавать")
        db.irregularVerbsDao().insert(word)
        word = Words(155, "reset", "reset", "reset", "возвращать")
        db.irregularVerbsDao().insert(word)
        word = Words(156, "resit", "resat", "resat", "пересиживать")
        db.irregularVerbsDao().insert(word)
        word = Words(157, "retake", "retook", "retaken", "забирать")
        db.irregularVerbsDao().insert(word)
        word = Words(158, "retell", "retold", "retold", "пересказывать")
        db.irregularVerbsDao().insert(word)
        word = Words(159, "rewrite", "rewrote", "rewritten", "перезаписать")
        db.irregularVerbsDao().insert(word)
        word = Words(160, "rid", "rid", "rid", "избавлять")
        db.irregularVerbsDao().insert(word)
        word = Words(161, "ride", "rode", "ridden", "ездить верхом")
        db.irregularVerbsDao().insert(word)
        word = Words(162, "ring", "rang", "rung", "звонить")
        db.irregularVerbsDao().insert(word)
        word = Words(163, "rise", "rose", "risen", "подняться")
        db.irregularVerbsDao().insert(word)
        word = Words(164, "run", "ran", "run", "бегать")
        db.irregularVerbsDao().insert(word)
        word = Words(165, "saw", "sawed", "sawed / sawn", "пилить")
        db.irregularVerbsDao().insert(word)
        word = Words(166, "say", "said", "said", "сказать, заявить")
        db.irregularVerbsDao().insert(word)
        word = Words(167, "see", "saw", "seen", "видеть")
        db.irregularVerbsDao().insert(word)
        word = Words(168, "seek", "sought", "sought", "искать")
        db.irregularVerbsDao().insert(word)
        word = Words(169, "sell", "sold", "sold", "продавать")
        db.irregularVerbsDao().insert(word)
        word = Words(170, "send", "sent", "sent", "посылать")
        db.irregularVerbsDao().insert(word)
        word = Words(171, "set", "set", "set", "ставить, устанавливать")
        db.irregularVerbsDao().insert(word)
        word = Words(172, "sew", "sewed", "sewn / sewed", "шить")
        db.irregularVerbsDao().insert(word)
        word = Words(173, "shake", "shook", "shaken", "трясти")
        db.irregularVerbsDao().insert(word)
        word = Words(174, "shave", "shaved", "shaved / shaven", "бриться")
        db.irregularVerbsDao().insert(word)
        word = Words(175, "shear", "sheared", "sheared / shorn", "стричь")
        db.irregularVerbsDao().insert(word)
        word = Words(176, "shed", "shed", "shed", "проливать")
        db.irregularVerbsDao().insert(word)
        word = Words(177, "shine", "shined / shone", "shined / shone", "светить, сиять, озарять")
        db.irregularVerbsDao().insert(word)
        word = Words(178, "shoot", "shot", "shot", "стрелять, давать побеги")
        db.irregularVerbsDao().insert(word)
        word = Words(179, "show", "showed", "shown / showed", "показывать")
        db.irregularVerbsDao().insert(word)
        word = Words(180, "shrink", "shrank / shrunk", "shrunk", "сокращаться, сжиматься")
        db.irregularVerbsDao().insert(word)
        word = Words(181, "shut", "shut", "shut", "закрывать, запирать, затворять")
        db.irregularVerbsDao().insert(word)
        word = Words(182, "sing", "sang", "sung", "петь")
        db.irregularVerbsDao().insert(word)
        word = Words(183, "sink", "sank / sunk", "sunk", "тонуть, погружаться (под воду)")
        db.irregularVerbsDao().insert(word)
        word = Words(184, "sit", "sat", "sat", "сидеть")
        db.irregularVerbsDao().insert(word)
        word = Words(185, "slay", "slew / slayed", "slain / slayed", "убивать")
        db.irregularVerbsDao().insert(word)
        word = Words(186, "sleep", "slept", "slept", "спать")
        db.irregularVerbsDao().insert(word)
        word = Words(187, "slide", "slid", "slid", "скользить")
        db.irregularVerbsDao().insert(word)
        word = Words(188, "sling", "slung", "slung", "бросать, швырять")
        db.irregularVerbsDao().insert(word)
        word = Words(189, "slink", "slunk", "slunk", "красться, идти крадучись")
        db.irregularVerbsDao().insert(word)
        word = Words(190, "slit", "slit", "slit", "разрезать, рвать в длину")
        db.irregularVerbsDao().insert(word)
        word = Words(191, "smell", "smelled / smelt", "smelled / smelt", "пахнуть, нюхать")
        db.irregularVerbsDao().insert(word)
        word = Words(192, "sow", "sowed", "sown / sowed", "сеять")
        db.irregularVerbsDao().insert(word)
        word = Words(193, "speak", "spoke", "spoken", "говорить")
        db.irregularVerbsDao().insert(word)
        word = Words(194, "speed", "sped / speeded", "sped / speeded", "ускорять, спешить")
        db.irregularVerbsDao().insert(word)
        word = Words(195, "spell", "spelled / spelt", "spelled / spelt", "писать или читать по буквам")
        db.irregularVerbsDao().insert(word)
        word = Words(196, "spend", "spent", "spent", "тратить, расходовать")
        db.irregularVerbsDao().insert(word)
        word = Words(197, "spill", "spilled / spilt", "spilled / spilt", "проливать, разливать")
        db.irregularVerbsDao().insert(word)
        word = Words(198, "spin", "spun", "spun", "прясть")
        db.irregularVerbsDao().insert(word)
        word = Words(199, "spit", "spit / spat", "spit / spat", "плевать")
        db.irregularVerbsDao().insert(word)
        word = Words(200, "split", "split", "split", "расщеплять")
        db.irregularVerbsDao().insert(word)
        word = Words(201, "spoil", "spoiled / spoilt ", "spoiled / spoilt ", "портить")
        db.irregularVerbsDao().insert(word)
        word = Words(202, "spread", "spread", "spread", "распространиться")
        db.irregularVerbsDao().insert(word)
        word = Words(203, "spring", "sprang / sprung", "sprung", "вскочить, возникнуть")
        db.irregularVerbsDao().insert(word)
        word = Words(204, "stand", "stood", "stood", "стоять")
        db.irregularVerbsDao().insert(word)
        word = Words(205, "steal", "stole", "stolen", "воровать, красть")
        db.irregularVerbsDao().insert(word)
        word = Words(206, "stick", "stuck", "stuck", "уколоть, приклеить")
        db.irregularVerbsDao().insert(word)
        word = Words(207, "sting", "stung", "stung", "жалить")
        db.irregularVerbsDao().insert(word)
        word = Words(208, "stink", "stunk / stank", "stunk", "вонять")
        db.irregularVerbsDao().insert(word)
        word = Words(209, "strew", "strewed", "strewn / strewed", "усеять, устлать")
        db.irregularVerbsDao().insert(word)
        word = Words(210, "stride", "strode", "stridden", "шагать, наносить удар")
        db.irregularVerbsDao().insert(word)
        word = Words(211, "strike", "struck", "struck", "ударить, бить, бастовать")
        db.irregularVerbsDao().insert(word)
        word = Words(212, "string", "strung", "strung", "нанизать, натянуть")
        db.irregularVerbsDao().insert(word)
        word = Words(213, "strive", "strove / strived", "striven / strived", "стараться")
        db.irregularVerbsDao().insert(word)
        word = Words(214, "sublet", "sublet", "sublet", "передавать в субаренду")
        db.irregularVerbsDao().insert(word)
        word = Words(215, "swear", "swore", "sworn", "клясться, присягать")
        db.irregularVerbsDao().insert(word)
        word = Words(216, "sweep", "swept", "swept", "мести, подметать, сметать")
        db.irregularVerbsDao().insert(word)
        word = Words(217, "swell", "swelled", "swollen / swelled", "разбухать")
        db.irregularVerbsDao().insert(word)
        word = Words(218, "swim", "swam", "swum", "плавать, плыть")
        db.irregularVerbsDao().insert(word)
        word = Words(219, "swing", "swung", "swung", "качать, раскачивать, вертеть")
        db.irregularVerbsDao().insert(word)
        word = Words(220, "take", "took", "taken", "брать, взять")
        db.irregularVerbsDao().insert(word)
        word = Words(221, "teach", "taught", "taught", "учить, обучать")
        db.irregularVerbsDao().insert(word)
        word = Words(222, "tear", "tore", "torn", "рвать")
        db.irregularVerbsDao().insert(word)
        word = Words(223, "tell", "told", "told", "рассказать")
        db.irregularVerbsDao().insert(word)
        word = Words(224, "think", "thought", "thought", "думать")
        db.irregularVerbsDao().insert(word)
        word = Words(225, "throw", "threw", "thrown", "бросить")
        db.irregularVerbsDao().insert(word)
        word = Words(226, "thrust", "thrust", "thrust", "колоть, пронзать")
        db.irregularVerbsDao().insert(word)
        word = Words(227, "tread", "trod", "trodden / trod", "ступать")
        db.irregularVerbsDao().insert(word)
        word = Words(228, "unbend", "unbent", "unbent", "выпрямляться, разгибаться")
        db.irregularVerbsDao().insert(word)
        word = Words(229, "underbid", "underbid", "underbid", "снижать цену")
        db.irregularVerbsDao().insert(word)
        word = Words(230, "undercut", "undercut", "undercut", "сбивать цены")
        db.irregularVerbsDao().insert(word)
        word = Words(231, "undergo", "underwent", "undergone", "испытывать, переносить")
        db.irregularVerbsDao().insert(word)
        word = Words(232, "underlie", "underlay", "underlain", "лежать в основе")
        db.irregularVerbsDao().insert(word)
        word = Words(233, "underpay", "underpaid", "underpaid", "оплачивать слишком низко")
        db.irregularVerbsDao().insert(word)
        word = Words(234, "undersell", "undersold", "undersold", "продавать дешевле")
        db.irregularVerbsDao().insert(word)
        word = Words(235, "understand", "understood", "understood", "понимать, постигать")
        db.irregularVerbsDao().insert(word)
        word = Words(236, "undertake", "undertook", "undertaken", "предпринять")
        db.irregularVerbsDao().insert(word)
        word = Words(237, "underwrite", "underwrote", "underwritten", "подписываться")
        db.irregularVerbsDao().insert(word)
        word = Words(238, "undo", "undid", "undone", "уничтожать сделанное")
        db.irregularVerbsDao().insert(word)
        word = Words(239, "unfreeze", "unfroze", "unfrozen", "размораживать")
        db.irregularVerbsDao().insert(word)
        word = Words(240, "unsay", "unsaid", "unsaid", "брать назад свои слова")
        db.irregularVerbsDao().insert(word)
        word = Words(241, "unwind", "unwound", "unwound", "развертывать")
        db.irregularVerbsDao().insert(word)
        word = Words(242, "uphold", "upheld", "upheld", "поддерживать")
        db.irregularVerbsDao().insert(word)
        word = Words(243, "upset", "upset", "upset", "опрокинуться")
        db.irregularVerbsDao().insert(word)
        word = Words(244, "wake", "woke / waked", "woken / waked", "просыпаться")
        db.irregularVerbsDao().insert(word)
        word = Words(245, "waylay", "waylaid", "waylaid", "подстерегать")
        db.irregularVerbsDao().insert(word)
        word = Words(246, "wear", "wore", "worn", "носить (одежду)")
        db.irregularVerbsDao().insert(word)
        word = Words(247, "weave", "wove / weaved", "woven / weaved", "ткать")
        db.irregularVerbsDao().insert(word)
        word = Words(248, "wed", "wed / wedded", "wed / wedded", "жениться, выдавать замуж")
        db.irregularVerbsDao().insert(word)
        word = Words(249, "weep", "wept", "wept", "плакать, рыдать")
        db.irregularVerbsDao().insert(word)
        word = Words(250, "wet", "wet / wetted", "wet / wetted", "мочить, увлажнять")
        db.irregularVerbsDao().insert(word)
        word = Words(251, "win", "won", "won", "победить, выиграть")
        db.irregularVerbsDao().insert(word)
        word = Words(252, "wind", "wound", "wound", "заводить (механизм)")
        db.irregularVerbsDao().insert(word)
        word = Words(253, "withdraw", "withdrew", "withdrawn", "взять назад, отозвать")
        db.irregularVerbsDao().insert(word)
        word = Words(254, "withhold", "withheld", "withheld", "воздерживаться, отказывать")
        db.irregularVerbsDao().insert(word)
        word = Words(255, "withstand", "withstood", "withstood", "противостоять")
        db.irregularVerbsDao().insert(word)
        word = Words(256, "wring", "wrung", "wrung", "скрутить, сжимать")
        db.irregularVerbsDao().insert(word)
        word = Words(257, "write", "wrote", "written", "писать")
        db.irregularVerbsDao().insert(word)
    }
}