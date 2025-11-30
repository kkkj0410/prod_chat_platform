package kr.co.fitview.api.app.global.random

class TestRandomCustom : RandomCustom{

    override fun <T> shuffled(seed: Long, list: List<T>): List<T> {
        return list.toList()
    }

    override fun nextLong(seed: Long, from: Long, until: Long): Long {
        return from
    }

}