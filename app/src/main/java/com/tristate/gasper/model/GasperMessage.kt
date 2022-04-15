package com.tristate.gasper.model

class GasperMessage {
    internal var text: String? = null
    internal var name: String? = null
    internal var photoURL: String? = null
    internal var imageURL: String? = null

    constructor(text: String?, name: String?, photoURL: String?, imageURL: String?) {
        this.text = text
        this.name = name
        this.photoURL = photoURL
        this.imageURL = imageURL
    }

    constructor() {}
}