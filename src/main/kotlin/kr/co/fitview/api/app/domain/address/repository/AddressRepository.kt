package kr.co.fitview.api.app.domain.address.repository

import kr.co.fitview.api.app.domain.address.entity.Address
import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Long> {

    fun findAllByMemberIdAndDeletedAtIsNull(memberId : Long) : List<Address>

    fun findByMemberIdAndDeletedAtIsNull(memberId : Long) : Address?

    fun findByIdAndDeletedAtIsNull(addressId: Long): Address?

    fun findByIdAndMemberIdAndDeletedAtIsNull(addressId : Long, memberId : Long) : Address?
}