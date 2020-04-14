package org.immuni.android.ui.home.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.home.home.model.*
import kotlin.reflect.full.primaryConstructor

class HomeListAdapter(val clickListener: HomeClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = ImmuniApplication.appContext
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

    inner class SurveyCardVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var bottomMessage: TextView = v.findViewById(R.id.bottomMessage)
        val button: Button = v.findViewById(R.id.next)
        val title: TextView = v.findViewById(R.id.title)
        val question: ImageView = v.findViewById(R.id.question)
        val questionText: TextView = v.findViewById(R.id.questionText)

        init {
            button.setOnClickListener {
                (items[adapterPosition] as? SurveyCard)?.tapQuestion = false
                onItemClick(adapterPosition)
            }

            question.setOnClickListener {
                (items[adapterPosition] as? SurveyCard)?.tapQuestion = true
                onItemClick(adapterPosition)
            }

            questionText.setOnClickListener {
                (items[adapterPosition] as? SurveyCard)?.tapQuestion = true
                onItemClick(adapterPosition)
            }
        }

        override fun onClick(v: View) {
            (items[adapterPosition] as? SurveyCard)?.tapQuestion = false
            onItemClick(adapterPosition)
        }
    }

    private fun onItemClick(pos: Int) {
        if (pos != RecyclerView.NO_POSITION) {
            clickListener.onClick(items[pos])
        }
    }

    inner class SurveyCardDoneVH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.name)
        val bottomMessage: TextView = v.findViewById(R.id.bottomMessage)
    }

    inner class HeaderCardVH(v: View) : RecyclerView.ViewHolder(v) {

    }

    inner class GeolocationCardVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        val title: TextView = v.findViewById(R.id.title)

        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }
    }

    inner class NotificationsCardVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }
    }

    inner class BluetoothCardVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }
    }

    inner class SuggestionsCardWhiteVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var container = v as ConstraintLayout
        var title: TextView = v.findViewById(R.id.title)
        var icon: ImageView = v.findViewById(R.id.icon)
        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }
    }

    inner class SuggestionsCardYellowVH(v: View) : RecyclerView.ViewHolder(v),
        View.OnClickListener {
        var container = v as ConstraintLayout
        var title: TextView = v.findViewById(R.id.title)
        var icon: ImageView = v.findViewById(R.id.icon)
        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }
    }

    inner class SuggestionsCardRedVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var container = v as ConstraintLayout
        var title: TextView = v.findViewById(R.id.title)
        var icon: ImageView = v.findViewById(R.id.icon)
        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }
    }


    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.setOnClickListener(holder as? View.OnClickListener)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.setOnClickListener(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val (layout, cardType) = when (viewType) {
            0 -> Pair(R.layout.home_survey_card_item, SurveyCardVH::class)
            1 -> Pair(R.layout.home_geolocation_card_item, GeolocationCardVH::class)
            2 -> Pair(R.layout.home_notifications_card_item, NotificationsCardVH::class)
            3 -> Pair(R.layout.home_header_card_item, HeaderCardVH::class)
            4 -> Pair(R.layout.home_suggestions_card_white_item, SuggestionsCardWhiteVH::class)
            5 -> Pair(R.layout.home_suggestions_card_yellow_item, SuggestionsCardYellowVH::class)
            7 -> Pair(R.layout.home_survey_card_done_item, SurveyCardDoneVH::class)
            8 -> Pair(R.layout.home_bluetooth_card_item, BluetoothCardVH::class)
            else -> Pair(R.layout.home_suggestions_card_red_item, SuggestionsCardRedVH::class)
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
        val resources = ImmuniApplication.appContext.resources

        when (holder) {
            is SuggestionsCardWhiteVH -> {
                val item = items[position] as SuggestionsCardWhite
                holder.title.text =
                    HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            is SuggestionsCardYellowVH -> {
                val item = items[position] as SuggestionsCardYellow
                holder.title.text =
                    HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            is SuggestionsCardRedVH -> {
                val item = items[position] as SuggestionsCardRed
                holder.title.text =
                    HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            is SurveyCardVH -> {
                val item = items[position] as SurveyCard
                val hasOneDiaryToLog = item.surveysToLog == 1
                val string =
                    if (hasOneDiaryToLog) R.string.you_have_one_clinic_diary_to_update else R.string.you_have_clinic_diaries_to_update
                val placeholder = resources.getString(string)
                val titleHtml = if (hasOneDiaryToLog) placeholder else String.format(
                    placeholder,
                    item.surveysToLog
                )
                holder.bottomMessage.text =
                    HtmlCompat.fromHtml(titleHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

                if (item.doesMainUserNeedToLog) {
                    holder.title.text = resources.getString(R.string.update_your_clinic_diary)
                } else if (item.familyMembersThatNeedToLog > 0) {
                    holder.title.text = resources.getString(R.string.update_your_family_diary)
                }
            }
            is SurveyCardDoneVH -> {
                val item = items[position] as SurveyCardDone
                if (item.surveysLogged > 1) {
                    holder.name.text = resources.getString(R.string.clinic_diaries_updated)
                    holder.bottomMessage.text = resources.getString(R.string.clinic_diaries_updated_message)
                } else {
                    holder.name.text = resources.getString(R.string.clinic_diary_updated)
                    holder.bottomMessage.text = resources.getString(R.string.clinic_diary_updated_message)
                }
            }
            is GeolocationCardVH -> {
                val item = items[position] as EnableGeolocationCard
                when(item.type) {
                    GeolocationType.PERMISSIONS -> {
                        holder.title.text = resources.getString(R.string.consent_geolocation)
                    }
                    GeolocationType.GLOBAL_GEOLOCATION -> {
                        holder.title.text = resources.getString(R.string.enable_geolocation)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SurveyCard -> 0
            is EnableGeolocationCard -> 1
            is EnableNotificationCard -> 2
            is HeaderCard -> 3
            is SuggestionsCardWhite -> 4
            is SuggestionsCardYellow -> 5
            is SuggestionsCardRed -> 6
            is SurveyCardDone -> 7
            is EnableBluetoothCard -> 8
            is AddToWhiteListCard -> 9
        }
    }

    override fun getItemCount(): Int = items.size
}

interface HomeClickListener {
    fun onClick(item: HomeItemType)
}