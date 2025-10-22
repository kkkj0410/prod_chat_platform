package kr.co.fitview.api.app.domain.image.repository

import kr.co.fitview.api.app.domain.image.entity.MemberImage

interface MemberImageRepositoryCustom {

    fun findWithImageByMemberIdAndProfileAndDeletedAtIsNull(memberId : Long) : MemberImage?

    fun findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(memberId : Long) : List<MemberImage>
}