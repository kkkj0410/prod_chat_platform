package kr.co.fitview.api.app.domain.review.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity

@Entity
@Table(name = "review_tag")
class ReviewTag(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_category_id", nullable = false)
    var reviewCategory: ReviewCategory? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "display_text", nullable = false, length = 100)
    var displayText: String? = null,

    @NotNull
    @Column(name = "seq", nullable = false)
    var seq: Int? = null

) : BaseSoftDeleteEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_tag_id", nullable = false)
    var id: Long? = null

}