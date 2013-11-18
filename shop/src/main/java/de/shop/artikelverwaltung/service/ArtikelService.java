package de.shop.artikelverwaltung.service;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.google.common.base.Strings;
import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.interceptor.Log;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Log
public class ArtikelService implements Serializable {
	private static final long serialVersionUID = 3076865030092242363L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass());

	@Inject
	private transient EntityManager em;

	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	public Artikel createArtikel(Artikel artikel) {
		if (artikel == null) {
			return artikel;
		}

		// Die Methode ist in Agabe 2 vorhanden , muss kopieren , und gemacht !
		// validateArtikel(artikel, locale, Default.class);

		// Pruefung, ob die Bezeichnung schon existiert
		try {
			LOGGER.trace("Prufung der Bezeichnung");
			em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_BEZEICHNUNG,
					Artikel.class)
					.setParameter(Artikel.PARAM_ARTIKEL_BEZEICHNUNG,
							artikel.getBezeichnung()).getSingleResult();
			throw new BezeichnungExistsException(artikel.getBezeichnung());
		} catch (NoResultException e) {
			// Noch kein Artikel mit dieser Bezeichnung
			LOGGER.trace("Bezeichnung existiert noch nicht");
		}
		LOGGER.trace("Bevor Persist");
		em.persist(artikel);
		LOGGER.trace("Nach Persist");
		return artikel;
	}

	/**
	 */
	public List<Artikel> findVerfuegbareArtikel() {
		final List<Artikel> result = em.createNamedQuery(
				Artikel.FIND_VERFUEGBARE_ARTIKEL, Artikel.class)
				.getResultList();
		return result;
	}

	/**
	 */
	public Artikel findArtikelById(Long id) {
		final Artikel artikel = em.find(Artikel.class, id);
		return artikel;
	}

	/**
	 */
	public List<Artikel> findArtikelByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}

		/**
		 * SELECT a FROM Artikel a WHERE a.id = ? OR a.id = ? OR ...
		 */
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Artikel> criteriaQuery = builder
				.createQuery(Artikel.class);
		final Root<Artikel> a = criteriaQuery.from(Artikel.class);

		final Path<Long> idPath = a.get("id");
		// final Path<String> idPath = a.get(Artikel_.id); // Metamodel-Klassen
		// funktionieren nicht mit Eclipse

		Predicate pred = null;
		if (ids.size() == 1) {
			// Genau 1 id: kein OR notwendig
			pred = builder.equal(idPath, ids.get(0));
		} else {
			// Mind. 2x id, durch OR verknuepft
			final Predicate[] equals = new Predicate[ids.size()];
			int i = 0;
			for (Long id : ids) {
				equals[i++] = builder.equal(idPath, id);
			}

			pred = builder.or(equals);
		}

		criteriaQuery.where(pred);

		final TypedQuery<Artikel> query = em.createQuery(criteriaQuery);

		final List<Artikel> artikel = query.getResultList();
		return artikel;
	}

	/**
	 * Liste mit Artikeln mit gleicher Bezeichnung suchen
	 * 
	 * @param bezeichnung
	 *            Die Bezeichnung der gesuchten Artikel suchen
	 * @return Liste der gefundenen Artikel suchen
	 */
	public List<Artikel> findArtikelByBezeichnung(String bezeichnung) {
		if (Strings.isNullOrEmpty(bezeichnung)) {
			return findVerfuegbareArtikel();
		}

		return em
				.createNamedQuery(Artikel.FIND_ARTIKEL_BY_BEZ, Artikel.class)
				.setParameter(Artikel.PARAM_BEZEICHNUNG,
						"%" + bezeichnung + "%").getResultList();

	}

	/**
	 */
	public Artikel updateArtikel(Artikel artikel) {
		if (artikel == null) {
			return null;
		}

		// kunde vom EntityManager trennen, weil anschliessend z.B. nach Id
		// gesucht wird
		em.detach(artikel);

		final Artikel tmp = findArtikelById(artikel.getId());
		if (tmp != null) {
			em.detach(tmp);
			if (tmp.getId().longValue() != artikel.getId().longValue()) {
				// anderes Objekt mit gleicher Bezeichnung
				throw new BezeichnungExistsException(artikel.getBezeichnung());
			}
		}

		em.merge(artikel);
		return artikel;
	}

	/**
	 */
	public List<Artikel> findArtikelByPreis(double preis) {
		final List<Artikel> artikel = em
				.createNamedQuery(Artikel.FIND_ARTIKEL_MAX_PREIS, Artikel.class)
				.setParameter(Artikel.PARAM_PREIS, preis).getResultList();
		return artikel;
	}
}
