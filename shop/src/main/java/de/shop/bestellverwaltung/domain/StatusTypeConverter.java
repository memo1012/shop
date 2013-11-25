package de.shop.bestellverwaltung.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class StatusTypeConverter implements AttributeConverter<StatusType, String> {
	@Override
	public String convertToDatabaseColumn(StatusType statusType) {
		if (statusType == null) {
			return null;
		}
		return statusType.getInternal();
	}
	
	@Override
	public StatusType convertToEntityAttribute(String internal) {
		return StatusType.build(internal);
	}

}
