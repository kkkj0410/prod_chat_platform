//package kr.co.fitview.api.app.domain.chat.config
//
//import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
//import org.springframework.boot.web.server.WebServerFactoryCustomizer
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.apache.catalina.connector.Connector
//@Configuration
//class WebSocketTimeoutConfig {
//
//    @Bean
//    fun tomcatCustomizer(): WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
//        return object : WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
//            override fun customize(factory: TomcatServletWebServerFactory) {
//
//                // 💡 [FIX 3] Explicitly create the TomcatConnectorCustomizer
//                val customizer = TomcatConnectorCustomizer { connector: Connector ->
//
//                    // 💡 [FIX] Use the direct properties, not setAttribute
//                    // (This is equivalent to connector.setConnectionTimeout(10000))
//                    connector.connectionTimeout = 10000 // 10 seconds
//
//                    // (This is equivalent to connector.setKeepAliveTimeout(10000))
//                    connector.keepAliveTimeout = 10000  // 10 seconds
//                }
//
//                factory.addConnectorCustomizers(customizer)
//            }
//        }
//    }
//}