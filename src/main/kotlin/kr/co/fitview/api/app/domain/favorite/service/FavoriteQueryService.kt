package kr.co.fitview.api.app.domain.favorite.service

import kr.co.fitview.api.app.domain.chat.service.ChatRoomQueryService
import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import kr.co.fitview.api.app.domain.favorite.repository.FavoriteRepository
import kr.co.fitview.api.app.domain.member.dto.response.LastWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestQueryService
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FavoriteQueryService(
    private val favoriteRepository : FavoriteRepository,
    private val workoutPartnerRequestQueryService : WorkoutPartnerRequestQueryService,
    private val chatRoomQueryService : ChatRoomQueryService
) {


    fun findFavoriteMembers(memberId: Long, condition: FavoriteMemberCondition) : Slice<FavoriteMemberResponse> {
        val findFavoriteMembers = favoriteRepository.findFavoriteMembers(memberId, condition)

        val otherMemberIds = findFavoriteMembers.content.map{it.memberId}

        val findWorkoutPartnerRequests = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = memberId,
            otherMemberIds = otherMemberIds
        )

        val findChatRooms = chatRoomQueryService.findChatRoomFrom(memberId, otherMemberIds)

        val requestMap = findWorkoutPartnerRequests.associateBy { request ->
            if (request.getFromMemberId() == memberId) request.getToMemberId() else request.getFromMemberId()
        }

        val chatRoomMap = findChatRooms.associate {
            it.otherMemberId to it.chatRoomId
        }

        return findFavoriteMembers.map { member ->

            val request = requestMap[member.memberId]

            val lastWorkoutPartnerRequest = request?.let { req ->

                val chatRoomId = if (req.status == WorkoutPartnerRequestStatus.ACCEPT) {
                    chatRoomMap[member.memberId]
                } else {
                    null
                }

                LastWorkoutPartnerRequestResponse(
                    workoutPartnerRequestId = req.id!!,
                    status = req.status!!,
                    chatRoomId = chatRoomId
                )
            }

            member.copy(lastWorkoutPartnerRequest = lastWorkoutPartnerRequest)
        }
    }

    fun findFavoriteFrom(fromMemberId : Long, toMemberId: Long) : Favorite?{
        return favoriteRepository.findByFromMemberIdAndToMemberId(fromMemberId, toMemberId)
    }


}