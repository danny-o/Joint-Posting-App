package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class TweetModel (

    @SerializedName("data"     ) var data     : TweetData,
    @SerializedName("includes" ) var includes : Includes? = Includes()

)

data class Attachments (

    @SerializedName("media_keys" ) var mediaKeys : ArrayList<String> = arrayListOf()

)
data class TweetData (

    @SerializedName("attachments" ) var attachments : Attachments? = Attachments(),
    @SerializedName("id"          ) var id          : String?      = null,
    @SerializedName("text"        ) var text        : String?      = null,
    @SerializedName("public_metrics" ) var publicMetrics : PublicMetrics,
    @SerializedName("lang" ) var language : String

)
data class Includes (

    @SerializedName("media" ) var media : ArrayList<TweetMedia> = arrayListOf()

)
data class TweetMedia (

    @SerializedName("media_key" ) var mediaKey : String? = null,
    @SerializedName("type"      ) var type     : String? = null

)
data class PublicMetrics (
    @SerializedName("like_count"    ) var likeCount    : String,
)
