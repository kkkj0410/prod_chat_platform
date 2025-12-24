package kr.co.fitview.api.app.domain.member.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWithdrawReasonReasonType
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity

@Entity
@Table(name = "member_withdraw_reason")
class MemberWithdrawReason(

    @Size(max = 100)
    @NotNull
    @Column(name = "reason_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    var reasonType: MemberWithdrawReasonReasonType? = null,

    @Size(max = 200)
    @NotNull
    @Column(name = "display_text", nullable = false, length = 200)
    var displayText: String? = null,

    @NotNull
    @Column(name = "seq", nullable = false)
    var seq: Int? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_withdraw_reason_id", nullable = false)
    var id: Long? = null

}