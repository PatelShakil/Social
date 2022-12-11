package com.bcgroup.social_media.models

class PostModel {
    var post_id:String = "id"
    lateinit var post_caption:String
    lateinit var post_author:String
    lateinit var post_url:String
    var post_time:Long = 0
    var post_like:Long = 0
constructor()
    constructor(
        post_caption: String,
        post_author: String,
        post_url: String,
        post_time: Long,
        post_like: Long
    ) {
        this.post_caption = post_caption
        this.post_author = post_author
        this.post_url = post_url
        this.post_time = post_time
        this.post_like = post_like
    }
}