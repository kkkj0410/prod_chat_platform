//package kr.co.fitview.api.app.global.config
//
//import kr.co.fitview.api.app.global.converter.RequestEnumConverter
//import org.springframework.boot.test.context.TestConfiguration
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.format.FormatterRegistry
//import org.springframework.web.reactive.function.client.WebClient
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
//
//@TestConfiguration
//class TestWebConfig() : WebMvcConfigurer {
//
//    @Bean
//    fun webClient(builder: WebClient.Builder): WebClient {
//        return builder.build()
//    }
//
//    @Bean
//    fun requestEnumConverter() = RequestEnumConverter()
//
//    override fun addFormatters(registry: FormatterRegistry) {
//        registry.addConverterFactory(requestEnumConverter())
//    }
//}