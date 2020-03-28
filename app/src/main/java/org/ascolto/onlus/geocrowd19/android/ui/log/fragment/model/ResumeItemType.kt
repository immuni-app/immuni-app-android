package org.ascolto.onlus.geocrowd19.android.ui.log.fragment.model

import org.ascolto.onlus.geocrowd19.android.models.User

sealed class ResumeItemType

class UserType(
    val user: User
): ResumeItemType()

class QuestionType(
    val question: String,
    val answer: String
): ResumeItemType()