package org.ascolto.onlus.geocrowd19.android.ui.home.home

import android.graphics.Color
import android.media.Image
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bendingspoons.base.extensions.setTint
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.models.survey.Severity
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.*

class HomeListAdapter(val clickListener: HomeClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = AscoltoApplication.appContext
    var items = mutableListOf<HomeItemType>()

    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(false).build()

    inner class SurveyCardVH(v: LinearLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {

        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class HeaderCardVH(v: LinearLayout) : RecyclerView.ViewHolder(v) {

    }

    inner class GeolocationCardVH(v: LinearLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {

        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class NotificationsCardVH(v: LinearLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {

        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class SuggestionsCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var container: ConstraintLayout = v
        var title: TextView = v.findViewById(R.id.title)
        var icon: ImageView = v.findViewById(R.id.icon)
        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
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
        if(viewType == 0) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_survey_card_item, parent, false)

            return SurveyCardVH(v as LinearLayout)
        }
        else if(viewType == 1) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_geolocation_card_item, parent, false)

            return GeolocationCardVH(v as LinearLayout)
        }
        else if(viewType == 2) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_notifications_card_item, parent, false)

            return NotificationsCardVH(v as LinearLayout)
        }
        else if(viewType == 3) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_header_card_item, parent, false)

            return HeaderCardVH(v as LinearLayout)
        }
        else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_suggestions_card_item, parent, false)

            return SuggestionsCardVH(v as ConstraintLayout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payload: List<Any>
    ) {
        when(holder) {
            is SuggestionsCardVH -> {
                val item = items[position] as SuggestionsCard
                val color = when(item.severity) {
                    Severity.LOW -> context.resources.getColor(R.color.background)
                    Severity.MID -> context.resources.getColor(R.color.card_yellow)
                    Severity.HIGH -> context.resources.getColor(R.color.card_red)
                }

                holder.container.background = when(item.severity) {
                    Severity.LOW -> context.resources.getDrawable(R.drawable.card_white_bg)
                    Severity.MID -> context.resources.getDrawable(R.drawable.card_yellow_bg)
                    Severity.HIGH -> context.resources.getDrawable(R.drawable.card_red_bg)
                }
                holder.container.setTint(color)
                holder.title.text = HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
                
                holder.title.setTextColor(when(item.severity) {
                    Severity.LOW -> context.resources.getColor(R.color.colorPrimary)
                    Severity.MID -> context.resources.getColor(R.color.background)
                    Severity.HIGH -> context.resources.getColor(R.color.background)
                })
                holder.icon.setImageResource(when(item.severity) {
                    Severity.LOW -> R.drawable.ic_info
                    Severity.MID -> R.drawable.ic_danger
                    Severity.HIGH -> R.drawable.ic_danger
                })
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is SurveyCard -> 0
            is EnableGeolocationCard -> 1
            is EnableNotificationCard -> 2
            is HeaderCard -> 3
            is SuggestionsCard -> 4
        }
    }

    override fun getItemCount(): Int = items.size
}

interface HomeClickListener {
    fun onClick(item: HomeItemType)
}