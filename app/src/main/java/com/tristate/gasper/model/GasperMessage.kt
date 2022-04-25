package com.tristate.gasper.model

class GasperMessage {
    var sender: String? = null
    var receiver: String? = null
    var text: String? = null
    var photoURI: String? = null

    constructor(sender: String?, receiver: String?, text: String?, photoURI: String?) {
        this.sender = sender
        this.receiver = receiver
        this.text = text
        this.photoURI = photoURI
    }

    constructor() {}
}