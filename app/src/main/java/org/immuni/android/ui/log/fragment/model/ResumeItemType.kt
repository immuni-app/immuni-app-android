package org.immuni.android.ui.log.fragment.model

import org.immuni.android.models.User

sealed class ResumeItemType

class UserType(
    val user: User,
    val userIndex: Int
): ResumeItemType()

class QuestionType(
    val question: String,
    val answer: String
): ResumeItemType()