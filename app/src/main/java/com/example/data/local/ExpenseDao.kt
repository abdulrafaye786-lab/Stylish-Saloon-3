package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date = :date ORDER BY createdAt DESC")
    fun getExpensesForDate(date: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, createdAt DESC")
    fun getExpensesForDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun clearAll()
}
