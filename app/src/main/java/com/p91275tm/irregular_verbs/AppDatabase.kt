package com.p91275tm.irregular_verbs

import android.content.Context
import android.provider.UserDictionary
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Words::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun irregularVerbsDao(): IrregualVerbsDao
    companion object {
        fun irregularVerbsDao(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "irregular_verbs"
            ).build()
        }
        fun getIrregularVerbsDao(context: Context): IrregualVerbsDao {
            return irregularVerbsDao(context).irregularVerbsDao()
        }
    }
}