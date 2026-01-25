package kr.co.fitview.api.app.global.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.exception.error.network.NetworkErrorCode
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(this::class.java)

    fun postByWebClient(
        formData: MultiValueMap<String, String>,
        url : String
    ): Map<String, Any> {


        log.info(
            "[Google OAuth Request] url={}, formKeys={}, grant_type={}, redirect_uri={}",
            url,
            formData.keys,
            formData["grant_type"],
            formData["redirect_uri"]
        )

        try {
            val mapType = object : ParameterizedTypeReference<Map<String, Any>>() {}

            val response: Map<String, Any> = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(mapType)
                .block() ?: emptyMap()

            log.info(
                "[Google OAuth Response SUCCESS] keys={}",
                response.keys
            )

            return response
        } catch (e: WebClientResponseException) {

            log.error(
                """
                [Google OAuth Response FAILED]
                status={}
                responseBody={}
                formKeys={}
            """.trimIndent(),
                e.statusCode,
                e.responseBodyAsString,
                formData.keys,
                e
            )

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

    fun getByWebClient(url: String, headers: Map<String, String>): Map<String, Any> {
        val client = WebClient.create()

        return client.get()
            .uri(url)
            .headers { httpHeaders ->
                headers.forEach { (key, value) ->
                    httpHeaders.set(key, value)
                }
            }
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() as Map<String, Any>
    }
}