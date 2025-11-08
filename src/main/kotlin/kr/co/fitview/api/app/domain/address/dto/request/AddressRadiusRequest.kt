package kr.co.fitview.api.app.domain.address.dto.request

data class AddressRadiusRequest(

    val radiusKm: Int

    ){
    fun toServiceRequest(): AddressRadiusServiceRequest {
        return AddressRadiusServiceRequest(
            radiusKm = radiusKm
        )
    }
}
