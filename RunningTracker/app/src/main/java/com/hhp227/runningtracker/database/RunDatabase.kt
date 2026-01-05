package com.hhp227.runningtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Run::class],
    version = 1
)
abstract class RunDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDao
}