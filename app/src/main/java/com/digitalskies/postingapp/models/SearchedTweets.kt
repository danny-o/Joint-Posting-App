package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class SearchedTweets(

    @SerializedName("statuses" ) var statuses : ArrayList<Statuses>
)
data class Statuses (

    @SerializedName("created_at"                ) var createdAt            : String?           = null,
    @SerializedName("id"                        ) var id                   : Long?              = null,
    @SerializedName("id_str"                    ) var idStr                : String?           = null,
    @SerializedName("text"                      ) var text                 : String?           = null,
    @SerializedName("truncated"                 ) var truncated            : Boolean?          = null,
    @SerializedName("entities"                  ) var entities             : SearchedTweetsEntities?,
    @SerializedName("source"                    ) var source               : String?           = null,
    @SerializedName("in_reply_to_status_id"     ) var inReplyToStatusId    : String?           = null,
    @SerializedName("in_reply_to_status_id_str" ) var inReplyToStatusIdStr : String?           = null,
    @SerializedName("in_reply_to_user_id"       ) var inReplyToUserId      : String?           = null,
    @SerializedName("in_reply_to_user_id_str"   ) var inReplyToUserIdStr   : String?           = null,
    @SerializedName("in_reply_to_screen_name"   ) var inReplyToScreenName  : String?           = null,
    @SerializedName("geo"                       ) var geo                  : String?           = null,
    @SerializedName("coordinates"               ) var coordinates          : String?           = null,
    @SerializedName("place"                     ) var place                : String?           = null,
    @SerializedName("contributors"              ) var contributors         : String?           = null,
    @SerializedName("is_quote_status"           ) var isQuoteStatus        : Boolean?          = null,
    @SerializedName("retweet_count"             ) var retweetCount         : Int?              = null,
    @SerializedName("favorite_count"            ) var favoriteCount        : Int?              = null,
    @SerializedName("favorited"                 ) var favorited            : Boolean?          = null,
    @SerializedName("retweeted"                 ) var retweeted            : Boolean?          = null,
    @SerializedName("possibly_sensitive"        ) var possiblySensitive    : Boolean?          = null,
    @SerializedName("lang"                      ) var lang                 : String?           = null

)

data class SearchedTweetsEntities (
    @SerializedName("symbols"       ) var symbols      : ArrayList<String>       = arrayListOf(),
    @SerializedName("user_mentions" ) var userMentions : ArrayList<TweetUserMentions> = arrayListOf(),
    @SerializedName("media"         ) var media        : ArrayList<SearchedTweetsMedia>        = arrayListOf()
)

data class TweetUserMentions (

    @SerializedName("screen_name" ) var screenName : String? = null,
    @SerializedName("name"        ) var name       : String? = null,
    @SerializedName("id"          ) var id         : Long?    = null,
    @SerializedName("id_str"      ) var idStr      : String? = null,

)
data class Thumb (

    @SerializedName("w"      ) var w      : Int?    = null,
    @SerializedName("h"      ) var h      : Int?    = null,
    @SerializedName("resize" ) var resize : String? = null

)
data class Medium (

    @SerializedName("w"      ) var w      : Int?    = null,
    @SerializedName("h"      ) var h      : Int?    = null,
    @SerializedName("resize" ) var resize : String? = null

)

data class SearchedTweetsMedia (

    @SerializedName("id"                   ) var id                : Long?           = null,
    @SerializedName("id_str"               ) var idStr             : String?        = null,
    @SerializedName("indices"              ) var indices           : ArrayList<Int> = arrayListOf(),
    @SerializedName("media_url"            ) var mediaUrl          : String?        = null,
    @SerializedName("media_url_https"      ) var mediaUrlHttps     : String?        = null,
    @SerializedName("url"                  ) var url               : String?        = null,
    @SerializedName("display_url"          ) var displayUrl        : String?        = null,
    @SerializedName("expanded_url"         ) var expandedUrl       : String?        = null,
    @SerializedName("type"                 ) var type              : String?        = null,
    @SerializedName("source_status_id"     ) var sourceStatusId    : Long?           = null,
    @SerializedName("source_status_id_str" ) var sourceStatusIdStr : String?        = null,
    @SerializedName("source_user_id"       ) var sourceUserId      : Long?           = null,
    @SerializedName("source_user_id_str"   ) var sourceUserIdStr   : String?        = null

)