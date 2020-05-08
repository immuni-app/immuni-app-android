package org.immuni.android.models.survey

import java.io.Serializable

/**
 * Answer to a [Survey] [Question] by the user.
 * Its concrete subclasses are [SimpleAnswer] and [CompositeAnswer].
 */
sealed class Answer : Serializable

/**
 * [Answer] with a single component, identified by its [index].
 * It's used in conjunction with [RadioWidget] or [MultipleChoicesWidget] [Question]s.
 */
data class SimpleAnswer(val index: AnswerIndex) : Answer()

/**
 * [Answer] with multiple components, identified by their [componentIndexes].
 * It's used in conjunction with [PickerWidget] [Question]s.
 */
data class CompositeAnswer(val componentIndexes: List<AnswerIndex>) : Answer()
