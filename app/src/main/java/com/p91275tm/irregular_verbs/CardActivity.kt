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
import kotlinx.coroutines.launch
import java.util.*

class CardActivity : AppCompatActivity() {

    private lateinit var front_anim: AnimatorSet
    private lateinit var back_anim: AnimatorSet
    private lateinit var wordsArray: Array<Word>
    private var isFront = true
    private var numberCard = 0
    var pref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
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
            val front = findViewById<TextView>(R.id.Card_Front) as MaterialCardView
            val back = findViewById<TextView>(R.id.Card_Back) as MaterialCardView
            val buttonLeft = findViewById<View>(R.id.buttonLeft)
            val buttonRight = findViewById<View>(R.id.buttonRight)
            val translation = findViewById<TextView>(R.id.transitionCard)
            val baseForm = findViewById<TextView>(R.id.base_formCard)
            val pastSimple = findViewById<TextView>(R.id.past_simpleCard)
            val pastParticiple = findViewById<TextView>(R.id.past_participleCard)
            if (numberCard == 0) {
                buttonLeft.visibility = View.INVISIBLE
            } else {
                buttonLeft.visibility = View.VISIBLE
            }
            buttonRight.setOnClickListener {
                numberCard++
                buttonLeft.visibility = View.VISIBLE
                if (!isFront) {
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                translation.text = wordsArray[numberCard].translation
                baseForm.text = wordsArray[numberCard].base_form
                pastSimple.text = wordsArray[numberCard].past_simple
                pastParticiple.text = wordsArray[numberCard].past_participle
            }
            buttonLeft.setOnClickListener {
                numberCard--
                if (numberCard == 0) {
                    buttonLeft.visibility = View.INVISIBLE
                } else {
                    buttonLeft.visibility = View.VISIBLE
                }
                if (!isFront) {
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                translation.text = wordsArray[numberCard].translation
                baseForm.text = wordsArray[numberCard].base_form
                pastSimple.text = wordsArray[numberCard].past_simple
                pastParticiple.text = wordsArray[numberCard].past_participle
            }
        } else {
            val front = findViewById<TextView>(R.id.Card_Front) as MaterialCardView
            val back = findViewById<TextView>(R.id.Card_Back) as MaterialCardView
            val buttonLeft = findViewById<View>(R.id.buttonLeft)
            val buttonRight = findViewById<View>(R.id.buttonRight)
            val baseForm = findViewById<TextView>(R.id.base_formCard)
            val pastSimple = findViewById<TextView>(R.id.past_simpleCard)
            val pastParticiple = findViewById<TextView>(R.id.past_participleCard)
            if (numberCard == 0) {
                buttonLeft.visibility = View.INVISIBLE
            } else {
                buttonLeft.visibility = View.VISIBLE
            }
            buttonRight.setOnClickListener {
                numberCard++
                buttonLeft.visibility = View.VISIBLE
                if (!isFront) {
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                baseForm.text = wordsArray[numberCard].base_form
                pastSimple.text = wordsArray[numberCard].past_simple
                pastParticiple.text = wordsArray[numberCard].past_participle
            }
            buttonLeft.setOnClickListener {
                numberCard--
                if (numberCard == 0) {
                    buttonLeft.visibility = View.INVISIBLE
                } else {
                    buttonLeft.visibility = View.VISIBLE
                }
                if (!isFront) {
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                }
                baseForm.text = wordsArray[numberCard].base_form
                pastSimple.text = wordsArray[numberCard].past_simple
                pastParticiple.text = wordsArray[numberCard].past_participle
            }
        }
        var scale = applicationContext.resources.displayMetrics.density
        val front = findViewById<TextView>(R.id.Card_Front) as MaterialCardView
        val back = findViewById<TextView>(R.id.Card_Back) as MaterialCardView

        front.cameraDistance = 8000 * scale
        back.cameraDistance = 8000 * scale


        front_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.flip_card_first
        ) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.flip_card_second
        ) as AnimatorSet

        front.setOnClickListener {
            if (isFront) {
                front_anim.setTarget(front);
                back_anim.setTarget(back);
                front_anim.start()
                back_anim.start()
                isFront = false
            } else {
                front_anim.setTarget(back)
                back_anim.setTarget(front)
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
                val translation = findViewById<TextView>(R.id.transitionCard)
                val baseForm = findViewById<TextView>(R.id.base_formCard)
                val pastSimple = findViewById<TextView>(R.id.past_simpleCard)
                val pastParticiple = findViewById<TextView>(R.id.past_participleCard)
                translation.text = wordsArray[numberCard].translation
                baseForm.text = wordsArray[numberCard].base_form
                pastSimple.text = wordsArray[numberCard].past_simple
                pastParticiple.text = wordsArray[numberCard].past_participle
            } else {
                val baseForm = findViewById<TextView>(R.id.base_formCard)
                val pastSimple = findViewById<TextView>(R.id.past_simpleCard)
                val pastParticiple = findViewById<TextView>(R.id.past_participleCard)
                baseForm.text = wordsArray[numberCard].base_form
                pastSimple.text = wordsArray[numberCard].past_simple
                pastParticiple.text = wordsArray[numberCard].past_participle
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