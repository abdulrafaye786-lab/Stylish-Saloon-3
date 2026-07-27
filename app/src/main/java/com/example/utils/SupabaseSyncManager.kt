package com.example.utils

import android.util.Log
import com.example.data.local.DailyEntryEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SalonSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseSyncManager {

    private const val TAG = "SupabaseSyncManager"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Test Supabase connection using the provided URL and Anon/Service Key.
     */
    suspend fun testConnection(supabaseUrl: String, supabaseKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase URL or API Key is empty."))
        }

        val formattedUrl = sanitizeUrl(supabaseUrl) + "/rest/v1/salon_settings?select=id&limit=1"

        try {
            val request = Request.Builder()
                .url(formattedUrl)
                .addHeader("apikey", supabaseKey.trim())
                .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Connection successful! Supabase backend is active.")
                } else {
                    Result.failure(Exception("Supabase returned HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sync Salon Settings to Supabase table `salon_settings`.
     */
    suspend fun syncSettings(supabaseUrl: String, supabaseKey: String, settings: SalonSettingsEntity) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext

        val endpoint = sanitizeUrl(supabaseUrl) + "/rest/v1/salon_settings"
        val jsonObj = JSONObject().apply {
            put("id", 1)
            put("salon_name", settings.salonName)
            put("owner_name", settings.ownerName)
            put("currency_symbol", settings.currency)
            put("working_hours", settings.workingHours)
            put("theme", settings.appTheme)
            put("language", settings.language)
            put("updated_at", System.currentTimeMillis())
        }

        val jsonArray = JSONArray().put(jsonObj)
        val body = jsonArray.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", supabaseKey.trim())
            .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync settings to Supabase", e)
        }
    }

    /**
     * Sync Employee to Supabase table `employees`.
     */
    suspend fun syncEmployee(supabaseUrl: String, supabaseKey: String, employee: EmployeeEntity) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext

        val endpoint = sanitizeUrl(supabaseUrl) + "/rest/v1/employees"
        val jsonObj = JSONObject().apply {
            if (employee.id > 0) put("id", employee.id)
            put("name", employee.name)
            put("role", employee.role)
            put("phone", employee.phone)
            put("is_active", employee.isEnabled)
        }

        val jsonArray = JSONArray().put(jsonObj)
        val body = jsonArray.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", supabaseKey.trim())
            .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync employee to Supabase", e)
        }
    }

    /**
     * Sync Daily Income Entry to Supabase table `daily_entries`.
     */
    suspend fun syncDailyEntry(supabaseUrl: String, supabaseKey: String, entry: DailyEntryEntity) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext

        val endpoint = sanitizeUrl(supabaseUrl) + "/rest/v1/daily_entries"
        val jsonObj = JSONObject().apply {
            if (entry.id > 0) put("id", entry.id)
            put("employee_id", entry.employeeId)
            put("employee_name", entry.employeeName)
            put("entry_date", entry.date)
            put("cash_earnings", entry.cashEarnings)
            put("online_payments", entry.onlinePayments)
            put("total_income", entry.totalIncome)
            put("amount_breakdown", entry.amountBreakdown)
            put("updated_at", System.currentTimeMillis())
        }

        val jsonArray = JSONArray().put(jsonObj)
        val body = jsonArray.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", supabaseKey.trim())
            .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync daily entry to Supabase", e)
        }
    }

    /**
     * Sync Expense to Supabase table `expenses`.
     */
    suspend fun syncExpense(supabaseUrl: String, supabaseKey: String, expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withContext

        val endpoint = sanitizeUrl(supabaseUrl) + "/rest/v1/expenses"
        val jsonObj = JSONObject().apply {
            if (expense.id > 0) put("id", expense.id)
            put("name", expense.name)
            put("category", expense.category)
            put("amount", expense.amount)
            put("expense_date", expense.date)
            put("notes", expense.notes)
        }

        val jsonArray = JSONArray().put(jsonObj)
        val body = jsonArray.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", supabaseKey.trim())
            .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync expense to Supabase", e)
        }
    }

    /**
     * Batch Sync All Local Data to Supabase.
     */
    suspend fun syncAllDataToSupabase(
        supabaseUrl: String,
        supabaseKey: String,
        settings: SalonSettingsEntity?,
        employees: List<EmployeeEntity>,
        dailyEntries: List<DailyEntryEntity>,
        expenses: List<ExpenseEntity>
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase URL/Key missing."))
        }

        var count = 0
        try {
            settings?.let {
                syncSettings(supabaseUrl, supabaseKey, it)
                count++
            }

            employees.forEach {
                syncEmployee(supabaseUrl, supabaseKey, it)
                count++
            }

            dailyEntries.forEach {
                syncDailyEntry(supabaseUrl, supabaseKey, it)
                count++
            }

            expenses.forEach {
                syncExpense(supabaseUrl, supabaseKey, it)
                count++
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeUrl(url: String): String {
        var clean = url.trim()
        if (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length - 1)
        }
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        return clean
    }
}
