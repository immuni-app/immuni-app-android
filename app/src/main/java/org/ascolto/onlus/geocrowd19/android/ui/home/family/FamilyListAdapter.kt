package org.ascolto.onlus.geocrowd19.android.ui.home.family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeManager
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.db.entity.iconResource
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class FamilyListAdapter(val clickListener: FamilyClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    val context = AscoltoApplication.appContext
    var items = mutableListOf<FamilyItemType>()
    val concierge: ConciergeManager by inject()

    inner class UserCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var name: TextView = v.findViewById(R.id.name)
        val age: TextView = v.findViewById(R.id.age)
        val icon: ImageView = v.findViewById(R.id.icon)
        val uploadButton: TextView = v.findViewById(R.id.uploadButton)

        init {
            uploadButton.setOnClickListener {
                val userItem = items[adapterPosition] as UserCard
                userItem.uploadTapped = true
                clickListener.onClick(userItem)
            }
        }

        override fun onClick(v: View) {
            val userItem = items[adapterPosition] as UserCard
            userItem.uploadTapped = false
            clickListener.onClick(userItem)
        }
    }

    inner class AddFamilyMemberCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v),
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

    inner class AddFamilyMemberTutorialCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v),
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

    inner class FamilyHeaderCardVH(v: LinearLayout) : RecyclerView.ViewHolder(v)

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.setOnClickListener(holder as? View.OnClickListener)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.setOnClickListener(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = when (viewType) {
            0 -> R.layout.family_card_user
            1 -> R.layout.family_card_button_item
            2 -> R.layout.family_card_tutorial_item
            else -> R.layout.family_header_card_item
        }
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return when (viewType) {
            0 -> UserCardVH(v as ConstraintLayout)
            1 -> AddFamilyMemberCardVH(v as ConstraintLayout)
            2 -> AddFamilyMemberTutorialCardVH(v as ConstraintLayout)
            else -> FamilyHeaderCardVH(v as LinearLayout)
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
                    true -> AscoltoApplication.appContext.resources.getString(R.string.you)
                    false -> item.user.name
                }
                holder.age.text = item.user.ageGroup.humanReadable(context)
                holder.icon.setImageResource(
                    item.user.gender.iconResource(
                        context,
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