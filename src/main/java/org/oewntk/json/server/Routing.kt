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
            lookupSynset(id)
                ?.let {
                    call.respond(it, Synset::toData, Synset::toOEWNData, preferences)
                } ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/sense/{id}") {
            val preferences = call.request.parsePreferHeader()
            val id =
                call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'id' parameter")
            lookupSense(id)
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
                ?.let { call.respond(it, Lex::toData, { lex -> lex.toOEWNData(model.senseResolver) }, preferences) }
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

        // GET /api/lemma/{lemma} Full synset records for a lemma. Returns the full synset record (definitions, relations, other members, ...) for every synset that has a sense for the exact given lemma (across every part of speech).
        // GET /api/synset/{id} Full synset record by id. The synset, or null if no synset has that id.
        // GET /api/by_lemma/{lemma} Synset ids for a lemma. Like /api/lemma/{lemma}, but returns just the matching synset ids instead of the full synset record for each.

        // GET /api/autocomplete/{query} Search-as-you-type suggestions. Matches query as a prefix against lemmas, bare synset ids (with or without the oewn- prefix used in the RDF/XML/Turtle exports), and ILIs. Results are sorted case-insensitively by display text.

        // GET /api/senses/{id}/concordance Corpus concordance for a synset. Matches query as a prefix against lemmas, bare synset ids (with or without the oewn- prefix used in the RDF/XML/Turtle exports), and ILIs. Results are sorted case-insensitively by display text.
        // GET /api/senses/{id}/count Approximate corpus occurrence count for a synset. One page of keyword-in-context (KWIC) rows from the Semcor corpus for every sense tagged with this synset id. page is 0-indexed and clamps to the last page rather than erroring if out of range.
        // GET /api/senses/{id} Corpus document ids for a synset. Sorted, deduplicated ids of every Semcor document containing at least one token tagged with this synset id. Documents where the only occurrence is packed into a ;-joined multi-key token are not found by the underlying index, so this can under-count.
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