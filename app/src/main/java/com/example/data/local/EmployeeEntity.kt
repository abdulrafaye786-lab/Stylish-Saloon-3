package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String = "Barber",
    val phone: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
