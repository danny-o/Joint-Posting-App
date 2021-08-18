package com.digitalskies.postingapp.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.digitalskies.postingapp.R
import com.digitalskies.postingapp.databinding.FragmentPostBinding
import com.digitalskies.postingapp.utils.Event
import com.digitalskies.postingapp.utils.FilePath
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.*
import com.facebook.share.widget.ShareDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File


const val MESSAGE="message"
const val LINK="LINK"
const val MEDIA_URI="media_uri"

const val MEDIA_TYPE="media_type"

const val POST_ON_TWITTER="post_on_twitter"

const val POST_ON_LINKEDIN="post_on_linkedin"

const val POST_ON_FACEBOOK="post_on_facebook"

const val POST_ON_WHATSAPP="post_on_whatsapp"

class PostingFragment:Fragment() {
    lateinit var postBinding: FragmentPostBinding

    var postOnTwitter:Job?=null

    var postOnLinkedIn:Job?=null

    private var toPostOnTwitter=false

    private var toPostOnLinkedIn=false

    private var toPostOnFacebook=false

    private var toPostOnWhatsapp=false

    private var postedToWhatSapp=false



    var mediaUri:Uri?=null

    var link:String?=null

    var mediaType:String?=null

    var message:String?=null

    var file: File?=null

    lateinit var callbackManager: CallbackManager

    lateinit var mainActivityViewModel: MainActivityViewModel

    lateinit var shareDialog:ShareDialog
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        postBinding= FragmentPostBinding.inflate(layoutInflater, container, false)

        val view=postBinding.root

        mainActivityViewModel= ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        if(arguments?.getString(MEDIA_URI)!=null){
           mediaUri= Uri.parse(arguments?.getString(MEDIA_URI))

            mediaType= arguments?.getString(MEDIA_TYPE)

            file = File(FilePath.getPath(this.requireActivity(), Uri.parse(arguments?.getString(MEDIA_URI))))
        }


        link=arguments?.getString(LINK)

        message=arguments?.getString(MESSAGE)

        callbackManager= CallbackManager.Factory.create()

        hidePostingUI()

        postOnTwitter()

        postBinding.btnGoBack.setOnClickListener {
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                    R.anim.slide_out_right,
                    R.anim.slide_in_left,

                    )
                    .replace(R.id.frame_layout,HomeFragment())
                    .commit()
        }

        mainActivityViewModel.twitterResponse.observe(viewLifecycleOwner){ twitterResponse->
            if(twitterResponse is MainActivityViewModel.TwitterResponse.TwitterResponseSuccessful){

                postBinding.tvTwitterPosting.text=getString(R.string.posting_successful)

                postBinding.twitterPostingProgressbar.visibility=View.INVISIBLE

                postBinding.ivTwitterPostingComplete.visibility=View.VISIBLE
            }
            else{

                postBinding.tvTwitterPosting.setTextColor(resources.getColor(R.color.red))

                postBinding.tvTwitterPosting.text=getString(R.string.error_while_posting)
            }
            postBinding.tvLinkedinPosting.text=getString(R.string.posting)

            postOnLinkedIn()


        }

        mainActivityViewModel.linkedInResponse.observe(viewLifecycleOwner){ linkedInResponse->

            if(linkedInResponse is MainActivityViewModel.LinkedInResponse.ResponseSuccessful){

                postBinding.tvLinkedinPosting.text=getString(R.string.posting_successful)

                postBinding.linkedinPostingProgressbar.visibility=View.INVISIBLE

                postBinding.ivLinkedinPostingComplete.visibility=View.VISIBLE
            }
            else{

                postBinding.tvLinkedinPosting.setTextColor(resources.getColor(R.color.red))
                postBinding.tvLinkedinPosting.text=getString(R.string.error_while_posting)
            }
            postBinding.tvFacebookPosting.text=getString(R.string.posting)
            postOnFaceBook()
        }



        return view
    }



    override fun onResume() {
        super.onResume()

        if(postedToWhatSapp){
            postBinding.tvWhatsappPosting.text=getString(R.string.complete)

            postBinding.whatsappPostingProgressbar.visibility=View.INVISIBLE

            postBinding.ivWhatsappPostingComplete.visibility=View.VISIBLE

            mainActivityViewModel.postingComplete.postValue(Event(true))
        }



    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if(savedInstanceState?.getBoolean(POST_ON_WHATSAPP)==true){
            Log.d(PostingFragment::class.java.simpleName,"${savedInstanceState.getBoolean(POST_ON_WHATSAPP)}")
        }


    }

    private fun hidePostingUI() {

        toPostOnTwitter=arguments?.getBoolean(POST_ON_TWITTER)?:false

        toPostOnFacebook=arguments?.getBoolean(POST_ON_FACEBOOK)?:false

        toPostOnLinkedIn=arguments?.getBoolean(POST_ON_LINKEDIN)?:false

        toPostOnWhatsapp=arguments?.getBoolean(POST_ON_WHATSAPP)?:false
        if(!toPostOnTwitter){

            postBinding.tvTwitterPosting.isVisible=false

            postBinding.ivTwitterLogo.isVisible=false

            postBinding.twitterPostingProgressbar.isVisible=false

            postBinding.ivTwitterPostingComplete.isVisible=false
        }

        if(!toPostOnLinkedIn){

            postBinding.tvLinkedinPosting.isVisible=false

            postBinding.ivLinkedinLogo.isVisible=false

            postBinding.linkedinPostingProgressbar.isVisible=false

            postBinding.ivLinkedinPostingComplete.isVisible=false
        }
        if(!toPostOnFacebook){

            postBinding.tvFacebookPosting.isVisible=false

            postBinding.ivFacebookLogo.isVisible=false

            postBinding.facebookPostingProgressbar.isVisible=false

            postBinding.ivFacebookPostingComplete.isVisible=false
        }
        if(!toPostOnWhatsapp){

            postBinding.tvWhatsappPosting.isVisible=false

            postBinding.ivWhatsappLogo.isVisible=false

            postBinding.whatsappPostingProgressbar.isVisible=false

            postBinding.ivWhatsappPostingComplete.isVisible=false
        }
    }

    private fun postOnFaceBook() {
        if(toPostOnFacebook){
            shareDialog= ShareDialog(this)
            shareDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result> {

                override fun onSuccess(result: Sharer.Result?) {

                    postBinding.ivFacebookPostingComplete.visibility=View.VISIBLE

                    postBinding.facebookPostingProgressbar.visibility=View.INVISIBLE
                    postBinding.tvFacebookPosting.text = getString(R.string.posting_successful)
                    postOnWhatsapp()

                }

                override fun onCancel() {


                    postBinding.tvFacebookPosting.text = getString(R.string.cancelled)
                    postOnWhatsapp()
                }

                override fun onError(error: FacebookException) {

                    postBinding.tvFacebookPosting.setTextColor(resources.getColor(R.color.red))
                    postBinding.tvFacebookPosting.text = getString(R.string.error_while_posting)
                    postOnWhatsapp()

                }
            })
            if(mediaUri!=null){
                if(mediaType?.startsWith("image") == true){

                    postPhotoOnFaceBook()
                }
                else if(mediaType?.startsWith("video") == true){
                    postVideoOnFaceBook()
                }
            }
            if(mediaUri==null&&link!=null){
                postLinkOnFacebook()
            }
        }
        else{
            postOnWhatsapp()
        }


    }

    private fun postLinkOnFacebook() {
       val shareLinkContent=ShareLinkContent.Builder()
               .setContentUrl(Uri.parse(link))
               .build()

        if(ShareDialog.canShow(ShareLinkContent::class.java)){
            shareDialog.show(shareLinkContent)
        }

    }


    private fun postVideoOnFaceBook() {
        val shareVideo=ShareVideo.Builder()
                .setLocalUrl(mediaUri)
                .build()

        val shareVideoContent=ShareVideoContent.Builder()
                .setVideo(shareVideo)
                .setVideo(shareVideo)
                .setContentDescription(message)
                .build()

        if(ShareDialog.canShow(ShareVideoContent::class.java)){
            shareDialog.show(shareVideoContent)
        }

    }

    private fun postPhotoOnFaceBook(){
        val sharePhoto: SharePhoto


        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, mediaUri)
        sharePhoto= SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption(message)
                .build()

        val sharePhotoContent: SharePhotoContent = SharePhotoContent.Builder()
                .addPhoto(sharePhoto)
                .build()

        if(ShareDialog.canShow(SharePhotoContent::class.java)){


            shareDialog.show(sharePhotoContent)
        }
    }

    private fun postOnWhatsapp() {

        if(toPostOnWhatsapp){

                val share = Intent(Intent.ACTION_SEND)

                share.setPackage("com.whatsapp")

                if(mediaUri!=null){

                    share.type = mediaType


                    val uri = mediaUri


                    share.putExtra(Intent.EXTRA_STREAM, uri)
                }


                share.putExtra(Intent.EXTRA_TEXT,message)

                share.type="text/plain"


                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val packageManager: PackageManager = requireActivity().packageManager
                if (share.resolveActivity(packageManager) != null) {
                    postedToWhatSapp=true
                    startActivity(share)
                    startActivity(Intent.createChooser(share, "Share to"))
                }






        }

        else{
            mainActivityViewModel.postingComplete.postValue(Event(true))
        }


    }

    private fun postOnLinkedIn() {

        if(toPostOnLinkedIn){
            postOnLinkedIn?.cancel()

            postOnLinkedIn=lifecycleScope.launch(Dispatchers.IO){

                mainActivityViewModel.postOnLinkedIn(post = message.toString(),articleUrl = link)
            }
        }
        else{
            postOnFaceBook()
        }

    }

    private fun postOnTwitter() {


        if(toPostOnTwitter){
            postOnTwitter?.cancel()

            postOnTwitter= lifecycleScope.launch(Dispatchers.IO) {

                if(file!=null){
                    file?.let {
                        mainActivityViewModel.postOnTwitterWithMedia(
                                message.toString(),
                                it,
                                mediaType.toString()
                        )

                    }

                }
                else{

                    mainActivityViewModel.postOnTwitter(message.toString())
                }
            }
        }

        else{
            postOnLinkedIn()
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode,resultCode,data)


        }
}