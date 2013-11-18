package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Artikel;

public class InvalidArtikelException extends ArtikelValidationException {	
	private static final long serialVersionUID = 2162536126617158666L;
	private final Artikel artikel;
	
	
	public InvalidArtikelException(Artikel artikel, Collection<ConstraintViolation<Artikel>> violations) {
		super(violations);
		this.artikel = artikel;
	}

	public Artikel getKunde() {
		return artikel;
		}

}
