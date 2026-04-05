package kr.co.fitview.api.app.global.exception.error.invitation

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class InvitationErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    INVITATION_MEMBER_NOT_FOUND("001", "Member not found for invitation code", "초대 코드에 해당하는 회원이 존재하지 않습니다");

    ;

    override val prefix: String
        get() = "INVITATION"

}