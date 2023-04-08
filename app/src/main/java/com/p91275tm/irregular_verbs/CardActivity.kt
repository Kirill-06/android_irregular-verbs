package com.p91275tm.irregular_verbs

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.p91275tm.irregular_verbs.databinding.ActivityCardBinding
import kotlinx.coroutines.launch
import java.util.*

class CardActivity : AppCompatActivity() {

    private lateinit var front_anim: AnimatorSet
    private lateinit var back_anim: AnimatorSet
    private lateinit var wordsArray: Array<Word>
    private var isFront = true
    private var numberCard = 0
    var pref: SharedPreferences? = null
    private lateinit var binding: ActivityCardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        super.onCreate(savedInstanceState)
        val newLocale = Locale(intent.getStringExtra("language"))
        Locale.setDefault(newLocale)
        val configuration = resources.configuration
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences("TABLE", Context.MODE_PRIVATE)
        numberCard = pref?.getInt("counter", 0)!!
        val db = AppDatabase.irregularVerbsDao(this@CardActivity)
        val wordsFlow = db.irregularVerbsDao().getAllWords()
        lifecycleScope.launch {
            wordsFlow.collect { wordsList ->
                wordsArray = Array(wordsList.size) { i -> Word("", "", "", "") }

                for (i in wordsList.indices) {
                    wordsArray[i] = Word(
                        wordsList[i].base_form,
                        wordsList[i].past_simple,
                        wordsList[i].past_participle,
                        wordsList[i].translation
                    )
                }
            }
        }
        if (intent.getStringExtra("language") == "ru") {
            if (numberCard == 0) {
                binding.buttonLeft.visibility = View.INVISIBLE
            } else {
                binding.buttonLeft.visibility = View.VISIBLE
            }
            binding.buttonRight.setOnClickListener {
                numberCard++
                binding.buttonLeft.visibility = View.VISIBLE
                if (!isFront) {
                    front_anim.setTarget(binding.CardBack)
                    back_anim.setTarget(binding.CardFront)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                binding.transitionCard!!.text = wordsArray[numberCard].translation
                binding.baseFormCard.text = wordsArray[numberCard].base_form
                binding.pastSimpleCard.text = wordsArray[numberCard].past_simple
                binding.pastParticipleCard.text = wordsArray[numberCard].past_participle
            }
            binding.buttonLeft.setOnClickListener {
                numberCard--
                if (numberCard == 0) {
                    binding.buttonLeft.visibility = View.INVISIBLE
                } else {
                    binding.buttonLeft.visibility = View.VISIBLE
                }
                if (!isFront) {
                    front_anim.setTarget(binding.CardBack)
                    back_anim.setTarget(binding.CardFront)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                binding.transitionCard!!.text = wordsArray[numberCard].translation
                binding.baseFormCard.text = wordsArray[numberCard].base_form
                binding.pastSimpleCard.text = wordsArray[numberCard].past_simple
                binding.pastParticipleCard.text = wordsArray[numberCard].past_participle
            }
        } else {
            if (numberCard == 0) {
                binding.buttonLeft.visibility = View.INVISIBLE
            } else {
                binding.buttonLeft.visibility = View.VISIBLE
            }
            binding.buttonRight.setOnClickListener {
                numberCard++
                binding.buttonLeft.visibility = View.VISIBLE
                if (!isFront) {
                    front_anim.setTarget(binding.CardBack)
                    back_anim.setTarget(binding.CardFront)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                binding.baseFormCard.text = wordsArray[numberCard].base_form
                binding.pastSimpleCard.text = wordsArray[numberCard].past_simple
                binding.pastParticipleCard.text = wordsArray[numberCard].past_participle
            }
            binding.buttonLeft.setOnClickListener {
                numberCard--
                if (numberCard == 0) {
                    binding.buttonLeft.visibility = View.INVISIBLE
                } else {
                    binding.buttonLeft.visibility = View.VISIBLE
                }
                if (!isFront) {
                    front_anim.setTarget(binding.CardBack)
                    back_anim.setTarget(binding.CardFront)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                binding.baseFormCard.text = wordsArray[numberCard].base_form
                binding.pastSimpleCard.text = wordsArray[numberCard].past_simple
                binding.pastParticipleCard.text = wordsArray[numberCard].past_participle
            }
        }
        var scale = applicationContext.resources.displayMetrics.density

        binding.CardFront.cameraDistance = 8000 * scale
        binding.CardBack.cameraDistance = 8000 * scale


        front_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.flip_card_first
        ) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.flip_card_second
        ) as AnimatorSet

        binding.CardFront.setOnClickListener {
            if (isFront) {
                front_anim.setTarget(binding.CardFront)
                back_anim.setTarget(binding.CardBack)
                front_anim.start()
                back_anim.start()
                isFront = false
            } else {
                front_anim.setTarget(binding.CardBack)
                back_anim.setTarget(binding.CardFront)
                back_anim.start()
                front_anim.start()
                isFront = true
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (intent.getStringExtra("language") == "ru") {
                binding.transitionCard!!.text = wordsArray[numberCard].translation
                binding.baseFormCard.text = wordsArray[numberCard].base_form
                binding.pastSimpleCard.text = wordsArray[numberCard].past_simple
                binding.pastParticipleCard.text = wordsArray[numberCard].past_participle
            } else {
                binding.baseFormCard.text = wordsArray[numberCard].base_form
                binding.pastSimpleCard.text = wordsArray[numberCard].past_simple
                binding.pastParticipleCard.text = wordsArray[numberCard].past_participle
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val myMenuItem = menu.findItem(R.id.card)
        val myMenuItem2 = menu.findItem(R.id.menu_item)
        myMenuItem.isVisible = false
        myMenuItem2.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
    private fun saveData(res: Int)
    {
        val editor = pref?.edit()
        editor?.putInt("counter", res)
        editor?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveData(numberCard)
    }
    override fun onResume() {
        super.onResume()
        saveData(numberCard)
    }

    override fun onPause() {
        super.onPause()
        saveData(numberCard)
    }
}