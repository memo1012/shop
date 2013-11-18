package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidNachnameException extends KundeValidationException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final String nachname;
	
	public InvalidNachnameException(String nachname, Collection<ConstraintViolation<Kunde>> violations) {
		super(violations);
		this.nachname = nachname;
	}

	public String getNachname() {
		return nachname;
	}
	
	@Override
	public String getMessageKey() {
		return null;
	}
}
