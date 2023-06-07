package com.digitalskies.postingapp


import android.util.Base64
import org.junit.Test

class Main {



    @Test
    fun main(){
        val b=BaseImpl(10)

        println(Derived(b).print(b.x))




    }
}