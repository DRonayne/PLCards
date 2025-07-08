package com.darach.plcards.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.darach.plcards.data.local.dao.CardDao
import com.darach.plcards.data.local.dao.RecentSearchDao
import com.darach.plcards.data.local.entity.CardEntity
import com.darach.plcards.data.local.entity.RecentSearchEntity

@Database(
    entities = [CardEntity::class, RecentSearchEntity::class],
    version = 3,
    exportSchema = false
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun recentSearchDao(): RecentSearchDao
}