package de.shop.artikelverwaltung.service;

import de.shop.util.AbstractShopException;

public class ArtikelServiceException extends AbstractShopException {
	private static final long serialVersionUID = 6714045313426734583L;

	public ArtikelServiceException(String msg) {
		super(msg);
	}
	
	public ArtikelServiceException(String msg, Throwable t) {
		super(msg, t);
	}
	
	@Override
	public String getMessageKey() {
		return null;
	}
}
