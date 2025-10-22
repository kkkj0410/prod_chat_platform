package kr.co.fitview.api.app.domain.image.repository

import kr.co.fitview.api.app.domain.image.entity.Image
import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository : JpaRepository<Image, Long> {
}