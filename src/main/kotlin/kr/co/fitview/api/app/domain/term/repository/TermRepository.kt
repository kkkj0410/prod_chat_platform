package kr.co.fitview.api.app.domain.term.repository

import kr.co.fitview.api.app.domain.term.entity.Term
import org.springframework.data.jpa.repository.JpaRepository

interface TermRepository : JpaRepository<Term, Long> {

    fun findByMemberId(memberId : Long) : List<Term>
}