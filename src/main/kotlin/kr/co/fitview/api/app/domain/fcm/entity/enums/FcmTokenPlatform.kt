package kr.co.fitview.api.app.domain.fcm.entity.enums

import kr.co.fitview.api.app.domain.image.enums.S3Prefix
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName

enum class FcmTokenPlatform(
    val description : String
) {
    IOS("애플"),
    ANDROID("안드로이드")

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescriptions(): List<String> {
            return entries.map { it.toString() }
        }
    }
}