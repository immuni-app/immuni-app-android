package org.immuni.android.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.immuni.android.extensions.storage.KVStorage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.db.DATABASE_VERSION
import org.immuni.android.models.User
import org.koin.core.KoinComponent

@JsonClass(generateAdapter = true)
data class UserList(
    @field:Json(name = "users") val users: List<User> = listOf()
)

class UserManager(
    val storage: KVStorage
) : KoinComponent {

    companion object {
        private const val usersKey = "users"
        private const val SETUP_COMPLETE = "SETUP_COMPLETE_$DATABASE_VERSION"
        private const val WELCOME_COMPLETE = "WELCOME_COMPLETE"
        private const val ONBOARDING_COMPLETE = "ONBOARDING_COMPLETE"
    }

    fun isSetupComplete(): Boolean {
        return storage.load<Boolean>(SETUP_COMPLETE) == true
    }

    fun setSetupCompleted(complete: Boolean) {
        storage.save(SETUP_COMPLETE, complete)
    }

    fun isWelcomeComplete(): Boolean {
        return storage.load<Boolean>(WELCOME_COMPLETE) == true
    }

    fun setWelcomeCompleted(complete: Boolean) {
        storage.save(WELCOME_COMPLETE, complete)
    }

    fun isOnboardingComplete(): Boolean {
        return storage.load<Boolean>(ONBOARDING_COMPLETE) == true
    }

    fun setOnboardingCompleted(complete: Boolean) {
        storage.save(ONBOARDING_COMPLETE, complete)
    }

    fun users(): List<User> {
        val foo = storage.load(usersKey, UserList())
        return foo.users
    }

    fun usersLiveData(): LiveData<List<User>> {
        val ld = MediatorLiveData<List<User>>()
        ld.addSource(storage.liveData<UserList>(usersKey)) {
            ld.value = it.users
        }
        return ld
    }

    fun mainUser(): User? {
        return users().find { it.isMain }
    }

    fun user(id: String): User? {
        return users().find { it.id == id }
    }

    fun familyMembers(): List<User> {
        return users().filter { !it.isMain }
    }

    private fun saveUsers(users: List<User>) {
        storage.save(usersKey, UserList(users))
    }

    fun addUser(user: User) {
        val users = users().toMutableList()
        users.add(user)
        saveUsers(users)
    }

    fun updateUser(user: User) {
        val users = users().toMutableList()
        users[users.indexOfFirst { it.id == user.id }] = user
        saveUsers(users)
    }

    fun deleteUser(userId: String) {
        val remainingUsers = users().toMutableList().filter { it.id != userId }
        saveUsers(remainingUsers)
    }

    fun indexForUser(userId: String): Int {
        val (_, index) = users().mapIndexed { index, user ->
            Pair(user, index)
        }.first { (user, _) -> user.id == userId }
        return index
    }
}
