package org.immuni.android.ui.log.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.immuni.android.ids.ConciergeManager
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.models.iconResource
import org.immuni.android.ui.log.fragment.model.QuestionType
import org.immuni.android.ui.log.fragment.model.ResumeItemType
import org.immuni.android.ui.log.fragment.model.UserType
import org.koin.core.KoinComponent
import org.koin.core.inject

class ResumeListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    val context = ImmuniApplication.appContext
    private var items = mutableListOf<ResumeItemType>()
    val concierge: ConciergeManager by inject()

    fun update(newList: List<ResumeItemType>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class UserVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val name: TextView = v.findViewById(R.id.name)
        val age: TextView = v.findViewById(R.id.age)
    }

    inner class QuestionVH(v: ConstraintLayout) : RecyclerView.ViewHolder(v) {
        val question: TextView = v.findViewById(R.id.question)
        val answer: TextView = v.findViewById(R.id.answer)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.resume_user_item, parent, false)

            return UserVH(v as ConstraintLayout)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.resume_question_item, parent, false)

            return QuestionVH(v as ConstraintLayout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payload: List<Any>
    ) {
        when (holder) {
            is UserVH -> {
                val item = items[position] as UserType
                holder.name.text = when (item.user.isMain) {
                    true -> ImmuniApplication.appContext.resources.getString(R.string.you)
                    false -> item.user.name
                }
                holder.age.text = item.user.ageGroup.humanReadable(ImmuniApplication.appContext)
                holder.icon.setImageResource(
                    item.user.gender.iconResource(
                        concierge.backupPersistentId.id,
                        item.userIndex
                    )
                )
            }
            is QuestionVH -> {
                val item = items[position] as QuestionType
                holder.question.text = item.question
                holder.answer.text = item.answer
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is UserType -> 0
            is QuestionType -> 1
        }
    }

    override fun getItemCount(): Int = items.size
}