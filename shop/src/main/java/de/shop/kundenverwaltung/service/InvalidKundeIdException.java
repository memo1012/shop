package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidKundeIdException extends KundeValidationException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final Long kundeId;
	
	public InvalidKundeIdException(Long kundeId, Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		this.kundeId = kundeId;
	}

	public Long getKundeId() {
		return kundeId;
	}
	
	@Override
	public String getMessageKey() {
		return null;
	}
}
