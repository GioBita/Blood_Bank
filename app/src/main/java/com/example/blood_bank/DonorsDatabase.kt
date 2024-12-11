package com.example.blood_bank

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Donors::class], version = 1)
abstract class DonorsDatabase : RoomDatabase() {
    abstract fun donorsDao(): DonorsDAO
}
