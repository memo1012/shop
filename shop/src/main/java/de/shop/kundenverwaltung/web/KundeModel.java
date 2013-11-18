package de.shop.kundenverwaltung.web;

import java.io.Serializable;

import javax.enterprise.inject.Model;
import javax.faces.context.Flash;
import javax.inject.Inject;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.interceptor.Log;


/**
 * Dialogsteuerung fuer die Kundenverwaltung
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
public class KundeModel implements Serializable {
	private static final long serialVersionUID = -8817180909526894740L;
	
	private static final String FLASH_KUNDE = "kunde";
	private static final String JSF_VIEW_KUNDE = "/kundenverwaltung/viewKunde";
	
	@Inject
	private KundeService ks;
	
	@Inject
	private Flash flash;
	
	private Long kundeId;

	@Override
	public String toString() {
		return "KundeModel [kundeId=" + kundeId + "]";
	}

	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}

	public Long getKundeId() {
		return kundeId;
	}

	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	@Log
	public String findKundeById() {
		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			flash.remove(FLASH_KUNDE);
			return null;
		}
		
		flash.put(FLASH_KUNDE, kunde);
		return JSF_VIEW_KUNDE;
	}
}
