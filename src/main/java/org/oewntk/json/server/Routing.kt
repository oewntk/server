package org.oewntk.json.server

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.oewntk.model.Key2
import org.oewntk.model.SenseKey
import org.oewntk.model.SynsetId

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        get("/synset/{id}") {
            call.respond(lookupSynset(call.parameters["id"]!!))
        }

        get("/sense/{id}") {
            call.respond(lookupSense(call.parameters["id"]!!))
        }

        get("/lex/{id}") {
            call.respond(lookupLex(call.parameters["id"]!!))
        }

        get("/word/{word}") {
            call.respond(lookupWord(call.parameters["word"]!!))
        }
    }
}

fun lookupSynset(id: SynsetId) {

}

fun lookupSense(id: SenseKey) {

}

fun lookupLex(id: Key2) {

}

fun lookupWord(word: String) {

}