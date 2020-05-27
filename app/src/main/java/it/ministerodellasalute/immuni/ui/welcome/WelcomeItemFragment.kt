/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.engine.DiskCacheStrategy
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.visible
import it.ministerodellasalute.immuni.util.GlideApp
import kotlinx.android.synthetic.main.welcome_item_fragment.*

class WelcomeItemFragment : Fragment() {

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        position = arguments?.getInt("position") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_item_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.text = when (position) {
            0 -> getString(R.string.welcome_view_items_first_title)
            1 -> getString(R.string.welcome_view_items_second_title)
            2 -> getString(R.string.welcome_view_items_third_title)
            else -> getString(R.string.welcome_view_items_fourth_title)
        }

        description.text = when (position) {
            0 -> getString(R.string.welcome_view_items_first_description)
            1 -> getString(R.string.welcome_view_items_second_description)
            2 -> getString(R.string.welcome_view_items_third_description)
            else -> getString(R.string.welcome_view_items_fourth_description)
        }

        knowMore.setSafeOnClickListener {
            val action = WelcomeFragmentDirections.actionHowitworks(false)
            findNavController().navigate(action)
        }

        checkSpacing()
    }

    private fun loadIllustrations() {
        val imageResource = when (position) {
            0 -> R.drawable.welcome_1
            1 -> R.drawable.welcome_2
            2 -> R.drawable.welcome_3
            else -> R.drawable.welcome_4
        }

        GlideApp
            .with(requireContext())
            .load(imageResource)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(image)
    }

    /**
     * Hide illustration when there is not enough space on top.
     * Use a maximum aspect ratio.
     */
    private fun checkSpacing() {
        title.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                title.removeOnLayoutChangeListener(this)
                val W = ScreenUtils.getScreenWidth(requireContext())
                val aspectRatio = W.toFloat() / top.toFloat()

                if (aspectRatio > 2) {
                    image.gone()
                } else {
                    image.visible()
                    loadIllustrations()
                }
            }
        })
    }
}
