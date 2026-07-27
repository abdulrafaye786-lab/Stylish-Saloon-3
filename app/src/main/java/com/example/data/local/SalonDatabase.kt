package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EmployeeEntity::class,
        DailyEntryEntity::class,
        ExpenseEntity::class,
        SalonSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SalonDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun salonSettingsDao(): SalonSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: SalonDatabase? = null

        fun getInstance(context: Context): SalonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalonDatabase::class.java,
                    "salon_register_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
