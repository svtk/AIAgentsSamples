package org.example.tools

import dev.langchain4j.agent.tool.Tool
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private val WEATHER_API_KEY = System.getenv("WEATHER_API_KEY")

private val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

object WeatherTool {
    @Tool
    fun getWeather(location: String): String = runBlocking {
        val weatherApiUrl =
            "https://api.weatherapi.com/v1/current.json" +
                    "?key=$WEATHER_API_KEY&q=${location}"
        httpClient.get(weatherApiUrl).bodyAsText()
    }
}