package com.pvcresin.githubsearch

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.nfc.Tag
import android.support.customtabs.CustomTabsIntent
import java.security.acl.Owner


class MainActivity : AppCompatActivity() {
    val TAG = "Main"

    lateinit var github: GitHub


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.actionBar?.hide()
        this.supportActionBar?.hide()



        github = GitHub(this.applicationContext)

        if (!github.authenticated) github.openOAuthPage()

        val searchView = findViewById(R.id.search_view) as SearchView

        val listView = findViewById(R.id.list_view) as ListView

        // リストビューに表示する要素を設定
        for (i in 0..20) {
            val item = Repo("full_name", "html_url", "description",
                    User("avatar_url", "name", "url", "login"))
        }




        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchWord: String): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                github.search(newText, "stars", "desc").callback(
                    onResponse = { res ->
                        val result = res?.body()
                        val repos = result?.items

                        if (repos != null) {
                            val listItems = mutableListOf<Repo>()
                            repos.forEach {
                                listItems.add(it)
                            }
                            listView.adapter =
                                    SampleListAdapter(this@MainActivity, R.layout.list_item, listItems)
                        }
                        result?.items?.forEachIndexed { index, repo ->
                            Log.d(TAG, "$index $repo")
                        }
                    },
                    onFailure = { t ->
                        Log.d(TAG, t.toString())
                    }
                )
                Log.d(TAG, "search: $newText")
                return false
            }
        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        github.getToken(intent)
    }
}


class SampleListAdapter(context: Context, val resource: Int, val items: List<Repo>)
        : ArrayAdapter<Repo>(context, resource, items) {

    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView != null) {
            view = convertView
        } else {
            view = inflater.inflate(resource, null)
        }

        val item = items[position]

        view.setOnClickListener {
            val customTab = CustomTabsIntent.Builder()
                    .setShowTitle(true).enableUrlBarHiding().build()
            customTab.launchUrl(context, Uri.parse(item.html_url))
        }

        val title = view.findViewById(R.id.title) as TextView
        title.text = item.full_name

        val desc = view.findViewById(R.id.description) as TextView
        desc.text = item.description

        return view
    }
}