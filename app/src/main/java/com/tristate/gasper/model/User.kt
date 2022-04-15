package com.tristate.gasper.model

class User {
    var id: String? = null
    var username: String? = null
    var imageURI: String? = null

    constructor(id: String?, username: String?, imageURL: String?) {
        this.id = id
        this.username = username
        this.imageURI = imageURL
    }

    constructor() {}
}