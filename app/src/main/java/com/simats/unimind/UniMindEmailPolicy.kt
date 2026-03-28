package com.simats.unimind

/**
 * Allowed signup/login/forgot emails:
 * - any address @gmail.com
 * - Saveetha student style: local part must contain ".sse" before @, domain saveetha.com (e.g. name.sse@saveetha.com)
 */
object UniMindEmailPolicy {

    fun isAllowed(email: String): Boolean {
        val e = email.trim().lowercase()
        if (e.isEmpty() || e.count { it == '@' } != 1) return false
        val at = e.indexOf('@')
        val local = e.substring(0, at)
        val domain = e.substring(at + 1)
        if (local.isBlank() || domain.isBlank()) return false
        return when (domain) {
            "gmail.com" -> true
            "saveetha.com" -> local.contains(".sse")
            else -> false
        }
    }
}
