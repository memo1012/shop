package de.shop.bestellverwaltung.service;

public class BestellungVerschicktException extends
		AbstractBestellungServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8377820862153125858L;

	public static final String MESSAGE_KEY = "bestellung.verschickt";
	private final Long id; 
	
	public BestellungVerschicktException(Long id) {
		super("Die Bestellung " + id + " wurde schon verschickt");
		this.id = id;
		
	}
	public Long getId() {
		return id;
	}
	
	@Override
	public String getMessageKey() {
		return MESSAGE_KEY;
	}

}
