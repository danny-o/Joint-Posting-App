package com.digitalskies.postingapp.api

import com.digitalskies.postingapp.models.MediaIdResponse
import com.digitalskies.postingapp.models.MediaUploadStatus
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
                           )authorization:String): Response<Unit>

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



}