package de.shop.bestellverwaltung.service;

import java.util.Collection;
import java.util.Date;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte einer Bestellung nicht korrekt sind
 */
@ApplicationException(rollback = true)
public class InvalidBestellungException extends AbstractBestellungValidationException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Date erzeugt;
	private final Long kundeId;
	
	public InvalidBestellungException(Bestellung bestellung,
			                             Collection<ConstraintViolation<Bestellung>> violations) {
		super(violations);
		
		if (bestellung == null) {
			this.erzeugt = null;
			this.kundeId = null;
		}
		else {
			this.erzeugt = bestellung.getErzeugt();
			final Kunde kunde = bestellung.getKunde();
			this.kundeId = kunde == null ? null : kunde.getId();
		}
	}
	
	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}
	
	public Long getKundeId() {
		return kundeId;
	}
	
	@Override
	public String getMessageKey() {
		return null;
	}
}
