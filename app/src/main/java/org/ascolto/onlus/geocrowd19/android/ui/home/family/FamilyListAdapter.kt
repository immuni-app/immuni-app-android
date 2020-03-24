package org.ascolto.onlus.geocrowd19.android.ui.home.family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.*

class FamilyListAdapter(val clickListener: FamilyClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = AscoltoApplication.appContext
    var items = mutableListOf<FamilyItemType>()

    inner class UserCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var name: TextView = v.findViewById(R.id.name)
        val age: TextView = v.findViewById(R.id.age)
        val userId: TextView = v.findViewById(R.id.userId)
        val icon: ImageView = v.findViewById(R.id.icon)
        val copyId: ImageView = v.findViewById(R.id.copyIdentifier)

        init {
            copyId.setOnClickListener {
                val userItem = items[adapterPosition] as UserCard
                userItem.userIdTapped = true
                clickListener.onClick(userItem)
            }
            userId.setOnClickListener {
                val userItem = items[adapterPosition] as UserCard
                userItem.userIdTapped = true
                clickListener.onClick(userItem)
            }
        }


        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class AddFamilyMemberCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {

        override fun onClick(v: View) {
            clickListener.onClick(items[adapterPosition])
        }
    }

    inner class AddFamilyMemberTutorialCardVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v), View.OnClickListener {

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
        if(viewType == 0) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.family_card_user, parent, false)

            return UserCardVH(v as ConstraintLayout)
        }
        else if(viewType == 1) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.family_card_button_item, parent, false)

            return AddFamilyMemberCardVH(v as ConstraintLayout)
        }
        else if(viewType == 2) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.family_card_tutorial_item, parent, false)

            return AddFamilyMemberTutorialCardVH(v as ConstraintLayout)
        }
        else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.family_header_card_item, parent, false)

            return FamilyHeaderCardVH(v as LinearLayout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payload: List<Any>
    ) {
        when(holder) {
            is UserCardVH -> {
                val item = items[position] as UserCard
                holder.name.text = when(item.user.isMain) {
                    true -> AscoltoApplication.appContext.resources.getString(R.string.you)
                    false -> item.user.name
                }
                holder.age.text = String.format(AscoltoApplication.appContext.resources.getString(R.string.age_placeholder), item.user.ageGroup)
                holder.userId.text = item.user.id
                holder.icon.setImageResource(when(item.user.gender) {
                    Gender.MALE -> listOf(
                        R.drawable.ic_male_purple,
                        R.drawable.ic_male_blue,
                        R.drawable.ic_male_violet,
                        R.drawable.ic_male_pink,
                        R.drawable.ic_male_yellow).shuffled().first()
                    Gender.FEMALE -> listOf(
                        R.drawable.ic_female_purple,
                        R.drawable.ic_female_blue,
                        R.drawable.ic_female_violet,
                        R.drawable.ic_female_pink,
                        R.drawable.ic_female_yellow).shuffled().first()
                })
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
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