package kr.co.fitview.api.app.global.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.exception.error.network.NetworkErrorCode
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URI


@Service
class NetworkService(
    val webClient : WebClient
) {

    fun postByWebClient(
        formData: MultiValueMap<String, String>,
        url : String
    ): Map<String, Any> {
        try {
            val mapType = object : ParameterizedTypeReference<Map<String, Any>>() {}

            val response: Map<String, Any> = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(mapType)
                .block() ?: emptyMap()
            return response
        } catch (e: WebClientResponseException) {
//            println("❌ [WebClient Error] status=${e.statusCode}")
//            println("❌ [WebClient Error] headers=${e.headers}")
//            println("❌ [WebClient Error] body=${e.responseBodyAsString}")


            throw GlobalException(NetworkErrorCode.NETWORK_SEND_ERROR)
        }
    }

    fun postKakaoProfile(url: String, accessToken: String): KakaoProfile {
        try {
            return webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KakaoProfile::class.java)
                .block()!!
        }
        catch(ex : Exception) {
            throw GlobalException(NetworkErrorCode.NETWORK_SEND_ERROR)
        }
    }


    fun getJwkSet(url : String) : JWKSet{
        return JWKSet.load(URI(url).toURL())
    }
}