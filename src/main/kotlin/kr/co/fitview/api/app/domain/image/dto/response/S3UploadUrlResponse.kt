package kr.co.fitview.api.app.domain.image.dto.response

import java.net.URL

data class S3UploadUrlResponse(
    val presignedUrl : String,
    val accessUrl : String
){
}
