package kr.co.fitview.api.app.domain.banner.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.app_feedback.entity.AppFeedback
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class BannerControllerTest : ControllerTestSupport() {

    @DisplayName("각 회원 전용 활성화 전체 배너 조회 API")
    @Test
    fun bannerActiveGet() {
        // given
        given(bannerQueryService.findActiveBanners(any()))
            .willReturn(
                listOf(
                    BannerActiveResponse.General(
                        bannerId = 1,
                        type = BannerType.APP_FEEDBACK,
                    )
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/banners/active")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].bannerId").value(1))
            .andExpect(jsonPath("$.data[0].type").value(BannerType.APP_FEEDBACK.name))
    }

    @DisplayName("배너 닫기 저장 API")
    @Test
    fun bannerDismiss() {
        // given


        // when // then
        mockMvc.perform(
            post("/api/v1/banners/{bannerId}/dismiss", 1)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());
    }

}