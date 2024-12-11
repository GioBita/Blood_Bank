package com.example.blood_bank

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DonorsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonors(donors: Donors)

    @Query("SELECT * FROM donors WHERE bloodGroup = :bloodGroup")
    suspend fun getDonorsByBloodGroup(bloodGroup: String): List<Donors>
}