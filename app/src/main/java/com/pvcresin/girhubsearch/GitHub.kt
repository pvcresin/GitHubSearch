package com.pvcresin.girhubsearch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.support.customtabs.CustomTabsIntent
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


class GitHub(val context: Context) {
    val TAG: String = this.javaClass.simpleName

    val clientData = ClientData(context)

    var api = initApi()
    var oauth = initOAuth()

    var code = ""

    val token: String
        get() = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "")
    val tokenType: String
        get() = PreferenceManager.getDefaultSharedPreferences(context).getString("type", "")
    val authenticated: Boolean
        get() = !token.isEmpty()

    init {
        Log.d(TAG, "authencated: $authenticated, token: $token")
    }

    // API
    fun initApi(): GitHubApi {
        val httpClient =
                OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Authorization", "$tokenType " + token)
                            .addHeader("Accept", "application/json")
                            .build()
                    chain.proceed(request)
                }.authenticator { route, response ->
                    response.request().newBuilder()
                            .addHeader("Authorization", "$tokenType " + token)
                            .build()
                }.build()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(GitHubApi::class.java)
    }

    fun search(query: String, sort: String, order: String) {
        api.searchRepositories(query, sort, order).callback(
            onResponse = { res ->
                val result = res?.body()
                result?.items?.forEachIndexed { index, repo ->
                    Log.d(TAG, "$index $repo")
                }
            },
            onFailure = { t ->
                Log.d(TAG, t.toString())
            }
        )
    }

    // OAuth
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

    fun openOAuthPage() {
        val customTab = CustomTabsIntent.Builder()
                .setShowTitle(true).enableUrlBarHiding().build()
        customTab.launchUrl(context,
                Uri.parse("https://github.com/login/oauth/authorize?client_id=${clientData.id}"))
    }

    fun getToken(intent: Intent?) {
        fun storeToken(token: String, type: String) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("token", token)
                    .putString("type", type)
                    .apply()
        }

        val code = intent?.data?.getQueryParameter("code")

        if (code == null) return
        else this.code = code

        oauth.getToken(this.code, clientData.id, clientData.secret).callback(
            onResponse = { res ->
                val result = res?.body()
                if (result != null) {
                    Log.d(TAG, "token result: " + result.toString())
                    storeToken(result.access_token, result.token_type)
                }
            },
            onFailure = { t ->
                Log.e(TAG, t.toString())
            }
        )
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

data class User(
    val avatar_url: String,
    val name: String,
    val url: String,
    val login: String
)
data class Repo(
    val full_name: String,
    val html_url: String,
    val description: String,
    val owner: User
)
data class SearchResult(val items: List<Repo>)
data class TokenResult(val access_token: String, val token_type: String)

interface GitHubApi {
    @GET("search/repositories")
    fun searchRepositories(
        @Query("q") query: String,      // search keywords
        @Query("sort") sort: String,    // [match], starts, forks
        @Query("order") order: String   // [desc], asc
    ): Call<SearchResult>
}

interface GitHubOAuth {
    @POST("login/oauth/access_token")
    fun getToken(
        @Query("code") code: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): Call<TokenResult>
}

fun <T> Call<T>.callback(
        onResponse: (res: Response<T>?) -> Unit,
        onFailure: (t: Throwable?) -> Unit) {
    this.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>?) {
            onResponse.invoke(response)
        }
        override fun onFailure(call: Call<T>?, t: Throwable?) {
            onFailure.invoke(t)
        }
    })
}
