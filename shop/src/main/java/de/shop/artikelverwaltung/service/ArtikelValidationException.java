package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Artikel;



public class ArtikelValidationException extends ArtikelServiceException {
	private static final long serialVersionUID = 8368265228635129483L;	

	private final Collection<ConstraintViolation<Artikel>> violations;
	
	public ArtikelValidationException(Collection<ConstraintViolation<Artikel>> violations) {
		super("Violations: " + violations);
		this.violations = violations;
	}
	
	public Collection<ConstraintViolation<Artikel>> getViolations() {
		return violations;
	}
}
