package org.immuni.android.ui.home.family.details.edit

import android.view.View
import androidx.core.widget.NestedScrollView
import com.bendingspoons.base.utils.ScreenUtils
import org.immuni.android.R
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment

open class BaseEditFragment: FullScreenDialogLightFragment() {

    override fun onStart() {
        super.onStart()
        // on scrolling the top mask hide/show
        view?.findViewById<NestedScrollView>(R.id.scrollView)?.setOnScrollChangeListener { v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            updateTopMask(scrollY)
        }

        updateTopMask(view?.findViewById<NestedScrollView>(R.id.scrollView)?.scrollY ?: 0)
    }


    fun updateTopMask(scrollY: Int) {
        val dp = ScreenUtils.convertDpToPixels(requireContext(), 8).toFloat()
        val elevation = resources.getDimension(R.dimen.top_scroll_mask_elevation)
        //this.view?.findViewById<View>(R.id.topMask)?.alpha = 1f//0f + scrollY/dp
        view?.findViewById<View>(R.id.topMask)?.elevation = (elevation * (0f + scrollY/dp).coerceIn(0f, 1f))
    }
}