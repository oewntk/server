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