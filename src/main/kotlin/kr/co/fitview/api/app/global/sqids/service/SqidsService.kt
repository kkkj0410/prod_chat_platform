package kr.co.fitview.api.app.global.sqids.service

import org.springframework.stereotype.Service
import org.sqids.Sqids


@Service
class SqidsService(
    private val sqids: Sqids
) {

    fun encode(pk: Long): String {
        val code = sqids.encode(listOf(pk))

        validateCodeLength(code)

        return code
    }


    fun decode(code: String): Long {

        validateCodeLength(code)

        return sqids.decode(code).firstOrNull()
            ?: throw IllegalArgumentException("잘못된 코드: $code")
    }

    private fun validateCodeLength(code: String) {
        if (code.length != 6) {
            throw IllegalStateException("코드 6자리가 아닙니다")
        }
    }

}