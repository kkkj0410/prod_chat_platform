package kr.co.fitview.api.app.domain.image.repository

interface MemberImageRepositoryCustom {

    fun findWithImageByMemberIdAndProfileAndDeletedAtIsNull(memberId : Long)
}