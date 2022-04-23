package com.tristate.gasper.model

open class User {
    var id: String? = null
    var username: String? = null
    var imageURI: String? = null

    constructor(id: String?, username: String?, imageURI: String?) {
        this.id = id
        this.username = username
        this.imageURI = imageURI
    }

    constructor() {}
}