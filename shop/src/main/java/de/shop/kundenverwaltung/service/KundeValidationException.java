package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

public abstract class KundeValidationException extends KundeServiceException {
	private static final long serialVersionUID = -6924234959157503601L;
	private final Collection<ConstraintViolation<Kunde>> violations;
	
	public KundeValidationException(Collection<ConstraintViolation<Kunde>> violations) {
		super("Violations: " + violations);
		this.violations = violations;
	}
	
	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}
