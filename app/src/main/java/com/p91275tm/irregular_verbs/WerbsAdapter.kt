package com.p91275tm.irregular_verbs

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p91275tm.irregular_verbs.databinding.WordsBinding
import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.collections.ArrayList

class WerbsAdapter: RecyclerView.Adapter<WerbsAdapter.WerbsHolder>() {
    private val list = ArrayList<Word>()
    private var language = ArrayList<Languge>()
    class WerbsHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val binding = WordsBinding.bind(item)
        fun bind(words: Word, language: Languge) = with(binding) {
            if (language.languge == "ru") {
                translation.visibility = View.VISIBLE
                baseForm.text = words.base_form
                pastSimple.text = words.past_simple
                pastParticiple.text = words.past_participle
                translation.text = words.translation
            } else {
                translation.visibility = View.GONE
                baseForm.text = words.base_form
                pastSimple.text = words.past_simple
                pastParticiple.text = words.past_participle
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WerbsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.words, parent, false)
        return WerbsHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: WerbsHolder, position: Int) {
        holder.bind(list[position], language[0])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun languch(lang: Languge) {
        language.add(lang)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addword(word: Word) {
        list.add(word)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }
}