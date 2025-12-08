package kr.co.fitview.api.app.domain.image.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import java.time.LocalDateTime

@Entity
@Table(name = "image")
class Image(

    @Size(max = 2048)
    @NotNull
    @Column(name = "url", nullable = false, length = 2048)
    var url: String? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    var id: Long? = null

    fun changeUrl(url : String){
        this.url = url
    }

    fun delete(now : LocalDateTime) : Image{
        this.deletedAt = now
        return this
    }
}