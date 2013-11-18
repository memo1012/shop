package de.shop.kundenverwaltung.service;

import static de.shop.util.Constants.MAX_AUTOCOMPLETE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import de.shop.auth.domain.RolleType;
import de.shop.auth.service.AuthService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellposition_;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Bestellung_;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.Kunde_;
import de.shop.util.NoMimeTypeException;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.ConcurrentDeletedException;
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import de.shop.util.persistence.MimeType;

/**
 * Anwendungslogik fuer die KundeService
 */
@Log
public class KundeService implements Serializable {
	private static final long serialVersionUID = -5520738420154763865L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());

	public enum FetchType {
		NUR_KUNDE, MIT_BESTELLUNGEN, NUR_ARTIKEL
	}

	public enum OrderByType {
		KEINE, ID
	}

	// genau 1 Eintrag mit 100 % Fuellgrad
	private static final Map<String, Object> GRAPH_BESTELLUNGEN = new HashMap<>(
			1, 1);

	static {
		GRAPH_BESTELLUNGEN.put("javax.persistence.loadgraph",
				Kunde.GRAPH_BESTELLUNGEN);
	}

	@Inject
	private transient EntityManager em;

	@Inject
	private AuthService authService;

	@Inject
	private FileHelper fileHelper;

	@Inject
	private transient ManagedExecutorService managedExecutorService;

	@Inject
	@NeuerKunde
	private transient Event<Kunde> event;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	/**
	 */
	public List<Kunde> findAllKunden(FetchType fetch, OrderByType order) {
		final TypedQuery<Kunde> query = OrderByType.ID.equals(order) ? em
				.createNamedQuery(Kunde.FIND_KUNDEN_ORDER_BY_ID, Kunde.class)
				: em.createNamedQuery(Kunde.FIND_KUNDEN, Kunde.class);
		switch (fetch) {
		case NUR_KUNDE:
			break;
		case MIT_BESTELLUNGEN:
			query.setHint("javax.persistence.loadgraph",
					Kunde.GRAPH_BESTELLUNGEN);
			break;
		default:
			break;
		}

		final List<Kunde> kunden = query.getResultList();
		return kunden;
	}

	/**
	 */
	public Kunde findKundeById(Long id, FetchType fetch) {
		if (id == null) {
			return null;
		}
		Kunde kunde = null;
		switch (fetch) {
		case NUR_KUNDE:
			kunde = em.find(Kunde.class, id);
			break;

		case MIT_BESTELLUNGEN:
			kunde = em
					.createNamedQuery(
							Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
							Kunde.class).setParameter(Kunde.PARAM_KUNDE_ID, id)
					.getSingleResult();
			break;

		default:
			kunde = em.find(Kunde.class, id);
			break;
		}

		return kunde;
	}

	/**
	 */
	public List<Kunde> findKundenByNachname(String nachname, FetchType fetch) {

		List<Kunde> kunden;
		switch (fetch) {
		case NUR_KUNDE:
			kunden = em
					.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME,
							Kunde.class)
					.setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
					.getResultList();
			break;

		case MIT_BESTELLUNGEN:
			kunden = em
					.createNamedQuery(
							Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
							Kunde.class)
					.setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
					.getResultList();
			break;

		default:
			kunden = em
					.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME,
							Kunde.class)
					.setParameter(Kunde.PARAM_KUNDE_NACHNAME, nachname)
					.getResultList();
			break;
		}

		// FIXME https://hibernate.atlassian.net/browse/HHH-8285 :
		// @NamedEntityGraph ab Java EE 7 bzw. JPA 2.1
		// final TypedQuery<AbstractKunde> query =
		// em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
		// AbstractKunde.class)
		// .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname);
		// switch (fetch) {
		// case NUR_KUNDE:
		// break;
		// case MIT_BESTELLUNGEN:
		// query.setHint("javax.persistence.loadgraph",
		// AbstractKunde.GRAPH_BESTELLUNGEN);
		// break;
		// case MIT_WARTUNGSVERTRAEGEN:
		// query.setHint("javax.persistence.loadgraph",
		// AbstractKunde.GRAPH_WARTUNGSVERTRAEGE);
		// break;
		// default:
		// break;
		// }
		//
		// final List<AbstractKunde> kunden = query.getResultList();
		return kunden;
	}

	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
		return em
				.createNamedQuery(Kunde.FIND_NACHNAMEN_BY_PREFIX, String.class)
				.setParameter(Kunde.PARAM_KUNDE_NACHNAME_PREFIX,
						nachnamePrefix + '%').setMaxResults(MAX_AUTOCOMPLETE)
				.getResultList();
	}

	public List<Kunde> findKundenByIdPrefix(Long id) {
		if (id == null) {
			return Collections.emptyList();
		}

		return em.createNamedQuery(Kunde.FIND_KUNDEN_BY_ID_PREFIX, Kunde.class)
				.setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, id.toString() + '%')
				.setMaxResults(MAX_AUTOCOMPLETE).getResultList();
	}

	public List<Long> findIdsByPrefix(String idPrefix) {
		if (Strings.isNullOrEmpty(idPrefix)) {
			return Collections.emptyList();
		}
		final List<Long> ids = em
				.createNamedQuery(Kunde.FIND_IDS_BY_PREFIX, Long.class)
				.setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
				.getResultList();
		return ids;
	}

	/**
	 */
	public Kunde findKundeByEmail(String email) {
		try {
			final Kunde kunde = em
					.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL, Kunde.class)
					.setParameter(Kunde.PARAM_KUNDE_EMAIL, email)
					.getSingleResult();
			return kunde;
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Den Kunden zu einem Benutzernamen suchen.
	 * 
	 * @param userName
	 *            Der Benutzername, zu dem der passende Kunde gesucht wird.
	 * @return Der gefundene Kunde oder null, falls es zum angegebenen
	 *         Benutzernamen keinen Kunden gibt.
	 */
	public Kunde findKundeByUserName(String userName) {
		try {
			return em
					.createNamedQuery(Kunde.FIND_KUNDE_BY_USERNAME, Kunde.class)
					.setParameter(Kunde.PARAM_KUNDE_USERNAME, userName)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Einem Kunden eine hochgeladene Datei ohne MIME Type (bei RESTful WS)
	 * zuordnen
	 * 
	 * @param kundeId
	 *            Die ID des Kunden
	 * @param bytes
	 *            Das Byte-Array der hochgeladenen Datei
	 */
	public Kunde setFile(Long kundeId, byte[] bytes) {
		final Kunde kunde = findKundeById(kundeId, FetchType.NUR_KUNDE);
		if (kunde == null) {
			return null;
		}
		final MimeType mimeType = fileHelper.getMimeType(bytes);
		setFile(kunde, bytes, mimeType);
		return kunde;
	}

	/**
	 * Einem Kunden eine hochgeladene Datei zuordnen
	 * 
	 * @param kunde
	 *            Der betroffene Kunde
	 * @param bytes
	 *            Das Byte-Array der hochgeladenen Datei
	 * @param mimeTypeStr
	 *            Der MIME-Type als String
	 */
	public Kunde setFile(Kunde kunde, byte[] bytes, String mimeTypeStr) {
		final MimeType mimeType = MimeType.build(mimeTypeStr);
		setFile(kunde, bytes, mimeType);
		return kunde;
	}

	private void setFile(Kunde kunde, byte[] bytes, MimeType mimeType) {
		if (mimeType == null) {
			throw new NoMimeTypeException();
		}

		final String filename = fileHelper.getFilename(kunde.getClass(),
				kunde.getId(), mimeType);

		// Gibt es noch kein (Multimedia-) File
		File file = kunde.getFile();
		if (kunde.getFile() == null) {
			file = new File(bytes, filename, mimeType);
			LOGGER.tracef("Neue Datei %s", file);
			kunde.setFile(file);
			em.persist(file);
		} else {
			file.set(bytes, filename, mimeType);
			LOGGER.tracef("Ueberschreiben der Datei %s", file);
			em.merge(file);
		}

		// Hochgeladenes Bild/Video/Audio in einem parallelen Thread als Datei
		// fuer die Web-Anwendung abspeichern
		final File newFile = kunde.getFile();
		final Runnable storeFile = new Runnable() {
			@Override
			public void run() {
				fileHelper.store(newFile);
			}
		};
		managedExecutorService.execute(storeFile);
	}

	private static boolean hasBestellungen(Kunde kunde) {
		LOGGER.debugf("hasBestellungen BEGINN: %s", kunde);

		boolean result = false;

		// Gibt es den Kunden und hat er mehr als eine Bestellung?
		// Bestellungen nachladen wegen Hibernate-Caching
		if (kunde != null && kunde.getBestellungen() != null
				&& !kunde.getBestellungen().isEmpty()) {
			result = true;
		}

		LOGGER.debugf("hasBestellungen ENDE: %s", result);
		return result;
	}

	/**
	 */
	public Kunde createKunde(Kunde kunde) {
		if (kunde == null) {
			return kunde;
		}

		// Pruefung, ob die Email-Adresse schon existiert
		// em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_EMAIL,
		// AbstractKunde.class)
		// .setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, kunde.getEmail())
		// .getSingleResult();
		// throw new EmailExistsException(kunde.getEmail());
		final Kunde tmp = findKundeByEmail(kunde.getEmail());
		if (tmp != null) {
			throw new EmailExistsException(kunde.getEmail());
		}

		// Password verschluesseln
		passwordVerschluesseln(kunde);

		// Rolle setzen
		kunde.addRollen(Sets.newHashSet(RolleType.KUNDE));

		em.persist(kunde);
		event.fire(kunde);

		return kunde;
	}

	private void passwordVerschluesseln(Kunde kunde) {
		LOGGER.debugf("passwordVerschluesseln BEGINN: %s", kunde);

		final String unverschluesselt = kunde.getPassword();
		final String verschluesselt = authService
				.verschluesseln(unverschluesselt);
		kunde.setPassword(verschluesselt);
		kunde.setPasswordWdh(verschluesselt);

		LOGGER.debugf("passwordVerschluesseln ENDE: %s", verschluesselt);
	}

	/**
	 */
	public Kunde updateKunde(Kunde kunde, boolean geaendertPassword) {
		if (kunde == null) {
			return null;
		}

		// kunde vom EntityManager trennen, weil anschliessend z.B. nach Id und
		// Email gesucht wird
		em.detach(kunde);

		// Wurde das Objekt konkurrierend geloescht?
		Kunde tmp = findKundeById(kunde.getId(), FetchType.NUR_KUNDE);
		if (tmp == null) {
			throw new ConcurrentDeletedException(kunde.getId());
		}
		em.detach(tmp);

		// Gibt es ein anderes Objekt mit gleicher Email-Adresse?
		tmp = findKundeByEmail(kunde.getEmail());
		if (tmp != null) {
			em.detach(tmp);
			if (tmp.getId().longValue() != kunde.getId().longValue()) {
				// anderes Objekt mit gleichem Attributwert fuer email
				throw new EmailExistsException(kunde.getEmail());
			}
		}

		// Password verschluesseln
		if (geaendertPassword) {
			passwordVerschluesseln(kunde);
		}

		kunde = em.merge(kunde); // OptimisticLockException
		kunde.setPasswordWdh(kunde.getPassword());

		return kunde;
	}

	/**
	 */
	public void deleteKunde(Kunde kunde) {
		if (kunde == null) {
			return;
		}

		// Bestellungen laden, damit sie anschl. ueberprueft werden koennen
		kunde = findKundeById(kunde.getId(), FetchType.MIT_BESTELLUNGEN);

		if (kunde == null) {
			return;
		}

		// Gibt es Bestellungen?
		if (!kunde.getBestellungen().isEmpty()) {
			throw new KundeDeleteBestellungException(kunde);
		}

		em.remove(kunde);
	}

	/**
	 */
	public List<Kunde> findKundenByPLZ(String plz) {
		final List<Kunde> kunden = em
				.createNamedQuery(Kunde.FIND_KUNDEN_BY_PLZ, Kunde.class)
				.setParameter(Kunde.PARAM_KUNDE_ADRESSE_PLZ, plz)
				.getResultList();
		return kunden;
	}

	public List<Kunde> findKundenByGeschlecht(GeschlechtType geschlecht) {
		return em.createNamedQuery(Kunde.FIND_BY_GESCHLECHT, Kunde.class)
				.setParameter(Kunde.PARAM_GESCHLECHT, geschlecht)
				.getResultList();
	}

	/**
	 */
	public List<Kunde> findKundenBySeit(Date seit) {
		final List<Kunde> kunden = em
				.createNamedQuery(Kunde.FIND_KUNDEN_BY_DATE, Kunde.class)
				.setParameter(Kunde.PARAM_KUNDE_SEIT, seit).getResultList();
		return kunden;
	}

	/**
	 */
	public List<Kunde> findKunden() {
		final List<Kunde> kunden = em.createNamedQuery(Kunde.FIND_KUNDEN,
				Kunde.class).getResultList();
		return kunden;
	}

	/**
	 */
	public List<Kunde> findKundenByNachnameCriteria(String nachname) {
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Kunde> criteriaQuery = builder
				.createQuery(Kunde.class);
		final Root<Kunde> k = criteriaQuery.from(Kunde.class);

		final Path<String> nachnamePath = k.get(Kunde_.nachname);

		// final Path<String> nachnamePath = k.get("nachname");

		final Predicate pred = builder.equal(nachnamePath, nachname);
		criteriaQuery.where(pred);

		// Ausgabe des komponierten Query-Strings. Voraussetzung: das Modul
		// "org.hibernate" ist aktiviert
		// if (LOGGER.isLoggable(FINEST)) {
		// query.unwrap(org.hibernate.Query.class).getQueryString();
		// }

		final List<Kunde> kunden = em.createQuery(criteriaQuery)
				.getResultList();
		return kunden;
	}

	/**
	 */
	public List<Kunde> findKundenMitMinBestMenge(short minMenge) {
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Kunde> criteriaQuery = builder
				.createQuery(Kunde.class);
		final Root<Kunde> k = criteriaQuery.from(Kunde.class);

		final Join<Kunde, Bestellung> b = k.join(Kunde_.bestellungen);
		final Join<Bestellung, Bestellposition> bp = b
				.join(Bestellung_.bestellpositionen);
		criteriaQuery.where(
				builder.gt(bp.<Short> get(Bestellposition_.anzahl), minMenge))
				.distinct(true);

		final List<Kunde> kunden = em.createQuery(criteriaQuery)
				.getResultList();
		return kunden;
	}

}
