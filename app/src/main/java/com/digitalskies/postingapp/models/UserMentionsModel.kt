package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class UserMentionsModel(@SerializedName("data")val data:List<UserMentionsData>)
data class UserMentionsData(@SerializedName("text")val text:String,
                            @SerializedName("referenced_tweets")val referencedTweets: List<ReferencedTweets>,
                            @SerializedName("entities")val entities:Entities
)

data class ReferencedTweets(@SerializedName("type")val type:String,
                            @SerializedName("id")val id:String,
                            @SerializedName("text") val text:String
)

data class Entities (@SerializedName("mentions" ) var mentions : ArrayList<Mentions>)

data class Mentions(@SerializedName("start")val start:Int,
                    @SerializedName("end")val end:Int,
                    @SerializedName("username")val userName:String,
                    @SerializedName("id")val id:String
)