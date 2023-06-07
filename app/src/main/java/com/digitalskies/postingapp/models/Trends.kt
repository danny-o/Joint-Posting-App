package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName



data class TrendingTopics (

    @SerializedName("trends"     ) var trends    : ArrayList<Trends>    = arrayListOf(),


)
data class Trends (

    @SerializedName("name"             ) var name            : String? = null,


)

data class Locations (

    @SerializedName("name"  ) var name  : String? = null,
    @SerializedName("woeid" ) var woeid : Int?    = null

)