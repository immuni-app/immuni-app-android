package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import org.ascolto.onlus.geocrowd19.android.R
import kotlinx.android.synthetic.main.web_view_dialog.*

class WebViewDialog: FullScreenBottomSheetDialogFragment()  {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.web_view_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webViewClient = WebViewClient()
        webView.loadUrl(arguments?.getString("url"))

        back.setOnClickListener {
            dismiss()
        }
    }
}