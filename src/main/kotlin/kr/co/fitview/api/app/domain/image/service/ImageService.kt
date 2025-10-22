package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.domain.image.dto.request.WorkoutImageUrlServiceRequest
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ImageService(
    val imageRepository: MemberImageRepository,
    val memberImageRepository: MemberImageRepository
) {

    @Transactional
    fun saveMemberImageProfile(member : Member, imageUrl : String){

    }

    @Transactional
    fun saveMemberImageWorkouts(member : Member, request : List<WorkoutImageUrlServiceRequest>){

    }


}