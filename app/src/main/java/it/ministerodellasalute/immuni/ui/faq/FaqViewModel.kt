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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.ministerodellasalute.immuni.api.services.Faq
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class FaqViewModel(
    val settingsManager: ConfigurationSettingsManager
) : ViewModel(), KoinComponent {

    private val _questionAndAnswers = MutableLiveData<FaqListViewData>()
    val questionAndAnswers: LiveData<FaqListViewData> = _questionAndAnswers

    private var filterJob: Job? = null

    fun onFaqSearchChanged(text: String) {
        // Stop previous filter, since it's not needed anymore
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            settingsManager.faqs.collect { faqList ->
                val exactMatch = mutableListOf<Faq>()
                val fuzzyMatch = mutableListOf<Faq>()

                faqList?.forEach { faq ->
                    if (!isActive) return@collect
                    when {
                        faq.title.contains(text, ignoreCase = true) -> exactMatch += faq
                        faq.title.fuzzyContains(text) -> fuzzyMatch += faq
                    }
                }

                val filteredFaq = (exactMatch + fuzzyMatch).map { faq ->
                    if (!isActive) return@collect
                    QuestionAndAnswer(
                        faq.title,
                        faq.content
                    )
                }

                _questionAndAnswers.postValue(FaqListViewData(text, filteredFaq))
            }
        }
    }

    init {
        onFaqSearchChanged("")
    }
}

data class FaqListViewData(val highlight: String, val faqList: List<QuestionAndAnswer>)

fun String.fuzzyContains(other: String): Boolean {
    val text = this
    if (other.length > text.length) return false

    var otherIdx = 0
    var textIdx = 0

    while (otherIdx != other.length) {
        if (textIdx == text.length) return false

        if (other[otherIdx].equals(text[textIdx], ignoreCase = true)) {
            otherIdx += 1
        }

        textIdx += 1
    }
    return true
}
