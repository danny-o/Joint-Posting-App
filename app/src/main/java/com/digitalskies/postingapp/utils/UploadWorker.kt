package com.digitalskies.postingapp.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import androidx.work.*
import com.digitalskies.postingapp.*
import com.digitalskies.postingapp.api.RestClient
import com.digitalskies.postingapp.application.dataStore
import com.digitalskies.postingapp.models.AccessTokenResponse
import com.digitalskies.postingapp.models.Statuses
import com.digitalskies.postingapp.ui.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class UploadWorker(val context: Context,workerParameters: WorkerParameters) :CoroutineWorker(context,workerParameters) {

     var file: File?=null



    override suspend fun doWork(): Result {

        val sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val secret=sharedPreferences.getString(TWITTER_CLIENT_SECRET, "")

        val key=sharedPreferences.getString(TWITTER_API_KEY, "")

        val encodedSecret=urlEncodeString(secret.toString())

        val encodedKey=urlEncodeString(key.toString())



        val b64EncodedSecretAndKey= Base64.getEncoder().encodeToString("$encodedKey:$encodedSecret".toByteArray())

        var logFolder:File?=null

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            logFolder=File(Environment.getExternalStorageDirectory(),"/PostingLogs")

            if(!logFolder.exists()){

                logFolder.mkdirs()
            }



        }


        try {
            val bearerToken= RestClient
                .getTwitterApi()
                .getTwitterBearerToken("${RestClient.BASIC} $b64EncodedSecretAndKey")

            val random=(1..5).random()




            if(random<5){
                getLatestTweets(bearerToken,logFolder)
            }
            else{
                val userName= listOf("ddl_vid1","GetVideoBot","SendVidBot","Get_This_V").random()
                getUserMentions(bearerToken,userName,logFolder)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
            Log.e(this::class.simpleName,e.stackTraceToString())
           if(logFolder!=null){

               val date=Date()

               val dateString=SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)

               file=File(logFolder,"$dateString.txt")

               if(file?.exists()==false){
                  file?.createNewFile()
               }
           }
            file?.let{
                withContext(Dispatchers.IO){


                    val writer = BufferedWriter(FileWriter(file?.absolutePath, true))

                    writer.append("Error uploading  at ${Date()} ${e.stackTraceToString()} ${e.localizedMessage}\n")

                    writer.close()
                }
            }


        }
        val postingMode=context.dataStore.data.map { it[intPreferencesKey("posting_mode")] }
            .first()

        if(postingMode== POSTING_MODE_ONETIME){
            reschedulePosting(context)
        }

        return Result.success()


    }

    private suspend fun reschedulePosting(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val interval=context.dataStore.data.map { it[stringPreferencesKey("posting_interval")] }
            .first()

        val notificationRequest: WorkRequest=OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setInitialDelay(Duration.ofMinutes(interval?.toLong()?:120))
            .setConstraints(constraints)
            .build()


        WorkManager.getInstance(context).enqueue(notificationRequest)
    }


    fun urlEncodeString(stringToEncode: String): String{
        return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8.toString())
    }

    suspend fun getLatestTweets(bearerToken: AccessTokenResponse, logFolder: File?){

        val twitterAPi= RestClient.getTwitterApi()

        val query=applicationContext.dataStore.data.map { it[stringPreferencesKey("tweets_query")] }.first()?:"\uD83D\uDE02,\uD83D\uDE32,\uD83E\uDD23"

        val resultType=applicationContext.dataStore.data.map { it[stringPreferencesKey("result_type")] }.first()?:"latest"

        val fullQuery="${query.replace(","," OR ")} filter:native_video"

        val searchedTweets=twitterAPi.searchTweets(
            authorization = "${RestClient.BEARER}${bearerToken.accessToken}",
            query = fullQuery,
            resultType = resultType
        )

        searchedTweets.statuses.shuffle()

        var selectedTweet: Statuses?=null


        if(resultType=="latest"||resultType=="mixed"){
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
        }
        else{
            for(tweet in searchedTweets.statuses){

                Log.d(this::class.simpleName,"tweet is ${tweet.text}")

                Log.d(this::class.simpleName,"tweet Rt: ${tweet.text?.substring(0,3)}")

                Log.d(this::class.simpleName,"tweet lang: ${tweet.lang}")

                if(tweet.lang=="en"||tweet.lang=="sw"){
                    selectedTweet= tweet
                    break
                }

            }
        }


        if(selectedTweet==null){

            Log.d(this::class.simpleName,"tweet is null")

            if(logFolder!=null){

                val date=Date()

                val dateString=SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)

                file=File(logFolder,"$dateString.txt")

                if(file?.exists()==false){
                    file?.createNewFile()
                }
            }
            file?.let{
                withContext(Dispatchers.IO){


                    val writer = BufferedWriter(FileWriter(file?.absolutePath, true))

                    writer.append("No upload, no tweet found \n")

                    writer.close()
                }
            }
            return
        }


        val addTrends=Random().nextBoolean()

        //val userName=selectedTweet.entities.userMentions[0].screenName
        val videoLink=selectedTweet.entities?.media?.get(0)?.expandedUrl?:return


        val useEmoji=Random().nextBoolean()

        var emoji=""

        if(useEmoji){
            emoji=when{
                selectedTweet.text?.contains("\uD83D\uDE02")==true ->"\uD83D\uDE02"

                selectedTweet.text?.contains("\uD83D\uDE32")==true ->"\uD83D\uDE32"

                selectedTweet.text?.contains("\uD83E\uDD23")==true ->"\uD83E\uDD23"

                else->  "\uD83D\uDE02"
            }
        }

        if(addTrends){
            val trends=twitterAPi.getTrends(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", id = "1528488")

            val topics="${trends[0].trends[0].name} ${trends[0].trends[1].name} ${trends[0].trends[2].name} ${trends[0].trends[3].name} ${trends[0].trends[4].name}"

            val text="$emoji \n\n" +
                    " $topics $videoLink"

            postOnTwitter(text)

        }
        else{

            postOnTwitter("$emoji $videoLink")

        }


    }

    suspend fun getUserMentions(
        bearerToken: AccessTokenResponse,
        userName: String,
        logFolder: File?
    ){

        val user=RestClient.getTwitterApi().getUser("${RestClient.BEARER}${bearerToken.accessToken}",userName)


        val userMentions=RestClient.getTwitterApi().getUserMentions(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", userId = user.data.id)

        RestClient.getTwitterApi().getTrends(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", id = "1528488")

         if(userMentions.data[0].referencedTweets[0].type=="replied_to"){
                    // postOnTwitter("@$user",inReplyTo = userMentions.data[0].referencedTweets[0].id)

                    val repliedToTweetId=  userMentions.data[0].referencedTweets[0].id

                    val repliedToUserName=userMentions.data[0].entities.mentions[0].userName

                    val twitterAPi=RestClient.getTwitterApi()



                    val tweet=twitterAPi.lookupTweet("${RestClient.BEARER}${bearerToken.accessToken}",repliedToTweetId )


                    Log.d(MainActivityViewModel::class.simpleName,"like count ${tweet.data.publicMetrics}")



                    if(tweet.data.attachments!=null &&
                        tweet.data.publicMetrics.likeCount.toInt()>100
                        && tweet.data.language=="en"
                    ){

                        val addTrends=Random().nextBoolean()

                        val videoLink="https://twitter.com/$repliedToUserName/status/$repliedToTweetId/video/1"

                        val useEmoji=Random().nextBoolean()

                        var emoji=""

                        if(useEmoji){
                            emoji=when{
                                tweet.data.text?.contains("\uD83D\uDE02")==true ->"\uD83D\uDE02"

                                tweet.data.text?.contains("\uD83D\uDE32")==true ->"\uD83D\uDE32"

                                tweet.data.text?.contains("\uD83E\uDD23")==true ->"\uD83E\uDD23"

                                else->  ""
                            }
                        }


                        if(addTrends){
                            val trends=twitterAPi.getTrends(authorization = "${RestClient.BEARER}${bearerToken.accessToken}", id = "1528488")

                            val topics="${trends[0].trends[0].name} ${trends[0].trends[1].name} ${trends[0].trends[2].name} ${trends[0].trends[3].name} ${trends[0].trends[4].name}"

                            val text="$emoji \n\n $topics $videoLink"

                            postOnTwitter(text)

                        }
                        else{
                            postOnTwitter("$emoji $videoLink")
                        }


                    }
                    else{

                        if(logFolder!=null){

                            val date=Date()

                            val dateString=SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)

                            file=File(logFolder,"$dateString.txt")

                            if(file?.exists()==false){
                                file?.createNewFile()
                            }
                        }
                        file?.let{
                            withContext(Dispatchers.IO){


                                val writer = BufferedWriter(FileWriter(file?.absolutePath, true))

                                writer.append("No tweet posted attachements null: ${tweet.data.attachments==null}," +
                                        " like count: ${tweet.data.publicMetrics.likeCount.toInt()} language: ${tweet.data.language}   \n")


                                writer.close()
                            }
                        }
                    }



                }

    }

    suspend fun postOnTwitter(status: String, mediaId: String? = null,inReplyTo:String?=null){

        val sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
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




        } catch (e: Exception) {
            e.printStackTrace()

        }

    }
}
