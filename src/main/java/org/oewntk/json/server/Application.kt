package org.oewntk.json.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import org.slf4j.event.Level
import org.oewntk.model.Model
import java.io.File
import org.oewntk.json.`in`.model.Factory as JsonModelFactory
import org.oewntk.yaml.`in`.Factory as YamlFactory

lateinit var model: Model

// Routing, plugins, and application setup go here
fun Application.module() {

    val modelPath = environment.config.propertyOrNull("model.path")?.getString()
    val modelType = environment.config.propertyOrNull("model.type")?.getString()
    val modelSubtype = environment.config.propertyOrNull("model.subtype")?.getString()

    println("[Args] $modelPath $modelType $modelSubtype")
    when (modelType) {
        "json", "JSON" -> {
            when (modelSubtype) {
                "model" -> {
                    model = JsonModelFactory(File(modelPath!!), inverses = true, verbose = true).get()!!
                }
            }
        }

        "yaml", "YAML" -> {
            val modelPath2 = environment.config.propertyOrNull("model.path2")?.getString()
            model = YamlFactory(File(modelPath!!), if (modelPath2 != null) File(modelPath2) else null, inverses = true, verbose = true).get()!!
        }
    }

    // content negotiation configuration
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureLogging() {
    install(CallLogging) {
        // Set the log level for HTTP calls
        level = Level.INFO

        // Only log API endpoints (ignore static assets or health checks)
        filter { call ->
            call.request.path().startsWith("/api")
        }

        // Customize the console string format
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val duration = call.processingTimeMillis() // Available in Ktor Server

            "HTTP $method $path -> Status: $status in ${duration}ms"
        }
    }
}