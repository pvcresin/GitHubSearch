package com.pvcresin.girhubsearch

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    val TAG = "Main"

    lateinit var github: GitHub

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        github = GitHub(this.applicationContext)

        (findViewById(R.id.button) as Button).setOnClickListener {
            github.openOAuthPage()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        github.getToken(intent)
    }

}

