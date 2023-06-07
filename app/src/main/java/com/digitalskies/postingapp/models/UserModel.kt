package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class UserModel(

    @SerializedName("data")val data:Data
)

data class Data(@SerializedName("id")val id:String)
