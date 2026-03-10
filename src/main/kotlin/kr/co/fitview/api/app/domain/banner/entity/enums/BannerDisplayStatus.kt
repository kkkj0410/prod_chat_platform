package kr.co.fitview.api.app.domain.banner.entity.enums

enum class BannerDisplayStatus(val description : String) {


    ACTIVE("활성화"),
    INACTIVE("비활성화")


    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}