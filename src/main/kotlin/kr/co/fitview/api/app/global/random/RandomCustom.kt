package kr.co.fitview.api.app.global.random

interface RandomCustom {


    fun <T> shuffled(seed: Long, list: List<T>): List<T>

    fun nextLong(seed: Long, from: Long, until: Long): Long
}