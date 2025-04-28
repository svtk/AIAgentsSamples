package org.example.agents.react

import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import org.example.util.llmWithReasoning

interface BankingAssistant {
    @SystemMessage(BANKING_ASSISTANT_SYSTEM_PROMPT)
    fun performTask(task: String): String
}

val bankingAssistant =
    AiServices.builder(BankingAssistant::class.java)
        .chatLanguageModel(llmWithReasoning)
        .tools(BankingAppTools)
        .build()

fun main() {
    val response = bankingAssistant.performTask("Send 25 pounds to Daniel")
    println(response)
}
