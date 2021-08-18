package com.digitalskies.postingapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.digitalskies.postingapp.databinding.FragmentAuthorizationBinding
import com.digitalskies.postingapp.databinding.FragmentFacebookLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


class AuthActivity:AppCompatActivity(), FacebookCallback<LoginResult> {

    lateinit var loginWithFacebookLoginBinding: FragmentFacebookLoginBinding;

    private val EMAIL = "email"
    private val USER_POSTS = "user_posts"


    private val AUTH_TYPE = "rerequest"

    lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginWithFacebookLoginBinding= FragmentFacebookLoginBinding.inflate(layoutInflater)
        setContentView(loginWithFacebookLoginBinding.root)

        callbackManager= CallbackManager.Factory.create()

        setUpLogin()

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    private fun setUpLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, this)

        LoginManager.getInstance().authType=AUTH_TYPE
        LoginManager.getInstance().logIn(this, arrayListOf(EMAIL))
    }
    private fun setUpLoginButton() {


        loginWithFacebookLoginBinding.loginButton.setPermissions(arrayListOf(EMAIL))

        loginWithFacebookLoginBinding.loginButton.authType=AUTH_TYPE



        loginWithFacebookLoginBinding.loginButton.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {

                override fun onSuccess(result: LoginResult?) {
                    Log.d(
                        AuthorizationFragment::class.java.simpleName,
                        "token is ${result?.accessToken}"
                    )

                    Toast.makeText(this@AuthActivity, "success", Toast.LENGTH_SHORT).show()

                    setResult(RESULT_OK)
                    finish()
                }

                override fun onCancel() {
                    setResult(RESULT_CANCELED)
                    finish()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@AuthActivity, "failed to login", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    override fun onSuccess(result: LoginResult?) {
        Log.d(
            AuthorizationFragment::class.java.simpleName,
            "token is ${result?.accessToken}"
        )
        AccessToken.setCurrentAccessToken(result?.accessToken)

        setResult(RESULT_OK)

        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()

        finish()

    }

    override fun onCancel() {

        Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
        setResult(RESULT_CANCELED);
        finish()
    }

    override fun onError(error: FacebookException?) {
        Toast.makeText(this, "failed to login", Toast.LENGTH_SHORT).show()
        supportFragmentManager.popBackStack()
        finish()
    }
}