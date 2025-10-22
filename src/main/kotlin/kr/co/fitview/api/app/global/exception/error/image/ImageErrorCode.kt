package kr.co.fitview.api.app.global.exception.error.image

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class ImageErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    S3_IMAGE_TOO_LARGE("001", "Image size exceeds 50MB limit", "이미지 업로드는 50MB 이하로 제한. 용량 초과하면 업로드 불가"),
    S3_UPLOAD_COUNT_LIMIT("002", "Cannot upload more than 50 files", "업로드 파일은 최대 50개까지 가능합니다. 50개 초과 시 업로드 불가"),
    MEMBER_WORKOUT_IMAGE_LIMIT("003", "Cannot upload more than 3 workout images", "회원 운동 사진은 최대 3개까지 업로드 가능합니다. 3개 초과 시 업로드 불가");

    ;

    override val prefix: String
        get() = "IMAGE"

}