package kr.co.fitview.api.app.global.exception.error.review

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class ReviewErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {


    POSITIVE_REVIEW_SUB_SELECTION_REQUIRED(
        "001",
        "Sub-selection required for positive review",
        "좋아요 후기에서는 서브 선택지가 필요하다"
    ),

    NEGATIVE_REVIEW_SUB_SELECTION_NOT_ALLOWED(
        "002",
        "Sub-selection not allowed for negative review",
        "나빠요 후기에서는 서브 선택지가 없어야 한다"
    ),

    REVIEW_ALREADY_EXISTS(
    "003",
    "Review already exists",
    "이미 해당 운동 이력에 대한 리뷰를 작성했기 때문에 더 이상 작성할 수 없다"
    );

    ;

    override val prefix: String
        get() = "REVIEW"

}