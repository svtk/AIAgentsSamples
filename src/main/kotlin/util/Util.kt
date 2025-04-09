package org.example.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun convertJsonToMap(jsonString: String): Map<String, Any> {
    val jsonElement = Json.parseToJsonElement(jsonString).jsonObject
    return jsonElement.mapValues { it.value.jsonPrimitive.content }
}
