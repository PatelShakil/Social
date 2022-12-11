package com.bcgroup.social_media.models

class UserModel {
    lateinit var username: String
    lateinit var email:String
    lateinit var password:String
    lateinit var name:String
    lateinit var profile_pic:String
    lateinit var uid:String
    lateinit var following:ArrayList<Follower>
    lateinit var followers:ArrayList<Follower>
    constructor()
    constructor(username: String, email: String, password: String, name: String,profile_url: String,uid:String) {
        this.username = username
        this.email = email
        this.password = password
        this.name = name
        this.profile_pic = profile_url
        this.uid = uid
    }
}
class Follower{
    lateinit var uid: String
    lateinit var time:String
}