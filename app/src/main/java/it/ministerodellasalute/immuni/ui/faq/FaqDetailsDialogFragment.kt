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

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.faq_details_dialog.*

class FaqDetailsDialogFragment : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.faq_details_dialog)

        val args = navArgs<FaqDetailsDialogFragmentArgs>()

        bindData(args.value.questionAndAnswer)
    }

    private fun bindData(questionAndAnswer: QuestionAndAnswer) {
        setTitle(questionAndAnswer.question)
        question.text = questionAndAnswer.question
        answer.text = questionAndAnswer.answer
    }
}
