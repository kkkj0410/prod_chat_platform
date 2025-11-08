package kr.co.fitview.api.app.domain.address.controller

import kr.co.fitview.api.app.domain.address.dto.request.AddressRadiusRequest
import kr.co.fitview.api.app.domain.address.dto.request.AddressUpdateRequest
import kr.co.fitview.api.app.domain.address.dto.response.AddressDetailResponse
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/addresses")
class AddressController(
    val addressService : AddressService
) {

    @GetMapping("/{addressId}")
    fun addressDetail(
        @PathVariable
        addressId: Long
    ) : ResponseEntity<ApiResponse<AddressDetailResponse>> {
        val response = addressService.findAddressFromAddressId(addressId)

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PatchMapping("/{addressId}")
    fun addressModify(
        @PathVariable
        addressId: Long,

        @RequestBody
        request : AddressUpdateRequest

    ) : ResponseEntity<ApiResponse<*>> {


        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @PatchMapping("/{addressId}/radius")
    fun addressRadiusModify(
        @PathVariable
        addressId: Long,

        @RequestBody
        request : AddressRadiusRequest

    ) : ResponseEntity<ApiResponse<*>> {
        return ResponseEntity.ok(ApiResponse.success("ok"))
    }



}