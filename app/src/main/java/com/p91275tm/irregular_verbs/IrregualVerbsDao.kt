package com.p91275tm.irregular_verbs

import android.provider.UserDictionary
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface IrregualVerbsDao {
    @Insert
    fun insert(irregularVerbs: Words)

    @Query("SELECT * FROM irregular_verbs")
    fun getAllWords(): Flow<List<Words>>

    @Query("SELECT * FROM irregular_verbs WHERE base_form LIKE :searchQuery OR past_simple LIKE :searchQuery OR past_participle LIKE :searchQuery OR translation LIKE :searchQuery")
    fun searchWords(searchQuery: String): Flow<List<Words>>

}