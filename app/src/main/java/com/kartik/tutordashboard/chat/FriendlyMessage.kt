package com.kartik.tutordashboard.chat

class FriendlyMessage {
    var text: String? = null
    var name: String? = null
    var photoUrl: String? = null
    var imageUrl: String? = null
    var email:String? = null

    // Empty constructor needed for Firestore serialization
    constructor()

    constructor(text: String?, name: String?, photoUrl: String?, imageUrl: String?, email:String?) {
        this.text = text
        this.name = name
        this.photoUrl = photoUrl
        this.imageUrl = imageUrl
        this.email = email
    }
}
