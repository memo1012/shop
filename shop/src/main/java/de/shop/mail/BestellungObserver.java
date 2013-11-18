package de.shop.mail;

import static javax.ejb.TransactionAttributeType.SUPPORTS;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.NeueBestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.interceptor.Log;
import de.shop.util.Config;

@ApplicationScoped
@Stateful
@Log
public class BestellungObserver implements Serializable {
	private static final long serialVersionUID = -1567643645881819340L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	private static final String NEWLINE = System.getProperty("line.separator");
	
	@Resource(lookup = "java:jboss/mail/Default")
	private transient Session mailSession;
	
	@Inject
	private Config config;
	
	private String mailAbsender;
	private String nameAbsender;

	@PostConstruct
	private void init() {
		mailAbsender = config.getAbsenderMail();
		if (mailAbsender == null) {
			LOGGER.warn("Der Absender fuer Bestellung-Emails ist nicht gesetzt.");
			return;
		}
		LOGGER.info("Absender fuer Bestellung-Emails: " + mailAbsender);
		
		nameAbsender = config.getAbsenderName();
	}
	
	@Asynchronous
	@TransactionAttribute(SUPPORTS)
	public void onCreateBestellung(@Observes @NeueBestellung Bestellung bestellung) {
		final Kunde kunde = bestellung.getKunde();
		final String mailEmpfaenger = kunde.getEmail();
		if (mailAbsender == null || mailEmpfaenger == null) {
			return;
		}
		final String vorname = kunde.getVorname() == null ? "" : kunde.getVorname();
		final String nameEmpfaenger = vorname + kunde.getNachname();
		
		final MimeMessage message = new MimeMessage(mailSession);

		try {
			// Absender setzen
			final InternetAddress absenderObj = new InternetAddress(mailAbsender, nameAbsender);
			message.setFrom(absenderObj);
			
			// Empfaenger setzen
			final InternetAddress empfaenger = new InternetAddress(mailEmpfaenger, nameEmpfaenger);
			message.setRecipient(RecipientType.TO, empfaenger);   // RecipientType: TO, CC, BCC

			// Subject setzen
			message.setSubject("Neue Bestellung Nr. " + bestellung.getId());
			
			// Text setzen mit MIME Type "text/plain"
			final StringBuilder sb = new StringBuilder(256);
			sb.append("Neue Bestellung Nr. " + bestellung.getId() + NEWLINE);
			for (Bestellposition bp : bestellung.getBestellpositionen()) {
				sb.append(bp.getAnzahl() + "\t" + bp.getArtikel().getBezeichnung() + NEWLINE);
			}
			final String text = sb.toString();
			LOGGER.trace(text);
			message.setText(text);

			// Hohe Prioritaet einstellen
			//message.setHeader("Importance", "high");
			//message.setHeader("Priority", "urgent");
			//message.setHeader("X-Priority", "1");

			Transport.send(message);
		}
		catch (MessagingException | UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage());
			return;
		}
	}
}
