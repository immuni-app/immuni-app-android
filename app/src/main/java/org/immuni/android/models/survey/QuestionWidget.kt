package org.immuni.android.models.survey

/**
 * Describes the widget that should render the answer options the user can choose from.
 * Its concrete subclasses are: [PickerWidget], [MultipleChoicesWidget], and [RadioWidget]
 */
sealed class QuestionWidget

/**
 * The [answers] should be shown as a picker constituting of a list of components. For each
 * component, the user can choose one of the options.
 */
data class PickerWidget(
    val components: List<List<String>>
) : QuestionWidget()

/**
 * The [answers] should be shown as a list of checkbox items, of which the user can choose a minimum
 * of [minNumberOfAnswers] and up to [maxNumberOfAnswers].
 */
data class MultipleChoicesWidget(
    val minNumberOfAnswers: Int,
    val maxNumberOfAnswers: Int,
    val answers: List<String>
) : QuestionWidget()

/**
 * The [answers] should be shown as a list of radio items, of which the user must choose exactly
 * one.
 */
data class RadioWidget(
    val answers: List<String>
) : QuestionWidget()
