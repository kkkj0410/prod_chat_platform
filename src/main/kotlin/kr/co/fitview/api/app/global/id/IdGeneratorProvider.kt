package kr.co.fitview.api.app.global.id

import org.springframework.stereotype.Component
import java.util.*

@Component
class IdGeneratorProvider : IdGenerator {

    override fun createUuid(): String {
        return UUID.randomUUID().toString();
    }
}