package com.example.clone_insta.navigation.model

import java.sql.Timestamp

data class ContentDTO(
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp: Long? = null,
    var favoriteCount : Int = 0,
    var favorites : MutableMap<String, Boolean> = HashMap()) {
    data class Comment(
        var userId: String? = null,
        var uid : String? = null,
        var comment : String? = null,
        var timestamp: Long? = null)
}