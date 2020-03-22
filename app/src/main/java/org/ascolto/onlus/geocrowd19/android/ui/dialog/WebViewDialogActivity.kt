package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.os.Bundle
import android.webkit.WebViewClient
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import kotlinx.android.synthetic.main.web_view_dialog.*
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R

class WebViewDialogActivity: AscoltoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.web_view_dialog_status_bar))

        webView.webViewClient = WebViewClient()
        webView.loadUrl(intent.extras!!.getString("url"))
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        back.setOnClickListener {
            finish()
        }
    }
}