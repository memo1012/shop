package de.shop.artikelverwaltung.rest;

import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.ARTIKEL_ID_URI;
import static de.shop.util.TestConstants.ARTIKEL_BEZEICHNUNG_URI;
import static de.shop.util.TestConstants.PASSWORD_MITARBEITER;
import static de.shop.util.TestConstants.USERNAME_MITARBEITER;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.fest.assertions.api.Assertions.assertThat;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.AbstractResourceTest;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;

import java.util.logging.Logger;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ArtikelResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());

	private static final Long ARTIKEL_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final Long ARTIKEL_ID = Long.valueOf(300);
	private static final Long ARTIKEL_ID_TO_UPDATE = Long.valueOf(301);
	private static final Double NEUER_PREIS = new Double("88.88");
	private static final String NEUE_BEZEICHNUNG = "Nachnameneu";
	private static final String ARTIKEL_BEZEICHNUNG_NICHT_VORHANDEN = "Eifelturm";
	private static final String ARTIKEL_BEZEICHNUNG = "Tisch 'Oval'";

	@Test
	@InSequence(0)
	public void validate() {
		assertThat(true).isTrue();
	}

	@Test
	@InSequence(1)
	public void findArtikelById() {
		LOGGER.finer("BEGINN");

		// Given
		final Long artikelId = ARTIKEL_ID;

		// When
		final Response response = getHttpsClient().target(ARTIKEL_ID_URI)
				.resolveTemplate(ArtikelResource.PARAM_ID, artikelId).request()
				.accept(APPLICATION_JSON).get();

		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		final Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId()).isEqualTo(artikelId);

		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(2)
	public void findArtikelByIdNichtVorhanden() {
		LOGGER.finer("BEGINN");

		// Given
		final Long artikelId = ARTIKEL_ID_NICHT_VORHANDEN;

		// When
		final Response response = getHttpsClient().target(ARTIKEL_ID_URI)
				.resolveTemplate(ArtikelResource.PARAM_ID, artikelId).request()
				.accept(APPLICATION_JSON).get();

		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_NOT_FOUND);
		final String fehlermeldung = response.readEntity(String.class);
		assertThat(fehlermeldung)
				.startsWith("Kein Artikel gefunden mit der ID");

		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(3)
	public void findArtikelByBezeichnung() {
		LOGGER.finer("BEGINN");

		// Given
		final String artikelBezeichnung = ARTIKEL_BEZEICHNUNG;

		// When
		final Response response = getHttpsClient()
				.target(ARTIKEL_BEZEICHNUNG_URI)
				.resolveTemplate(ArtikelResource.PARAM_BEZEICHNUNG,
						artikelBezeichnung).request().accept(APPLICATION_JSON)
				.get();

		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);

		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(4)
	public void findArtikelByBezeichnungNichtVorhanden() {
		LOGGER.finer("BEGINN");

		// Given
		final String artikelBezeichnung = ARTIKEL_BEZEICHNUNG_NICHT_VORHANDEN;

		// When
		final Response response = getHttpsClient()
				.target(ARTIKEL_BEZEICHNUNG_URI)
				.resolveTemplate(ArtikelResource.PARAM_BEZEICHNUNG,
						artikelBezeichnung).request().accept(APPLICATION_JSON)
				.get();

		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);

		final String respstring = response.readEntity(String.class);
		assertThat(respstring).startsWith("[]");

		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(10)
	public void createArtikel() {
		LOGGER.finer("BEGINN");

		// Given
		final Artikel artikel = new Artikel(NEUE_BEZEICHNUNG, NEUER_PREIS);

		// When
		Long id;
		Response response = getHttpsClient(USERNAME_MITARBEITER,
				PASSWORD_MITARBEITER).target(ARTIKEL_URI).request()
				.post(json(artikel));

		// Then
		assertThat(response.getStatus()).isEqualTo(HTTP_CREATED);
		final String location = response.getLocation().toString();
		response.close();

		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		id = Long.valueOf(idStr);
		assertThat(id).isPositive();

		// Gibt es den neuen Artikel?
		response = getHttpsClient().target(ARTIKEL_ID_URI)
				.resolveTemplate(ArtikelResource.PARAM_ID, id).request()
				.accept(APPLICATION_JSON).get();
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);
		response.close();

		LOGGER.finer("ENDE");
	}

	@Test
	@InSequence(11)
	public void updateArtikel() {
		LOGGER.finer("BEGINN");

		// Given
		final Long artikelId = ARTIKEL_ID_TO_UPDATE;
		final String neueBezeichnung = NEUE_BEZEICHNUNG;

		// When
		Response response = getHttpsClient().target(ARTIKEL_ID_URI)
				.resolveTemplate(ArtikelResource.PARAM_ID, artikelId).request()
				.accept(APPLICATION_JSON).get();
		final Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId()).isEqualTo(artikelId);

		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem
		// Nachnamen bauen
		artikel.setBezeichnung(neueBezeichnung);

		response = getHttpsClient(USERNAME_MITARBEITER, PASSWORD_MITARBEITER)
				.target(ARTIKEL_URI).request().accept(APPLICATION_JSON)
				.put(json(artikel));
		assertThat(response.getStatus()).isEqualTo(HTTP_OK);

		response.close();

		LOGGER.finer("ENDE");
	}

}
