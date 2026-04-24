package kr.co.fitview.api.app.global.health

import kr.co.fitview.api.app.ControllerTestSupport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class HealthControllerTest  : ControllerTestSupport() {


    @DisplayName("헬스체크 API")
    @Test
    fun healthCheck() {
        // given

        // when // then
        mockMvc.perform(
            get("/health")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
    }
}