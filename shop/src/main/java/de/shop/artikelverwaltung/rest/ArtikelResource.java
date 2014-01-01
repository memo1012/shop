package de.shop.artikelverwaltung.rest;

import static de.shop.util.Constants.SELF_LINK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static de.shop.util.Constants.KEINE_ID;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.util.interceptor.Log;
import de.shop.util.rest.NotFoundException;
import de.shop.util.rest.UriHelper;

@Path("/artikel/")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75",
		TEXT_XML + ";qs=0.5" })
@Consumes
@Log
public class ArtikelResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());

	// public fuer Testklassen
	public static final String PARAM_ID = "artikelId";
	public static final String PARAM_BEZEICHNUNG = "artikelBezeichnung";

	@Context
	private UriInfo uriInfo;

	@Inject
	private ArtikelService as;

	@Inject
	private UriHelper uriHelper;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	@GET
	@Path("{id:[1-9][0-9]*}")
	public Response findArtikelById(@PathParam("id") Long id,
			@Context UriInfo uriInfo) {
		final Artikel artikel = as.findArtikelById(id);
		if (artikel == null) {
			final String msg = "Kein Artikel gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		return Response.ok(artikel)
				.links(getTransitionalLinks(artikel, uriInfo)).build();
	}
	
	
	

	private Link[] getTransitionalLinks(Artikel artikel, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriArtikel(artikel, uriInfo))
				.rel(SELF_LINK).build();

		return new Link[] {self};
	}

	public URI getUriArtikel(Artikel artikel, UriInfo uriInfo) {
		return uriHelper.getUri(ArtikelResource.class, "findArtikelById",
				artikel.getId(), uriInfo);
	}

	@GET
	@Path("/bezeichnung/{bezeichnung}")
	public List<String> findArtikelByBezeichnung(
			@PathParam("bezeichnung") String bezeichnung,
			@Context UriInfo uriInfo) {
		final List<String> artikel = as.findArtikelByPrefix(bezeichnung);
		return artikel;
	}

	/**
	 * Mit der URL /artikel einen Artikel per PUT aktualisieren
	 * 
	 * @param artikel
	 *            zu aktualisierende Daten des Artikel
	 */
	@PUT
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Transactional
	public Response updateArtikel(Artikel artikel) {
		// Vorhandenen Artikel ermitteln
		final Artikel origArt = as.findArtikelById(artikel.getId());
		if (origArt == null) {
			final String msg = "Kein Artikel gefunden mit der ID "
					+ artikel.getId();
			throw new NotFoundException(msg);
		}
		LOGGER.tracef("Artikel vorher: %s", origArt);

		// Daten des vorhandenen Kunden ueberschreiben
		origArt.setValues(artikel);
		LOGGER.tracef("Artikel nachher: %s", origArt);

		// Update durchfuehren
		artikel = as.updateArtikel(origArt);
		if (artikel == null) {
			// TODO msg passend zu locale
			final String msg = "Kein Kunde gefunden mit der ID "
					+ origArt.getId();
			throw new NotFoundException(msg);
		}

		return Response.ok(artikel)
				.links(getTransitionalLinks(artikel, uriInfo)).build();
	}

	@POST
	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
	@Produces
	@Transactional
	public Response createArtikel(@Valid Artikel artikel) {
		LOGGER.trace("In Artikel Post");
		LOGGER.tracef("Prob Artikel: %s", artikel);

		artikel.setId(KEINE_ID);
		// artikel.setBezeichnung(artikel.getBezeichnung());

		// kunde = (Privatkunde) ks.createKunde(kunde, locale);
		artikel = as.createArtikel(artikel);
		LOGGER.tracef("Artikel: %s", artikel);

		return Response.created(getUriArtikel(artikel, uriInfo)).build();
	}
}
