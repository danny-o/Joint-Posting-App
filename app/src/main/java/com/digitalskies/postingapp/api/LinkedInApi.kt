package com.digitalskies.postingapp.api

import com.digitalskies.postingapp.models.LinkedInAccessTokenResponse
import com.digitalskies.postingapp.models.LinkedInEmailResponse
import com.digitalskies.postingapp.models.LinkedInPost
import com.digitalskies.postingapp.models.LinkedInProfile
import com.digitalskies.postingapp.ui.HomeFragment
import retrofit2.Response
import retrofit2.http.*

interface LinkedInApi {


    @GET("authorization")
    suspend fun getLinkedInAuthorization(@Query("response_type") code:String="code",
                                 @Query("redirect_uri")redirectUrl:String="https://www.google.com",
                                 @Query("client_id")clientId:String=RestClient.LINKEDIN_CLIENT_ID,
                                 @Query("scope")scope:String="w_member_social"):String

    @FormUrlEncoded
    @POST("accessToken")
    suspend fun getLinkedInAccessToken(@Field("grant_type")grantType:String="authorization_code",
                                       @Field("code")code: String?,
                                       @Field("client_id")clientId:String?,
                                       @Field("client_secret")clientSecret:String?,
                                       @Field("redirect_uri")redirectUrl: String="https://www.linkedin.com"):LinkedInAccessTokenResponse





    @GET("me")
    suspend fun getLinkedInUser(@Header(RestClient.LINKEDIN_AUTHORIZATION)authorization:String):LinkedInProfile

    @GET("emailAddress")
    suspend fun getLinkedInEmail(@Header(RestClient.LINKEDIN_AUTHORIZATION)authorization:String,
                                 @Query("q")query:String="members",
                                 @Query("projection")projection:String="(elements*(handle~))"):LinkedInEmailResponse

    @Headers("X-Restli-Protocol-Version: 2.0.0")
    @POST("ugcPosts")
    suspend fun postOnLinkedIn(@Header(RestClient.LINKEDIN_AUTHORIZATION)authorization:String,@Body()requestBody:LinkedInPost)

}