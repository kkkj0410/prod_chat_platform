package kr.co.fitview.api.app.domain.address.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AddressRepositoryTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val addressRepository: AddressRepository
) : IntegrationTestSupport(){

    @DisplayName("해당 회원의 모든 주소를 조회한다.")
    @Test
    fun findAllByMemberIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val address1 = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu1",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val address2 = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu2",
            eupMyeonDong = "테헤란로2",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        addressRepository.save(address1)
        addressRepository.save(address2)

        // when
        val findAddresses = addressRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(findAddresses)
            .extracting("member", "siDo", "siGunGu")
            .containsExactlyInAnyOrder(
                tuple(savedMember, AddressSiDo.SEOUL, "siGunGu1"),
                tuple(savedMember, AddressSiDo.SEOUL, "siGunGu2"),
            )
    }

    @DisplayName("해당 회원의 1개 주소를 조회한다.")
    @Test
    fun findByMemberIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu1",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        addressRepository.save(address)

        // when
        val findAddress = addressRepository.findByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(findAddress)
            .extracting("member", "siDo", "siGunGu")
            .contains(savedMember, AddressSiDo.SEOUL, "siGunGu1")
    }


    @DisplayName("주소 id로 해당 주소를 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu1",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val savedAddress = addressRepository.save(address)

        // when
        val findAddress = addressRepository.findByIdAndDeletedAtIsNull(savedAddress.id!!)

        // then
        assertThat(findAddress)
            .extracting("member", "siDo", "siGunGu")
            .contains(savedMember, savedAddress.siDo, savedAddress.siGunGu)

    }

    @DisplayName("해당 회원의 주소를 조회한다.")
    @Test
    fun findByIdAndMemberIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu1",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val savedAddress = addressRepository.save(address)

        // when
        val findAddress = addressRepository.findByIdAndMemberIdAndDeletedAtIsNull(
            addressId = savedAddress.id!!,
            memberId = savedMember.id!!
        )

        // then
        assertThat(findAddress)
            .extracting("member", "siDo", "siGunGu")
            .contains(savedMember, savedAddress.siDo, savedAddress.siGunGu)

    }


}