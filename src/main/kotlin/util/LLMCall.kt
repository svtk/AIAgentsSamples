package org.example.util

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.agent.tool.ToolSpecifications
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName

val apiKey = System.getenv("OPENAI_API_KEY") ?: "YOUR_OPENAI_API_KEY"

val llm = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName(OpenAiChatModelName.GPT_4_O_MINI)
    .temperature(0.3)
//    .logRequests(true)
//    .logResponses(true)
    .build()

val llmWithReasoning = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName(OpenAiChatModelName.O1)
//    .logRequests(true)
//    .logResponses(true)
    .build()

fun callLLM(
    prompt: String,
    systemPrompt: String? = null
): String {
    val systemMessage = if (systemPrompt != null) SystemMessage(systemPrompt) else null
    val messages = listOfNotNull(systemMessage, UserMessage(prompt))
    return llm
        .chat(messages)
        .aiMessage().text()
}

fun callLLM(messages: List<ChatMessage>): String =
    llm.chat(messages).aiMessage().text()

fun ChatLanguageModel.chat(
    messages: List<ChatMessage>,
    toolSpecifications: List<ToolSpecification>?
): AiMessage {
    val request: ChatRequest = ChatRequest.builder()
        .messages(messages)
        .toolSpecifications(toolSpecifications)
        .build()
    println("Request:\n$request")
    return chat(request).aiMessage()
}

/*
fun callLLM(
    messages: List<ChatMessage>,
    toolSpecifications: List<ToolSpecification>?
): AiMessage {
    val initialRequest: ChatRequest = ChatRequest.builder()
        .messages(messages)
        .toolSpecifications(toolSpecifications)
        .build()
    return llm.chat(initialRequest).aiMessage()
        .also { println("Response:\n$it") }
}
*/

inline fun <reified T : Any> tools(): List<ToolSpecification> =
    ToolSpecifications.toolSpecificationsFrom(T::class.java)

fun main() {
    val response = callLLM("Hello, how are you?")
    println(response)
    llm.chat("")
}