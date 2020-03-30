package org.immuni.android.ui.dialog

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import kotlinx.android.synthetic.main.web_view_dialog.*
import org.immuni.android.AscoltoActivity
import org.immuni.android.R

class WebViewDialogActivity: AscoltoActivity() {

    companion object {
        const val TRIAGE_DIALOG_RESULT = 900
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.transparent))

        webView.webViewClient = WebViewClient()
        webView.loadUrl(intent.extras!!.getString("url"))
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        back.setOnClickListener {
            finish()
        }

        // top gradient color
        val colorRes = applicationContext.resources.getColor(intent.extras!!.getInt("color", R.color.home_background))
        val colorAplhaZero = Color.argb(0, colorRes.red, colorRes.green, colorRes.blue)
        webView.setBackgroundColor(colorRes)
        val drawableTop = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(colorRes, colorRes, colorAplhaZero)
        )
        topGradient.background = drawableTop
    }
}