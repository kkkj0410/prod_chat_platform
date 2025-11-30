package kr.co.fitview.api.app.global.random

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class RandomCustomProvider : RandomCustom {

    override fun <T> shuffled(seed: Long, list: List<T>): List<T> {
        return list.shuffled(Random(seed))
    }

    override fun nextLong(seed: Long, from: Long, until: Long): Long {
        return Random(seed).nextLong(from, until)
    }

}