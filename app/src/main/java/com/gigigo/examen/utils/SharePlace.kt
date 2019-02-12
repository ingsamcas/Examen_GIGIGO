package com.gigigo.examen.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.FileProvider
import android.widget.Toast
import java.io.File
import java.net.URI
import android.content.pm.LabeledIntent



class SharePlace() {
    val Facebook = "com.facebook.katana"
    val Twitter = "com.twitter.android"
    val Instagram = "com.instagram.android"
    val Pinterest = "com.pinterest"
    val socialNets=listOf<String>(Facebook,Twitter, Instagram, Pinterest)
    private var activity: Activity? = null


    fun sharingToSocialMedia(activity: Activity,placeName:String, promoImage: String, latitude: Double?, longitude: Double?) {

        this.activity = activity
        var shareChooser:Intent?=null
        var intentList:ArrayList<Intent> =ArrayList<Intent>()
        for (net in socialNets){
            if(checkAppInstall(net)){

                val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                val uri = placeName + " "+"http://maps.google.com/maps?daddr=$latitude,$longitude"
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
                sharingIntent.setPackage(net)
                if(shareChooser==null)shareChooser=(Intent.createChooser(sharingIntent,"Selecciona.."))
                else{intentList.add(sharingIntent)}

            }
        }

        if (shareChooser!=null) {//At least One activity coul hold the intent
            // convert intentList to array
            if(intentList.size>0){
                val extraIntents = intentList.toArray(arrayOfNulls<Intent>(intentList.size))
                shareChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
            }

            activity.startActivity(shareChooser)

        } else {
            Toast.makeText(activity.applicationContext,
                    "Instale alguna red social primero", Toast.LENGTH_LONG).show()
        }

    }


    private fun checkAppInstall(uri: String): Boolean {
        val pm = activity!!.packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }

        return false
    }
}