package org.example.tools

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool

object BankingTool {
    @Tool("Transfers a specified amount to a recipient.")
    fun sendMoney(
        @P("The amount to be transferred, specified in the user's default currency.") amount: Double,
        @P("The name of the contact receiving the transfer.") recipient: String,
        @P("A brief description or reason for the transaction.") purpose: String
    ): String {
        // sending money
        println("Please confirm that you are sending $amount EUR to " +
                "$recipient with the purpose: '$purpose'.")
        return "Money was sent."
    }
}