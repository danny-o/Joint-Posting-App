package com.digitalskies.postingapp.utils

data class SignatureParams(
        val httpMethod: String?,
        val callbackUrl: String?,
        val apiKey: String,
        val apiSecret: String,
        val nonce: String,
        val epochSeconds: Long,
        val url: String?,
        val oAuthToken: String?,
        var urlParams: MutableMap<String, String>?,
        var authParams: MutableMap<String, String>
){

    constructor(
            apiKey: String,
            apiSecret: String,
            nonce: String,
            epochSeconds: Long,
            url:String,
            oAuthToken: String?,
            authParams: MutableMap<String, String>
    ) : this( null,
            callbackUrl = null,
            apiKey = apiKey,
            apiSecret=apiSecret,
            nonce=nonce,
            epochSeconds=epochSeconds,
            url=url,
            oAuthToken = oAuthToken,
            urlParams=null,
            authParams=authParams)
}