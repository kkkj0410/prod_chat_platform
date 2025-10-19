package kr.co.fitview.api.app.domain.term.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.BaseEntity

@Entity
@Table(name = "term")
class Term(

    @Size(max = 100)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 100)
    var name: TermName? = null,

    @NotNull
    @Lob
    @Column(name = "description", nullable = false)
    var description: String? = null,

    @NotNull
    @Column(name = "version", nullable = false)
    var version: Int? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id", nullable = false)
    var id: Long? = null
}