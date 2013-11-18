package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidEmailException extends KundeValidationException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final String email;
	
	public InvalidEmailException(String email, Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
	
	@Override
	public String getMessageKey() {
		return null;
	}
}
