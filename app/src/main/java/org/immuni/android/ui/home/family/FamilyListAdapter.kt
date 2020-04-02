package org.immuni.android.ui.home.family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bendingspoons.concierge.ConciergeManager
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.db.entity.iconResource
import org.immuni.android.ui.home.family.model.*
import org.immuni.android.util.Flags
import org.koin.core.KoinComponent
import org.koin.core.inject

class FamilyListAdapter(val clickListener: FamilyClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    val context = ImmuniApplication.appContext
    var items = mutableListOf<FamilyItemType>()
    val concierge: ConciergeManager by inject()

    inner class UserCardVH(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var name: TextView = v.findViewById(R.id.name)
        val age: TextView = v.findViewById(R.id.age)
        val icon: ImageView = v.findViewById(R.id.icon)

        override fun onClick(v: View) {
            val userItem = items[adapterPosition] as UserCard
            userItem.uploadTapped = false
            clickListener.onClick(userItem)
        }
    }

    inner class AddFamilyMemberCardVH(v: View) : RecyclerView.ViewHolder(v),
        View.OnClickListener {

        private val addButton: Button = v.findViewById(R.id.addButton)

        init {
            addButton.setOnClickListener {
                clickListener.onClick(items[adapterPosition])
            }
        }

        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class AddFamilyMemberTutorialCardVH(v: View) : RecyclerView.ViewHolder(v),
        View.OnClickListener {

        private val addButton: Button = v.findViewById(R.id.addButton)

        init {
            addButton.setOnClickListener {
                clickListener.onClick(items[adapterPosition])
            }

            if (Flags.transient.shouldOpenAddRelativeActivity) {
                Flags.transient.shouldOpenAddRelativeActivity = false
                clickListener.onClick(AddFamilyMemberTutorialCard())
            }
        }

        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class FamilyHeaderCardVH(v: View) : RecyclerView.ViewHolder(v)

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.setOnClickListener(holder as? View.OnClickListener)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.setOnClickListener(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            0 -> R.layout.family_card_user
            1 -> R.layout.family_card_button_item
            2 -> R.layout.family_card_tutorial_item
            else -> R.layout.family_header_card_item
        }
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return when (viewType) {
            0 -> UserCardVH(v)
            1 -> AddFamilyMemberCardVH(v)
            2 -> AddFamilyMemberTutorialCardVH(v)
            else -> FamilyHeaderCardVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payload: List<Any>
    ) {
        when (holder) {
            is UserCardVH -> {
                val item = items[position] as UserCard
                holder.name.text = when (item.user.isMain) {
                    true -> ImmuniApplication.appContext.resources.getString(R.string.you)
                    false -> item.user.name
                }
                holder.age.text = item.user.ageGroup.humanReadable(context)
                holder.icon.setImageResource(
                    item.user.gender.iconResource(
                        concierge.backupPersistentId.id,
                        item.userIndex
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is UserCard -> 0
            is AddFamilyMemberButtonCard -> 1
            is AddFamilyMemberTutorialCard -> 2
            is FamilyHeaderCard -> 3
        }
    }

    override fun getItemCount(): Int = items.size
}

interface FamilyClickListener {
    fun onClick(item: FamilyItemType)
}