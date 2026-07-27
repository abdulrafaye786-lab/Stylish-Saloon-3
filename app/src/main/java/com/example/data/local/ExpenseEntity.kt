package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val name: String,
    val category: String, // Food, Drinks, Tea, Coffee, Electricity, Cleaning Supplies, Hair Products, Equipment Repair, Miscellaneous
    val amount: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
