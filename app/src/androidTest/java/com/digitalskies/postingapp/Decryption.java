package com.digitalskies.postingapp;

import android.util.Base64;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@RunWith(AndroidJUnit4.class)
public class Decryption {

    @Test
    public void decrypt(){

        String CRYPT_IV="MCvyRMdSJW15wfBb";
        String CRYPT_KEY = "k6qBTDf7HVWSWdThFVkgYiTEdZFIRSAd";

        String deccrypted="5E3l7NtnPiTpy0dvjHPl+9+/FbX8rCadOnGTvldsheOYf3DkHgjgLEo6vYmrpjt2h71KgVW8FX2ivvbdx4uhnKcgeZ4dRk5vARtSKfXvXsVuGcacu6RkTfHqojuT6LQ3PIwfFLfPvVffupjQ7z+q2XHXhTmlUTuN0XaSVm1L1hikJiAjrZ7njLY80xO9Z0YZuIFd9ePqAN4dgejkbctcb7F1eKuRFWIg40N5Lm1IXDdZNbfACRyHqk/RzWvN2uOVqoDxAmI4UkxvbCnmiaGtH7BAkCZ0XwKxjh1vtZ+JKLajP0bMMBnU5dCGr+RZI67o6DrcdQIQh7Qak7OH9+EsXydTDR7qGXgrceL+pL1b5RnT5DNkBKSEytjXjyzhcs98bPb8tc/wNigcW6YkTW3oNO6YKGJjlDTCdTh19pqRZ0dxZimUg90jEEeFtx35OLu0SdsPrzl9B1KQAi5chLzjsLkzkvmD/vygXDFiXoHnaVOH5gmOD82s0Yqb4ovLInDIpmUmrsQbpIY+dkmvQnhGUBcm1m1OibjqdFuvmHSCs3ev6gT1DdhRkJ/H9TOmd5qmDF0/A0/Jv2cKxpPNwh9lhh3L4XQ6MiuxTICwX6CEYF6fP5Sl9rBpoY85nb4sS1Ia7Bq//JUeYI/tYaCO9SYsXuIj07QZBSfuwWMByKiE7kB9VBpV3pHDOpqoNLFo7C8QrhK5LvuwmA7TEb07YbKH63A6SA8vdOp1F9kbatapHEk3l3y4WE9ANfs93pFOAmBnwFtPPJNpBHRwdgVWI4XhgfYFVLCjdTynvwNK1g6cUc49+Y9GQVGVsaGMR/pt6guKkWbGbKmni6fvQdX7+uu9Q/jMIGXOar9Vg/UlqahsFqcsRU4Ej+weg1czV/Ic0gdDSs3PGin6wp6yYjWpgmLbh0WsNtzfHsbkG5iSIfHkK1hJfkw5UGXP39A9mIQpJDN8pLJwGyF+C311XNB1gy7EEQ==";




        byte[] bytes = Base64.decode(deccrypted, Base64.DEFAULT);

        ByteArrayInputStream byteInputStream = null;
        try {
            SecretKey skey = new SecretKeySpec(CRYPT_KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(CRYPT_IV.getBytes("UTF-8")));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(cipher.doFinal(bytes));

            System.out.println(new String(cipher.doFinal(bytes)));
            byteInputStream = new ByteArrayInputStream(bos.toByteArray());


        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
