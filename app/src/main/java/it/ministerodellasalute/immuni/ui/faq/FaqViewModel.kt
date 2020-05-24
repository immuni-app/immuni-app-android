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
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.models.FetchFaqsResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class FaqViewModel(
    private val settingsManager: ConfigurationSettingsManager
) : ViewModel(), KoinComponent {

    private val _questionAndAnswers = MutableLiveData<List<QuestionAndAnswer>>()
    val questionAndAnswers: LiveData<List<QuestionAndAnswer>> = _questionAndAnswers

    val loading = MutableLiveData<Boolean>()
    val loadingError = MutableLiveData<Event<Boolean>>()

    init {
        loadQuestionAndAnswers()
    }

    fun loadQuestionAndAnswers(delay: Long = 0L) {
        viewModelScope.launch {
            loading.value = true
            delay(delay)
            val response = settingsManager.fetchFaqs()
            loading.value = false
            when (response) {
                is FetchFaqsResult.Success -> {
                    val faqs = response.faqs
                    _questionAndAnswers.value = faqs.faqs.filterNotNull().map {
                        QuestionAndAnswer(
                            it.title,
                            it.content
                        )
                    }
                }
                else -> {
                    loadingError.value = Event(true)
                }
            }
        }
    }
}
