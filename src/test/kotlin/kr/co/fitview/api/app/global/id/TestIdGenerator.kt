package kr.co.fitview.api.app.global.id

class TestIdGenerator(
    val uuid : String
) : IdGenerator {
    override fun createUuid(): String {
        return uuid
    }
}