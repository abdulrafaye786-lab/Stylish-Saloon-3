package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun getEntriesForDate(date: String): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM daily_entries WHERE date = :date AND employeeId = :employeeId")
    suspend fun getEntryForEmployeeAndDate(employeeId: Long, date: String): DailyEntryEntity?

    @Query("SELECT * FROM daily_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM daily_entries")
    fun getAllEntries(): Flow<List<DailyEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEntry(entry: DailyEntryEntity)

    @Query("DELETE FROM daily_entries WHERE employeeId = :employeeId")
    suspend fun deleteEntriesForEmployee(employeeId: Long)

    @Query("DELETE FROM daily_entries")
    suspend fun clearAll()
}
