package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val employeeId: Long,
    val employeeName: String,
    val cashEarnings: Double = 0.0,
    val onlinePayments: Double = 0.0,
    val amountBreakdown: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalIncome: Double
        get() = cashEarnings + onlinePayments
}
