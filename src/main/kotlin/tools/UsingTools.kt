package org.example.tools

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.UserMessage
import org.example.util.*

fun main() {
    // Without the tools
//    val message = "What's the weather in Copenhagen?"
//    val response = llmClient.chat(message)
//    println(response)
//    return

    // With the tools
//    val userMessage = UserMessage(message)
    bankingExample()

    //.also { println("Tool Execution: $it") }
//    println("Action: $action")
//    messages += finalMessage
}

private fun bankingExample() {
    val userMessage = UserMessage("Send 25 euros to Daniel for dinner at the restaurant")

    val tool = Tools.from(BankingTool)
    val messages = mutableListOf<ChatMessage>(userMessage)

    println(tool.specs)

    val action = llm.chat(messages, tool.specs)//.also { println("Action: $it") }
    messages += action

    val executionResult = tool.execute(action)//.also { println("Tool Execution Result: $it") }
    messages += executionResult

    val result = llm.chat(messages, tool.specs)

    println(result.text())
}


private fun weatherExample() {
    val userMessage = UserMessage("What's the weather in Copenhagen? Provide a concise one-sentence answer.")

    val tool = Tools.from(WeatherTool)
    val messages = mutableListOf<ChatMessage>(userMessage)

    val action = llm.chat(messages, tool.specs).also { println("Action: $it") }
    messages += action

    val executionResult = tool.execute(action).also { println("Tool Execution Result: $it") }
    messages += executionResult

    val result = llm.chat(messages, tool.specs)

    println(result.text())
}