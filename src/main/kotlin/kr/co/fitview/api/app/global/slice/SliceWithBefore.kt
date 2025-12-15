package kr.co.fitview.api.app.global.slice

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl

class SliceWithBefore<T>(
    content: List<T>,
    pageable: Pageable,
    hasNext: Boolean,
    val hasBefore: Boolean
) : SliceImpl<T>(content, pageable, hasNext)