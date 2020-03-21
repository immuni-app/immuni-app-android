package com.bendingspoons.ascolto.ui.dialog

import android.os.Bundle
import android.webkit.WebViewClient
import com.bendingspoons.ascolto.AscoltoActivity
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.toast
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.web_view_dialog.*
import java.lang.IllegalArgumentException

class WebViewActivity: AscoltoActivity()  {

    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        url = intent.extras?.getString("url") ?: throw IllegalArgumentException()

        setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        setContentView(R.layout.web_view_dialog)

        back.setOnClickListener {
            finish()
        }

        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }
}