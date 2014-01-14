package de.shop.artikelverwaltung.web;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static javax.ejb.TransactionAttributeType.SUPPORTS;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.context.Flash;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;
import org.richfaces.push.cdi.Push;
import org.richfaces.ui.iteration.SortOrder;
import org.richfaces.ui.toggle.panelMenu.UIPanelMenuItem;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.artikelverwaltung.service.BezeichnungExistsException;
import de.shop.auth.web.AuthModel;
import de.shop.util.AbstractShopException;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.ConcurrentDeletedException;
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import de.shop.util.web.Client;

//Neon Import
import de.shop.util.web.Captcha;
import de.shop.util.web.Messages;

/////////////////////////////////

/**
 * Dialogsteuerung fuer ArtikelService
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
//@Model
@SessionScoped
@Named
@Stateful
@TransactionAttribute(SUPPORTS)
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
	private static final String CLIENT_ID_CREATE_CAPTCHA_INPUT = "createArtikelForm:captchaInput";
	private static final String MSG_KEY_CREATE_ARTIKEL_WRONG_CAPTCHA = "artikel.wrongCaptcha";
	private static final String CLIENT_ID_CREATE_BEZEICHNUNG = "createArtikelForm:bezeichnung";
	private static final String MSG_KEY_BEZEICHNUNG_EXISTS = ".artikel.bezeichnungExists";
	
	private static final String CLIENT_ID_UPDATE_BEZEICHNUNG = "updateArtikelForm:bezeichnung";
	private static final String MSG_KEY_CONCURRENT_UPDATE = "persistence.concurrentUpdate";
	private static final String MSG_KEY_CONCURRENT_DELETE = "persistence.concurrentDelete";	
	//private static final String CLIENT_ID_DELETE_BUTTON = "form:deleteButton";
	private static final String JSF_UPDATE_ARTIKEL = JSF_ARTIKELVERWALTUNG + "updateArtikel";
	//private static final String MSG_KEY_DELETE_ARTIKEL = "artikel.delete";
	private static final String REQUEST_ARTIKEL_ID = "artikelId";
	private static final String JSF_DELETE_OK = JSF_ARTIKELVERWALTUNG + "okDelete";

	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	@Inject
	private FileHelper fileHelper;
	
	@Inject
	private transient HttpServletRequest request;
	
	@Inject
	private Captcha captcha;
	
	@Inject
	private AuthModel auth;
	
	@Inject
	@Push(topic = "marketing") //Whats a topic ?
	private transient Event<String> neuerArtikelEvent;
	
	/*@Inject
	@Push(topic = "updateArtikel")
	private transient Event<String> updateArtikelEvent;*/
	
	private Long artikelId;
	private Artikel artikel;

	
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
		
		
		@TransactionAttribute
		@Log
		public String createArtikel() {
			
			if (!captcha.getValue().equals(captchaInput)) {
				final String outcome = createArtikelErrorMsg(null);
				return outcome;
			}
			
			// Push-Event fuer Webbrowser
			neuerArtikelEvent.fire(String.valueOf(neuerArtikel.getId()));
			
			//In DB speichern
			neuerArtikel = as.createArtikel(neuerArtikel);
			
			// Aufbereitung fuer viewKunde.xhtml
			artikelId = neuerArtikel.getId();
			artikel = neuerArtikel;
			neuerArtikel = null;  // zuruecksetzen
			
			
			
			return JSF_VIEW_ARTIKEL + JSF_REDIRECT_SUFFIX;
		}

		private String createArtikelErrorMsg(AbstractShopException e) {
			if (e == null) {
				messages.error(MSG_KEY_CREATE_ARTIKEL_WRONG_CAPTCHA, locale, CLIENT_ID_CREATE_CAPTCHA_INPUT);
			}
			else {
				final Class<?> exceptionClass = e.getClass(); //A verifier pour le nom de l exeption
				if (BezeichnungExistsException.class.equals(exceptionClass)) {
					final BezeichnungExistsException e2 = BezeichnungExistsException.class.cast(e);
					messages.error(MSG_KEY_BEZEICHNUNG_EXISTS, locale, CLIENT_ID_CREATE_BEZEICHNUNG, e2.getBezeichnung());
				}
				else {
					throw new RuntimeException(e);
				}
			}
			
			return null;
		}

		public void createEmptyArtikel() {
			captchaInput = null;

			if (neuerArtikel != null) {
				return;
			}

			neuerArtikel = new Artikel();
			/*
			final Adresse adresse = new Adresse();
			adresse.setKunde(neuerKunde);
			neuerKunde.setAdresse(adresse);
			*/
		}
		
		@TransactionAttribute    // Bestellungen ggf. nachladen
		@Log
		public String details(Artikel ausgewaehlterArtikel) {
			if (ausgewaehlterArtikel == null) {
				return null;
			}
			
			artikel = ausgewaehlterArtikel;
			artikelId = ausgewaehlterArtikel.getId();
			
			return JSF_VIEW_ARTIKEL;
		}
		

		
		/**
		 * Verwendung als ValueChangeListener bei updateKunde.xhtml
		 * @param e Ereignis-Objekt mit der Aenderung in einem Eingabefeld, z.B. inputText
		 */
		public void geaendert(ValueChangeEvent e) {
			if (geaendertArtikel) {
				return;
			}
			
			if (e.getOldValue() == null) {
				if (e.getNewValue() != null) {
					geaendertArtikel = true;
				}
				return;
			}

			if (!e.getOldValue().equals(e.getNewValue())) {
				geaendertArtikel = true;				
			}
		}
		
		/////// Block TO DO
		
		@TransactionAttribute
		@Log
		public String update() {
			auth.preserveLogin();
			
			if (!geaendertArtikel || artikel == null) {
				return JSF_INDEX;
			}
					
			LOGGER.tracef("Aktualisierter Artikel: %s", artikel);
			try {
				artikel = as.updateArtikel(artikel);
			}
			catch (BezeichnungExistsException | ConcurrentDeletedException | OptimisticLockException e) {
				final String outcome = updateErrorMsg(e, artikel.getClass());
				return outcome;
			}

			// Push-Event fuer Webbrowser
			neuerArtikelEvent.fire(String.valueOf(artikel.getId()));
			return JSF_VIEW_ARTIKEL + JSF_REDIRECT_SUFFIX;
		}
		
		private String updateErrorMsg(RuntimeException e, Class<? extends Artikel> artikelClass) {
			final Class<? extends RuntimeException> exceptionClass = e.getClass();
			if (BezeichnungExistsException.class.equals(exceptionClass)) {
				final BezeichnungExistsException e2 = BezeichnungExistsException.class.cast(e);
				messages.error(MSG_KEY_BEZEICHNUNG_EXISTS, locale, CLIENT_ID_UPDATE_BEZEICHNUNG, e2.getBezeichnung());
			}
			else if (OptimisticLockException.class.equals(exceptionClass)) {
				messages.error(MSG_KEY_CONCURRENT_UPDATE, locale, null);

			}
			else if (ConcurrentDeletedException.class.equals(exceptionClass)) {
				messages.error(MSG_KEY_CONCURRENT_DELETE, locale, null);
			}
			else {
				throw new RuntimeException(e);
			}
			return null;
		}
		
		@Log
		public String selectForUpdate(Artikel ausgewaehlterArtikel) {
			if (ausgewaehlterArtikel == null) {
				return null;
			}
			
			artikel = ausgewaehlterArtikel;
			
			return Artikel.class.equals(ausgewaehlterArtikel.getClass())
				   ? JSF_UPDATE_ARTIKEL
				   : ""; //JSF_UPDATE_FIRMENKUNDE;
		}
		
		
		// TO DO
		/**
		 * Action Methode, um einen zuvor gesuchten Kunden zu l&ouml;schen
		 * @return URL fuer Startseite im Erfolgsfall, sonst wieder die gleiche Seite
		 */
		@TransactionAttribute
		@Log
		public String deleteAngezeigtenArtikel() {
			if (artikel == null) {
				return null;
			}
			
			LOGGER.trace(artikel);
			//try {
				as.deleteArtikel(artikel);
			//}
			/*catch (Exception e) {
				messages.error(MSG_KEY_DELETE_ARTIKEL, locale, CLIENT_ID_DELETE_BUTTON);
				return null;}*/
			
			
			// Aufbereitung fuer ok.xhtml
			request.setAttribute(REQUEST_ARTIKEL_ID, artikel.getId());
			return JSF_DELETE_OK;
		}

		@TransactionAttribute
		@Log
		public String delete(Artikel ausgewaehlterArtikel) {
			//try {
				as.deleteArtikel(ausgewaehlterArtikel);
		//	}
			//catch (Exception e) {
			//	messages.error(MSG_KEY_DELETE_ARTIKEL, locale, null);
			//	return null;
			//}

			// Aufbereitung fuer ok.xhtml
			request.setAttribute(REQUEST_ARTIKEL_ID, artikel.getId());
			return JSF_DELETE_OK;
		}
		
		public String getFilename(File file) {
			if (file == null) {
				return "";
			}
			
			fileHelper.store(file);
			return file.getFilename();
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
