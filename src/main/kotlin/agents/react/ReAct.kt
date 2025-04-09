package org.example.agents.react

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.agent.tool.ToolSpecifications
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import org.example.util.client
import org.example.util.convertJsonToMap


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
    Contact(103, "Daniel", "+49 160 11223344"),
)
val contactMap = contactList.associateBy { it.id }


object BankingAppTools {
    @Tool("Send the specified sum (the amount in default user currency) to a recipient contact by a given id. Verify that the recipient is a valid contact and the user has sufficient funds.")
    fun sendMoney(
        @P("the ID of the current user") userID: Int,
        @P("the sum of money to be sent in the default currency") sum: Int,
        @P("the ID of the recipient contact") recipientID: Int,
        @P("the purpose of the transaction") purpose: String
    ) {
        val user = "${contactMap[recipientID]?.name} (${contactMap[recipientID]?.phoneNumber})"
        println("Please confirm: sending money to $user with sum $sum and purpose $purpose")
    }

    @Tool("Get the default currency of a current user.")
    fun getDefaultCurrency(userID: Int): String = "eur"

    @Tool("Get the list of contacts of a current user.")
    fun getContactList(userID: Int): List<Contact> {
        return contactList
    }

    @Tool("Get the current balance of a current user in the default currency.")
    fun getBalance(userID: Int): Int = 200
}

fun ToolExecutionRequest.invokeTool(): String {
    val argumentsMap = convertJsonToMap(arguments())
    fun userID() = argumentsMap["arg0"].toString().toInt()

    return when (name()) {
        "sendMoney" -> {
            BankingAppTools.sendMoney(
                userID = userID(),
                sum = argumentsMap["arg1"].toString().toInt(),
                recipientID = argumentsMap["arg2"].toString().toInt(),
                purpose = argumentsMap["arg3"].toString())
            "Money was sent."
        }
        "getDefaultCurrency" -> BankingAppTools.getDefaultCurrency(userID())
        "getContactList" -> BankingAppTools.getContactList(userID()).toString()
        "getBalance" -> BankingAppTools.getBalance(userID()).toString()
        else -> "No tool found"
    }
}

fun main() {
    val systemPrompt = SystemMessage(
        reActSystemMessage + """
        You are a banking assistant. You're having a conversation with a user with userID=123. 
        Check that you can accomplish what the user asks for with the provided tools. 
        If not, respond "Can't perform the task". 
        """.trimIndent())
    val userMessage = UserMessage("Send 20 eur to Daniel for the dinner at the restaurant")

    val toolSpecifications = ToolSpecifications.toolSpecificationsFrom(
        BankingAppTools::class.java
    )

    val initialRequest: ChatRequest = ChatRequest.builder()
        .messages(systemPrompt, userMessage)
        .toolSpecifications(toolSpecifications)
        .build()
    val initialResponse: ChatResponse = client.chat(initialRequest)
    var aiMessage = initialResponse.aiMessage()
    println("First response:")
    println(aiMessage)

    while (aiMessage.hasToolExecutionRequests()) {
        val toolExecutionMessages =
            aiMessage.toolExecutionRequests().map { request ->
                val toolExecutionResult = request.invokeTool()
                ToolExecutionResultMessage.from(request, toolExecutionResult)
            }

        val request: ChatRequest = ChatRequest.builder()
            .messages(listOf(systemPrompt, userMessage, aiMessage) + toolExecutionMessages)
            .toolSpecifications(toolSpecifications)
            .build()
        aiMessage = client.chat(request).aiMessage()
        println("Response:")
        println(aiMessage)
    }
}