package kr.co.fitview.api.app.global.redis.enums

enum class RedisKey(val prefix: String) {
    MEMBER_LOCAL("member:%d:local")

    ;

    fun key(memberId: Long, seed: Long): String = String.format(prefix, memberId) + ":$seed"
}