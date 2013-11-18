package de.shop.bestellverwaltung.service;

import static de.shop.util.Constants.KEINE_ID;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.interceptor.Log;

@Log
public class BestellungServiceImpl implements Serializable, BestellungService {
	private static final long serialVersionUID = -9145947650157430928L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());

	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private KundeService ks;

	@Inject
	@NeueBestellung
	private transient Event<Bestellung> event;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public Bestellung findBestellungById(Long id, FetchType fetch) {
		Bestellung bestellung = null;
		if (fetch == null || FetchType.NUR_BESTELLUNG.equals(fetch)) {
			bestellung = em.find(Bestellung.class, id);
		}
		else if (FetchType.MIT_LIEFERUNGEN.equals(fetch)) {
			try {
			bestellung = em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_LIEFERUNGEN, Bestellung.class)
					       .setParameter(Bestellung.PARAM_ID, id)
					       .getSingleResult();
			}
			catch (NoResultException e) {
				return null;
			}
		}
		return bestellung;
	}

	/**
	 */
	@Override
	public Kunde findKundeById(Long id) {
		try {
			final Kunde kunde = em
					.createNamedQuery(Bestellung.FIND_KUNDE_BY_ID, Kunde.class)
					.setParameter(Bestellung.PARAM_ID, id).getSingleResult();
			return kunde;
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 */
	@Override
	public List<Bestellung> findBestellungenByKunde(Kunde kunde, FetchType fetch) {
		if (kunde == null) {
			return Collections.emptyList();
		}

		List<Bestellung> bestellungen;
		switch (fetch) {
		case NUR_BESTELLUNG:
			bestellungen = em
					.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDEID,
							Bestellung.class)
					.setParameter(Bestellung.PARAM_KUNDEID, kunde.getId())
					.getResultList();
			break;
		case MIT_LIEFERUNGEN:
			bestellungen = em
					.createNamedQuery(
							Bestellung.FIND_BESTELLUNGEN_BY_KUNDEID_FETCH_LIEFERUNGEN,
							Bestellung.class)
					.setParameter(Bestellung.PARAM_KUNDEID, kunde.getId())
					.getResultList();
			break;
		default:
			bestellungen = Collections.emptyList();
		}
		return bestellungen;
	}

	/**
	 * Zuordnung einer neuen, transienten Bestellung zu einem existierenden,
	 * persistenten Kunden. Der Kunde ist fuer den EntityManager bekannt, die
	 * Bestellung dagegen nicht. Das Zusammenbauen wird sowohl fuer einen Web
	 * Service aus auch fuer eine Webanwendung benoetigt.
	 */
	@Override
	public Bestellung createBestellung(Bestellung bestellung, String username) {
		if (bestellung == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		final Kunde kunde = ks.findKundeByUserName(username);
		return createBestellung(bestellung, kunde);
	}

	/**
	 * Zuordnung einer neuen, transienten Bestellung zu einem existierenden,
	 * persistenten Kunden. Der Kunde ist fuer den EntityManager bekannt, die
	 * Bestellung dagegen nicht. Das Zusammenbauen wird sowohl fuer einen Web
	 * Service aus auch fuer eine Webanwendung benoetigt.
	 */
	@Override
	public Bestellung createBestellung(Bestellung bestellung, Kunde kunde) {
		if (bestellung == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		if (!em.contains(kunde)) {
			kunde = ks.findKundeById(kunde.getId(),
					KundeService.FetchType.MIT_BESTELLUNGEN);
		}
		kunde.addBestellung(bestellung);
		bestellung.setKunde(kunde);

		// Vor dem Abspeichern IDs zuruecksetzen:
		// IDs koennten einen Wert != null haben, wenn sie durch einen Web
		// Service uebertragen wurden
		bestellung.setId(KEINE_ID);
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bp.setId(KEINE_ID);
			LOGGER.tracef("Bestellposition: %s", bp);
		}

		// validateBestellung(bestellung, locale, Default.class);
		em.persist(bestellung);
		event.fire(bestellung);

		return bestellung;
	}

	/*
	 * private void validateBestellung(Bestellung bestellung, Locale locale,
	 * Class<?>... groups) { final Validator validator =
	 * validatorProvider.getValidator(locale);
	 * 
	 * final Set<ConstraintViolation<Bestellung>> violations =
	 * validator.validate(bestellung); if (violations != null &&
	 * !violations.isEmpty()) { LOGGER.debugf("createBestellung: violations=%s",
	 * violations); throw new InvalidBestellungException(bestellung,
	 * violations); } }
	 */

	/**
	 */
	@Override
	public List<Lieferung> findLieferungen(String nr) {
		final List<Lieferung> lieferungen = em
				.createNamedQuery(
						Lieferung.FIND_LIEFERUNGEN_BY_LIEFERNR_FETCH_BESTELLUNGEN,
						Lieferung.class)
				.setParameter(Lieferung.PARAM_LIEFERNR, nr).getResultList();
		return lieferungen;
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public Lieferung createLieferung(Lieferung lieferung) {
		if (lieferung == null) {
			return null;
		}
		
		lieferung.setId(KEINE_ID);
		em.persist(lieferung);
		return lieferung;
	}
}
