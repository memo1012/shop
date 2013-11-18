package de.shop.util.web;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.jboss.logging.Logger;

import de.shop.util.interceptor.Log;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationScoped
@Log
public class Messages implements Serializable {
	private static final long serialVersionUID = -2209093106110666329L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private transient ResourceBundle defaultBundle;
	private Locale defaultLocale;
	private transient Map<Locale, ResourceBundle> bundles;
	private transient Map<String, ResourceBundle> bundlesLanguageStr;	 // z.B. "en" als Schluessel auch fuer en_US

	@PostConstruct
	private void postConstruct() {
		final Application application = FacesContext.getCurrentInstance().getApplication();
		
		final String messageBundle = application.getMessageBundle();
		defaultLocale = application.getDefaultLocale();
		LOGGER.infof("Default Locale: %s", defaultLocale);
		
		defaultBundle = ResourceBundle.getBundle(messageBundle, defaultLocale);
		
		bundles = new HashMap<>();
		bundles.put(defaultLocale, defaultBundle);
		
		bundlesLanguageStr = new HashMap<>();
		String localeStr = defaultLocale.toString();
		if (localeStr.length() > 2) {
			localeStr = localeStr.substring(0, 2);
		}
		bundlesLanguageStr.put(localeStr, defaultBundle);
		
		final Iterator<Locale> locales = application.getSupportedLocales();
		final Set<String> languages = new HashSet<>();
		while (locales.hasNext()) {
			final Locale lc = locales.next();
			final ResourceBundle bundle = ResourceBundle.getBundle(messageBundle, lc);
			bundles.put(lc, bundle);
			
			localeStr = lc.toString();
			if (localeStr.length() > 2) {
				localeStr = localeStr.substring(0, 2);
			}
			if (!languages.contains(localeStr)) {
				bundlesLanguageStr.put(localeStr, bundle);
				languages.add(localeStr);
			}
		}
		LOGGER.infof("Locales: %s", bundles.keySet());
	}
	
	/**
	 * Fuer Fehlermeldungen an der Web-Oberflaeche, die durch Exceptions verursacht werden
	 * @param msgKey Schluessel in ApplicationMessages
	 * @param locale Locale im Webbrowser
	 * @param idUiKomponente ID, bei der in der JSF-Seite die Meldung platziert werden soll
	 * @param args Werte fuer die Platzhalter in der lokalisierten Meldung aus ApplicationMessages
	 */
	public void error(String msgKey, Locale locale, String idUiKomponente, Object... args) {
		createMsg(msgKey, locale, idUiKomponente, SEVERITY_ERROR, args);
	}
	
	public void warn(String msgKey, Locale locale, String idUiKomponente, Object... args) {
		createMsg(msgKey, locale, idUiKomponente, SEVERITY_WARN, args);
	}
	
	public void info(String msgKey, Locale locale, String idUiKomponente, Object... args) {
		createMsg(msgKey, locale, idUiKomponente, SEVERITY_INFO, args);
	}
	
	private void createMsg(String msgKey,
			               Locale locale,
			               String idUiKomponente,
			               Severity severity,
			               Object... args) {
		ResourceBundle bundle = bundles.get(locale);
		if (bundle == null) {
			// Sprache (z.B. "en") statt Locale (z.B. "en_US") verwenden, da die Sprache allgemeiner ist
			String localeStr = locale.toString();
			if (localeStr.length() > 2) {
				localeStr = localeStr.substring(0, 2);
				bundle = bundlesLanguageStr.get(localeStr);
			}
			
			if (bundle == null) {
				System.out.println("!!!default");
				// Keine Texte zu aktuellen Sprache gefunden: Default-Sprache verwenden
				bundle = defaultBundle;
				locale = defaultLocale;
			}
		}
		final String msgPattern = bundle.getString(msgKey);
		final MessageFormat formatter = new MessageFormat(msgPattern, locale);
		final String msg = formatter.format(args);
		
		final FacesMessage facesMsg = new FacesMessage(severity, msg, null);
		FacesContext.getCurrentInstance().addMessage(idUiKomponente, facesMsg);
	}
}
