package com.pvcresin.girhubsearch

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ClientData(this)
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