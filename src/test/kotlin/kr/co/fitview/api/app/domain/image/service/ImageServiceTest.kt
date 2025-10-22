package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ImageServiceTest @Autowired constructor(
    val imageService: ImageService,
    val imageRepository: ImageRepository,
    val memberImageRepository : MemberImageRepository
) : IntegrationTestSupport(){

    @DisplayName("회원 프로필 이미지를 저장한다.")
    @Test
    fun saveMemberImageProfile() {
        // given

        // when

        // then
    }

    @DisplayName("회원 프로필 이미지가 존재하면 덮어씌워서 새 프로필을 저장한다.")
    @Test
    fun saveMemberImageProfileExistingProfile() {
        // given

        // when

        // then

    }
}