package org.example.simple

import org.example.util.llm

fun main() {
    val sentence = "Sie können KI-Agenten in Kotlin erstellen"
    val output = llm.chat("Translate into English: $sentence")
    println(output)
}