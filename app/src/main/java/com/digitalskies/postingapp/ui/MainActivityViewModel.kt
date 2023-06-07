package com.digitalskies.postingapp.ui


import android.app.Application
import android.os.Environment
import android.util.Base64

import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.digitalskies.postingapp.*
import com.digitalskies.postingapp.api.RestClient
import com.digitalskies.postingapp.application.dataStore
import com.digitalskies.postingapp.models.*
import com.digitalskies.postingapp.utils.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64.getEncoder;
import kotlin.collections.set
import kotlin.random.Random

class MainActivityViewModel(private val postingApplication: Application):AndroidViewModel(postingApplication) {


    var linkedInAuthCode:String?=null

    var twitterOAuthToken:String?=null

    var twitterOAuthVerifier:String?=null

    val TWITTER_MAX_FILE_SIZE=100*1024;

    val linkedInUserIsSet: Flow<Boolean>
    get() = checkLinkedInLogin(application = postingApplication)

    val linkedInCredentialsExpired= checkLinkedCredentialsExpired(postingApplication)

    val twitterUserIsSet: Flow<Boolean>
        get() = checkTwitterLogin(application = postingApplication)

    var twitterResponse:MutableLiveData<Event<TwitterResponse>> = MutableLiveData()

    var linkedInResponse:MutableLiveData<Event<LinkedInResponse>> = MutableLiveData()

    var loginResponse:MutableLiveData<Event<LoginResponse>> = MutableLiveData()

    var postingComplete:MutableLiveData<Event<Boolean>> = MutableLiveData()



    fun setUpLinkedInUser(){

       val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)

       val clientId =sharedPreferences.getString(LINKEDIN_CLIENT_ID, "")
       val clientSecret =sharedPreferences.getString(LINKEDIN_CLIENT_SECRET, "")
       viewModelScope.launch(Dispatchers.IO) {

           try {
               val accessTokenResponse= RestClient.getPostingApi().getLinkedInAccessToken(code = linkedInAuthCode, clientId = clientId, clientSecret = clientSecret)

               val linkedInProfile=RestClient.getLinkedInProfileApi().getLinkedInUser("$BEARER ${accessTokenResponse.accessToken}")

                val userEmail= RestClient.getLinkedInProfileApi().getLinkedInEmail("$BEARER ${accessTokenResponse.accessToken}")
                                .elements[0].emailHandle.email



               sharedPreferences.edit().putString(LINKEDIN_EMAIL,userEmail).apply()

               sharedPreferences.edit().putString(LINKEDIN_ID,linkedInProfile.linkedInId).apply()

               sharedPreferences.edit().putString(LINKEDIN_ACCESS_TOKEN,accessTokenResponse.accessToken).apply()


               sharedPreferences.edit().putString(LINKEDIN_USER_NAME,"${linkedInProfile.firstName} ${linkedInProfile.lastName}").apply()

               sharedPreferences.edit().putLong(LINKEDIN_ACCESS_TOKEN_EXPIRY_TIME,(System.currentTimeMillis()/1000)+accessTokenResponse.expiryTime).apply()


               postingApplication.dataStore.edit {preferences->



                   preferences[booleanPreferencesKey(LINKEDIN_USER_SET)]=true

                   loginResponse.postValue(Event<LoginResponse>(LoginResponse.LoginSuccessful))
               }
           } catch (exception: Exception) {

               loginResponse.postValue(Event<LoginResponse>(LoginResponse.LoginFailed(exception.message)))
           }


       }


    }
    suspend fun postOnLinkedIn(post: String, articleUrl: String? = null, imageUrl: String? = null){
            val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)



            val accessToken=sharedPreferences.getString(LINKEDIN_ACCESS_TOKEN,"")

            val linkedId=sharedPreferences.getString(LINKEDIN_ID,"")




            val shareCommentary=ShareCommentary(post)

            var shareContent:ShareContent= ShareContent(shareCommentary, NONE)

            if(articleUrl!=null){

                val media=Media(status=READY,originalUrl = articleUrl)

                shareContent=ShareContent(shareCommentary, ARTICLE, listOf(media))

            }

            val specificContent=SpecificContent(shareContent)



            val visibility=Visibility("PUBLIC")


            val linkedInPost=LinkedInPost(URN_PERSON + linkedId.toString(), PUBLISHED, specificContent, visibility)

                try {
                    RestClient.getLinkedInProfileApi().postOnLinkedIn(authorization = "$BEARER $accessToken", linkedInPost)

                    linkedInResponse.postValue(Event(LinkedInResponse.ResponseSuccessful))
                } catch (e: Exception) {
                    linkedInResponse.postValue(Event(LinkedInResponse.ResponseFailed(e.message)))
                }




    }


 suspend fun getTwitterAuthToken():TokenResponse{
     val apiKey=PreferenceManager.getDefaultSharedPreferences(postingApplication).getString(
             TWITTER_API_KEY, "")
     val apiSecret=PreferenceManager.getDefaultSharedPreferences(postingApplication).getString(
             TWITTER_CLIENT_SECRET, "")

     val twitterHeaderBuilder=TwitterHeaderBuilder(apiKey.toString(), apiSecret.toString(), null, null)

     twitterHeaderBuilder.callbackUrl=SignatureBuilder.REDIRECT_URL
     twitterHeaderBuilder.url=SignatureBuilder.AUTH_REQUEST_TOKEN_URL


     val header=twitterHeaderBuilder.buildString().toString()

     val response=RestClient.getTwitterApi().requestTwitterToken(authorization = header)

     return parseTokenResponse(response)


 }

    fun setUpTwitterUser() {

        viewModelScope.launch(Dispatchers.IO) {

            try {
                val responseString = RestClient.getTwitterApi().getAccessToken(twitterOAuthToken.toString(), twitterOAuthVerifier.toString())

                val accessTokenResponse= parseAccessTokenResponse(responseString)


                val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)

                sharedPreferences.edit().putString(TWITTER_OAUTH_TOKEN,accessTokenResponse.oauthToken.toString()).apply()

                sharedPreferences.edit().putString(TWITTER_OAUTH_TOKEN_SECRET,accessTokenResponse.oauthTokenSecret).apply()

                sharedPreferences.edit().putString(TWITTER_SCREEN_NAME,accessTokenResponse.screenName).apply()




                postingApplication.dataStore.edit {prefs->


                    prefs[booleanPreferencesKey(TWITTER_USER_SET)]=true

                }


               loginResponse.postValue(Event(LoginResponse.LoginSuccessful))
            } catch (e: Exception) {

                loginResponse.postValue(Event<LoginResponse>(LoginResponse.LoginFailed(e.message)))




            }


        }

    }

suspend fun postOnTwitter(status: String, mediaId: String? = null,inReplyTo:String?=null){

    val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)
    val secret=sharedPreferences.getString(TWITTER_CLIENT_SECRET, "")



    val key=sharedPreferences.getString(TWITTER_API_KEY, "")




    val token=sharedPreferences.getString(TWITTER_OAUTH_TOKEN,"")

    val tokenSecret=sharedPreferences.getString(TWITTER_OAUTH_TOKEN_SECRET,"")



    val twitterHeaderBuilder=TwitterHeaderBuilder(key.toString(), secret.toString(), token, tokenSecret.toString())
    twitterHeaderBuilder.callbackUrl=null
    twitterHeaderBuilder.httpMethod=SignatureBuilder.HTTP_POST
    twitterHeaderBuilder.url=SignatureBuilder.POST_STATUS_URL
    twitterHeaderBuilder.isAppendingMedia=false

   val parameters=HashMap<String, String>()

    parameters["status"]=status


    Log.d(MainActivityViewModel::class.java.simpleName,"status is ${parameters["status"]}")

    mediaId?.let{


        parameters["media_ids"]= it

    }


    twitterHeaderBuilder.parameters=parameters

            try {
            val header=twitterHeaderBuilder.buildString()
            Log.d(MainActivityViewModel::class.java.simpleName, header)

            RestClient.getTwitterApi().tweet(authorization = header, status = status, mediaId = mediaId,inReplyTo = inReplyTo)

            twitterResponse.postValue(Event(TwitterResponse.TwitterResponseSuccessful))


        } catch (e: Exception) {
            e.printStackTrace()
            twitterResponse.postValue(Event(TwitterResponse.TwitterResponseFailed(e.message)))
        }







}

   suspend fun postOnTwitterWithMedia(status: String = "", file: File, mediaType: String){

        val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)
        val secret=sharedPreferences.getString(TWITTER_CLIENT_SECRET, "")

        val key=sharedPreferences.getString(TWITTER_API_KEY, "")



       val token=sharedPreferences.getString(TWITTER_OAUTH_TOKEN,"")

       val tokenSecret=sharedPreferences.getString(TWITTER_OAUTH_TOKEN_SECRET,"")



        var twitterHeaderBuilder=TwitterHeaderBuilder(key.toString(), secret.toString(), token, tokenSecret.toString())
       twitterHeaderBuilder.isAppendingMedia=false
        twitterHeaderBuilder.callbackUrl=null
        twitterHeaderBuilder.httpMethod=SignatureBuilder.HTTP_POST
        twitterHeaderBuilder.url=SignatureBuilder.UPLOAD_MEDIA_URL

        var parameters=HashMap<String, String>()


        parameters["command"]="INIT"
        parameters["total_bytes"]= file.length().toString()
        parameters["media_type"]=mediaType

        twitterHeaderBuilder.parameters=parameters



       try {
           var header=twitterHeaderBuilder.buildString()

           val mediaIdResponse=RestClient.getTwitterApi().initiateMediaUpload(mediaType = mediaType, totalBytes = file.length().toString(), authorization = header)


           val mediaId= mediaIdResponse.mediaId


           val ifExceedsMAxFileSize= file.length()>TWITTER_MAX_FILE_SIZE

           twitterHeaderBuilder=TwitterHeaderBuilder(key.toString(), secret.toString(), token, tokenSecret.toString())

           twitterHeaderBuilder.httpMethod=SignatureBuilder.HTTP_POST
           twitterHeaderBuilder.url=SignatureBuilder.UPLOAD_MEDIA_URL
           twitterHeaderBuilder.callbackUrl=null



          if(ifExceedsMAxFileSize){
            withContext(Dispatchers.IO){
                try {
                    var chunkId=0
                    var bytesRead:Int
                    BufferedInputStream(FileInputStream(file)).use { inputStream ->

                        var buffer = ByteArray(TWITTER_MAX_FILE_SIZE)
                        while (inputStream.read(buffer).also { bytesRead = it } > 0) {

                            if(bytesRead<TWITTER_MAX_FILE_SIZE){
                                var lastBuffer = ByteArray(bytesRead);
                                System.arraycopy(buffer, 0, lastBuffer, 0, bytesRead);
                                buffer = ByteArray(bytesRead);
                                buffer = lastBuffer;

                            }

                            val base64= Base64.encodeToString(buffer,0)

                            parameters=HashMap<String, String>()
                            parameters["command"]="APPEND"
                            parameters["media_id"]= mediaId
                            parameters["segment_index"]=chunkId.toString()
                            parameters["media_data"]= base64



                            twitterHeaderBuilder.parameters=parameters
                            twitterHeaderBuilder.isAppendingMedia=false
                            header=twitterHeaderBuilder.buildString()

                            Log.d(MainActivityViewModel::class.java.simpleName, "Appending $chunkId segment")
                            RestClient.getTwitterApi().appendMedia(command = "APPEND", mediaId = mediaId, file = base64, segmentIndex = chunkId.toString(), authorization = header)
                            chunkId+=1
                        }
                    }
                } catch (e: Exception) {

                    twitterResponse.postValue(Event(TwitterResponse.TwitterResponseFailed(e.message)))
                }


            }

        }
           else{




              val base64:String= withContext(Dispatchers.IO){

                  val fileInputStream=FileInputStream(file)
                  val bytes=ByteArray(file.length().toInt())
                  fileInputStream.read(bytes)
                 Base64.encodeToString(bytes, Base64.DEFAULT)
              }




              parameters=HashMap<String, String>()
              parameters["command"]="APPEND"
              parameters["media_id"]= mediaId
              parameters["segment_index"]="0"
              parameters["media_data"]= base64



              twitterHeaderBuilder.parameters=parameters
              twitterHeaderBuilder.isAppendingMedia=false


              header=twitterHeaderBuilder.buildString()
              RestClient.getTwitterApi().appendMedia(command = "APPEND", mediaId = mediaId, file = base64, segmentIndex = "0", authorization = header)

          }

           twitterHeaderBuilder.isAppendingMedia=false
           parameters= HashMap<String, String>()
           parameters["command"]="FINALIZE"
           parameters["media_id"]= mediaId

           twitterHeaderBuilder.parameters=parameters
           header=twitterHeaderBuilder.buildString()



           val mediaUploadStatus=RestClient.getTwitterApi().finalizeMediaUpload(mediaId = mediaId, authorization = header)
           if(mediaUploadStatus.processingInfo==null){

                    postOnTwitter(status, mediaUploadStatus.mediaID)
           }

       }

       catch (e: Exception){

           e.printStackTrace()

           twitterResponse.postValue(Event(TwitterResponse.TwitterResponseFailed(e.message)))
       }


    }



   fun getUser(userName:String){

        val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)
        val secret=sharedPreferences.getString(TWITTER_CLIENT_SECRET, "")

        val key=sharedPreferences.getString(TWITTER_API_KEY, "")

        val encodedSecret=urlEncodeString(secret.toString())

        val encodedKey=urlEncodeString(key.toString())



        val b64EncodedSecretAndKey= getEncoder().encodeToString("$encodedKey:$encodedSecret".toByteArray())





        viewModelScope.launch {


            try {
                val bearerToken=RestClient
                    .getTwitterApi()
                    .getTwitterBearerToken("${RestClient.BASIC} $b64EncodedSecretAndKey")

                val user=RestClient.getTwitterApi().getUser("${RestClient.BEARER}${bearerToken.accessToken}",userName)

                val userMentions=RestClient.getTwitterApi().getUserMentions("${RestClient.BEARER}${bearerToken.accessToken}",user.data.id)

                if(userMentions.data[0].referencedTweets[0].type=="replied_to"){
                    // postOnTwitter("@$user",inReplyTo = userMentions.data[0].referencedTweets[0].id)
                    val tweetId=  userMentions.data[0].referencedTweets[0].id
                    lookupTweet()

                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }

        }





    }

    fun lookupTweet(){


        val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(postingApplication)
        val secret=sharedPreferences.getString(TWITTER_CLIENT_SECRET, "")

        val key=sharedPreferences.getString(TWITTER_API_KEY, "")

        val encodedSecret=urlEncodeString(secret.toString())

        val encodedKey=urlEncodeString(key.toString())



        val b64EncodedSecretAndKey= getEncoder().encodeToString("$encodedKey:$encodedSecret".toByteArray())





        viewModelScope.launch {


            try {
                val bearerToken=RestClient
                    .getTwitterApi()
                    .getTwitterBearerToken("${RestClient.BASIC} $b64EncodedSecretAndKey")

                getLatestTweets2(bearerToken)

                //val user=RestClient.getTwitterApi().getUser("${RestClient.BEARER}${bearerToken.accessToken}",userName)

                //val query=""

               //val userMentions=RestClient.getTwitterApi().getUserMentions(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", userId = user.data.id)

                //RestClient.getTwitterApi().getTrends(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", id = "1528488")


               /* if(userMentions.data[0].referencedTweets[0].type=="replied_to"){
                    // postOnTwitter("@$user",inReplyTo = userMentions.data[0].referencedTweets[0].id)

                    val repliedToTweetId=  userMentions.data[0].referencedTweets[0].id

                    val repliedToUserName=userMentions.data[0].entities.mentions[0].userName



                    val tweet=RestClient.getTwitterApi().lookupTweet("${RestClient.BEARER}${bearerToken.accessToken}",repliedToTweetId )


                    Log.d(MainActivityViewModel::class.simpleName,"like count ${tweet.data.publicMetrics}")

                    val videoLink="https://twitter.com/$repliedToUserName/status/$repliedToTweetId/video/1"


                    if(tweet.data.attachments!=null &&
                        tweet.data.publicMetrics.likeCount.toInt()>100
                        && tweet.data.language=="en"
                    ){

                        val videoLink="https://twitter.com/$repliedToUserName/status/$repliedToTweetId/video/1"

                        postOnTwitter(status = videoLink)
                    }


                }*/
            }
            catch (e:Exception){
                e.printStackTrace()
            }

        }


    }
    suspend fun getLatestTweets2(bearerToken:AccessTokenResponse){

        val twitterAPi= RestClient.getTwitterApi()

        val searchedTweets=twitterAPi.searchTweets(
            authorization = "${RestClient.BEARER}${bearerToken.accessToken}",
            query = "\uD83D\uDE02 OR \uD83D\uDE32 OR \uD83E\uDD23 filter:native_video"
        )


        var selectedTweet: Statuses?=null


        searchedTweets.statuses.shuffle()


        for(tweet in searchedTweets.statuses){

            Log.d(this::class.simpleName,"tweet is ${tweet.text}")

            Log.d(this::class.simpleName,"tweet Rt: ${tweet.text?.substring(0,3)}")

            Log.d(this::class.simpleName,"tweet lang is ${tweet.lang}")
            if (tweet.text?.substring(0,3)?.trim()=="RT") {
                Log.d(this::class.simpleName,"tweet Rt")
                if(tweet.lang=="en"||tweet.lang=="sw"){

                    selectedTweet= tweet
                    break
                }

            }

        }
        if(selectedTweet==null){
            Log.d(this::class.simpleName,"no tweet found")

            return
        }


        val addTrends= java.util.Random().nextBoolean()

        //val userName=selectedTweet.entities.userMentions[0].screenName

        var videoLink:String?=null

        if(selectedTweet.entities?.media?.isNotEmpty() == true){

            videoLink=selectedTweet.entities?.media?.get(0)?.expandedUrl?:return
        }
        else{
            return
        }





        //postOnTwitter(videoLink)

        if(addTrends){
            val trends=twitterAPi.getTrends(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", id = "1528488")

            val topics="${trends[0].trends[0].name} ${trends[0].trends[1].name} ${trends[0].trends[2].name} ${trends[0].trends[3].name} ${trends[0].trends[4].name}"

            val text="Follow us for an hourly dose of awesome videos $topics $videoLink"

            postOnTwitter(text)

        }
        else{
            val useEmoji= java.util.Random().nextBoolean()
            if(useEmoji){
                val emoji=when{
                    selectedTweet.text?.contains("\uD83D\uDE02")==true ->"\uD83D\uDE02"

                    selectedTweet.text?.contains("\uD83D\uDE32")==true ->"\uD83D\uDE32"

                    selectedTweet.text?.contains("\uD83E\uDD23")==true ->"\uD83E\uDD23"

                    else->  "\uD83D\uDE02"
                }
                postOnTwitter("$emoji $videoLink")
            }
            else{
                postOnTwitter(videoLink)
            }

        }


    }
    suspend fun getLatestTweets(bearerToken:AccessTokenResponse){

        val twitterAPi= RestClient.getTwitterApi()

        val searchedTweets=twitterAPi.searchTweets(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", query = "\uD83D\uDE02 OR \uD83D\uDE32 OR \uD83E\uDD23 filter:native_video")


        var selectedTweet:Statuses?=null


        for(tweet in searchedTweets.statuses){

            Log.d(this::class.simpleName,"tweet is ${tweet.text}")

            Log.d(this::class.simpleName,"tweet Rt: ${tweet.text?.substring(0,3)}")
          if (tweet.text?.substring(0,3)?.trim()=="RT") {
              Log.d(this::class.simpleName,"tweet Rt")
             selectedTweet= tweet
              break
          }

        }
        if(selectedTweet==null){
            Log.d(this::class.simpleName,"no tweet found")
            return
        }


        val addTrends=(1..4).random()

       //val userName=selectedTweet.entities.userMentions[0].screenName
        val videoLink=selectedTweet.entities?.media?.get(0)?.expandedUrl?:return



        //postOnTwitter(videoLink)

        if(addTrends>3){
            val trends=twitterAPi.getTrends(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", id = "1528488")

            val topics="${trends[0].trends[0].name} ${trends[0].trends[1].name} ${trends[0].trends[2].name} ${trends[0].trends[3].name} ${trends[0].trends[4].name}"

            val text="Follow us for an hourly dose of awesome videos $topics $videoLink"

            postOnTwitter(text)

        }
        else{
            postOnTwitter(videoLink)
        }


    }

    fun urlEncodeString(stringToEncode: String): String{
        return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8.toString())
    }


    sealed class LoginResponse(){

        object LoginSuccessful : LoginResponse()



        data class LoginFailed(var errorMessage: String?):LoginResponse()
    }




    sealed class TwitterResponse{

        object TwitterResponseSuccessful : TwitterResponse()

        data class TwitterResponseFailed(var errorMessage: String?):TwitterResponse()
    }

    sealed class LinkedInResponse{

        object ResponseSuccessful : LinkedInResponse()

        data class ResponseFailed(var errorMessage: String?):LinkedInResponse()
    }
    companion object{

        const val BEARER="Bearer"

        const val READY="READY"

        const val PUBLISHED:String="PUBLISHED"

        const val NONE:String="NONE"

        const val ARTICLE="ARTICLE"

        const val URN_PERSON = "urn:li:person:"






    }


}