package de.shop.kundenverwaltung.rest;

import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.KEINE_ID;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.LIST_LINK;
import static de.shop.util.Constants.REMOVE_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UPDATE_LINK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.BestellungResource;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.File;
import de.shop.util.rest.NotFoundException;
import de.shop.util.rest.UriHelper;

@Path("/kunden")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75",
		TEXT_XML + ";qs=0.5" })
@Consumes
@Log
public class KundeResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());
	private static final String VERSION = "1.0";

	// public fuer Testklassen
	public static final String KUNDEN_ID_PATH_PARAM = "kundeId";
	public static final String KUNDEN_NACHNAME_QUERY_PARAM = "nachname";
	public static final String KUNDEN_PLZ_QUERY_PARAM = "plz";
	public static final String KUNDEN_EMAIL_QUERY_PARAM = "email";
	public static final String KUNDEN_GESCHLECHT_QUERY_PARAM = "geschlecht";
	
	
	private static final String NOT_FOUND_ID = "kunde.notFound.id";
	private static final String NOT_FOUND_FILE = "kunde.notFound.file";

	@Context
	private UriInfo uriInfo;

	@Inject
	private KundeService ks;

	@Inject
	private BestellungService bs;

	@Inject
	private BestellungResource bestellungResource;

	@Inject
	private UriHelper uriHelper;

	@Inject
	private Principal principal;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	@GET
	@Produces(TEXT_PLAIN)
	@Path("version")
	public String getVersion() {
		return VERSION;
	}

	/**
	 * Mit der URL /kunden/{id} einen Kunden ermitteln
	 * 
	 * @param id
	 *            ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Response findKundeById(@PathParam("id") Long id) {
		// final Locale locale = localeHelper.getLocale(headers);
		String username = principal.getName();
		Kunde angemeldeter_kunde = ks.findKundeByUserName(username);

		
		final Kunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE);
		if (kunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		setStructuralLinks(kunde, uriInfo);

		return Response.ok(kunde).links(getTransitionalLinks(kunde, uriInfo))
				.build();
	}

	public void setStructuralLinks(Kunde kunde, UriInfo uriInfo) {
		// URI fuer Bestellungen setzen
		final URI uri = getUriBestellungen(kunde, uriInfo);
		kunde.setBestellungenUri(uri);

		LOGGER.trace(kunde);
	}

	private URI getUriBestellungen(Kunde kunde, UriInfo uriInfo) {
		return uriHelper.getUri(KundeResource.class,
				"findBestellungenByKundeId", kunde.getId(), uriInfo);
	}

	public URI getUriKunde(Kunde kunde, UriInfo uriInfo) {
		return uriHelper.getUri(KundeResource.class, "findKundeById",
				kunde.getId(), uriInfo);
	}

	public Link[] getTransitionalLinks(Kunde kunde, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriKunde(kunde, uriInfo))
				.rel(SELF_LINK).build();

		final Link list = Link
				.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
				.rel(LIST_LINK).build();

		final Link add = Link
				.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
				.rel(ADD_LINK).build();

		final Link update = Link
				.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
				.rel(UPDATE_LINK).build();

		final Link remove = Link
				.fromUri(
						uriHelper.getUri(KundeResource.class, "deleteKunde",
								kunde.getId(), uriInfo)).rel(REMOVE_LINK)
				.build();

		return new Link[] { self, list, add, update, remove };
	}

	/**
	 * Mit der URL /kunden werden alle Kunden ermittelt oder mit
	 * kundenverwaltung/kunden?nachname=... diejenigen mit einem bestimmten
	 * Nachnamen.
	 * 
	 * @return Collection mit den gefundenen Kundendaten
	 */
	@GET
	public Response findKundenByNachname(
			@QueryParam("nachname") @DefaultValue("") String nachname) {
		List<Kunde> kunden = null;
		if ("".equals(nachname)) {
			kunden = ks.findAllKunden(FetchType.NUR_KUNDE, null);
			if (kunden.isEmpty()) {
				final String msg = "Keine Kunden vorhanden";
				throw new NotFoundException(msg);
			}
		} else {
			// final Locale locale = localeHelper.getLocale(headers);
			kunden = ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
			if (kunden.isEmpty()) {
				final String msg = "Kein Kunde gefunden mit Nachname "
						+ nachname;
				throw new NotFoundException(msg);
			}
		}

		// URLs innerhalb der gefundenen Kunden anpassen
		for (Kunde kunde : kunden) {
			setStructuralLinks(kunde, uriInfo);
		}

		return Response.ok(kunden)
				.links(getTransitionalLinksKunden(kunden, uriInfo)).build();
	}

	private Link[] getTransitionalLinksKunden(List<? extends Kunde> kunden,
			UriInfo uriInfo) {
		if (kunden == null || kunden.isEmpty()) {
			return null;
		}

		final Link first = Link.fromUri(getUriKunde(kunden.get(0), uriInfo))
				.rel(FIRST_LINK).build();
		final int lastPos = kunden.size() - 1;
		final Link last = Link
				.fromUri(getUriKunde(kunden.get(lastPos), uriInfo))
				.rel(LAST_LINK).build();

		return new Link[] { first, last };
	}

	@GET
	@Path("/prefix/id/{id:[1-9][0-9]*}")
	public Collection<Long> findIdsByPrefix(@PathParam("id") String idPrefix) {
		final Collection<Long> ids = ks.findIdsByPrefix(idPrefix);
		return ids;
	}

	@GET
	@Path("/prefix/nachname/{nachname}")
	public Collection<String> findNachnamenByPrefix(
			@PathParam("nachname") String nachnamePrefix) {
		final Collection<String> nachnamen = ks
				.findNachnamenByPrefix(nachnamePrefix);
		return nachnamen;
	}

	/**
	 * Mit der URL /kunden/{id}/bestellungen die Bestellungen zu eine Kunden
	 * ermitteln
	 * 
	 * @param kundeId
	 *            ID des Kunden
	 * @return Objekt mit Bestellungsdaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungen")
	public Response findBestellungenByKundeId(@PathParam("id") Long kundeId) {
		// final Locale locale = localeHelper.getLocale(headers);

		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			throw new NotFoundException("Kein Kunde mit der ID " + kundeId
					+ " gefunden.");
		}

		final List<Bestellung> bestellungen = bs.findBestellungenByKunde(kunde,
				BestellungService.FetchType.NUR_BESTELLUNG);

		// URIs innerhalb der gefundenen Bestellungen anpassen
		if (bestellungen != null) {
			for (Bestellung bestellung : bestellungen) {
				bestellungResource.setStructuralLinks(bestellung, uriInfo);
			}
		}

		// URLs innerhalb der gefundenen Bestellungen anpassen
		// for (Bestellung bestellung : bestellungen) {
		// uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		// }

		return Response
				.ok(new GenericEntity<List<Bestellung>>(bestellungen) {
				})
				.links(getTransitionalLinksBestellungen(bestellungen, kunde,
						uriInfo)).build();
	}

	private Link[] getTransitionalLinksBestellungen(
			List<Bestellung> bestellungen, Kunde kunde, UriInfo uriInfo) {
		if (bestellungen == null || bestellungen.isEmpty()) {
			return new Link[0];
		}

		final Link self = Link.fromUri(getUriBestellungen(kunde, uriInfo))
				.rel(SELF_LINK).build();

		final Link first = Link
				.fromUri(
						bestellungResource.getUriBestellung(
								bestellungen.get(0), uriInfo)).rel(FIRST_LINK)
				.build();

		final int lastPos = bestellungen.size() - 1;
		final Link last = Link
				.fromUri(
						bestellungResource.getUriBestellung(
								bestellungen.get(lastPos), uriInfo))
				.rel(LAST_LINK).build();

		return new Link[] { self, first, last };
	}

	@GET
	@Path("{id:[1-9][0-9]*}/bestellungenIds")
	@Produces({ APPLICATION_JSON, TEXT_PLAIN + ";qs=0.75",
			APPLICATION_XML + ";qs=0.5" })
	public Response findBestellungenIdsByKundeId(@PathParam("id") Long kundeId) {
		final Kunde kunde = ks.findKundeById(kundeId,
				FetchType.MIT_BESTELLUNGEN);
		if (kunde == null) {
			throw new NotFoundException(NOT_FOUND_ID, kundeId);
		}
		final Collection<Bestellung> bestellungen = bs.findBestellungenByKunde(
				kunde, BestellungService.FetchType.NUR_BESTELLUNG);

		final int anzahl = bestellungen.size();
		final Collection<Long> bestellungenIds = new ArrayList<>(anzahl);
		for (Bestellung bestellung : bestellungen) {
			bestellungenIds.add(bestellung.getId());
		}

		return Response.ok(
				new GenericEntity<Collection<Long>>(bestellungenIds) {
				}).build();
	}

	/**
	 * Mit der URL /kunden einen Privatkunden per POST anlegen.
	 * 
	 * @param kunde
	 *            neuer Kunde
	 * @return Response-Objekt mit URL des neuen Privatkunden
	 */
	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	public Response createKunde(@Valid Kunde kunde) {
		// final Locale locale = localeHelper.getLocale(headers);

		kunde.setId(KEINE_ID);
		kunde.setPasswordWdh(kunde.getPassword());

		final Adresse adresse = kunde.getAdresse();
		if (adresse != null) {
			adresse.setKunde(kunde);
		}
		kunde.setBestellungenUri(null);

		kunde = ks.createKunde(kunde);
		LOGGER.tracef("Kunde: %s", kunde);

		return Response.created(getUriKunde(kunde, uriInfo)).build();
	}

	/**
	 * Mit der URL /kunden einen Kunden per PUT aktualisieren
	 * 
	 * @param kunde
	 *            zu aktualisierende Daten des Kunden
	 */
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Transactional
	public Response updateKunde(@Valid Kunde kunde) {
		// Vorhandenen Kunden ermitteln
		// final Locale locale = localeHelper.getLocale(headers);
		final Kunde origKunde = ks.findKundeById(kunde.getId(),
				FetchType.NUR_KUNDE);
		if (origKunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID "
					+ kunde.getId();
			throw new NotFoundException(msg);
		}
		LOGGER.tracef("Kunde vorher: %s", origKunde);

		// Daten des vorhandenen Kunden ueberschreiben
		origKunde.setValues(kunde);
		LOGGER.tracef("Kunde nachher: %s", origKunde);

		// Update durchfuehren
		kunde = ks.updateKunde(origKunde, false);
		setStructuralLinks(kunde, uriInfo);

		return Response.ok(kunde).links(getTransitionalLinks(kunde, uriInfo))
				.build();
	}

	/**
	 * Mit der URL /kunden{id} einen Kunden per DELETE l&ouml;schen
	 * 
	 * @param kundeId
	 *            des zu l&ouml;schenden Kunden
	 */
	@Path("{id:[0-9]+}")
	@DELETE
	@Produces
	public void deleteKunde(@PathParam("id") Long kundeId) {
		// final Locale locale = localeHelper.getLocale(headers);
		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		ks.deleteKunde(kunde);
	}

	@Path("{id:[1-9][0-9]*}/file")
	@POST
	@Consumes({ "image/jpeg", "image/pjpeg", "image/png" })
	// RESTEasy unterstuetzt nicht video/mp4
	@Transactional
	public Response upload(@PathParam("id") Long kundeId, byte[] bytes) {
		ks.setFile(kundeId, bytes);
		return Response.created(
				uriHelper.getUri(KundeResource.class, "download", kundeId,
						uriInfo)).build();
	}

	@Path("{id:[1-9][0-9]*}/file")
	@GET
	@Produces({ "image/jpeg", "image/pjpeg", "image/png" })
	@Transactional
	// Nachladen der Datei : AbstractKunde referenziert File mit Lazy Fetching
	public byte[] download(@PathParam("id") Long kundeId) {
		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			throw new NotFoundException(NOT_FOUND_ID);
		}

		final File file = kunde.getFile();
		if (file == null) {
			throw new NotFoundException(NOT_FOUND_FILE);
		}
		LOGGER.tracef("%s", file.toString());

		return file.getBytes();
	}
}
