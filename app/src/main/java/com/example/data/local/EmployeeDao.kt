package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY id ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE isEnabled = 1 ORDER BY id ASC")
    fun getActiveEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employees: List<EmployeeEntity>)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Long)
}
