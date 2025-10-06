package kr.co.fitview.api.app.global.id

interface IdGenerator {
    fun createUuid(): String
}