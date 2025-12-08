package kr.co.fitview.api.app.domain.review.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "review_tag_count")
class ReviewTagCount(
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_tag_id", nullable = false)
    var reviewTag: ReviewTag? = null,

    @NotNull
    @ColumnDefault("1")
    @Column(name = "count", nullable = false)
    var count: Int? = null

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_tag_count_id", nullable = false)
    var id: Long? = null

    fun increaseCount(amount: Int = 1) {
        require(amount > 0) { "증가시킬 카운트는 양수여야 합니다." }
        this.count = (this.count ?: 0) + amount
    }

    companion object {
        fun of(member: Member, reviewTag: ReviewTag): ReviewTagCount {
            return ReviewTagCount(
                member = member,
                reviewTag = reviewTag,
                count = 1
            )
        }
    }

}