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

package it.ministerodellasalute.immuni.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.color
import it.ministerodellasalute.immuni.extensions.view.*
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import kotlin.reflect.full.primaryConstructor

class HomeListAdapter(
    val context: Context,
    private val clickListener: HomeClickListener,
    val settingsManager: ConfigurationSettingsManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = mutableListOf<HomeItemType>()

    fun update(newList: List<HomeItemType>) {
        val diffCallback =
            HomeDiffCallback(
                items,
                newList
            )
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        diffResult.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(newList)
    }

    private fun onItemClick(pos: Int, @IdRes viewId: Int = -1) {
        if (pos != RecyclerView.NO_POSITION) {
            clickListener.onClick(items[pos], viewId)
        }
    }

    inner class ProtectionCardVH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val subtitle: TextView = v.findViewById(R.id.subtitle)
        val reactivate: Button = v.findViewById(R.id.reactivate)
        val lottieBg: LottieAnimationView = v.findViewById(R.id.lottieAnimation)
        val lottieFg: LottieAnimationView = v.findViewById(R.id.lottieAnimationForeground)
        val knowMore: TextView = v.findViewById(R.id.knowMore)

        init {
            reactivate.setSafeOnClickListener { onItemClick(adapterPosition, R.id.reactivate) }
            knowMore.setSafeOnClickListener { onItemClick(adapterPosition, R.id.knowMore) }
            lottieBg.setSafeOnClickListener { onItemClick(adapterPosition, R.id.knowMore) }
        }
    }

    inner class SectionHeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.header)
    }

    inner class InformationHowAppWorksVH(v: View) : RecyclerView.ViewHolder(v) {
        val container: ViewGroup = v.findViewById(R.id.container)
    }

    inner class InformationSelfCareVH(v: View) : RecyclerView.ViewHolder(v)

    inner class CountriesOfInterestVH(v: View) : RecyclerView.ViewHolder(v)

    inner class ReportPositivityVH(v: View) : RecyclerView.ViewHolder(v)

    inner class GreenPassVH(v: View) : RecyclerView.ViewHolder(v)

    inner class DisableExposureApiVH(v: View) : RecyclerView.ViewHolder(v) {
        val disableExposureApi: Button = v.findViewById(R.id.disableExposureApi)

        init {
            disableExposureApi.setSafeOnClickListener { onItemClick(adapterPosition) }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.setSafeOnClickListener { onItemClick(holder.adapterPosition) }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.setOnClickListener(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val (layout, cardType) = when (viewType) {
            0 -> Pair(R.layout.home_protection_state_card, ProtectionCardVH::class)
            1 -> Pair(R.layout.home_section_header_item, SectionHeaderVH::class)
            2 -> Pair(R.layout.home_green_certificate_card, GreenPassVH::class)
            3 -> Pair(R.layout.home_report_positivity_card, ReportPositivityVH::class)
            4 -> Pair(R.layout.home_countries_of_interest, CountriesOfInterestVH::class)
            5 -> Pair(R.layout.home_information_how_app_works_card, InformationHowAppWorksVH::class)
            6 -> Pair(R.layout.home_information_self_care_card, InformationSelfCareVH::class)
            7 -> Pair(R.layout.home_disable_exposure_api, DisableExposureApiVH::class)
            else -> error("Unhandled viewType $viewType")
        }

        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return cardType.primaryConstructor!!.call(this, v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payload: List<Any>
    ) {
        val resources = context.resources

        when (holder) {
            is SectionHeaderVH -> {
                val item = items[position] as SectionHeader
                holder.title.text = item.title
            }
            is InformationHowAppWorksVH -> {
                val item = items[position] as HowItWorksCard
                holder.container.clipToOutline = true
            }
            is ProtectionCardVH -> {
                val item = items[position] as ProtectionCard
                if (item.active) {
                    holder.knowMore.visible()
                    holder.reactivate.gone()
                    holder.title.text = resources.getString(R.string.home_protection_active)
                        .color(
                            '{', '}',
                            resources.getColor(R.color.colorPrimary)
                        )

                    val reopenReminder = settingsManager.settings.value.reopenReminder

                    holder.subtitle.text = when (reopenReminder) {
                        true -> resources.getString(R.string.home_view_service_active_advice)
                        false -> resources.getString(R.string.home_view_service_active_subtitle)
                    }
                    // animate fade-in to avoid glitch on tab change
                    holder.lottieBg.alpha = 0f
                    holder.lottieBg.setAnimation(R.raw.lottie_shield_full)
                    holder.lottieBg.visible()
                    holder.lottieBg.animateShow()
                    holder.itemView.post { holder.lottieBg.playAnimation() }

                    holder.lottieFg.alpha = 0f
                    holder.lottieFg.visible()
                    holder.lottieFg.animateShow()
                    holder.itemView.post { holder.lottieFg.playAnimation() }
                } else {
                    holder.knowMore.gone()
                    holder.reactivate.visible()
                    holder.title.text = resources.getString(R.string.home_protection_not_active)
                        .color(
                            '{', '}',
                            resources.getColor(R.color.danger)
                        )
                    holder.subtitle.text =
                        resources.getString(R.string.home_view_service_not_active_subtitle)
                    holder.lottieBg.alpha = 0f
                    holder.lottieBg.setAnimation(R.raw.lottie_protection_not_active)
                    holder.lottieBg.visible()
                    holder.lottieBg.animateShow()
                    holder.itemView.post { holder.lottieBg.playAnimation() }

                    holder.lottieFg.gone()

                    if (item.status != ExposureStatus.None()) {
                        holder.reactivate.setTint(context.getColor(R.color.grey_dark))
                    } else {
                        holder.reactivate.setTint(context.getColor(R.color.danger))
                    }
                }
            }
            is DisableExposureApiVH -> {
                holder.disableExposureApi.isEnabled =
                    (items[position] as DisableExposureApi).isEnabled
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProtectionCard -> 0
            is SectionHeader -> 1
            is GreenPassCard -> 2
            is ReportPositivityCard -> 3
            is CountriesOfInterestCard -> 4
            is HowItWorksCard -> 5
            is SelfCareCard -> 6
            is DisableExposureApi -> 7
        }
    }

    override fun getItemCount(): Int = items.size
}

interface HomeClickListener {
    fun onClick(item: HomeItemType, @IdRes viewId: Int = -1)
}
