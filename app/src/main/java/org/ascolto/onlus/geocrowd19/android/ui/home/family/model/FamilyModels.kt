package org.ascolto.onlus.geocrowd19.android.ui.home.family.model

import org.ascolto.onlus.geocrowd19.android.models.User

// this represent an item in the home list

sealed class FamilyItemType

data class UserCard(
    val user: User,
    val userIndex: Int
): FamilyItemType() {
    var uploadTapped: Boolean = false
}

class AddFamilyMemberTutorialCard: FamilyItemType()

class AddFamilyMemberButtonCard: FamilyItemType()

class FamilyHeaderCard(
    val title: String
): FamilyItemType()
