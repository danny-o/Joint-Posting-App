package com.digitalskies.postingapp.ui


import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.digitalskies.postingapp.*
import com.digitalskies.postingapp.api.RestClient
import com.digitalskies.postingapp.application.dataStore
import com.digitalskies.postingapp.models.*
import com.digitalskies.postingapp.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.collections.HashMap
import kotlin.collections.set

class MainActivityViewModel(private val postingApplication: Application):AndroidViewModel(postingApplication) {


    var linkedInAuthCode:String?=null

    var twitterOAuthToken:String?=null

    var twitterOAuthVerifier:String?=null

    val TWITTER_MAX_FILE_SIZE=40*1024;

    val linkedInUserIsSet: Flow<Boolean>
    get() = checkLinkedInLogin(application = postingApplication)

    val linkedInCredentialsExpired= checkLinkedCredentialsExpired(postingApplication)

    val twitterUserIsSet: Flow<Boolean>
        get() = checkTwitterLogin(application = postingApplication)

    var twitterResponse:MutableLiveData<TwitterResponse> = MutableLiveData()

    var linkedInResponse:MutableLiveData<LinkedInResponse> = MutableLiveData()

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

                    linkedInResponse.postValue(LinkedInResponse.ResponseSuccessful)
                } catch (e: Exception) {
                    linkedInResponse.postValue(LinkedInResponse.ResponseFailed(e.message))
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

suspend fun postOnTwitter(status: String = "Hello", mediaId: String? = null){

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



    twitterResponse.postValue(TwitterResponse.TwitterResponseSuccessful)
    twitterHeaderBuilder.parameters=parameters


            try {
            val header=twitterHeaderBuilder.buildString()
            Log.d(MainActivityViewModel::class.java.simpleName, header)

            RestClient.getTwitterApi().tweet(authorization = header, status = status, mediaId = mediaId)

            twitterResponse.postValue(TwitterResponse.TwitterResponseSuccessful)


        } catch (e: Exception) {
            e.printStackTrace()
            twitterResponse.postValue(TwitterResponse.TwitterResponseFailed(e.message))
        }







}

   suspend fun postOnTwitterWithMedia(status: String = "Hello", file: File, mediaType: String){

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

           Log.d(MainActivityViewModel::class.java.simpleName, "Media Id is $mediaId")





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

                    twitterResponse.postValue(TwitterResponse.TwitterResponseFailed(e.message))
                }


            }

        }
           else{




              Log.d(MainActivityViewModel::class.java.simpleName, "File within limit")

              Log.d(MainActivityViewModel::class.java.simpleName, "Appending file")



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

           twitterResponse.postValue(TwitterResponse.TwitterResponseFailed(e.message))
       }


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