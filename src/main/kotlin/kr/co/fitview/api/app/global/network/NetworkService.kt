package kr.co.fitview.api.app.global.network

import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.network.NetworkErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException


@Service
class NetworkService(
    val webClient : WebClient
) {

    fun postByWebClient(
        formData: MultiValueMap<String, String>,
        url : String
    ): Map<String, Any> {
        try {
            val response: Map<String, Any> = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() as? Map<String, Any> ?: emptyMap()
            return response
        } catch (e: WebClientResponseException) {
            throw GlobalException(NetworkErrorCode.NETWORK_SEND_ERROR)
        }
    }
}