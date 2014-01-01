package de.shop.artikelverwaltung.web;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Model;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;
import org.richfaces.ui.iteration.SortOrder;
import org.richfaces.ui.toggle.panelMenu.UIPanelMenuItem;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.interceptor.Log;
import de.shop.util.web.Client;

//Neon Import
import de.shop.util.web.Captcha;
import de.shop.util.web.Messages;

/////////////////////////////////

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
	
	////////////////////
	//Neon update
	private static final String JSF_ARTIKELVERWALTUNG = "/artikelverwaltung/";
	private static final String JSF_VIEW_ARTIKEL = JSF_ARTIKELVERWALTUNG + "viewArtikel";
	private static final String CLIENT_ID_ARTIKELID = "form:artikelIdInput";
	private static final String MSG_KEY_ARTIKEL_NOT_FOUND_BY_ID = "artikel.notFound.id";
	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	@Inject
	private transient HttpServletRequest request;
	
	private Long artikelId;
	private Artikel artikel;
	private List<Artikel> artikeln = Collections.emptyList();
	
	private SortOrder bezeichnungSortOrder = SortOrder.unsorted;
	private String bezeichnungFilter = "";
	
	private boolean geaendertArtikel;    // fuer ValueChangeListener
	private Artikel neuerArtikel;
	private String captchaInput;

	private transient UIPanelMenuItem menuItemEmail;
	
	public Artikel getArtikel() {
		return artikel;
	}

	public void setArtikel(Artikel artikel) {
		this.artikel = artikel;
	}

	
	public Long getArtikelId() {
		return artikelId;
	}

	public void setArtikelId(Long artikelId) {
		this.artikelId = artikelId;
	}
	
	
	
	public SortOrder getBezeichnungSortOrder() {
		return bezeichnungSortOrder;
	}

	public void setBezeichnungSortOrder(SortOrder vornameSortOrder) {
		this.bezeichnungSortOrder = vornameSortOrder;
	}

	public void sortByBezeichnung() {
		bezeichnungSortOrder = bezeichnungSortOrder.equals(SortOrder.ascending)
						   ? SortOrder.descending
						   : SortOrder.ascending;
	} 
	
	public String getBezeichnungFilter() {
		return bezeichnungFilter;
	}
	
	public void setBezeichnungFilter(String bezeichnungFilter) {
		this.bezeichnungFilter = bezeichnungFilter;
	}
	
	public Artikel getNeuerArtikel() {
		return neuerArtikel;
	}
	
	public String getCaptchaInput() {
		return captchaInput;
	}

	public void setCaptchaInput(String captchaInput) {
		this.captchaInput = captchaInput;
	}

	public void setMenuItemEmail(UIPanelMenuItem menuItemEmail) {
		this.menuItemEmail = menuItemEmail;
	}
	public UIPanelMenuItem getMenuItemEmail() {
		return menuItemEmail;
	}
	
	//Neon update
		@Log
		public String findArtikelById() {
			if (artikelId == null) {
				return null;
			}
			
			artikel = as.findArtikelById(artikelId);
			if (artikel == null) {
				// Kein Kunde zu gegebener ID gefunden
				return findArtikelByIdErrorMsg(artikelId.toString());
			}		
			
			return JSF_VIEW_ARTIKEL;
		}
		
		private String findArtikelByIdErrorMsg(String id) {
			messages.error(MSG_KEY_ARTIKEL_NOT_FOUND_BY_ID, locale, CLIENT_ID_ARTIKELID, id);
			return null;
		}
		
		
		/**
		 * F&uuml;r rich:autocomplete
		 * @param idPrefix Praefix fuer potenzielle Kunden-IDs
		 * @return Liste der potenziellen Kunden
		 */
		@Log
		public List<Artikel> findArtikelByIdPrefix(String idPrefix) {
			List<Artikel> artikelnPrefix = null;
			Long id = null; 
			try {
				id = Long.valueOf(idPrefix);
			}
			catch (NumberFormatException e) {
				findArtikelByIdErrorMsg(idPrefix);
				return null;
			}
			
			artikelnPrefix = as.findArtikelnByIdPrefix(id);
			if (artikelnPrefix == null || artikelnPrefix.isEmpty()) {
				// Kein Kunde zu gegebenem ID-Praefix vorhanden
				findArtikelByIdErrorMsg(idPrefix);
				return null;
			}
			
			return artikelnPrefix;
		}
		
		@Log
		public void loadArtikelById() {
			// Request-Parameter "kundeId" fuer ID des gesuchten Kunden
			final String idStr = request.getParameter("artikelId");
			Long id;
			try {
				id = Long.valueOf(idStr);
			}
			catch (NumberFormatException e) {
				return;
			}
			
			// Suche durch den Anwendungskern
			artikel = as.findArtikelById(id);
			
		}
		
		
	
	////////////////////////////////////
	////////////////////////////////////
	
	
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
		return "ArtikelModel [artikelId=" + artikelId + " , bezeichnung=" + bezeichnung + "]";
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
