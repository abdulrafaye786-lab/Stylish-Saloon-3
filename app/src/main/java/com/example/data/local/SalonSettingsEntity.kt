package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salon_settings")
data class SalonSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val salonName: String = "Crown Salon",
    val ownerName: String = "Owner",
    val currency: String = "PKR",
    val currencySymbol: String = "₨",
    val workingHours: String = "09:00 AM - 09:00 PM",
    val appTheme: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val language: String = "EN", // EN, UR, AR
    val isSetupCompleted: Boolean = false,
    val userRole: String = "OWNER"
)
