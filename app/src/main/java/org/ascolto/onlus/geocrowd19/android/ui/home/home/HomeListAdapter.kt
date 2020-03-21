package org.ascolto.onlus.geocrowd19.android.ui.home.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.*

class HomeListAdapter(val clickListener: HomeClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    inner class SuggestionsCardVH(v: LinearLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {

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
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_survey_card_item, parent, false)

        return SurveyCardVH(v as LinearLayout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payload: List<Any>
    ) {
        // TODO
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