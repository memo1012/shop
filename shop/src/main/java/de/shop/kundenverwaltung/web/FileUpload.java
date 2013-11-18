package de.shop.kundenverwaltung.web;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;
import org.richfaces.model.UploadedFile;
import org.richfaces.ui.input.fileUpload.FileUploadEvent;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.interceptor.Log;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named("fileUploadKunde")  // es koennte aus eine FileUpload-Klasse fuer Artikel geben
@SessionScoped
public class FileUpload implements Serializable {
	private static final long serialVersionUID = 3377481542931338167L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private KundeService ks;
	
	private Long kundeId;

	private byte[] bytes;
	private String contentType;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehfiges Bean %s wurde erzeugt", this);
		
		// TODO Conversation starten und in upload() beenden.
		// Bug in RichFaces: der Upload-Listener beendet die Conversation, bevor die Methode upload() aufgerufen ist
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehfiges Bean %s wird geloescht", this);
	}

	@Override
	public String toString() {
		return "FileUpload [kundeId=" + kundeId + "]";
	}
	
	public Long getKundeId() {
		return kundeId;
	}

	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}

	@Log
	public void uploadListener(FileUploadEvent event) {
		final UploadedFile uploadedFile = event.getUploadedFile();
		contentType = uploadedFile.getContentType();
		LOGGER.debugf("MIME-Type der hochgeladenen Datei: %s", contentType);
		bytes = uploadedFile.getData();
	}

	@Transactional
	@Log
	public String upload() {
		final Kunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			return null;
		}
		ks.setFile(kunde, bytes, contentType);
		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}
	
	public String resetUpload() {
		kundeId = null;
		contentType = null;
		bytes = null;
		
		return null;
	}
}
