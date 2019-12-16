package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {

    private val map = mutableMapOf<String, User>()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun requestAccessCode(login: String) {
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

    fun importUsers(list: List<String>): List<User> {
        return list.filter { it.isNotBlank() }
            .map {str ->
                val fields = str.split(";").map { it.trim() }
                val fullname = fields[0]
                val email = fields[1].ifEmpty { null }
                val saltPass = fields[2].split(":")
                val salt = saltPass[0]
                val passwordHash = saltPass[1]
                val phone = fields[3].ifEmpty { null }

                registerUserFromImport(fullname, email, salt, passwordHash, phone)
            }
    }

    private fun registerUserFromImport(fullname: String, email: String?, salt: String, passwordHash: String, phone: String?): User {
        return User.makeUser(fullname, email = email, salt = salt, passwordHash = passwordHash, phone = phone).also {
            if (map.containsKey(it.login)) {
                return map[it.login]!!
            }
            map[it.login] = it
        }
    }
}