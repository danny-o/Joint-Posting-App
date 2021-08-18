package com.digitalskies.postingapp.utils

import com.digitalskies.postingapp.models.TwitterAccessTokenResponse
import com.digitalskies.postingapp.models.TokenResponse


fun parseTokenResponse(response:String): TokenResponse {
    val splitResponse = response.split("&")
    return if(splitResponse.size > 2){
        val oauthToken = splitResponse[0].split("=")[1]
        val oauthTokenSecret = splitResponse[1].split("=")[1]
        val oauthCallbackConfirmed = splitResponse[2].split("=")[1]
        TokenResponse(oauthToken, oauthTokenSecret, oauthCallbackConfirmed=="true", null)
    }else{
        TokenResponse(null, null, null, "Unable to parse request token response: $response")
    }
}

/**
 * Parses the response from the access token request
 */
fun parseAccessTokenResponse(response:String): TwitterAccessTokenResponse {
    val splitResponse = response.split("&")
    return if(splitResponse.size > 2){
        val oauthToken = splitResponse[0].split("=")[1]
        val oauthTokenSecret = splitResponse[1].split("=")[1]
        val screenName = splitResponse[3].split("=")[1]
        TwitterAccessTokenResponse(oauthToken, oauthTokenSecret, screenName, null)
    }else{
        TwitterAccessTokenResponse(null, null, null, "Unable to parse request token response: $response")
    }
}

fun parseMediaId(response: String):String{
    val splitResponse = response.split(",")

        val mediaId = splitResponse[0].split(":")[1]

        return mediaId


}