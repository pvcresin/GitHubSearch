package com.pvcresin.girhubsearch

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query


class GitHub(val context: Context) {
    val clientData = ClientData(context)

    var oauth = initOAuth()

    var code = ""

    fun initOAuth(): GitHubOAuth {
        val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .build()
                    chain.proceed(request)
                }.build()
        return Retrofit.Builder()
                .baseUrl("https://github.com")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubOAuth::class.java)
    }

    fun getToken() = oauth.getToken(code, clientData.id, clientData.secret)

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


data class TokenResult(val access_token: String, val token_type: String)

interface GitHubOAuth {
    @POST("login/oauth/access_token")
    fun getToken(
        @Query("code") code: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): Call<TokenResult>
}