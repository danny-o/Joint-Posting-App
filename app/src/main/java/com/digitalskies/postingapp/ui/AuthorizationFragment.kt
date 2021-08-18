package com.digitalskies.postingapp.ui

//import com.tycz.tweedle.lib.ExperimentalApi
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.digitalskies.postingapp.databinding.FragmentAuthorizationBinding
import com.digitalskies.postingapp.databinding.FragmentFacebookLoginBinding
import com.digitalskies.postingapp.utils.Event
import com.digitalskies.postingapp.utils.EventObserver
import com.digitalskies.postingapp.utils.OnEventChanged
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder


const val SIGN_IN_WITH="sign_in_with"
const val linkedIn="com.digitalskies.postingapp.linkedIn"
const val twitter="com.digitalskies.postingapp.twitter"
const val facebook="com.digitalskies.postingapp.facebook"

class AuthorizationFragment:Fragment(),FacebookCallback<LoginResult> {
    lateinit var authorizationBinding: FragmentAuthorizationBinding

    lateinit var loginWithFacebookLoginBinding: FragmentFacebookLoginBinding;

    lateinit var mainActivityViewModel:MainActivityViewModel

    lateinit var callbackManager: CallbackManager



    private val EMAIL = "email"
    private val USER_POSTS = "user_posts"


    private val AUTH_TYPE = "rerequest"

    val LINKEDIN_AUTH_URL="https://www.linkedin.com/oauth/v2/authorization"
    val TWITTER_AUTH_URL="https://api.twitter.com/oauth/authenticate"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val signInWith=arguments?.getString(SIGN_IN_WITH)
        val view:View



        mainActivityViewModel= ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

            authorizationBinding= FragmentAuthorizationBinding.inflate(
                requireActivity().layoutInflater,
                container,
                false
            )
            view=authorizationBinding.root






        mainActivityViewModel.loginResponse.observe(viewLifecycleOwner,EventObserver {data->
            hideLoading()
            if (data != null) {
                if (data is MainActivityViewModel.LoginResponse.LoginSuccessful) {


                    Toast.makeText(this@AuthorizationFragment.requireActivity(), "Login successful", Toast.LENGTH_LONG).show()

                    Log.d(HomeFragment::class.java.simpleName, "success")

                    requireActivity().supportFragmentManager.popBackStack()
                } else if (data is MainActivityViewModel.LoginResponse.LoginFailed) {

                    Log.d(HomeFragment::class.java.simpleName, data.errorMessage.toString())
                    Toast.makeText(requireContext(), data.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })

        /*Handler(requireActivity().mainLooper).postDelayed({
            mainActivityViewModel.loginResponse.postValue(Event(MainActivityViewModel.LoginResponse.LoginSuccessful))

            //activity?.supportFragmentManager?.popBackStack()

        },3000)
*/



        authorizationBinding.wvAuthorization.webChromeClient=object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                authorizationBinding.loadingProgress.progress=newProgress

                authorizationBinding.loadingProgress.visibility=View.VISIBLE
                if(newProgress==100){
                    authorizationBinding.loadingProgress.visibility= View.GONE
                }
            }
        }


            authorizationBinding.wvAuthorization.webViewClient=object: WebViewClient(){

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    if(signInWith== linkedIn){
                        val error = request?.url?.getQueryParameters(ERROR)?.getOrNull(0)
                        if (error != null) {

                            (activity as MainActivity).toast(error)

                            Log.d(AuthorizationFragment::class.java.simpleName,error)
                            requireActivity().supportFragmentManager.popBackStack()
                            return false
                        }

                        val authCode = request?.url?.getQueryParameters(CODE)?.getOrNull(0)
                        if (authCode != null) {

                            Log.d("AuthFragment", authCode.toString())

                            mainActivityViewModel.linkedInAuthCode=authCode

                            showLoading()


                            mainActivityViewModel.setUpLinkedInUser()





                            authorizationBinding.wvAuthorization.stopLoading()





                            return true

                        }

                    }

                    if(signInWith== twitter){

                        if(request!!.url.toString().startsWith(TWITTER_REDIRECT_URI)){

                            val decodeUrl=URLDecoder.decode(request.url.toString(), "UTF-8")

                            if(decodeUrl.contains("oauth_token=")){

                                val uri=Uri.parse(decodeUrl)




                                mainActivityViewModel.twitterOAuthToken=uri.getQueryParameter(
                                    TWITTER_OAUTH_TOKEN
                                )

                                mainActivityViewModel.twitterOAuthVerifier=uri.getQueryParameter(
                                    TWITTER_OAUTH_VERIFIER
                                )

                                //showLoading()

                                mainActivityViewModel.setUpTwitterUser()








                            }
                            return true

                        }



                    }

                    return false
                }
            }

            authorizationBinding.wvAuthorization.settings.javaScriptEnabled=true

            var url:String?=null

            if(signInWith== twitter){

                lifecycleScope.launch(Dispatchers.IO){
                    try {


                        val tokenResponse=mainActivityViewModel.getTwitterAuthToken()

                        Log.d(
                            AuthorizationFragment::class.java.simpleName,
                            tokenResponse.oauthToken.toString()
                        )



                        tokenResponse.oauthToken?.let {
                            url=generateTwitterAuthUrl(it)

                            Log.d(AuthorizationFragment::class.java.simpleName, url.toString())

                            withContext(Dispatchers.Main){
                                authorizationBinding.wvAuthorization.loadUrl(url.toString())
                            }


                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            else if(signInWith== linkedIn){
                url=generateLinkedInAuthUrl()

                authorizationBinding.wvAuthorization.loadUrl(url!!)
            }









        return view
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    fun showLoading(){
       ( requireActivity() as MainActivity).showLoading()
    }

    fun hideLoading(){
       ( requireActivity() as MainActivity).hideLoading()
    }

    fun generateLinkedInAuthUrl():String{
        return Uri.parse(LINKEDIN_AUTH_URL)
            .buildUpon()
            .appendQueryParameter("response_type", CODE)
            .appendQueryParameter("client_id", LINKEDIN_CLIENT_ID)
            .appendQueryParameter("redirect_uri", LINKEDIN_REDIRECT_URI)
            .appendQueryParameter("scope", SCOPE).build().toString()
    }
    fun generateTwitterAuthUrl(authToken: String):String{


        return Uri.parse(TWITTER_AUTH_URL)
                .buildUpon()
                .appendQueryParameter("oauth_token", authToken)
                .appendQueryParameter("force_login", "true")
                .toString()
    }
    companion object{
        private const val ERROR="error"
        private const val CODE="code"
        private const val LINKEDIN_CLIENT_ID = "86lv9vqbo6dmwh"
        private const val LINKEDIN_REDIRECT_URI = "https://www.linkedin.com"
        private const val SCOPE = "w_member_social r_liteprofile r_emailaddress"

        private const val TWITTER_REDIRECT_URI = "https://www.twitter.com"

        private const val TWITTER_OAUTH_TOKEN="oauth_token"

        private const val TWITTER_OAUTH_VERIFIER="oauth_verifier"

        private const val TWITTER_API_KEY="HksreL3zhC75C1E55CxLJr1XQ"


    }

    override fun onSuccess(result: LoginResult?) {
        Log.d(
            AuthorizationFragment::class.java.simpleName,
            "token is ${result?.accessToken}"
        )
        AccessToken.setCurrentAccessToken(result?.accessToken)

        Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
        requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onCancel() {

        requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onError(error: FacebookException?) {
        Toast.makeText(requireContext(), "failed to login", Toast.LENGTH_SHORT).show()
        requireActivity().supportFragmentManager.popBackStack()
    }
}