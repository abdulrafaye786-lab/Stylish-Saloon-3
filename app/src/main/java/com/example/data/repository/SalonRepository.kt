package com.example.data.repository

import com.example.data.local.*
import com.example.utils.SupabaseSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class SalonRepository(private val db: SalonDatabase) {
    private val employeeDao = db.employeeDao()
    private val dailyEntryDao = db.dailyEntryDao()
    private val expenseDao = db.expenseDao()
    private val settingsDao = db.salonSettingsDao()

    val settingsFlow: Flow<SalonSettingsEntity?> = settingsDao.getSettingsFlow()
    val allEmployeesFlow: Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees()
    val activeEmployeesFlow: Flow<List<EmployeeEntity>> = employeeDao.getActiveEmployees()
    val allExpensesFlow: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allDailyEntriesFlow: Flow<List<DailyEntryEntity>> = dailyEntryDao.getAllEntries()

    suspend fun ensureInitialData() {
        // Seed Settings if not exists
        val currentSettings = settingsDao.getSettings()
        if (currentSettings == null) {
            settingsDao.insertOrUpdateSettings(SalonSettingsEntity())
        }

        // Seed 5 initial employees if empty
        val count = employeeDao.getEmployeeCount()
        if (count == 0) {
            val defaultEmployees = listOf(
                EmployeeEntity(name = "Ahmed", role = "Senior Barber"),
                EmployeeEntity(name = "Ali", role = "Hair Stylist"),
                EmployeeEntity(name = "Hassan", role = "Beard Specialist"),
                EmployeeEntity(name = "Bilal", role = "Junior Barber"),
                EmployeeEntity(name = "Tariq", role = "Hair Colorist")
            )
            employeeDao.insertAll(defaultEmployees)
        }
    }

    private suspend fun triggerSupabaseSync(action: suspend (url: String, key: String) -> Unit) {
        val s = settingsDao.getSettings()
        if (s != null && s.isSupabaseSyncEnabled && s.supabaseUrl.isNotBlank() && s.supabaseKey.isNotBlank()) {
            try {
                action(s.supabaseUrl, s.supabaseKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveSettings(settings: SalonSettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
        triggerSupabaseSync { url, key ->
            SupabaseSyncManager.syncSettings(url, key, settings)
        }
    }

    fun getEntriesForDate(date: String): Flow<List<DailyEntryEntity>> {
        return dailyEntryDao.getEntriesForDate(date)
    }

    fun getExpensesForDate(date: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForDate(date)
    }

    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<DailyEntryEntity>> {
        return dailyEntryDao.getEntriesForDateRange(startDate, endDate)
    }

    fun getExpensesForDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForDateRange(startDate, endDate)
    }

    suspend fun updateEmployeeDailyEntry(
        employeeId: Long,
        employeeName: String,
        date: String,
        cash: Double,
        online: Double,
        amountBreakdown: String = ""
    ) {
        val existing = dailyEntryDao.getEntryForEmployeeAndDate(employeeId, date)
        val entry = DailyEntryEntity(
            id = existing?.id ?: 0,
            date = date,
            employeeId = employeeId,
            employeeName = employeeName,
            cashEarnings = cash,
            onlinePayments = online,
            amountBreakdown = amountBreakdown,
            updatedAt = System.currentTimeMillis()
        )
        dailyEntryDao.insertOrUpdateEntry(entry)
        val finalEntry = if (entry.id == 0L) {
            dailyEntryDao.getEntryForEmployeeAndDate(employeeId, date) ?: entry
        } else {
            entry
        }

        triggerSupabaseSync { url, key ->
            SupabaseSyncManager.syncDailyEntry(url, key, finalEntry)
        }
    }

    suspend fun addEmployee(name: String, role: String = "Barber", phone: String = ""): Long {
        val emp = EmployeeEntity(name = name, role = role, phone = phone)
        val id = employeeDao.insertEmployee(emp)
        val finalEmp = emp.copy(id = id)

        triggerSupabaseSync { url, key ->
            SupabaseSyncManager.syncEmployee(url, key, finalEmp)
        }
        return id
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        employeeDao.updateEmployee(employee)
        triggerSupabaseSync { url, key ->
            SupabaseSyncManager.syncEmployee(url, key, employee)
        }
    }

    suspend fun deleteEmployee(employee: EmployeeEntity) {
        dailyEntryDao.deleteEntriesForEmployee(employee.id)
        employeeDao.deleteEmployee(employee)
        triggerSupabaseSync { url, key ->
            // Mark inactive or update state in Supabase
            SupabaseSyncManager.syncEmployee(url, key, employee.copy(isEnabled = false))
        }
    }

    suspend fun addExpense(date: String, name: String, category: String, amount: Double, notes: String = ""): Long {
        val exp = ExpenseEntity(
            date = date,
            name = name,
            category = category,
            amount = amount,
            notes = notes
        )
        val id = expenseDao.insertExpense(exp)
        val finalExp = exp.copy(id = id)

        triggerSupabaseSync { url, key ->
            SupabaseSyncManager.syncExpense(url, key, finalExp)
        }
        return id
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun syncAllDataToSupabase(): Result<Int> {
        val s = settingsDao.getSettings() ?: return Result.failure(Exception("Settings not loaded"))
        val employees = allEmployeesFlow.firstOrNull() ?: emptyList()
        val entries = allDailyEntriesFlow.firstOrNull() ?: emptyList()
        val expenses = allExpensesFlow.firstOrNull() ?: emptyList()

        return SupabaseSyncManager.syncAllDataToSupabase(
            supabaseUrl = s.supabaseUrl,
            supabaseKey = s.supabaseKey,
            settings = s,
            employees = employees,
            dailyEntries = entries,
            expenses = expenses
        )
    }

    suspend fun exportDataAsJson(): String {

        val json = JSONObject()
        
        val settings = settingsDao.getSettings()
        if (settings != null) {
            val settingsJson = JSONObject().apply {
                put("salonName", settings.salonName)
                put("ownerName", settings.ownerName)
                put("currency", settings.currency)
                put("workingHours", settings.workingHours)
            }
            json.put("settings", settingsJson)
        }

        val employees = allEmployeesFlow.firstOrNull() ?: emptyList()
        val employeesArray = JSONArray()
        employees.forEach { e ->
            employeesArray.put(JSONObject().apply {
                put("name", e.name)
                put("role", e.role)
                put("phone", e.phone)
                put("isEnabled", e.isEnabled)
            })
        }
        json.put("employees", employeesArray)

        val entries = allDailyEntriesFlow.firstOrNull() ?: emptyList()
        val entriesArray = JSONArray()
        entries.forEach { entry ->
            entriesArray.put(JSONObject().apply {
                put("date", entry.date)
                put("employeeName", entry.employeeName)
                put("cashEarnings", entry.cashEarnings)
                put("onlinePayments", entry.onlinePayments)
            })
        }
        json.put("entries", entriesArray)

        val expenses = allExpensesFlow.firstOrNull() ?: emptyList()
        val expensesArray = JSONArray()
        expenses.forEach { exp ->
            expensesArray.put(JSONObject().apply {
                put("date", exp.date)
                put("name", exp.name)
                put("category", exp.category)
                put("amount", exp.amount)
                put("notes", exp.notes)
            })
        }
        json.put("expenses", expensesArray)

        return json.toString(2)
    }

    suspend fun restoreDataFromJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            
            if (json.has("settings")) {
                val sObj = json.getJSONObject("settings")
                val current = settingsDao.getSettings() ?: SalonSettingsEntity()
                val updated = current.copy(
                    salonName = sObj.optString("salonName", current.salonName),
                    ownerName = sObj.optString("ownerName", current.ownerName),
                    currency = sObj.optString("currency", current.currency),
                    workingHours = sObj.optString("workingHours", current.workingHours)
                )
                settingsDao.insertOrUpdateSettings(updated)
            }

            if (json.has("expenses")) {
                val exArray = json.getJSONArray("expenses")
                for (i in 0 until exArray.length()) {
                    val item = exArray.getJSONObject(i)
                    addExpense(
                        date = item.optString("date"),
                        name = item.optString("name"),
                        category = item.optString("category"),
                        amount = item.optDouble("amount", 0.0),
                        notes = item.optString("notes", "")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
