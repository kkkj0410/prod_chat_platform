package kr.co.fitview.api.app.domain.term.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.term.dto.request.TermServiceRequest
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
    fun addTerms(member : Member, requests : List<TermServiceRequest>) : List<Term>{

        validateAllCheck(requests)

        validateIsRequiredTrue(requests)

        validateDuplicate(requests)

        val terms = createTerms(requests, member)

        return termRepository.saveAll(terms)
    }


    private fun validateAllCheck(requests: List<TermServiceRequest>) {

        val requestTermNameSet = requests.map { it.termName }.toSet()

        val allTermNameSet = TermName.entries.toSet()

        if (isNotMatch(requestTermNameSet, allTermNameSet)) {
            throw GlobalException(TermErrorCode.ALL_TERMS_NOT_CHECKED)
        }
    }

    private fun isNotMatch(
        requestTermNameSet: Set<TermName>,
        allTermNameSet: Set<TermName>
    ) = requestTermNameSet != allTermNameSet

    private fun validateIsRequiredTrue(requests: List<TermServiceRequest>) {
        val hasRequiredButNotAgreed = requests.any { termRequest -> isRequiredButNotAgreed(termRequest) }

        if (hasRequiredButNotAgreed) {
            throw GlobalException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED)
        }
    }

    private fun isRequiredButNotAgreed(termRequest: TermServiceRequest) =
        termRequest.termName.isRequired && isNotAgreed(termRequest.isAgreed)

    private fun isNotAgreed(isAgreed: Boolean) = !isAgreed

    private fun validateDuplicate(requests: List<TermServiceRequest>) {
        val termNames = requests.map { it.termName }

        if (isDuplicatedTermName(termNames)) {
            throw GlobalException(TermErrorCode.DUPLICATE_TERM_ENTRY)
        }
    }

    private fun isDuplicatedTermName(termNames: List<TermName>) =
        termNames.size != termNames.toSet().size

    private fun createTerms(
        requests: List<TermServiceRequest>,
        member: Member
    ) = requests.map { termRequest ->
        Term(
            member = member,
            name = termRequest.termName,
            isAgreed = termRequest.isAgreed
        )
    }
}