package org.example.util

import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.agent.tool.ToolSpecifications
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.service.IllegalConfigurationException
import dev.langchain4j.service.tool.DefaultToolExecutor
import dev.langchain4j.service.tool.ToolExecutor

class Tools
private constructor(
    val specs: List<ToolSpecification>,
    private val toolExecutors: Map<String, ToolExecutor>
) {

    companion object {
        fun from(objectWithTool: Any): Tools {
            val toolSpecifications = mutableListOf<ToolSpecification>()
            val toolExecutors = mutableMapOf<String, ToolExecutor>()
            for (method in objectWithTool.javaClass.declaredMethods) {
                if (method.isAnnotationPresent(Tool::class.java)) {
                    val toolSpecification = ToolSpecifications.toolSpecificationFrom(method)
                    toolExecutors[toolSpecification.name()] = DefaultToolExecutor(objectWithTool, method)
                    toolSpecifications.add(ToolSpecifications.toolSpecificationFrom(method))
                }
            }
            return Tools(toolSpecifications, toolExecutors)
        }
    }

    fun execute(aiMessage: AiMessage): List<ToolExecutionResultMessage> {
        val toolExecutionRequests = aiMessage.toolExecutionRequests()
        return toolExecutionRequests.map { request ->
            val toolExecutor = toolExecutors[request.name()]
                ?: throw IllegalConfigurationException("No tool executor found for tool name: ${request.name()}")
            val memoryIdToBeIgnored = Any()
            ToolExecutionResultMessage.from(request, toolExecutor.execute(request, memoryIdToBeIgnored))
//                .also { println("Tool execution result:\n$request") }
        }
    }
}
