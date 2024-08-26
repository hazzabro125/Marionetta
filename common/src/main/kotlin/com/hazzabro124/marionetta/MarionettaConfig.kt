package com.hazzabro124.marionetta

import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema

object MarionettaConfig {
    @JvmField
    val CLIENT = Client()

    @JvmField
    val SERVER = Server()

    class Client {
    }

    class Server {
        @JsonSchema(description = "Beware")
        var removeAllAttachments = false
    }
}