package kr.co.fitview.api.app.domain.review.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "review_category")
class ReviewCategory(

    @Size(max = 100)
    @NotNull
    @Column(name = "display_text", nullable = false, length = 100)
    var displayText: String? = null,

    @NotNull
    @Column(name = "seq", nullable = false)
    var seq: Int? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_category_id", nullable = false)
    var id: Long? = null


    @OneToMany(mappedBy = "reviewCategory")
    var reviewTags: MutableSet<ReviewTag> = mutableSetOf()
}