package com.digitalskies.postingapp.utils



import android.util.Log
import io.ktor.http.auth.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Base64.getEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


internal class SignatureBuilder {

    companion object{
        const val HTTP_POST = "POST"
        const val HTTP_GET = "GET"

        const val AUTH_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token"

        const val POST_STATUS_URL = "https://api.twitter.com/1.1/statuses/update.json"

        const val UPLOAD_MEDIA_URL = "https://upload.twitter.com/1.1/media/upload.json"



        const val REDIRECT_URL = "https://www.twitter.com"
    }

    fun createSignature(params: SignatureParams): String{
        val signatureBuilder = StringBuilder()

        val combinedParams:MutableMap<String, String> = mutableMapOf()

        params.urlParams?.let {
            params.urlParams!!.forEach {
                combinedParams[it.key] = it.value.toString()
            }
        }

        params.authParams[HttpAuthHeader.Parameters.OAuthConsumerKey] = params.apiKey
        params.authParams[HttpAuthHeader.Parameters.OAuthSignatureMethod] = "HMAC-SHA1"
        params.authParams[HttpAuthHeader.Parameters.OAuthVersion] = "1.0"
        params.authParams[HttpAuthHeader.Parameters.OAuthTimestamp] = params.epochSeconds.toString()

        if(params.oAuthToken != null){
            params.authParams[HttpAuthHeader.Parameters.OAuthToken] = params.oAuthToken
        }

        params.authParams[HttpAuthHeader.Parameters.OAuthNonce] = params.nonce

        val signingKey = params.apiSecret

        signatureBuilder.append(params.httpMethod)
        signatureBuilder.append("&")
        signatureBuilder.append(urlEncodeString(params.url.toString()))
        signatureBuilder.append("&")
        if(params.callbackUrl != null && params.callbackUrl.isNotBlank()){
            params.authParams[HttpAuthHeader.Parameters.OAuthCallback] = params.callbackUrl
        }

        params.authParams.forEach {
            combinedParams[it.key] = it.value
        }
        var urlParams=""
        val sortedParams = combinedParams.entries.sortedBy{ it.key }
        sortedParams.forEachIndexed { index, mutableEntry ->
            val param = if(mutableEntry.key == HttpAuthHeader.Parameters.OAuthCallback){
                "${mutableEntry.key}=${mutableEntry.value}"
            }
           /* else if(mutableEntry.key=="status"){
                urlParams="${mutableEntry.key}%3D${mutableEntry.value.replace(" ","%2520")}"
                ""
            }*/

            else{
                "${mutableEntry.key}=${urlEncodeString(mutableEntry.value)}"
            }
            signatureBuilder.append(urlEncodeString(param))
            if(index < combinedParams.size-1){
                signatureBuilder.append(urlEncodeString("&"))
            }

        }

        //special handling of encoding of spaces
        val signature=signatureBuilder.toString().replace("%2B","%2520")

        signatureBuilder.append(urlParams)
        Log.d(SignatureBuilder::class.java.simpleName,"signature builder is ${signature}")

        return generateHmacSha1Signature(signingKey, signature.trim())
    }
    fun createSignatureWithoutParams(params: SignatureParams): String{
        val signatureBuilder = StringBuilder()

        val combinedParams:MutableMap<String, String> = mutableMapOf()


        params.authParams[HttpAuthHeader.Parameters.OAuthConsumerKey] = params.apiKey
        params.authParams[HttpAuthHeader.Parameters.OAuthSignatureMethod] = "HMAC-SHA1"
        params.authParams[HttpAuthHeader.Parameters.OAuthVersion] = "1.0"
        //params.authParams[HttpAuthHeader.Parameters.OAuthTimestamp] = params.epochSeconds.toString()

        if(params.oAuthToken != null){
            params.authParams[HttpAuthHeader.Parameters.OAuthToken] = params.oAuthToken
        }

        //params.authParams[HttpAuthHeader.Parameters.OAuthNonce] = params.nonce

        val signingKey = params.apiSecret


        params.authParams.forEach {
            combinedParams[it.key] = it.value
        }

        val sortedParams = combinedParams.entries.sortedBy{ it.key }
        sortedParams.forEachIndexed { index, mutableEntry ->
            val param = "${mutableEntry.key}=${urlEncodeString(mutableEntry.value)}"

            signatureBuilder.append(urlEncodeString(param))
            if(index < combinedParams.size-1){
                signatureBuilder.append(urlEncodeString("&"))
            }
        }

        Log.d(SignatureBuilder::class.java.simpleName,"signature builder is ${signatureBuilder}")

        return generateHmacSha1Signature(signingKey, signatureBuilder.toString())
    }

    fun generateNonce(): String{
        val epoch = epochSeconds()
        return (epoch + getRandomInteger()).toString()
    }

    fun generateTimeStamp(): Long{
        return epochSeconds()
    }

    fun epochMillis() = System.currentTimeMillis()

    fun epochSeconds() = System.currentTimeMillis() / 1000

    fun urlEncodeString(stringToEncode: String): String{
        return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8.toString())
    }

    fun getRandomInteger() = Random().nextInt()

    fun generateHmacSha1Signature(key: String, value: String): String{
        val type = "HmacSHA1"
        val spec = SecretKeySpec(key.toByteArray(), type)
        val mac = Mac.getInstance(type)
        mac.init(spec)
        val bytes = mac.doFinal(value.toByteArray())
        return getEncoder().encodeToString(bytes).replace("\r\n", "")
    }
}