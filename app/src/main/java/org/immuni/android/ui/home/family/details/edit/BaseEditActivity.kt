package org.immuni.android.ui.home.family.details.edit

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.core.widget.NestedScrollView
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.utils.ScreenUtils
import org.immuni.android.ImmuniActivity
import org.immuni.android.R

open class BaseEditActivity: ImmuniActivity() {

    override fun onStart() {
        super.onStart()
        setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        // on scrolling the top mask hide/show
        findViewById<NestedScrollView>(R.id.scrollView)?.setOnScrollChangeListener { v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            updateTopMask(scrollY)
        }

        updateTopMask(findViewById<NestedScrollView>(R.id.scrollView)?.scrollY ?: 0)
    }


    fun updateTopMask(scrollY: Int) {
        val dp = ScreenUtils.convertDpToPixels(applicationContext, 8).toFloat()
        val elevation = resources.getDimension(R.dimen.top_scroll_mask_elevation)
        //this.view?.findViewById<View>(R.id.topMask)?.alpha = 1f//0f + scrollY/dp
        findViewById<View>(R.id.topMask)?.elevation = (elevation * (0f + scrollY/dp).coerceIn(0f, 1f))
    }
}