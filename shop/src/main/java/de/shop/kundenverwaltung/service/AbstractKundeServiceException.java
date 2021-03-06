package de.shop.kundenverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class AbstractKundeServiceException extends AbstractShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractKundeServiceException(String msg) {
		super(msg);
	}

	public AbstractKundeServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
