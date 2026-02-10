package kr.co.fitview.api.app.global.slack

interface SlackNotifier {
    fun send(message: String)
}