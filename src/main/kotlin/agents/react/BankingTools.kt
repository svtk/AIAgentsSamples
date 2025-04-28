package org.example.agents.react

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolSpecifications

data class Contact(/*val id: Int, */val name: String, val phoneNumber: String)

val contactList = listOf(
    Contact(/*100, */"Alice", "+49 151 23456789"),
    Contact(/*101, */"Bob", "+49 89 98765432"),
    Contact(/*102, */"Charlie", "+36 20 123 4567"),
    Contact(/*103, */"Daniel", "+44 7911 123456"),
)
val contactMap = contactList.associateBy { it.phoneNumber }

data class AmountWithCurrency(val amount: Double, val currency: String)

object BankingAppTools {
    private const val DEFAULT_CURRENCY = "EUR"

    @Tool("Transfers a specified amount (in the sender's default currency) to a recipient contact by their phone. " +
            "Fails if the recipient is not a valid contact or if the user lacks sufficient funds.")
    fun sendMoney(
        @P("The unique identifier of the user initiating the transfer.") senderId: Int,
        @P("The amount to be transferred, specified in the user's default currency.") amount: Int,
        @P("The phone number of the contact receiving the transfer.") recipientPhone: String,
//        @P("The unique identifier of the recipient contact.") recipientId: Int,
        @P("A brief description or reason for the transaction.") purpose: String
    ): String {
        val recipient = contactMap[recipientPhone] ?: return "Invalid recipient."
        println("Please confirm that you are sending $amount $DEFAULT_CURRENCY to " +
                "${recipient.name} ($recipientPhone) with the purpose: \"$purpose\".")
        return "Money was sent."
    }

    @Tool("Retrieves the list of contacts associated with the user identified by their ID.")
    fun getContacts(@P("The unique identifier of the user whose contact list is being retrieved.") userId: Int): List<Contact> {
        return contactList
    }

    @Tool("Retrieves the current balance of the user identified by their ID.")
    fun getBalance(@P("The unique identifier of the user whose balance is being retrieved.") userId: Int): AmountWithCurrency = AmountWithCurrency(200.0, DEFAULT_CURRENCY)

//    @Tool("Retrieves the default currency associated with the user identified by their ID.")
//    fun getDefaultCurrency(@P("The unique identifier of the user whose default currency is being retrieved.") userId: Int): String = DEFAULT_CURRENCY

    @Tool("Retrieves the exchange rate between two currencies, specified by their 3-letter ISO codes (e.g., \"USD\", \"EUR\").")
    fun getExchangeRate(
        @P("The 3-letter ISO currency code representing the base currency you want to convert from (e.g., \"USD\" for US Dollar, \"EUR\" for Euro).") from: String,
        @P("The 3-letter ISO currency code representing the target currency you want to convert to (e.g., \"GBP\" for British Pound, \"JPY\" for Japanese Yen).") to: String,
    ): String = when (from to to) {
        "EUR" to "USD" -> "1.1"
        "EUR" to "GBP" -> "0.86"
        "GBP" to "EUR" -> "1.16"
        "USD" to "EUR" -> "0.9"
        else -> "No information about exchange rate available."
    }
}

fun main() {
    val tools = ToolSpecifications.toolSpecificationsFrom(BankingAppTools::class.java)
    println(tools)
}