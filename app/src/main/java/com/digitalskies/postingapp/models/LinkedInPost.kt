package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class LinkedInPost (

    @SerializedName("author") val author : String,
    @SerializedName("lifecycleState") val lifecycleState : String,
    @SerializedName("specificContent") val specificContent : SpecificContent,
    @SerializedName("visibility") val visibility : Visibility
)

data class SpecificContent (

    @SerializedName("com.linkedin.ugc.ShareContent") val ShareContent : ShareContent
)
data class ShareContent (

@SerializedName("shareCommentary") val shareCommentary : ShareCommentary,
@SerializedName("shareMediaCategory") val shareMediaCategory : String,
@SerializedName("media") val media : List<Media>?=null
)

data class ShareCommentary (

    @SerializedName("text") val text : String
)

data class Visibility (

    @SerializedName("com.linkedin.ugc.MemberNetworkVisibility") val Visibility : String
)

data class Media (

    @SerializedName("status") val status : String,
    @SerializedName("description") val description : Description?=null,
    @SerializedName("originalUrl") val originalUrl : String,
    @SerializedName("title") val title : Title?=null,
    @SerializedName("media") val media : String?=null,
)
data class Title (

    @SerializedName("text") val text : String
)

data class Description (

    @SerializedName("text") val text : String
)