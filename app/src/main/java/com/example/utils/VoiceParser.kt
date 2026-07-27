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
        "tea" to "Tea",
        "chai" to "Tea",
        "coffee" to "Coffee",
        "food" to "Food",
        "khana" to "Food",
        "lunch" to "Food",
        "drinks" to "Drinks",
        "drink" to "Drinks",
        "cold drink" to "Drinks",
        "electricity" to "Electricity",
        "bijli" to "Electricity",
        "cleaning" to "Cleaning Supplies",
        "safai" to "Cleaning Supplies",
        "products" to "Hair Products",
        "shampoo" to "Hair Products",
        "repair" to "Equipment Repair",
        "kharcha" to "Miscellaneous",
        "expense" to "Miscellaneous",
        "cost" to "Miscellaneous"
    )

    fun parse(spokenText: String): VoiceParseResult {
        val text = spokenText.trim().lowercase()
        if (text.isEmpty()) return VoiceParseResult.Unparsed

        // Find numbers in text
        val numberRegex = Regex("""\d+(\.\d+)?""")
        val numberMatches = numberRegex.findAll(text).toList()
        if (numberMatches.isEmpty()) return VoiceParseResult.Unparsed

        val totalAmount = numberMatches.sumOf { it.value.toDoubleOrNull() ?: 0.0 }
        if (totalAmount <= 0) return VoiceParseResult.Unparsed

        val isExpenseKeyword = text.contains("expense") || text.contains("cost") || text.contains("kharcha") ||
                expenseCategories.any { text.contains(it.first.lowercase()) }

        if (isExpenseKeyword) {
            var matchedCategory = "Miscellaneous"
            var name = "Voice Expense"

            for ((key, cat) in expenseCategories) {
                if (text.contains(key.lowercase())) {
                    matchedCategory = cat
                    name = key.replaceFirstChar { it.uppercase() }
                    break
                }
            }

            // Extract text before numbers if available
            val firstNumberMatch = numberMatches.first()
            val textBeforeNumber = text.substring(0, firstNumberMatch.range.first).trim()
                .replace("expense", "")
                .replace("cost", "")
                .replace("kharcha", "")
                .trim()

            if (textBeforeNumber.isNotEmpty()) {
                name = textBeforeNumber.replaceFirstChar { it.uppercase() }
            }

            return VoiceParseResult.ExpenseEntryResult(
                expenseName = name,
                category = matchedCategory,
                amount = totalAmount
            )
        } else {
            // Employee Entry
            val isOnline = text.contains("online") || text.contains("card") || text.contains("transfer") ||
                    text.contains("easypaisa") || text.contains("jazzcash")
            
            // Extract employee name
            var cleaned = text
                .replace("earned", "")
                .replace("cash", "")
                .replace("online", "")
                .replace("payment", "")
                .replace("card", "")
                .replace("easypaisa", "")
                .replace("jazzcash", "")
                .replace("rs", "")
                .replace("rupees", "")

            numberMatches.forEach { match ->
                cleaned = cleaned.replace(match.value, "")
            }

            val nameQuery = cleaned.trim().replaceFirstChar { it.uppercase() }

            return VoiceParseResult.EmployeeEntryResult(
                employeeNameQuery = nameQuery.ifEmpty { "Employee" },
                cashAmount = if (!isOnline) totalAmount else null,
                onlineAmount = if (isOnline) totalAmount else null
            )
        }
    }
}
