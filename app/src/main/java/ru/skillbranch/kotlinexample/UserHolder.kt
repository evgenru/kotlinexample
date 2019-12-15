package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {

    private val map = mutableMapOf<String, User>()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }

    fun requestAccessCode(login: String){
        map[login]!!.requestAccessCode()
    }

    fun registerUser(fullName: String, email: String, password: String): User {
        return User.makeUser(fullName, email = email, password = password).also {
            if (map.containsKey(it.login)) {
                throw IllegalArgumentException("A user with this email already exists")
            }
            map[it.login] = it
        }
    }

    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        return User.makeUser(fullName, phone = rawPhone).also {
            if (map.containsKey(rawPhone)) {
                throw IllegalArgumentException("A user with this phone already exists")
            }
            map[rawPhone] = it
        }
    }

    fun loginUser(login: String, password: String): String? {
        return map[login.trim()]?.let {
            if (it.checkPassword(password))
                it.userInfo
            else
                null
        }
    }
}