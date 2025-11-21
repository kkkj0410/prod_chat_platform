package kr.co.fitview.api.app.docs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder


@ExtendWith(RestDocumentationExtension::class)
abstract class RestDocsSupport {

    protected lateinit var mockMvc: MockMvc
    protected var objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        findAndRegisterModules()
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        registerModule(KotlinModule.Builder().build())
    }


    @BeforeEach
    fun setUp(provider: RestDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(provider))
            .build()
    }

    protected abstract fun initController(): Any?
}