package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.os.Bundle
import android.webkit.WebViewClient
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast
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