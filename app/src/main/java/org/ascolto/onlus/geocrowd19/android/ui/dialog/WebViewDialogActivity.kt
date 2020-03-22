package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.os.Bundle
import android.webkit.WebViewClient
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.extensions.setNavigationBarColor
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import kotlinx.android.synthetic.main.web_view_dialog.*
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast

class WebViewDialogActivity: AscoltoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.dialog_status_bar_alpha))

        webView.webViewClient = WebViewClient()
        webView.loadUrl(intent.extras!!.getString("url"))
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        back.setOnClickListener {
            finish()
        }
    }
}