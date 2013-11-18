package de.shop.auth.web;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import de.shop.auth.domain.RolleType;
import de.shop.auth.service.AuthService;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.interceptor.Log;
import de.shop.util.web.Client;
import de.shop.util.web.Messages;


/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
public class AuthModel implements Serializable {
	private static final long serialVersionUID = -8604525347843804815L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String MSG_KEY_LOGIN_ERROR = "auth.login.error";
	private static final String CLIENT_ID_USERNAME = "loginFormHeader:username";
	private static final String MSG_KEY_UPDATE_ROLLEN_KEIN_USER = "kunde.notFound.username";
	private static final String CLIENT_ID_USERNAME_INPUT = "rollenForm:usernameInput";
	
	private String username;
	private String password;
	
	private String usernameUpdateRollen;
	private Long kundeId;

	@Inject
	private Principal principal;
	
	@Produces
	@SessionScoped
	@KundeLoggedIn
	private Kunde user;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private AuthService authService;
	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	@Inject
	private transient HttpServletRequest request;
	
	// FIXME https://issues.jboss.org/browse/WFLY-1533
	//@Inject
	//private transient HttpSession session;
	
	private List<RolleType> ausgewaehlteRollen;
	private List<RolleType> ausgewaehlteRollenOrig;
	private List<RolleType> verfuegbareRollen;

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
		return "AuthModel [username=" + username + ", password=" + password + ", user=" + user + "]";
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUsernameUpdateRollen() {
		return usernameUpdateRollen;
	}

	public void setUsernameUpdateRollen(String usernameUpdateRollen) {
		this.usernameUpdateRollen = usernameUpdateRollen;
	}

	public List<RolleType> getAusgewaehlteRollen() {
		return ausgewaehlteRollen;
	}

	public void setAusgewaehlteRollen(List<RolleType> ausgewaehlteRollen) {
		this.ausgewaehlteRollen = ausgewaehlteRollen;
	}

	public List<RolleType> getVerfuegbareRollen() {
		return verfuegbareRollen;
	}

	public void setVerfuegbareRollen(List<RolleType> verfuegbareRollen) {
		this.verfuegbareRollen = verfuegbareRollen;
	}

	/**
	 * Einloggen eines registrierten Kunden mit Benutzername und Password.
	 * @return Pfad zur aktuellen Seite fuer Refresh
	 */
	@Log
	public String login() {
		if (Strings.isNullOrEmpty(username)) {
			reset();
			messages.error(MSG_KEY_LOGIN_ERROR, locale, CLIENT_ID_USERNAME);
			return null;   // Gleiche Seite nochmals aufrufen: mit den fehlerhaften Werten
		}
		
		try {
			request.login(username, password);
		}
		catch (ServletException e) {
			LOGGER.tracef("username=%s, password=%s", username, password);
			reset();
			messages.error(MSG_KEY_LOGIN_ERROR, locale, CLIENT_ID_USERNAME);
			return null;   // Gleiche Seite nochmals aufrufen: mit den fehlerhaften Werten
		}
		
		user = ks.findKundeByUserName(username);
		if (user == null) {
			logout();
			throw new InternalError("Kein Kunde mit dem Loginnamen \"" + username + "\" gefunden");
		}
		
		// Gleiche JSF-Seite erneut aufrufen: Re-Render fuer das Navigationsmenue stattfindet
		final String path = FacesContext.getCurrentInstance().getViewRoot().getViewId();
		return path;
	}
	
	/**
	 * Nachtraegliche Einloggen eines registrierten Kunden mit Benutzername und Password.
	 */
	@Log
	public void preserveLogin() {
		if (username != null && user != null) {
			return;
		}
		
		// Benutzername beim Login ermitteln
		username = principal.getName();

		user = ks.findKundeByUserName(username);
		if (user == null) {
			// Darf nicht passieren, wenn unmittelbar zuvor das Login erfolgreich war
			logout();
			throw new InternalError("Kein Kunde mit dem Loginnamen \"" + username + "\" gefunden");
		}
	}


	/**
	 */
	private void reset() {
		username = null;
		password = null;
		user = null;
	}

	
	/**
	 * Ausloggen und L&ouml;schen der gesamten Session-Daten.
	 * @return Pfad zur Startseite einschliesslich Redirect
	 */
	@Log
	public String logout() {
		try {
			request.logout();  // Der Loginname wird zurueckgesetzt
		}
		catch (ServletException ignore) {
			return null;
		}
		
		reset();
		// FIXME https://issues.jboss.org/browse/WFLY-1533
		//session.invalidate();
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().clear();
		
		// redirect bewirkt neuen Request, der *NACH* der Session ist
		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}

	/**
	 * &Uuml;berpr&uuml;fen, ob Login-Informationen vorhanden sind.
	 * @return true, falls man eingeloggt ist.
	 */
	@Log
	public boolean isLoggedIn() {
		return user != null;
	}
	
	@Log
	public List<String> findUsernameListByUsernamePrefix(String usernamePrefix) {
		final List<String> usernameList = authService.findUsernameListByUsernamePrefix(usernamePrefix);
		return usernameList;
	}
	
	@Log
	public String findRollenByUsername() {
		// Gibt es den Usernamen ueberhaupt?
		final Kunde kunde = ks.findKundeByUserName(usernameUpdateRollen);
		if (kunde == null) {
			kundeId = null;
			ausgewaehlteRollenOrig = null;
			ausgewaehlteRollen = null;
			
			messages.error(MSG_KEY_UPDATE_ROLLEN_KEIN_USER, locale, CLIENT_ID_USERNAME_INPUT, usernameUpdateRollen);
			return null;
		}
		
		ausgewaehlteRollenOrig = Lists.newArrayList(kunde.getRollen());
		ausgewaehlteRollen = Lists.newArrayList(kunde.getRollen());
		kundeId = kunde.getId();
		LOGGER.tracef("Rollen von %s: %s", usernameUpdateRollen, ausgewaehlteRollen);

		if (verfuegbareRollen == null) {
			verfuegbareRollen = Arrays.asList(RolleType.values());
		}
		
		return null;
	}
	
	@Transactional
	@Log
	public String updateRollen() {
		// Zusaetzliche Rollen?
		final List<RolleType> zusaetzlicheRollen = new ArrayList<>();
		for (RolleType rolle : ausgewaehlteRollen) {
			if (!ausgewaehlteRollenOrig.contains(rolle)) {
				zusaetzlicheRollen.add(rolle);
			}
		}
		authService.addRollen(kundeId, zusaetzlicheRollen);
		
		// Zu entfernende Rollen?
		final List<RolleType> zuEntfernendeRollen = new ArrayList<>();
		for (RolleType rolle : ausgewaehlteRollenOrig) {
			if (!ausgewaehlteRollen.contains(rolle)) {
				zuEntfernendeRollen.add(rolle);
			}
		}
		authService.removeRollen(kundeId, zuEntfernendeRollen);
		
		// zuruecksetzen
		usernameUpdateRollen = null;
		ausgewaehlteRollenOrig = null;
		ausgewaehlteRollen = null;
		kundeId = null;

		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}
}
