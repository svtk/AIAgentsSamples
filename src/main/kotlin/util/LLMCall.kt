package org.example.util

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
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

fun main() {
    val response = llmCall("Hello, how are you?")
    println(response)
}