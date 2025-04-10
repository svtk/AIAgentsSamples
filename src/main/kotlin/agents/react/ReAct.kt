package org.example.agents.react

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.data.message.UserMessage
import org.example.util.convertJsonToMap
import org.example.util.llmCall
import org.example.util.toolSpecifications


const val reActSystemMessage = """
You are an AI assistant designed to help users efficiently and accurately. Your primary goal is to provide helpful, precise, and clear responses.

You should think step by step in order to fulfill the objective with a reasoning divided in Thought/Action/Observation that can repeat multiple times if needed.

You should first reflect with ‘Thought: {your_thoughts}’ on the current situation, then (if necessary), call tools. When you accomplished the user task, print 'Done.'
"""

data class Contact(val id: Int, val name: String, val phoneNumber: String)

val contactList = listOf(
    Contact(100, "Alice", "+49 151 23456789"),
    Contact(101, "Bob", "+49 89 98765432"),
    Contact(102, "Charlie", "+36 20 123 4567"),
    Contact(103, "Daniel", "+44 7911 123456"),
)
val contactMap = contactList.associateBy { it.id }

data class Money(val sum: Int, val currency: String)

object BankingAppTools {
    private const val DEFAULT_CURRENCY = "EUR"

    @Tool("Send the specified sum (the amount in default user currency) to a recipient contact by a given id. Verify that the recipient is a valid contact and the user has sufficient funds. Convert the sum to the default currency of the recipient before sending.")
    fun sendMoney(
        @P("the ID of the current user") userID: Int,
        @P("the sum of money to be sent in the default currency") sum: Int,
        @P("the ID of the recipient contact") recipientID: Int,
        @P("the purpose of the transaction") purpose: String
    ): String {
        val user = "${contactMap[recipientID]?.name} (${contactMap[recipientID]?.phoneNumber})"
        println("Please confirm that you are sending $sum $DEFAULT_CURRENCY to $user with the purpose '$purpose'.")
        return "Money was sent."
    }

    @Tool("Get the list of contacts of a current user.")
    fun getContactList(userID: Int): List<Contact> {
        return contactList
    }

    @Tool("Get the balance of a current user.")
    fun getBalance(userID: Int): Money = Money(200, DEFAULT_CURRENCY)

    @Tool("Get the information about the exchange rate between two currencies.")
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

fun ToolExecutionRequest.execute(): String {
    val argumentsMap = convertJsonToMap(arguments())
    fun userID() = argumentsMap["arg0"].toString().toInt()

    return when (name()) {
        "sendMoney" -> {
            BankingAppTools.sendMoney(
                userID = userID(),
                sum = argumentsMap["arg1"].toString().toInt(),
                recipientID = argumentsMap["arg2"].toString().toInt(),
                purpose = argumentsMap["arg3"].toString())
        }
        "getContactList" -> BankingAppTools.getContactList(userID()).toString()
        "getBalance" -> BankingAppTools.getBalance(userID()).toString()
        "getExchangeRate" -> {
            BankingAppTools.getExchangeRate(
                from = argumentsMap["arg0"].toString(),
                to = argumentsMap["arg1"].toString())
        }
        else -> "No tool found"
    }
}

fun ToolExecutionRequest.executeTool(): ToolExecutionResultMessage =
    ToolExecutionResultMessage.from(this, execute())
        .also { println("Tool execution result:\n$it") }

val systemPrompt = SystemMessage(
    reActSystemMessage + """
    You are a banking assistant. You're having a conversation with a user with userID=123. 
    Check that you can accomplish what the user asks for with the provided tools. 
    If not, respond "Can't perform the task". 
    """.trimIndent()
)

fun main() {
    val userMessage = UserMessage("Send 25 pounds to Daniel for the dinner at the restaurant")

    val tools = toolSpecifications<BankingAppTools>()
    val messages = mutableListOf(systemPrompt, userMessage)

    var aiMessage = llmCall(messages, tools)
    messages += aiMessage

    while (aiMessage.hasToolExecutionRequests()) {
        val toolExecutionMessages =
            aiMessage.toolExecutionRequests()
                .map { it.executeTool() }
        messages += toolExecutionMessages

        aiMessage = llmCall(messages, tools)
        messages += aiMessage
    }
}
