package com.digitalskies.postingapp

import android.util.Base64
import android.util.Log

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleInstrumentedTest {
    val CRYPT_IV:String="MCvyRMdSJW15wfBb"
    private val CRYPT_KEY = "k6qBTDf7HVWSWdThFVkgYiTEdZFIRSAd"
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val deccrypted="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"


        val tt=Base64.decode(deccrypted,Base64.DEFAULT)

        //println("text ${String(tt)}")



        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

        val skey: SecretKey = SecretKeySpec(CRYPT_KEY.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, skey, IvParameterSpec(CRYPT_IV.toByteArray()));


        String(cipher.doFinal(Base64.decode(deccrypted,Base64.DEFAULT)))
    }
}