package de.shop.artikelverwaltung.rest;


import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
import static java.util.Locale.GERMAN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.AbstractResourceTest;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;








@RunWith(Arquillian.class)
public class ArtikelResourceTest extends AbstractResourceTest{
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long ARTIKEL_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final Long ARTIKEL_ID = Long.valueOf(300);
	private static final BigDecimal NEUER_PREIS = new BigDecimal("88.88");
	private static final String NEUER_BEZEICHNUNG = "Nachnameneu";
	private static final String NEUER_BEZEICHNUNG_INVALID = "!";
	
	
	@Test
	@InSequence(1)
	public void validate() {
		assertThat(true).isTrue();
	}
		
	@Test
	@InSequence(10)
	public void findArtikelById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long artikelId = ARTIKEL_ID;
		
		final Response response = getHttpsClient().target(ARTIKEL_URI)
                								.request()
                								.accept(APPLICATION_JSON)
                								.get();

    	// Then
		assertThat(response.getStatus())
		.isEqualTo(HTTP_OK);
		final  Artikel artikel= response.readEntity(Artikel.class);
		assertThat(artikel.getId()).isEqualTo(artikelId);

		
		LOGGER.finer("ENDE");
	}
	
	@Test
	@InSequence(10)
	public void findArtikelByIdNichtVorhanden() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long artikelId = ARTIKEL_ID_NICHT_VORHANDEN;
		
		final Response response = getHttpsClient().target(ARTIKEL_URI)
                								.request()
                								.accept(APPLICATION_JSON)
                								.get();

    	// Then
    	assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung).startsWith("Kein Artike mit der ID");
		
		LOGGER.finer("ENDE");
	}
	
	

}
