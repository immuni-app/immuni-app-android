package org.ascolto.onlus.geocrowd19.android.ui.log.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.visible
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.model.QuestionType
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.model.ResumeItemType
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.model.UserType

class ResumeListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = AscoltoApplication.appContext
    private var items = mutableListOf<ResumeItemType>()

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
        if(viewType == 0) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.resume_user_item, parent, false)

            return UserVH(v as ConstraintLayout)
        }
        else {
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
        when(holder) {
            is UserVH -> {
                val item = items[position] as UserType
                holder.name.text = when(item.user.isMain) {
                    true -> AscoltoApplication.appContext.resources.getString(R.string.you)
                    false -> item.user.name
                }
                holder.age.text = item.user.ageGroup.humanReadable(AscoltoApplication.appContext)
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
            is QuestionVH -> {
                val item = items[position] as QuestionType
                holder.question.text = item.question
                holder.answer.text = item.answer
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is UserType -> 0
            is QuestionType -> 1
        }
    }

    override fun getItemCount(): Int = items.size
}