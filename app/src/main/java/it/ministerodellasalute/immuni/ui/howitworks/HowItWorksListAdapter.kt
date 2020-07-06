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

package it.ministerodellasalute.immuni.ui.howitworks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.isHighEndDevice
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import java.lang.ref.WeakReference
import kotlin.reflect.full.primaryConstructor
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class HowItWorksListAdapter(
    val context: Context,
    private val clickListener: HowItWorksClickListener,
    val showFaq: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    private val dataSource: HowItWorksDataSource by inject { parametersOf(showFaq) }
    private val data = dataSource.data

    private val lottiesMap = mutableMapOf<Int, WeakReference<LottieAnimationView>>()

    private fun onItemClick(pos: Int) {
        if (pos != RecyclerView.NO_POSITION) {
            clickListener.onClick(data[pos])
        }
    }

    fun onScrolling() {
        lottiesMap.values.forEach {
            it.get()?.pauseAnimation()
        }
    }

    fun onIdle() {
        lottiesMap.values.forEach {
            it.get()?.resumeAnimation()
        }
    }

    inner class TitleVH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
    }

    inner class ImageVH(v: View) : RecyclerView.ViewHolder(v) {
        val lottieAnimation: LottieAnimationView = v.findViewById(R.id.lottieAnimation)
    }

    inner class ParagraphVH(v: View) : RecyclerView.ViewHolder(v) {
        val text: TextView = v.findViewById(R.id.text)
    }

    inner class ParagraphTitleVH(v: View) : RecyclerView.ViewHolder(v) {
        val text: TextView = v.findViewById(R.id.text)
    }

    inner class SeparatorVH(v: View) : RecyclerView.ViewHolder(v)

    inner class FooterVH(v: View) : RecyclerView.ViewHolder(v) {
        val button = v.findViewById<Button>(R.id.faqButton)

        init {
            button.setSafeOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    // play Lottie animation on view attached to recycler view
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.setOnClickListener(holder as? View.OnClickListener)
        when (holder) {
            is ImageVH -> {
                if (isHighEndDevice(context)) {
                    lottiesMap[holder.adapterPosition] = WeakReference(holder.lottieAnimation)
                    holder.lottieAnimation.playAnimation()
                }
            }
        }
    }

    // pause Lottie animation on view attached to recycler view
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.setOnClickListener(null)
        when (holder) {
            is ImageVH -> {
                lottiesMap.remove(holder.adapterPosition)
                holder.lottieAnimation.pauseAnimation()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val (layout, cardType) = when (viewType) {
            0 -> Pair(R.layout.how_it_works_title, TitleVH::class)
            1 -> Pair(R.layout.how_it_works_image, ImageVH::class)
            2 -> Pair(R.layout.how_it_works_paragraph, ParagraphVH::class)
            3 -> Pair(R.layout.how_it_works_separator, SeparatorVH::class)
            4 -> Pair(R.layout.how_it_works_footer, FooterVH::class)
            5 -> Pair(R.layout.how_it_works_paragraph_title, ParagraphTitleVH::class)
            else -> error("Unhandled viewType $viewType")
        }

        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return cardType.primaryConstructor!!.call(this, v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is HowItWorksItem.Title -> 0
            is HowItWorksItem.Image -> 1
            is HowItWorksItem.Paragraph -> 2
            is HowItWorksItem.Separator -> 3
            is HowItWorksItem.Footer -> 4
            is HowItWorksItem.ParagraphTitle -> 5
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataItem = data[position]

        when (holder) {
            is TitleVH -> {
                val item = dataItem as HowItWorksItem.Title
                holder.title.text = item.title
            }
            is ImageVH -> {
                val item = dataItem as HowItWorksItem.Image

                if (isHighEndDevice(context)) {
                    holder.lottieAnimation.setAnimation(item.animation)
                } else {
                    holder.lottieAnimation.setImageResource(item.image)
                }

                val constraintSet = ConstraintSet()
                constraintSet.clone(holder.itemView as ConstraintLayout)
                constraintSet.setDimensionRatio(R.id.lottieAnimation, item.ratio)

                constraintSet.applyTo(holder.itemView as ConstraintLayout)
            }
            is ParagraphVH -> {
                val item = dataItem as HowItWorksItem.Paragraph
                holder.text.text = item.text
            }
            is ParagraphTitleVH -> {
                val item = dataItem as HowItWorksItem.ParagraphTitle
                holder.text.text = item.text
            }
        }
    }
}

interface HowItWorksClickListener {
    fun onClick(item: HowItWorksItem)
}

sealed class HowItWorksItem {
    class Title(val title: String) : HowItWorksItem()
    class Image(@RawRes val animation: Int, @DrawableRes val image: Int, val ratio: String) : HowItWorksItem()
    class ParagraphTitle(val text: String) : HowItWorksItem()
    class Paragraph(val text: String) : HowItWorksItem()
    class Separator : HowItWorksItem()
    class Footer : HowItWorksItem()
}
