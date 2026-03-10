package kr.co.fitview.api.app.global.util

interface SecurityProvider {
    fun getMemberId(): Long
    
    fun getMemberIdOrNull(): Long?
}
