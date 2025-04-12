package com.stupidtree.hitax.ui.eas.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.stupidtree.hitax.R
import com.stupidtree.hitax.data.model.eas.EASToken
import com.stupidtree.hitax.data.source.preference.EasPreferenceSource
import com.stupidtree.hitax.utils.JsonUtils
import org.jsoup.Jsoup

class LoginWebViewActivity : AppCompatActivity() {
    private var onResponseListener: OnResponseListener? = null
    private lateinit var webView: WebView
    var isLoginSuccess = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_web_view)

        WebView.setWebContentsDebuggingEnabled(true)
        webView = findViewById(R.id.login_web_view)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        webView.webViewClient = LoginWebViewClient(this)
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies {}
        cookieManager.flush()

        webView.loadUrl("http://jw.hitsz.edu.cn/cas")
    }


    override fun finish() {
        super.finish()
        webView.destroy()
        if (isLoginSuccess) {
            onResponseListener?.onSuccess(this)
        } else {
            onResponseListener?.onFailed(this)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    interface OnResponseListener {
        fun onSuccess(window: LoginWebViewActivity)
        fun onFailed(window: LoginWebViewActivity)
    }

}

class LoginWebViewClient(val activity: LoginWebViewActivity) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request != null) {
            Log.d("LoginWebView", request.url.toString())
        }
        if (request != null && request.url.toString() == "http://jw.hitsz.edu.cn/authentication/main") {
            val cookieManager = CookieManager.getInstance()
            val cookieStr = cookieManager.getCookie(request.url.toString())
            createEasTokenThenFinish(cookieStr)
            return true
        }
        return false
    }

    private fun createEasTokenThenFinish(cookieString: String) {
        Thread {
            val cookieMap = HashMap<String, String>()
            cookieString.split(";").forEach {
                val kv = it.split("=")
                if (kv.count() == 2) {
                    cookieMap[kv[0]] = kv[1]
                }
            }

            val defaultRequestHeader = HashMap<String, String>()
            defaultRequestHeader["Accept"] = "*/*"
            defaultRequestHeader["Connection"] = "keep-alive"
            defaultRequestHeader["User-Agent"] =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36"

            val easPreferenceSource = EasPreferenceSource.getInstance(activity.application)
            val token = easPreferenceSource.getEasToken()
            token.cookies = cookieMap
            token.username = ""
            token.password = ""
            val s = Jsoup.connect("http://jw.hitsz.edu.cn/UserManager/queryxsxx").timeout(5000)
                .cookies(cookieMap).headers(defaultRequestHeader)
                .header("X-Requested-With", "XMLHttpRequest").ignoreContentType(true)
                .ignoreHttpErrors(true).post()
            val json = s.getElementsByTag("body").text()
            val jo = JsonUtils.getJsonObject(json)
            token.stutype = if (jo?.getString("PYLX")
                    ?.lowercase() == "1"
            ) EASToken.TYPE.UNDERGRAD else EASToken.TYPE.GRAD
            token.school = jo?.optString("YXMC")
            token.sfxsx = jo?.optString("sfxsx")
            token.major = jo?.optString("ZYMC")
            token.picture = jo?.optString("ZPBSLJ")
            token.phone = jo?.optString("LXDH")
            token.id = jo?.optString("ID")
            token.email = jo?.optString("DZYX")
            token.grade = jo?.optString("NJMC")
            token.stuId = jo?.optString("XH")
            token.name = jo?.optString("XM")
            token.username = token.stuId

            easPreferenceSource.saveEasToken(token)
            activity.isLoginSuccess = true
            activity.runOnUiThread { run { activity.finish() } }
        }.start()
    }
}