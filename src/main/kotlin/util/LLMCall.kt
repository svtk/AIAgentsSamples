package org.example.util

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.agent.tool.ToolSpecifications
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName

val apiKey = System.getenv("OPENAI_API_KEY") ?: "YOUR_OPENAI_API_KEY"

val client = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName(OpenAiChatModelName.GPT_4_O_MINI)
    .temperature(0.3)
    .logRequests(true)
    .logResponses(true)
    .build()

fun llmCall(
    prompt: String,
    systemPrompt: String? = null
): String {
    return client
        .chat(SystemMessage(systemPrompt), UserMessage(prompt))
        .aiMessage().text()
}

fun llmCall(messages: List<ChatMessage>): String =
    client.chat(messages).aiMessage().text()

fun llmCall(
    messages: List<ChatMessage>,
    toolSpecifications: List<ToolSpecification>?
): AiMessage {
    val initialRequest: ChatRequest = ChatRequest.builder()
        .messages(messages)
        .toolSpecifications(toolSpecifications)
        .build()
    return client.chat(initialRequest).aiMessage()
        .also { println("Response:\n$it") }
}

inline fun <reified T: Any> toolSpecifications(): List<ToolSpecification> =
    ToolSpecifications.toolSpecificationsFrom(T::class.java)

fun main() {
    val response = llmCall("Hello, how are you?")
    println(response)
}