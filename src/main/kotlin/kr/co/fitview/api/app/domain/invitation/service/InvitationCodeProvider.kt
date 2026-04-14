package kr.co.fitview.api.app.domain.invitation.service

import org.springframework.stereotype.Component


@Component
class InvitationCodeProvider {
    private val OFFSET = 394817L
    private val PERMUTATION = intArrayOf(3, 0, 5, 1, 4, 2)
    private val INVERSE_PERM = intArrayOf(1, 3, 5, 0, 4, 2)

    fun encode(pk: Long): String {
        val shifted = (pk + OFFSET) % 1_000_000
        val digits = shifted.toString().padStart(6, '0')
        return PERMUTATION.map { digits[it] }.joinToString("")
    }

    fun decode(code: String): Long {
        val digits = INVERSE_PERM.map { code[it] }.joinToString("")
        val shifted = digits.toLong()
        return (shifted - OFFSET + 1_000_000) % 1_000_000
    }
}