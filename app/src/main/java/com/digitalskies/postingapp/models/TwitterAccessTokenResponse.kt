package com.digitalskies.postingapp.models

data class TwitterAccessTokenResponse(
    val oauthToken: String?,
    val oauthTokenSecret:String?,
    val screenName:String?,
    val error: String?
)