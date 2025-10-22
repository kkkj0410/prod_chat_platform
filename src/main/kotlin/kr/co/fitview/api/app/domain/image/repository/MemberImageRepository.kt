package kr.co.fitview.api.app.domain.image.repository

import kr.co.fitview.api.app.domain.image.entity.MemberImage
import org.springframework.data.jpa.repository.JpaRepository

interface MemberImageRepository : JpaRepository<MemberImage, Long>, MemberImageRepositoryCustom {
}