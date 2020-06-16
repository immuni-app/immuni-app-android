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

package it.ministerodellasalute.immuni.ui.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.colorHighlight
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener

class FaqListAdapter(private val clickListener: FaqClickListener) :
    RecyclerView.Adapter<FaqListAdapter.FaqVH>() {

    private var highlight: String = ""
    private var data: List<QuestionAndAnswer> = emptyList()

    fun submitData(newData: List<QuestionAndAnswer>, newHighlight: String) {
        val diffResult =
            DiffUtil.calculateDiff(
                QuestionAndAnswerDiffCallback(
                    oldList = data,
                    newList = newData,
                    oldHighlight = highlight,
                    newHighlight = newHighlight
                )
            )
        data = newData
        highlight = newHighlight
        diffResult.dispatchUpdatesTo(this)
    }

    private fun onItemClick(pos: Int) {
        if (pos != RecyclerView.NO_POSITION) {
            clickListener.onClick(data[pos])
        }
    }

    inner class FaqVH(v: View) : RecyclerView.ViewHolder(v) {
        val question: TextView = v.findViewById(R.id.question)

        init {
            v.setSafeOnClickListener { onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.faq_list_item, parent, false)
        return FaqVH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: FaqVH, position: Int) {
        val dataItem = data[position]
        val question = dataItem.question

        val highlightColor = holder.question.context.getColorCompat(R.color.colorPrimary)
        holder.question.text = question.colorHighlight(highlight, highlightColor)
    }
}

class QuestionAndAnswerDiffCallback(
    private val oldList: List<QuestionAndAnswer>,
    private val newList: List<QuestionAndAnswer>,
    private val oldHighlight: String,
    private val newHighlight: String
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].answer == newList[newItemPosition].answer && oldHighlight == newHighlight
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].answer == newList[newItemPosition].answer
    }
}

interface FaqClickListener {
    fun onClick(item: QuestionAndAnswer)
}
