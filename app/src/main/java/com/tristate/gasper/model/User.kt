package com.tristate.gasper.model

class User {
    var id: String? = null
    var username: String? = null
    var imageURI: String? = null
    var status: String? = null
    //var search: String? = null

    constructor(
        id: String?,
        username: String?,
        imageURI: String?,
        status: String?,
        //search: String?
    ) {
        this.id = id
        this.username = username
        this.imageURI = imageURI
        this.status = status
        //this.search = search
    }

    constructor() {}
}