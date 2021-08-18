package com.digitalskies.postingapp.utils

import io.ktor.http.auth.*
import kotlinx.coroutines.yield
import java.net.URLEncoder


class TwitterHeaderBuilder(private val key: String, private val secret: String, private val oAuthToken: String?, private val oAuthSecret: String?) {

    private val _signatureBuilder = SignatureBuilder()

    /**
     * Set if the request requires some callback url
     */
    internal var callbackUrl: String? = "https://www.twitter.com"
    internal var httpMethod: String? = SignatureBuilder.HTTP_POST
    internal var url: String? = SignatureBuilder.AUTH_REQUEST_TOKEN_URL
    internal var parameters: MutableMap<String, String>? = null
    internal var isAppendingMedia=false

   suspend fun buildString(): String {

        if(httpMethod == null){
            throw IllegalArgumentException("Must set httpMethod for OAuth1")
        }

        if(url == null){
            throw IllegalArgumentException("Must set url for OAuth1")
        }

        val headerBuilder = StringBuilder()

        val epoch = _signatureBuilder.generateTimeStamp()

        val s = _signatureBuilder.generateNonce()

        var encodedCallback = ""
        callbackUrl?.let {
            encodedCallback = urlEncodeString(callbackUrl!!)
        }

        val signingKey = if(oAuthSecret != null){
            "${urlEncodeString(secret)}&${urlEncodeString(oAuthSecret)}"
        }else{
            "${urlEncodeString(secret)}&"
        }

        val authParameters: MutableMap<String, String> = mutableMapOf()

       val authSig:String
       authSig = if(isAppendingMedia){
           _signatureBuilder.createSignatureWithoutParams(
                   SignatureParams(  key, signingKey, s, epoch,url!!, oAuthToken, authParameters)
           )
       } else{
           _signatureBuilder.createSignature(
                   SignatureParams(httpMethod!!, encodedCallback, key, signingKey, s, epoch, url!!, oAuthToken, parameters, authParameters)
           )
       }


        parameters?.let {
            val paramBuilder = StringBuilder()
            parameters!!.onEachIndexed { index, entry ->
                paramBuilder.append("${entry.key}=${urlEncodeString(entry.value.toString())}")
                if(index < parameters!!.entries.size - 1){
                    paramBuilder.append("&")
                }
            }
        }

        headerBuilder.append("${AuthScheme.OAuth} ")

        var oauthToken = ""
        oAuthToken?.let {
            oauthToken = urlEncodeString(oAuthToken)
        }

        headerBuilder.append("${HttpAuthHeader.Parameters.OAuthConsumerKey}=\"${authParameters[HttpAuthHeader.Parameters.OAuthConsumerKey]!!}\", ")

        headerBuilder.append("${HttpAuthHeader.Parameters.OAuthNonce}=\"${authParameters[HttpAuthHeader.Parameters.OAuthNonce]!!}\", ")


        headerBuilder.append("${HttpAuthHeader.Parameters.OAuthSignature}=\"${urlEncodeString(authSig)}\", ")
        headerBuilder.append("${HttpAuthHeader.Parameters.OAuthSignatureMethod}=\"${authParameters[HttpAuthHeader.Parameters.OAuthSignatureMethod]!!}\", ")

        if(encodedCallback.isNotBlank()){
            headerBuilder.append("${HttpAuthHeader.Parameters.OAuthCallback}=\"${authParameters[HttpAuthHeader.Parameters.OAuthCallback]!!}\", ")
        }

       headerBuilder.append("${HttpAuthHeader.Parameters.OAuthTimestamp}=\"${authParameters[HttpAuthHeader.Parameters.OAuthTimestamp]!!}\", ")




        if(oauthToken.isNotBlank()){
            headerBuilder.append("${HttpAuthHeader.Parameters.OAuthToken}=\"${authParameters[HttpAuthHeader.Parameters.OAuthToken]!!}\", ")
        }

        headerBuilder.append("${HttpAuthHeader.Parameters.OAuthVersion}=\"${authParameters[HttpAuthHeader.Parameters.OAuthVersion]!!}\"")




        return headerBuilder.toString()
    }

    fun urlEncodeString(string: String):String{

        return URLEncoder.encode(string,"UTF-8")

    }

    companion object{

        val HEADER_OAUTH_CALLBACK="oauth_callback"
        val HEADER_OAUTH_CONSUMER_KEY="oauth_consumer_key"
        val HEADER_OAUTH_NONCE="oauth_nonce"
        val HEADER_OAUTH_SIGNATURE="oauth_signature"
        val HEADER_OAUTH_SIGNATURE_METHOD="oauth_signature_method"
        val HEADER_OAUTH_TIMESTAMP= "oauth_timestamp"
        val HEADER_OAUTH_VERSION= "oauth_version"

        val SIGNATURE_METHOD="HMAC-SHA1"






       val ee="OAuth oauth_callback=\"http%3A%2F%2Flocalhost%2Fsign-in-with-twitter%2F\",\n" +
               "              oauth_consumer_key=\"cChZNFj6T5R0TigYB9yd1w\",\n" +
               "              oauth_nonce=\"ea9ec8429b68d6b77cd5600adbbb0456\",\n" +
               "              oauth_signature=\"F1Li3tvehgcraF8DMJ7OyxO4w9Y%3D\",\n" +
               "              oauth_signature_method=\"HMAC-SHA1\",\n" +
               "              oauth_timestamp=\"1318467427\",\n" +
               "              oauth_version=\"1.0\""
    }
}