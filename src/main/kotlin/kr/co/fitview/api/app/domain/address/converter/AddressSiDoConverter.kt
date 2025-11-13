package kr.co.fitview.api.app.domain.address.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

@Converter(autoApply = true)
class AddressSiDoConverter : AttributeConverter<AddressSiDo, String> {

    override fun convertToDatabaseColumn(attribute: AddressSiDo?): String? {
        return attribute?.fullName
    }

    override fun convertToEntityAttribute(dbData: String?): AddressSiDo? {
        return dbData?.let { AddressSiDo.from(it) }
    }
}