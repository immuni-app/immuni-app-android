package org.ascolto.onlus.geocrowd19.android.ui.home.home

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.*

class HomeDiffCallback(private val oldList: List<HomeItemType>, private val newList: List<HomeItemType>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        var new = newList[newItemPosition]
        if(old::class != new::class) return false
        if(old is SurveyCardDone) return true
        if(old is EnableNotificationCard) return true
        if(old is EnableGeolocationCard) return true
        if(old is EnableBluetoothCard) return true
        if(old is SurveyCard) return true
        else return old == new
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return false
    }

    @Nullable
    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return Any()
    }
}