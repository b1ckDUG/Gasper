package com.tristate.gasper.model

class GasperMessage {
    var sender: String? = null
    var receiver: String? = null
    var message: String? = null

    constructor(sender: String?, receiver: String?, message: String?) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
    }

    constructor() {}
}