package de.shop.bestellverwaltung.web;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.util.interceptor.Log;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
public class Warenkorb implements Serializable {
	private static final long serialVersionUID = -1981070683990640854L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String JSF_VIEW_WARENKORB = "/bestellverwaltung/viewWarenkorb?init=true";
	private static final int TIMEOUT = 5;
	
	private List<Bestellposition> positionen;
	private Long artikelId;  // fuer selectArtikel.xhtml
	
	@Inject
	private transient Conversation conversation;
	
	@Inject
	private ArtikelService as;

	@PostConstruct
	private void postConstruct() {
		positionen = new ArrayList<>();
		
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	public List<Bestellposition> getPositionen() {
		return positionen;
	}
		
	public void setArtikelId(Long artikelId) {
		this.artikelId = artikelId;
	}

	public Long getArtikelId() {
		return artikelId;
	}

	@Override
	public String toString() {
		return "Warenkorb " + positionen;
	}
	
	/**
	 * Den selektierten Artikel zum Warenkorb hinzufuergen
	 * @param artikel Der selektierte Artikel
	 * @return Pfad zur Anzeige des aktuellen Warenkorbs
	 */
	@Log
	public String add(Artikel artikel) {
		beginConversation();
		
		for (Bestellposition bp : positionen) {
			if (bp.getArtikel().equals(artikel)) {
				// bereits im Warenkorb
				final short vorhandeneAnzahl = bp.getAnzahl();
				bp.setAnzahl((short) (vorhandeneAnzahl + 1));
				return JSF_VIEW_WARENKORB;
			}
		}
		
		final Bestellposition neu = new Bestellposition(artikel);
		positionen.add(neu);
		return JSF_VIEW_WARENKORB;
	}
	
	/**
	 * Den selektierten Artikel zum Warenkorb hinzufuergen
	 * @return Pfad zur Anzeige des aktuellen Warenkorbs
	 */
	@Log
	public String add() {
		final Artikel artikel = as.findArtikelById(artikelId);
		if (artikel == null) {
			return null;
		}
		
		final String outcome = add(artikel);
		artikelId = null;
		return outcome;
	}
	
	@Log
	public void beginConversation() {
		if (!conversation.isTransient()) {
			LOGGER.debug("Die Conversation ist bereits gestartet");
			return;
		}
		
		LOGGER.debug("Neue Conversation");
		conversation.begin();
		conversation.setTimeout(MINUTES.toMillis(TIMEOUT));
		LOGGER.trace("Conversation beginnt");
	}
	
	@Log
	public void endConversation() {
		conversation.end();
		LOGGER.trace("Conversation beendet");
	}
	
	/**
	 * Eine potenzielle Bestellposition entfernen
	 * @param bestellposition Die zu entfernende Bestellposition
	 */
	@Log
	public void remove(Bestellposition bestellposition) {
		positionen.remove(bestellposition);
		if (positionen.isEmpty()) {
			endConversation();
		}
	}
}
