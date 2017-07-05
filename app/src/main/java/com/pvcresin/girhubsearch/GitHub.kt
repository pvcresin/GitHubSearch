package com.pvcresin.girhubsearch

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent


class GitHub(val context: Context) {
    val clientData = ClientData(context)

    var code = ""

    fun openOAuthPage() {
        val customTab = CustomTabsIntent.Builder()
                .setShowTitle(true).enableUrlBarHiding().build()
        customTab.launchUrl(context,
                Uri.parse("https://github.com/login/oauth/authorize?client_id=${clientData.id}"))
    }

    inner class ClientData(context: Context) {
        var id: String
        var secret: String
        init {
            val lines = context.assets.open("client.txt").reader().readLines()
//        lines.forEach { Log.d(this.javaClass.name, it) }
            id = lines[1]
            secret = lines[3]
        }
    }
}