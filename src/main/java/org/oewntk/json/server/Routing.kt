package org.oewntk.json.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.oewntk.model.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("OEWN")
        }

        get("/api/synset/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'id' parameter")
            lookupSynset(id)
                ?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/sense/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'id' parameter")
            lookupSense(id)
                ?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/lex/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing parameters")
            val parts = id.split(",")
            if (parts.size < 2) return@get call.respond(HttpStatusCode.BadRequest, "Must provide both lemma and key2 separated by a comma")
            val lemma = parts[0]
            val key2 = parts[1]
            lookupLex(lemma, key2)
                ?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/api/word/{lemma}") {
            val lemma = call.parameters["lemma"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'lemma' parameter")
            lookupWord(lemma)
                ?.let { call.respond(it) }
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