package com.example.blood_bank

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class Donors(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val name:String,
    val location:String,
    val dob:String,
    val age:Int,
    val lastBloodDonation:String,
    val bloodGroup:String,
    val mobile:String
)