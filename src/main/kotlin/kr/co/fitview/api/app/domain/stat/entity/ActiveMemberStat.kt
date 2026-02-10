package kr.co.fitview.api.app.domain.stat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import java.time.LocalDate

@Entity
@Table(name = "active_member_stat")
open class ActiveMemberStat(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    open var member: Member? = null,

    @NotNull
    @Column(name = "stat_date", nullable = false)
    open var statDate: LocalDate? = null

) : BaseEntity(){

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "active_member_stat_id", nullable = false)
    open var id: Long? = null

    companion object {
        fun of(member : Member, statDate : LocalDate) : ActiveMemberStat{
            return ActiveMemberStat(
                member = member,
                statDate = statDate
            )
        }
    }

}