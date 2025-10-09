//package kr.co.fitview.api.app.domain.oauth2.service
//
//import com.auth0.jwt.JWT
//import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
//import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
//import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
//import kr.co.fitview.api.app.domain.oauth2.util.AppleJwtProvider
//import org.springframework.http.MediaType
//import org.springframework.stereotype.Service
//import org.springframework.util.LinkedMultiValueMap
//import org.springframework.util.MultiValueMap
//import org.springframework.web.reactive.function.BodyInserters
//import org.springframework.web.reactive.function.client.WebClient
//
//
//@Service
//class OAuth2Service(
//    val appleConfig : AppleConfig,
//    val appleJwtProvider : AppleJwtProvider,
//    val webClient: WebClient
//) {
//
//
//
//    fun loginApple(
//        authCode : String
//    ) : OAuth2LoginResponse{
//
//        val clientSecret = appleJwtProvider.createClientSecret()
//
//
//        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
//        formData.add("client_id", appleConfig.clientId)
//        formData.add("client_secret", clientSecret)
//        formData.add("code", authCode)
//        formData.add("grant_type", "authorization_code")
//        formData.add("redirect_uri", appleConfig.redirectUrl)
//
//        // WebClient POST 요청
//        val response: Map<String, Any> = webClient.post()
//            .uri("https://appleid.apple.com/auth/token")
//            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//            .body(BodyInserters.fromFormData(formData))
//            .retrieve()
//            .bodyToMono(Map::class.java)
//            .block() as? Map<String, Any> ?: emptyMap()
//
//
//        val idToken = response["id_token"] as? String
//            ?: throw IllegalStateException("id_token not found")
//
//        // 5️⃣ JWT 디코딩
//        val jwt = JWT.decode(idToken)
//        val userId = jwt.subject                  // Apple 고유 사용자 ID (sub)
//        val email = jwt.getClaim("email").asString()
//        val emailVerified = jwt.getClaim("email_verified").asBoolean()
//
//        println(userId)
//        println(email)
//        println(emailVerified)
//
//
//        return OAuth2LoginResponse("", "")
//    }
//
//}