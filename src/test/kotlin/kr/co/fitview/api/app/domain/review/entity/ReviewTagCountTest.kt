package kr.co.fitview.api.app.domain.review.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ReviewTagCountTest : IntegrationTestSupport(){

    @DisplayName("리뷰에서 받은 태그값을 증가시킨다.")
    @Test
    fun increaseCount() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        val reviewCategory = ReviewCategory(
            displayText = "reviewCategory",
            seq = 100
        )

        val reviewTag = ReviewTag(
            reviewCategory = reviewCategory,
            displayText = "reviewTag",
            seq = 100,
        )

        val reviewTagCount = ReviewTagCount(
            member = member,
            reviewTag = reviewTag,
            count = 0
        )

        // when
        reviewTagCount.increaseCount(1)

        // then
        assertThat(reviewTagCount.count).isEqualTo(1)
    }

    @DisplayName("리뷰에서 받은 태그값 개수는 감소시킬 수 없다.")
    @Test
    fun increaseCountNotNegative() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        val reviewCategory = ReviewCategory(
            displayText = "reviewCategory",
            seq = 100
        )

        val reviewTag = ReviewTag(
            reviewCategory = reviewCategory,
            displayText = "reviewTag",
            seq = 100,
        )

        val reviewTagCount = ReviewTagCount(
            member = member,
            reviewTag = reviewTag,
            count = 0
        )

        // when & then
        assertThatThrownBy {
            reviewTagCount.increaseCount(-1)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("증가시킬 카운트는 양수여야 합니다.")
    }

}