package de.shop.artikelverwaltung.web;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Model;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.util.interceptor.Log;


/**
 * Dialogsteuerung fuer ArtikelService
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
public class ArtikelModel implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
	private static final String FLASH_ARTIKEL = "artikel";
	
	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";

	private String bezeichnung;
	

	@Inject
	private ArtikelService as;
	
	@Inject
	private Flash flash;
	
	@Inject
	private transient HttpSession session;

	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@Override
	public String toString() {
		return "ArtikelModel [bezeichnung=" + bezeichnung + "]";
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}


	/*public List<Artikel> getLadenhueter() {
		return ladenhueter;
	}*/

	@Log
	public String findArtikelByBezeichnung() {
		final List<Artikel> artikel = as.findArtikelByBezeichnung(bezeichnung);
		flash.put(FLASH_ARTIKEL, artikel);

		return JSF_LIST_ARTIKEL;
	}
	

	/*@Log
	public void loadLadenhueter() {
		ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
	}*/
	
	@Log
	public String selectArtikel() {
		if (session.getAttribute(SESSION_VERFUEGBARE_ARTIKEL) == null) {
			final List<Artikel> alleArtikel = as.findVerfuegbareArtikel();
			session.setAttribute(SESSION_VERFUEGBARE_ARTIKEL, alleArtikel);
		}
		
		return JSF_SELECT_ARTIKEL;
	}
}
