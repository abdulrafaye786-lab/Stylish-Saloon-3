package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.SalonRepository
import com.example.utils.VoiceParseResult
import com.example.utils.VoiceParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SalonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SalonRepository(SalonDatabase.getInstance(application))

    private val pktTimeZone = TimeZone.getTimeZone("Asia/Karachi")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = pktTimeZone
    }

    private fun getPktToday(): String {
        return dateFormat.format(Calendar.getInstance(pktTimeZone).time)
    }

    private val _selectedDate = MutableStateFlow(getPktToday())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val settings: StateFlow<SalonSettingsEntity> = repository.settingsFlow
        .map { it ?: SalonSettingsEntity() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SalonSettingsEntity()
        )

    val activeEmployees: StateFlow<List<EmployeeEntity>> = repository.activeEmployeesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmployees: StateFlow<List<EmployeeEntity>> = repository.allEmployeesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayEntries: StateFlow<List<DailyEntryEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getEntriesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayExpenses: StateFlow<List<ExpenseEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getExpensesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpensesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyEntries: StateFlow<List<DailyEntryEntity>> = repository.allDailyEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _voiceFeedback = MutableStateFlow<String?>(null)
    val voiceFeedback: StateFlow<String?> = _voiceFeedback.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
        }
        // Background ticker to auto-transition date when Pakistan Timezone reaches midnight
        viewModelScope.launch {
            var lastPktDate = getPktToday()
            while (true) {
                kotlinx.coroutines.delay(10000)
                val currentPktDate = getPktToday()
                if (currentPktDate != lastPktDate) {
                    if (_selectedDate.value == lastPktDate) {
                        _selectedDate.value = currentPktDate
                    }
                    lastPktDate = currentPktDate
                }
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun navigateDate(offsetDays: Int) {
        try {
            val curDate = dateFormat.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance(pktTimeZone).apply { time = curDate }
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = getPktToday()
        }
    }

    fun completeSetup(salonName: String, ownerName: String, currency: String, workingHours: String) {
        viewModelScope.launch {
            val symbol = when (currency.uppercase()) {
                "USD" -> "$"
                "EUR" -> "€"
                "INR" -> "₹"
                "GBP" -> "£"
                "AED" -> "د.إ"
                "SAR" -> "ر.س"
                else -> "₨"
            }
            val updated = settings.value.copy(
                salonName = salonName.ifBlank { "Crown Salon" },
                ownerName = ownerName.ifBlank { "Owner" },
                currency = currency,
                currencySymbol = symbol,
                workingHours = workingHours.ifBlank { "09:00 AM - 09:00 PM" },
                isSetupCompleted = true
            )
            repository.saveSettings(updated)
        }
    }

    fun updateSettings(
        salonName: String,
        ownerName: String,
        currency: String,
        workingHours: String,
        theme: String,
        language: String,
        supabaseUrl: String = settings.value.supabaseUrl,
        supabaseKey: String = settings.value.supabaseKey,
        isSupabaseSyncEnabled: Boolean = settings.value.isSupabaseSyncEnabled
    ) {
        viewModelScope.launch {
            val symbol = when (currency.uppercase()) {
                "USD" -> "$"
                "EUR" -> "€"
                "INR" -> "₹"
                "GBP" -> "£"
                "AED" -> "د.إ"
                "SAR" -> "ر.س"
                else -> "₨"
            }
            val updated = settings.value.copy(
                salonName = salonName,
                ownerName = ownerName,
                currency = currency,
                currencySymbol = symbol,
                workingHours = workingHours,
                appTheme = theme,
                language = language,
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey,
                isSupabaseSyncEnabled = isSupabaseSyncEnabled
            )
            repository.saveSettings(updated)
        }
    }

    suspend fun testSupabaseConnection(url: String, key: String): Result<String> {
        return com.example.utils.SupabaseSyncManager.testConnection(url, key)
    }

    suspend fun syncAllDataToSupabase(): Result<Int> {
        return repository.syncAllDataToSupabase()
    }

    fun saveEmployeeDailyEntry(
        employeeId: Long,
        employeeName: String,
        cash: Double,
        online: Double,
        breakdown: String = ""
    ) {
        viewModelScope.launch {
            repository.updateEmployeeDailyEntry(employeeId, employeeName, _selectedDate.value, cash, online, breakdown)
        }
    }

    fun addEmployee(name: String, role: String, phone: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addEmployee(name, role.ifBlank { "Barber" }, phone)
            }
        }
    }

    fun updateEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
        }
    }

    fun deleteEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    fun addExpense(name: String, category: String, amount: Double, notes: String) {
        viewModelScope.launch {
            if (name.isNotBlank() && amount > 0) {
                repository.addExpense(_selectedDate.value, name, category, amount, notes)
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun processVoiceCommand(speechText: String) {
        viewModelScope.launch {
            val result = VoiceParser.parse(speechText)
            when (result) {
                is VoiceParseResult.EmployeeEntryResult -> {
                    val activeList = activeEmployees.value
                    val matchedEmp = activeList.find {
                        it.name.equals(result.employeeNameQuery, ignoreCase = true) ||
                                it.name.lowercase().contains(result.employeeNameQuery.lowercase())
                    } ?: activeList.firstOrNull()

                    if (matchedEmp != null) {
                        val currentEntry = todayEntries.value.find { it.employeeId == matchedEmp.id }
                        val cash = result.cashAmount ?: (currentEntry?.cashEarnings ?: 0.0)
                        val online = result.onlineAmount ?: (currentEntry?.onlinePayments ?: 0.0)
                        saveEmployeeDailyEntry(matchedEmp.id, matchedEmp.name, cash, online)
                        _voiceFeedback.value = "Updated ${matchedEmp.name}: Cash ${cash.toInt()}, Online ${online.toInt()}"
                    } else {
                        _voiceFeedback.value = "No employee found for '${result.employeeNameQuery}'"
                    }
                }
                is VoiceParseResult.ExpenseEntryResult -> {
                    addExpense(result.expenseName, result.category, result.amount, "Voice entry")
                    _voiceFeedback.value = "Added Expense: ${result.expenseName} (${result.amount.toInt()})"
                }
                is VoiceParseResult.Unparsed -> {
                    _voiceFeedback.value = "Could not parse voice entry"
                }
            }
        }
    }

    fun clearVoiceFeedback() {
        _voiceFeedback.value = null
    }

    suspend fun exportBackupJson(): String {
        return repository.exportDataAsJson()
    }

    suspend fun restoreBackupJson(jsonStr: String) {
        repository.restoreDataFromJson(jsonStr)
    }
}
