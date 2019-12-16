package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import ru.skillbranch.kotlinexample.User.Factory.fullNameToPair
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    private var meta: Map<String, Any>? = null
) {
    val fullName: String = listOfNotNull(firstName, lastName)
        .joinToString(" ")
        .capitalize()

    val initials: String = listOfNotNull(firstName, lastName)
        .map { it.first().toUpperCase() }
        .joinToString(" ")

    var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")?.also {
                if (!it.matches("\\+\\d{11}".toRegex())) {
                    throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
                }
            }
        }

    private var _login: String? = null
    var login: String
        set(value) {
            _login = value?.toLowerCase()
        }
        get() = _login!!

    private var salt: String = ""
        get() {
            if (field.isBlank()) {
                field = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
            }
            return field
        }

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null


    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    private constructor(
        firstName: String,
        lastName: String?,
        email: String? = null,
        salt: String,
        passwordHash: String,
        phone: String? = null
    ) : this(firstName, lastName, email, phone, mapOf("src" to "csv")) {
        this.salt = salt
        this.passwordHash = passwordHash
    }

    init {
        check(!firstName.isBlank()) { "FirstName must be not blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "Email or phone must be not blank" }

        phone = rawPhone
        login = email ?: phone!!
    }

    val userInfo = """
      firstName: $firstName
      lastName: $lastName
      login: $login
      fullName: $fullName
      initials: $initials
      email: $email
      phone: $phone
      meta: $meta
    """.trimIndent()

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass))
            passwordHash = encrypt(newPass)
        else
            throw IllegalArgumentException("The entered password does not match the current password")
    }

    fun requestAccessCode() {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(phone!!, code)
    }

    private fun encrypt(password: String) = salt.plus(password).md5()


    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    this.append(possible[index])
                }
            }
        }.toString()
    }

    private fun sendAccessCodeToUser(phone: String, code: String) {
        println("... sending access code: $code on $phone")
    }

    companion object Factory {

        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email,
                    password
                )
                else -> throw IllegalArgumentException("Email or phone must not be null or blank")
            }
        }

        fun makeUser(
            fullName: String,
            email: String? = null,
            salt: String,
            passwordHash: String,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()
            return User(firstName, lastName, email, salt, passwordHash, phone)
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException(
                            "Fullname must be only first name " +
                                    "and last name, current split result ${this@fullNameToPair}"
                        )
                    }
                }

        }

    }
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(toByteArray())
    val hexString = BigInteger(1, digest).toString(16)
    return hexString.padStart(32, '0')
}
