package com.pvcresin.girhubsearch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clientData = ClientData(this)

        fun openOAuthPage() {
            val customTab = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .enableUrlBarHiding().build()
            customTab.launchUrl(this,
                    Uri.parse("https://github.com/login/oauth/authorize?client_id=${clientData.id}"))
        }

        (findViewById(R.id.button) as Button).setOnClickListener {
            openOAuthPage()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val code = intent?.data?.getQueryParameter("code")
        Log.d(TAG, "code $code")
    }
}


class ClientData(context: Context) {
    var id: String
    var secret: String
    init {
        val lines = context.assets.open("client.txt").reader().readLines()
//        lines.forEach { Log.d(this.javaClass.name, it) }
        id = lines[1]
        secret = lines[3]
    }
}