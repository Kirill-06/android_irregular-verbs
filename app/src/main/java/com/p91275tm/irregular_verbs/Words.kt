package com.p91275tm.irregular_verbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "irregular_verbs")
data class Words(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "base_form") val base_form: String,
    @ColumnInfo(name = "past_simple")val past_simple: String,
    @ColumnInfo(name = "past_participle")val past_participle: String,
    @ColumnInfo(name = "translation")val translation: String
)
