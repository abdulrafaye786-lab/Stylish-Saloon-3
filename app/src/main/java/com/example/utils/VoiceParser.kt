package com.example.utils

sealed class VoiceParseResult {
    data class EmployeeEntryResult(
        val employeeNameQuery: String,
        val cashAmount: Double?,
        val onlineAmount: Double?
    ) : VoiceParseResult()

    data class ExpenseEntryResult(
        val expenseName: String,
        val category: String,
        val amount: Double
    ) : VoiceParseResult()

    data object Unparsed : VoiceParseResult()
}

object VoiceParser {
    private val expenseCategories = listOf(
        "Tea" to "Tea",
        "Coffee" to "Coffee",
        "Food" to "Food",
        "Drinks" to "Drinks",
        "Drink" to "Drinks",
        "Electricity" to "Electricity",
        "Cleaning" to "Cleaning Supplies",
        "Products" to "Hair Products",
        "Shampoo" to "Hair Products",
        "Repair" to "Equipment Repair",
        "Expense" to "Miscellaneous"
    )

    fun parse(spokenText: String): VoiceParseResult {
        val text = spokenText.trim().lowercase()
        if (text.isEmpty()) return VoiceParseResult.Unparsed

        // Find numbers in text
        val numberRegex = Regex("""\d+(\.\d+)?""")
        val numberMatch = numberRegex.find(text)
        val number = numberMatch?.value?.toDoubleOrNull() ?: return VoiceParseResult.Unparsed

        val isExpenseKeyword = text.contains("expense") || text.contains("cost") || 
                expenseCategories.any { text.contains(it.first.lowercase()) }

        if (isExpenseKeyword) {
            var matchedCategory = "Miscellaneous"
            var name = "Voice Expense"

            for ((key, cat) in expenseCategories) {
                if (text.contains(key.lowercase())) {
                    matchedCategory = cat
                    name = key
                    break
                }
            }

            // Extract text before numbers
            val textBeforeNumber = text.substring(0, numberMatch.range.first).trim()
                .replace("expense", "")
                .replace("cost", "")
                .trim()

            if (textBeforeNumber.isNotEmpty()) {
                name = textBeforeNumber.replaceFirstChar { it.uppercase() }
            }

            return VoiceParseResult.ExpenseEntryResult(
                expenseName = name,
                category = matchedCategory,
                amount = number
            )
        } else {
            // Employee Entry
            val isOnline = text.contains("online") || text.contains("card") || text.contains("transfer")
            
            // Extract employee name
            val cleaned = text
                .replace("earned", "")
                .replace("cash", "")
                .replace("online", "")
                .replace("payment", "")
                .replace("card", "")
                .replace(numberMatch.value, "")
                .trim()

            val nameQuery = cleaned.replaceFirstChar { it.uppercase() }

            return VoiceParseResult.EmployeeEntryResult(
                employeeNameQuery = nameQuery.ifEmpty { "Employee" },
                cashAmount = if (!isOnline) number else null,
                onlineAmount = if (isOnline) number else null
            )
        }
    }
}
