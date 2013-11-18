package de.shop.bestellverwaltung.web;

import static de.shop.util.Constants.JSF_DEFAULT_ERROR;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

import de.shop.auth.web.AuthModel;
import de.shop.auth.web.KundeLoggedIn;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.interceptor.Log;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
public class BestellungModel implements Serializable {
	private static final long serialVersionUID = -1790295502719370565L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final String JSF_VIEW_BESTELLUNG = "/bestellverwaltung/viewBestellung";
	
	@Inject
	private Warenkorb warenkorb;
	
	@Inject
	private BestellungService bs;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private AuthModel auth;
	
	@Inject
	@KundeLoggedIn
	private Kunde kunde;
	
	@Inject
	private Flash flash;
	

	@Transactional
	@Log
	public String bestellen() {
		auth.preserveLogin();
		
		if (warenkorb == null || warenkorb.getPositionen() == null || warenkorb.getPositionen().isEmpty()) {
			// Darf nicht passieren, wenn der Button zum Bestellen verfuegbar ist
			return JSF_DEFAULT_ERROR;
		}
		
		// Den eingeloggten Kunden mit seinen Bestellungen ermitteln, und dann die neue Bestellung zu ergaenzen
		kunde = ks.findKundeById(kunde.getId(), FetchType.MIT_BESTELLUNGEN);
		
		// Aus dem Warenkorb nur Positionen mit Anzahl > 0
		final List<Bestellposition> positionen = warenkorb.getPositionen();
		final List<Bestellposition> neuePositionen = new ArrayList<>(positionen.size());
		for (Bestellposition bp : positionen) {
			if (bp.getAnzahl() > 0) {
				neuePositionen.add(bp);
			}
		}
		
		// Warenkorb zuruecksetzen
		warenkorb.endConversation();
		
		// Neue Bestellung mit neuen Bestellpositionen erstellen
		Bestellung bestellung = new Bestellung();
		bestellung.setBestellpositionen(neuePositionen);
		LOGGER.tracef("Neue Bestellung: %s\nBestellpositionen: %s", bestellung, bestellung.getBestellpositionen());
		
		// Bestellung mit VORHANDENEM Kunden verknuepfen:
		// dessen Bestellungen muessen geladen sein, weil es eine bidirektionale Beziehung ist
		bestellung = bs.createBestellung(bestellung, kunde);
		
		// Bestellung im Flash speichern wegen anschliessendem Redirect
		flash.put("bestellung", bestellung);
		
		return JSF_VIEW_BESTELLUNG;
	}
}
