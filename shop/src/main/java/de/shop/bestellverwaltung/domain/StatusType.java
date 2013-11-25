package de.shop.bestellverwaltung.domain;

public enum StatusType {
	INBEARBEITUNG("I"),
	VERSCHICKT("V");
	
	private String internal;
	
	private StatusType(String internal) {
		this.internal = internal;
	}
	
	public String getInternal() {
		return internal;
	}
	
	public static StatusType build(String internal) {
		if (internal == null) {
			return null;
		}
		
		switch (internal) {
		case "I":
			return INBEARBEITUNG;
		case "V":
			return VERSCHICKT;
			
		default:
			throw new IllegalArgumentException(internal + " ist kein gueltiger Wert fuer StatusType");
		}
		
	}
		
		
	
}
