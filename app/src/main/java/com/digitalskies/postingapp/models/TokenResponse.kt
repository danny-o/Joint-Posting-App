package com.digitalskies.postingapp.models

data class TokenResponse(
    val oauthToken: String?,
    val oauthTokenSecret: String?,
    val oauthCallbackConfirmed: Boolean?,
    val error: String?
)
