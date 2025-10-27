package kr.co.fitview.api.app.docs.exception

import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.GlobalExceptionController
import kr.co.fitview.api.app.global.exception.GlobalExceptionHandler
import kr.co.fitview.api.app.global.exception.GlobalExceptionService
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import kr.co.fitview.api.app.global.exception.error.image.ImageErrorCode
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.network.NetworkErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.exception.error.security.SecurityErrorCode
import kr.co.fitview.api.app.global.exception.error.term.TermErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder


@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerDocsTest  : RestDocsSupport() {

    private val globalExceptionService: GlobalExceptionService = mock(GlobalExceptionService::class.java)

    override fun initController(): Any {
        return GlobalExceptionController(globalExceptionService)
    }

    @BeforeEach
    fun setUpGlobalExceptionHandler(provider: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
            .setControllerAdvice(GlobalExceptionHandler())
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(provider))
            .build()
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    fun handleGlobalException(errorCode: ErrorCode) {
        `when`(globalExceptionService.throwError())
            .thenThrow(GlobalException(errorCode))


        // when // then
        mockMvc.perform(
            get("/api/v1/exception")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized())
            .andDo(
                document(errorCode.code,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description(errorCode.description),
                    fieldWithPath("data").type(JsonFieldType.STRING)
                        .description("응답 데이터")
                        .optional()
                )
            )
            )
    }

    companion object {
        @JvmStatic
        fun errorScenarios(): List<ErrorCode> {
            return AuthErrorCode.entries +
                    JwtErrorCode.entries +
                    MemberErrorCode.entries +
                    RequestErrorCode.entries +
                    SecurityErrorCode.entries +
                    OAuth2ErrorCode.entries +
                    NetworkErrorCode.entries +
                    ImageErrorCode.entries +
                    TermErrorCode.entries +
                    AddressErrorCode.entries
        }
    }

}