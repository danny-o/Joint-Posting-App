package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class LinkedInProfile(
    @SerializedName("id")var linkedInId:String,
    @SerializedName("localizedLastName")var lastName:String,
    @SerializedName("localizedFirstName")var firstName:String)
