package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.entity.MemberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.image.ImageErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ImageService(
    val imageRepository: ImageRepository,
    val memberImageRepository: MemberImageRepository,
    val time : Time
) {
    val maxMemberImageWorkout = 3

    @Transactional
    fun saveMemberImageProfile(member : Member, profileImageUrl : String) : MemberImage{
        val findMemberImage = memberImageRepository.findWithImageByMemberIdAndProfileAndDeletedAtIsNull(member.id!!)

        if(isNotNull(findMemberImage)){
            return findMemberImage!!.changeImageUrl(profileImageUrl)
        }

        val memberImage = createMemberImage(profileImageUrl, member)

        return memberImageRepository.save(memberImage)
    }


    @Transactional
    fun saveMemberImageWorkouts(member : Member, imageUrls : List<String>) : List<MemberImage>{
        if(isMemberImageWorkoutLimitExceeded(imageUrls)){
            throw GlobalException(ImageErrorCode.MEMBER_WORKOUT_IMAGE_LIMIT)
        }

        val findMemberImages = memberImageRepository.findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(member.id!!)

        deleteMemberImages(findMemberImages)

        return addWorkoutMemberImages(imageUrls, member)
    }

    private fun addWorkoutMemberImages(
        imageUrls: List<String>,
        member: Member
    ): List<MemberImage> {

        val savedImages = addImages(imageUrls)

        val savedImagesMap = savedImages.associateBy { it.url }

        val memberImages = imageUrls.mapIndexed { index, request ->
            MemberImage(
                member = member,
                image = savedImagesMap[request]!!,
                type = MemberImageType.WORKOUT,
                seq = (index + 1) * 100
            )
        }

        return memberImageRepository.saveAll(memberImages)
    }

    private fun deleteMemberImages(findMemberImages: List<MemberImage>) {
        findMemberImages.forEach { memberImage ->
            memberImage.delete(time.nowLocalDateTime)
        }
    }

    private fun isMemberImageWorkoutLimitExceeded(imageUrls: List<String>) =
        imageUrls.size > maxMemberImageWorkout

    private fun createMemberImage(
        profileImageUrl: String,
        member: Member
    ): MemberImage {
        val savedImage = addImage(profileImageUrl)

        return MemberImage(
            member,
            savedImage,
            MemberImageType.PROFILE,
        )
    }

    private fun isNotNull(value: Any?) = value != null

    private fun addImage(url : String) : Image {
        val image = Image(url = url)
        return imageRepository.save(image)
    }

    private fun addImages(urls : List<String>) : List<Image>{
        val images = urls.map { url ->
            Image(url = url)
        }
        return imageRepository.saveAll(images)
    }


}