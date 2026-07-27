package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SalonSettingsDao {
    @Query("SELECT * FROM salon_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SalonSettingsEntity?>

    @Query("SELECT * FROM salon_settings WHERE id = 1")
    suspend fun getSettings(): SalonSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SalonSettingsEntity)
}
