package com.reevent.app.core.auth

/** Keeps sign-up and recovery password checks consistent without exposing password content. */
object PasswordRules {
    const val MIN_LENGTH = 8

    fun isValid(password: String): Boolean = password.length >= MIN_LENGTH
    fun matchesConfirmation(password: String, confirmation: String): Boolean = password == confirmation
}
