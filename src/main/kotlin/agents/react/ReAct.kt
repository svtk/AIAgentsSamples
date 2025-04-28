package org.example.agents.react

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import org.example.util.*


const val SYSTEM_PROMPT = """
You are an AI assistant designed to help users efficiently, accurately, and clearly. Your primary goal is to understand the user's intent and provide helpful, precise responses by thinking step by step.

Follow a structured reasoning process using the ReAct framework:
1. Thought: Reflect logically on the current situation.
2. Action: If needed, invoke a tool to gather more information or perform a task.
3. Observation: Review the tool’s output.

Repeat this Thought → Action → Observation cycle as many times as needed.
Begin every reasoning step with:
- Thought: {your reasoning here}
- If action is required, invoke the appropriate tool.
- Upon receiving a result, log it with: Observation: {tool_output}. Then, proceed with another Thought: to evaluate the new context.

When the task is fully resolved, conclude by outputting: Done.

Always aim to be clear, logical, and efficient in your reasoning.
"""

const val BANKING_ASSISTANT_SYSTEM_PROMPT = SYSTEM_PROMPT + """
You are a banking assistant interacting with a user (userId=123).
Your goal is to understand the user's request and determine whether it can be fulfilled using the available tools.

If the task can be accomplished with the provided tools, proceed accordingly.
If the task cannot be performed with the tools available, respond with:
"Can't perform the task." 
"""

val systemPrompt = SystemMessage(BANKING_ASSISTANT_SYSTEM_PROMPT)

fun AiMessage.isDone(): Boolean = !hasToolExecutionRequests()// || text().contains("Done.")

fun main() {
    val userMessage = UserMessage("Send 25 pounds to Daniel for dinner at the restaurant")

    // Basic Agent in Kotlin:

//    val history = mutableListOf(systemPrompt, userMessage)
    val history = mutableListOf<ChatMessage>(userMessage)

    val tools = Tools.from(BankingAppTools)

    while (true) {
        val action = llm.chat(history, tools.specs)
        history += action.also { println("#Action# $it") }
        if (action.isDone()) break

        history += tools.execute(action).also { println("Tool Execution Result: $it") }
    }
}
/*
// Basic Agent pseudocode:

env = Environment()
tools = Tools(env)
system_prompt = "Goals, constraints, and how to act"

while True:
    action = llm.run(system_prompt + env.state)
    env.state = tools.run(action)
 */