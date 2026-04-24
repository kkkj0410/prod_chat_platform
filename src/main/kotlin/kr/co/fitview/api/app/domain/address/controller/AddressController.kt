package kr.co.fitview.api.app.domain.address.controller

import kr.co.fitview.api.app.domain.address.dto.request.AddressUpdateRequest
import kr.co.fitview.api.app.domain.address.dto.response.AddressDetailResponse
import kr.co.fitview.api.app.domain.address.service.AddressQueryService
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/addresses")
class AddressController(
    private val addressService: AddressService,
    private val addressQueryService: AddressQueryService,
    private val securityUtil: SecurityUtil
) {

    @GetMapping("/{addressId}")
    fun addressDetail(
        @PathVariable
        addressId: Long
    ) : ResponseEntity<ApiResponse<AddressDetailResponse>> {
        val response = addressQueryService.findAddressFromAddressId(addressId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PatchMapping("/{addressId}")
    fun addressModify(
        @PathVariable
        addressId: Long,

        @RequestBody
        request : AddressUpdateRequest

    ) : ResponseEntity<ApiResponse<String>> {
        addressService.modifyAddress(securityUtil.getMemberId(), addressId, request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

}
