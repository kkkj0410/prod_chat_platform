package kr.co.fitview.api.app.domain.term.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.term.entity.Term
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.domain.term.repository.TermRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.term.TermErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class TermService(
    val termRepository : TermRepository
) {

    @Transactional
    fun addRequiredTerms(member : Member) : List<Term>{
        val findTerms = termRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)
        if(findTerms.isNotEmpty()){
            throw GlobalException(TermErrorCode.DUPLICATE_TERM_AGREEMENT)
        }

        val terms = createRequiredTerms(member)
        return termRepository.saveAll(terms)
    }

    private fun createRequiredTerms(member : Member) : List<Term>{
        return TermName
            .entries
            .filter { it.isRequired }
            .map{
                Term(
                    member = member,
                    name = it,
                    isAgreed = true
                )
            }
    }

}