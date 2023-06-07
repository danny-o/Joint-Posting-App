package com.digitalskies.postingapp.api

import com.digitalskies.postingapp.models.*
import retrofit2.Response
import retrofit2.http.*

interface TwitterApi {


    @FormUrlEncoded
    @POST("oauth/request_token")
    suspend fun requestTwitterToken(@Field("oauth_callback") redirectUrl:String="https://www.twitter.com",
            @Header(RestClient.TWITTER_AUTHORIZATION)authorization:String): String

    @FormUrlEncoded
    @POST("oauth/access_token")
    suspend fun getAccessToken(@Field("oauth_token") oauthToken:String,
                               @Field("oauth_verifier")oauth_verifier:String):String



    @FormUrlEncoded
    @POST("/1.1/statuses/{update}")
    suspend fun tweet(@Path("update")path:String="update.json",
                      @Field("status") status:String,
                      @Field("in_reply_to_status_id")inReplyTo:String?=null,
                      @Field("media_ids") mediaId:String?=null,
                      @Header(RestClient.TWITTER_AUTHORIZATION)authorization:String,
                      @Header("Accept")string: String="Application/json")

    @FormUrlEncoded
    @POST
    suspend fun initiateMediaUpload(@Url url:String="https://upload.twitter.com/1.1/media/upload.json",
                                    @Field("command")command:String="INIT",
                                    @Field("media_type")mediaType:String,
                                    @Field("total_bytes")totalBytes:String,
                                    @Header(RestClient.TWITTER_AUTHORIZATION)authorization:String):MediaIdResponse



    @Headers("Accept:application/octet-stream")
    @FormUrlEncoded
    @POST
    suspend fun appendMedia(@Url url:String="https://upload.twitter.com/1.1/media/upload.json",
                            @Field("command")command:String,
                            @Field("media_id")mediaId:String,
                            @Field("media_data") file:String,
                            @Field("segment_index")segmentIndex:String,
                            @Header(RestClient.TWITTER_AUTHORIZATION,
                           )authorization:String,
                            @Header("connection_timeout")connectionTimeout:Int=1): Response<Unit>

    @FormUrlEncoded
    @POST
    suspend fun appendMediaInChunks(@Url url:String="https://upload.twitter.com/1.1/media/upload.json",
                                    @Field("command")command:String,
                                    @Field("media_id")mediaId:String,
                                    @Field("media_data") file:String,
                                    @Field("segment_index")segmentIndex:String,
                                    @Header(RestClient.TWITTER_AUTHORIZATION)authorization:String):Response<Unit>

    @FormUrlEncoded
    @POST
    suspend fun finalizeMediaUpload(@Url url:String="https://upload.twitter.com/1.1/media/upload.json",
                                    @Field("command")command:String="FINALIZE",
                                    @Field("media_id")mediaId:String,
                                    @Header(RestClient.TWITTER_AUTHORIZATION)authorization:String):MediaUploadStatus




    @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
    @POST("oauth2/token")
    suspend fun getTwitterBearerToken(@Header(RestClient.AUTHORIZATION)authorization:String,@Body() body:String="grant_type=client_credentials"): AccessTokenResponse



    @GET("/2/users/by/username/{user_name}")
    suspend fun getUser(@Header(RestClient.AUTHORIZATION)authorization:String,@Path("user_name")userName:String): UserModel

    @GET("/2/users/{id}/mentions")
    suspend fun getUserMentions(@Header(RestClient.AUTHORIZATION)authorization:String,
                                @Path("id")userId:String,
                                @Query("expansions")expansions:String= "referenced_tweets.id,geo.place_id,entities.mentions.username",
                                @Query("place.fields")place:String="country,contained_within,country_code,full_name,geo,id,name,place_type",
                                @Query("user.fields")userFields:String="entities"
    ): UserMentionsModel

    @GET("/2/tweets/{id}")
    suspend fun lookupTweet(@Header(RestClient.AUTHORIZATION)authorization:String,
                            @Path("id")tweetId:String,
                            @Query("expansions")expansions:String= "attachments.media_keys,geo.place_id",
                            @Query("place.fields") mediaFields:String="country,country_code,geo",
                            @Query("tweet.fields") tweetFields:String="public_metrics,lang"

    ):TweetModel


    @GET
    suspend fun getVideoUrl(@Url()url: String="https://api.twitter.com/1.1/search/tweets.json",
                            @Header(RestClient.AUTHORIZATION)authorization:String,
                            @Query("id")tweetId:String,
                            @Query("include_entities")includeEntities:Boolean= true,

    )
    @GET
    suspend fun searchTweets(@Url()url: String="https://api.twitter.com/1.1/search/tweets.json",
                             @Header(RestClient.AUTHORIZATION)authorization:String,
                             @Query("q") query:String,
                             @Query("result_type")resultType:String="latest"
    ):SearchedTweets

    @GET
    suspend fun getTrends(@Url()url: String="https://api.twitter.com/1.1/trends/place.json",
                             @Header(RestClient.AUTHORIZATION)authorization:String,
                             @Query("id") id:String
    ):List<TrendingTopics>


}