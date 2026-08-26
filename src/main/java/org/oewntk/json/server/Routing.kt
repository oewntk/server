package org.oewntk.json.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.oewntk.json.out.AnySerializerThroughJsonElement
import org.oewntk.json.out.Value
import org.oewntk.json.out.toValue
import org.oewntk.model.*

// A handy extension to parse comma-separated key=value or standalone preferences
fun ApplicationRequest.parsePreferHeader(): Map<String, String?> {
    val rawHeader = headers[HttpHeaders.Prefer] ?: return emptyMap()

    return rawHeader.split(",")
        .map { it.trim() }
        .associate { part ->
            val segments = part.split("=", limit = 2)
            val key = segments[0].lowercase() // RFC states keys are case-insensitive
            val value = segments.getOrNull(1)
            key to value
        }
}

suspend fun <T> RoutingCall.respond(obj: T, toData: (T) -> Any, toOEWNData: (T) -> Any, preferences: Map<String, String?>) {
    val mode = preferences["mode"]
    when (mode) {
        null -> {
            respond(obj as Any)
        }

        "model" -> {
            response.header(HttpHeaders.PreferenceApplied, "mode=model")
            respond(obj as Any)
        }

        "data" -> {
            val data = toData.invoke(obj)
            val method = preferences["method"]
            val response = if (method == "typed") {
                response.header(HttpHeaders.PreferenceApplied, "mode=data,method=typed")
                Json.encodeToString<Value>(data.toValue())
            } else {
                response.header(HttpHeaders.PreferenceApplied, "mode=data")
                Json.encodeToString(AnySerializerThroughJsonElement, data)
            }
            // Respond with the raw text payload and specify the content type
            respondText(response, ContentType.Application.Json)
        }

        "oewn" -> {
            val data = toOEWNData.invoke(obj)
            response.header(HttpHeaders.PreferenceApplied, "mode=oewn")
            val response = Json.encodeToString(AnySerializerThroughJsonElement, data)
            respondText(response)
        }
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("OEWN")
        }

        get("/api/synset/{id}") {
            val preferences = call.request.parsePreferHeader()
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'id' parameter")
            lookupSynset(SynsetId(id))
                ?.let {
                    call.respond(it, Synset::toData, Synset::toOEWNData, preferences)
                } ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/sense/{id}") {
            val preferences = call.request.parsePreferHeader()
            val id =
                call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'id' parameter")
            lookupSense(SenseKey(id))
                ?.let { call.respond(it, Sense::toData, Sense::toOEWNData, preferences) }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/lex/{id}") {
            val preferences = call.request.parsePreferHeader()
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing parameters")
            val parts = id.split(",")
            if (parts.size < 2) return@get call.respond(
                HttpStatusCode.BadRequest,
                "Must provide both lemma and key2 separated by a comma"
            )
            val lemma = parts[0]
            val key2 = parts[1]
            lookupLex(lemma, key2)
                ?.let { call.respond(it, Lex::toData, { lex -> lex.toOEWNDataValue(model.senseResolver) }, preferences) }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/word/{lemma}") {
            val preferences = call.request.parsePreferHeader()
            val lemma = call.parameters["lemma"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing 'lemma' parameter"
            )
            lookupWord(lemma)
                ?.let {
                    call.respond(
                        it,
                        { lexes -> lexes.map(Lex::toData).toList() },
                        { lexes -> lexes.map { lex -> lex.toOEWNData(model.senseResolver) }.toList() },
                        preferences
                    )
                }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/starts/{start}") {
            val start = call.parameters["start"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing 'start' parameter"
            )
            lookupStarts(start)
                ?.let {
                    call.respond(it)
                }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/contains/{include}") {
            val include = call.parameters["include"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing 'include' parameter"
            )
            lookupContains(include)
                ?.let {
                    call.respond(it)
                }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/matches/{regex}") {
            val regex = call.parameters["regex"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing 'regex' parameter"
            )
            lookupMatches(regex)
                ?.let {
                    call.respond(it)
                }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/schema/{schema}") {
            val schema = call.parameters["schema"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing 'schema' parameter"
            )
            lookupSchema(schema)
                ?.let {
                    call.respond(it)
                }
                ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun lookupSynset(id: SynsetId): Synset? {
    return model.synsetFinder(id)
}

fun lookupSense(id: SenseKey): Sense? {
    return model.senseFinder(id)
}

fun lookupLex(lemma: Lemma, key2: Key2): Lex? {
    return model.lexFinder1(lemma, key2)
}

fun lookupWord(word: String): Collection<Lex>? {
    return model.lexFinder(word)
}

fun lookupStarts(start: String): Collection<Lemma>? {
    return model.lexes
        .map(Lex::lemma)
        .filter { it.startsWith(start) }
        .sorted()
        .ifEmpty { null }
}

fun lookupStartsIgnoreCase(start: String): Collection<Lemma>? {
    val lcStart = start.lowercase()
    return model.lexes
        .map(Lex::lCLemma)
        .filter { it.startsWith(lcStart) }
        .sorted()
        .ifEmpty { null }
}

fun lookupContains(start: String): Collection<Lemma>? {
    return model.lexes
        .map(Lex::lemma)
        .filter { it.contains(start) }
        .sorted()
        .ifEmpty { null }
}

fun lookupIContainsIgnoreCase(start: String): Collection<Lemma>? {
    val lcStart = start.lowercase()
    return model.lexes
        .map(Lex::lCLemma)
        .filter { it.contains(lcStart) }
        .sorted()
        .ifEmpty { null }
}

fun lookupMatches(regex: String): Collection<Lemma>? {
    val re = regex.toRegex()
    return model.lexes
        .map(Lex::lemma)
        .filter { re.matches(it) }
        .sorted()
        .ifEmpty { null }
}

fun lookupSchema(schema: String): String? {
    return when {
        schema in setOf("oewn", "data", "model") -> lookupSchema2(schema)
        schema in setOf("frames", "templates") -> lookupSchema1("schema-$schema")
        schema.startsWith("schema") || schema.startsWith("defs") -> lookupSchema1(schema)
        else -> null
    }
}

fun lookupSchema1(schema: String): String? {
    val schemaName = "$schema.json"
    return text("/schema/$schemaName")
}

fun lookupSchema2(schemaGroup: String): String? {
    val mainSchemaName = "schema-$schemaGroup.json"
    val defSchemaName = "defs-$schemaGroup.json"
    val mainSchema = text("/schema/$mainSchemaName")
    val defsSchema = text("/schema/$defSchemaName")
    return if (mainSchema != null && defsSchema != null) """
{
"$mainSchemaName": { 
$mainSchema 
}
"$defSchemaName": { 
$defsSchema 
}
}"""
    else null
}

private fun text(path: String): String? = object {}.javaClass.getResource(path)?.readText()
