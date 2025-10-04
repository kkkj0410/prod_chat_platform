package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.member.service.MemberService
import org.springframework.test.context.bean.override.mockito.MockitoBean


class MemberControllerTest : ControllerTestSupport(){

    @MockitoBean
    private lateinit var memberService: MemberService


}