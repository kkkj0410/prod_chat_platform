package kr.co.fitview.api.app.global.redis.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.response.withIsMe
import kr.co.fitview.api.app.global.stomp.constant.StompConstant
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired

class RedisServiceTest @Autowired constructor(
    private val redisService : RedisService
) : IntegrationTestSupport(){

    @DisplayName("캐시에 key-value를 저장한다.")
    @Test
    fun setKey() {
        // given

        // when
        redisService.setKey(
            key = "key",
            value = "value",
            minute = 5
        )

        // then
        then(redisClient).should().set(
            key = "key",
            value = "value",
            minute = 5
        )
    }

    @DisplayName("캐시에서 key의 value를 꺼낸다.")
    @Test
    fun getKey() {
        // given
        given(redisClient.get("key")).willReturn("value")

        // when
        val cacheValue = redisService.getKey(
            key = "key",
        )

        // then
        assertThat(cacheValue).isEqualTo("value")
    }

    @DisplayName("캐시에서 key가 없으면 value를 꺼내지 못한다.")
    @Test
    fun getKeyValueIsNull() {
        // given
        given(redisClient.get("key")).willReturn(null)

        // when
        val cacheValue = redisService.getKey(
            key = "key",
        )

        // then
        assertThat(cacheValue).isNull()
    }

    @DisplayName("인근 회원 조회 랜덤 키에 대한 값을 조회한다.")
    @Test
    fun getMemberLocalKey() {
        // given
        val randomMemberId = 3453533L
        val memberId = 1L
        val seed = 123L
        given(redisClient.get("member:$memberId:local:$seed")).willReturn(randomMemberId.toString())

        // when
        val findRandomMemberId = redisService.getMemberLocalKey(
            memberId = memberId,
            seed = seed
        )

        // then
        assertThat(findRandomMemberId).isEqualTo(randomMemberId)
    }

    @DisplayName("인근 회원 조회 랜덤 키를 캐시에 저장한다.")
    @Test
    fun setMemberLocalKey() {
        // given
        val memberId = 1L
        val randomMemberId = 123L
        val seed = 1234L

        // when
        redisService.setMemberLocalKey(
            memberId = memberId,
            randomMemberId = randomMemberId,
            seed = seed
        )

        // then
        then(redisClient).should().set(
            key = "member:$memberId:local:$seed",
            value = randomMemberId.toString(),
            minute = 30L
        )
    }


    @DisplayName("캐시에 key가 존재하지 않으면 값을 저장하고 true를 반환한다.")
    @Test
    fun setIfAbsentKeySuccess() {
        // given
        val key = "reserveKey"
        val value = "member123"
        val minute = 30L

        given(redisClient.setIfAbsent(key, value, minute)).willReturn(true)

        // when
        val isSaved = redisService.setIfAbsentKey(
            key = key,
            value = value,
            minute = minute
        )

        // then
        assertThat(isSaved).isTrue()
        then(redisClient).should().setIfAbsent(key, value, minute)
    }

    @DisplayName("캐시에 이미 key가 존재하면 값을 저장하지 않고 false를 반환한다.")
    @Test
    fun setIfAbsentKeyFail() {
        // given
        val key = "reserveKey"
        val value = "member123"
        val minute = 30L

        given(redisClient.setIfAbsent(key, value, minute)).willReturn(false)

        // when
        val isSaved = redisService.setIfAbsentKey(
            key = key,
            value = value,
            minute = minute
        )

        // then
        assertThat(isSaved).isFalse()
        then(redisClient).should().setIfAbsent(key, value, minute)
    }
}