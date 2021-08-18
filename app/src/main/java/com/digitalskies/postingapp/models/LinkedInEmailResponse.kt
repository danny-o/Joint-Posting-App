package com.digitalskies.postingapp.models

import com.google.gson.annotations.SerializedName

data class LinkedInEmailResponse(
    @SerializedName("elements") val elements : List<Elements>,

)

data class Elements(
        @SerializedName("handle") val handle : String,
        @SerializedName("handle~") val emailHandle : Handle,
)
data class Handle(
    @SerializedName("emailAddress") val email : String,
)