package kr.co.fitview.api.app.domain.image.dto.request

import kr.co.fitview.api.app.domain.image.enums.S3Prefix

data class S3UploadUrlServiceRequest(

    val prefix : S3Prefix,

    val imageByte : Long,

    val contentType : String?
){

}
