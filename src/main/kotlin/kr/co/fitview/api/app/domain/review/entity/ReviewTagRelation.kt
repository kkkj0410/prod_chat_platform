package kr.co.fitview.api.app.domain.review.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity

@Entity
@Table(name = "review_tag_relation")
class ReviewTagRelation(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    var review: Review? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_tag_id", nullable = false)
    var reviewTag: ReviewTag? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_tag_relation_id", nullable = false)
    var id: Long? = null

}