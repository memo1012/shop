package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.KEINE_ID;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIMESTAMP;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;



import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.ScriptAssert;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.persistence.File;
import de.shop.auth.domain.RolleType;

// Alternativen bei @Inheritance
//   strategy=SINGLE_TABLE (=default), TABLE_PER_CLASS, JOINED
// Alternativen bei @DiscriminatorColumn
//   discriminatorType=STRING (=default), CHAR, INTEGER
@Entity
// Zu email wird unten ein UNIQUE Index definiert
@Table(name = "kunde", indexes = { @Index(columnList = "nachname"),
		@Index(columnList = "file_fk") })
@NamedQueries({
		@NamedQuery(name = Kunde.FIND_KUNDEN, query = "SELECT k"
				+ " FROM   Kunde k"),
		/*
		 * @NamedQuery(name = Kunde.FIND_KUNDEN_FETCH_BESTELLUNGEN, query =
		 * "SELECT  DISTINCT k" +
		 * " FROM Kunde k LEFT JOIN FETCH k.bestellungen"),
		 */
		@NamedQuery(name = Kunde.FIND_KUNDEN_ORDER_BY_ID, query = "SELECT   k"
				+ " FROM  Kunde k" + " ORDER BY k.id"),
		@NamedQuery(name = Kunde.FIND_IDS_BY_PREFIX, query = "SELECT   k.id"
				+ " FROM  Kunde k" + " WHERE CONCAT('', k.id) LIKE :"
				+ Kunde.PARAM_KUNDE_ID_PREFIX + " ORDER BY k.id"),
		@NamedQuery(name = Kunde.FIND_KUNDEN_BY_NACHNAME, query = "SELECT k"
				+ " FROM   Kunde k" + " WHERE  UPPER(k.nachname) = UPPER(:"
				+ Kunde.PARAM_KUNDE_NACHNAME + ")"),
		@NamedQuery(name = Kunde.FIND_NACHNAMEN_BY_PREFIX, query = "SELECT   DISTINCT k.nachname"
				+ " FROM  Kunde k "
				+ " WHERE UPPER(k.nachname) LIKE UPPER(:"
				+ Kunde.PARAM_KUNDE_NACHNAME_PREFIX + ")"),
		@NamedQuery(name = Kunde.FIND_NACHNAMEN_BY_PREFIX, query = "SELECT   DISTINCT k.nachname"
				+ " FROM  Kunde k "
				+ " WHERE UPPER(k.nachname) LIKE UPPER(:"
				+ Kunde.PARAM_KUNDE_NACHNAME_PREFIX + ")"),
		@NamedQuery(name = Kunde.FIND_ALL_NACHNAMEN, query = "SELECT      DISTINCT k.nachname"
				+ " FROM     Kunde k" + " ORDER BY k.nachname"),
		@NamedQuery(name = Kunde.FIND_KUNDEN_OHNE_BESTELLUNGEN, query = "SELECT k"
				+ " FROM   Kunde k" + " WHERE  k.bestellungen IS EMPTY"),
		@NamedQuery(name = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN, query = "SELECT DISTINCT k"
				+ " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
				+ " WHERE  UPPER(k.nachname) = UPPER(:"
				+ Kunde.PARAM_KUNDE_NACHNAME + ")"),
		@NamedQuery(name = Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN, query = "SELECT DISTINCT k"
				+ " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
				+ " WHERE  k.id = :" + Kunde.PARAM_KUNDE_ID),
		@NamedQuery(name = Kunde.FIND_KUNDE_BY_EMAIL, query = "SELECT DISTINCT k"
				+ " FROM   Kunde k"
				+ " WHERE  k.email = :"
				+ Kunde.PARAM_KUNDE_EMAIL),
		@NamedQuery(name = Kunde.FIND_KUNDEN_BY_PLZ, query = "SELECT k"
				+ " FROM  Kunde k" + " WHERE k.adresse.plz = :"
				+ Kunde.PARAM_KUNDE_ADRESSE_PLZ),
		@NamedQuery(name = Kunde.FIND_KUNDE_BY_USERNAME, query = "SELECT   k"
				+ " FROM  Kunde k" + " WHERE CONCAT('', k.id) = :"
				+ Kunde.PARAM_KUNDE_USERNAME),
		@NamedQuery(name = Kunde.FIND_USERNAME_BY_USERNAME_PREFIX, query = "SELECT   CONCAT('', k.id)"
				+ " FROM  Kunde k"
				+ " WHERE CONCAT('', k.id) LIKE :"
				+ Kunde.PARAM_USERNAME_PREFIX),
		@NamedQuery(name = Kunde.FIND_KUNDEN_BY_DATE, query = "SELECT k"
				+ " FROM  Kunde k" + " WHERE k.seit = :"
				+ Kunde.PARAM_KUNDE_SEIT),
		@NamedQuery(name = Kunde.FIND_KUNDEN, query = "SELECT k"
				+ " FROM  Kunde k") })
@NamedEntityGraphs({
		@NamedEntityGraph(name = Kunde.GRAPH_BESTELLUNGEN, attributeNodes = @NamedAttributeNode("bestellungen")) })
@ScriptAssert(lang = "javascript", script = "_this.password != null && !_this.password.equals(\"\")"
		+ "&& _this.password.equals(_this.passwordWdh)", message = "{kundenverwaltung.kunde.password.notEqual}", groups = {
		Default.class, PasswordGroup.class })
// script = "(_this.password == null && _this.passwordWdh == null)"
// + "|| (_this.password != null && _this.password.equals(_this.passwordWdh))",
// message = "{kundenverwaltung.kunde.password.notEqual}", groups =
// PasswordGroup.class)
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@XmlRootElement
@Formatted
public class Kunde implements Serializable, Cloneable {
	private static final long serialVersionUID = 5685115602958386843L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles
			.lookup().lookupClass().getName());
	// Pattern mit UTF-8 (statt Latin-1 bzw. ISO-8859-1) Schreibweise fuer
	// Umlaute:
	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]";
	private static final String PREFIX_ADEL = "(o'|von|von der|von und zu|van)?";

	public static final String VORNAME_PATTERN =  NAME_PATTERN + "( " + NAME_PATTERN + ")?";
	public static final String NACHNAME_PATTERN = PREFIX_ADEL + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
	private static final int NACHNAME_LENGTH_MIN = 2;
	private static final int NACHNAME_LENGTH_MAX = 32;
	private static final int VORNAME_LENGTH_MAX = 32;
	private static final int EMAIL_LENGTH_MAX = 128;
	
	private static final int PASSWORD_LENGTH_MAX = 256;

	private static final String PREFIX = "Kunde.";
	public static final String FIND_KUNDEN = PREFIX + "findKunden";
	/*
	 * public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX +
	 * "findKundenFetchBestellungen";
	 */
	public static final String FIND_BY_GESCHLECHT = PREFIX + "findByGeschlecht";
	public static final String FIND_KUNDEN_ORDER_BY_ID = PREFIX
			+ "findKundenOrderById";
	public static final String FIND_IDS_BY_PREFIX = PREFIX + "findIdsByPrefix";
	public static final String FIND_KUNDEN_BY_ID_PREFIX = PREFIX
			+ "findKundenByIdPrefix";
	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX
			+ "findKundenByNachname";
	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN = PREFIX
			+ "findKundenByNachnameFetchBestellungen";
	public static final String FIND_NACHNAMEN_BY_PREFIX = PREFIX
			+ "findNachnamenByPrefix";
	/*
	 * public static final String FIND_KUNDE_BY_ID_FETCH_WARTUNGSVERTRAEGE =
	 * PREFIX + "findKundenByNachnameFetchWartungsvertraege";
	 */
	public static final String FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN = PREFIX
			+ "findKundeByIdFetchBestellungen";
	public static final String FIND_KUNDE_BY_EMAIL = PREFIX
			+ "findKundeByEmail";
	public static final String FIND_ALL_NACHNAMEN = PREFIX + "findAllNachnamen";
	public static final String FIND_KUNDEN_OHNE_BESTELLUNGEN = PREFIX
			+ "findKundenOhneBestellungen";
	public static final String FIND_KUNDEN_BY_PLZ = PREFIX + "findKundenByPlz";
	public static final String FIND_KUNDE_BY_USERNAME = PREFIX
			+ "findKundeByUsername";
	public static final String FIND_USERNAME_BY_USERNAME_PREFIX = PREFIX
			+ "findKundeByUsernamePrefix";
	public static final String FIND_KUNDEN_BY_DATE = PREFIX
			+ "findKundenByDate";


	public static final String PARAM_KUNDE_ID = "kundeId";
	public static final String PARAM_KUNDE_ID_PREFIX = "idPrefix";
	public static final String PARAM_KUNDE_NACHNAME = "nachname";
	public static final String PARAM_KUNDE_NACHNAME_PREFIX = "nachnamePrefix";
	public static final String PARAM_GESCHLECHT = "geschlecht";
	public static final String PARAM_KUNDE_ADRESSE_PLZ = "plz";
	public static final String PARAM_KUNDE_USERNAME = "username";
	public static final String PARAM_USERNAME_PREFIX = "usernamePrefix";
	public static final String PARAM_KUNDE_SEIT = "seit";
	public static final String PARAM_KUNDE_EMAIL = "email";

	public static final String GRAPH_BESTELLUNGEN = "bestellungen";


	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	// @Min(value = MIN_ID, message = "{kundenverwaltung.kunde.id.min}", groups
	// = IdGroup.class)
	private Long id = KEINE_ID;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;

	@Column(length = NACHNAME_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.nachname.length}")
	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
	private String nachname = "";

	@Column(length = VORNAME_LENGTH_MAX)
	@Size(max = VORNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.vorname.length}")
	@Pattern(regexp = VORNAME_PATTERN, message = "{kundenverwaltung.kunde.vorname.pattern}")
	private String vorname = "";

	@Column(name = "geschlecht_fk")
	private GeschlechtType geschlecht = GeschlechtType.WEIBLICH;

	@Basic(optional = false)
	@Temporal(DATE)
	@Past(message = "{kundenverwaltung.kunde.seit.past}")
	private Date seit;

	@Column(precision = 5, scale = 4)
	private BigDecimal rabatt;

	@Column(precision = 15, scale = 3)
	private BigDecimal umsatz = BigDecimal.ZERO;

	@Column(length = EMAIL_LENGTH_MAX, nullable = false, unique = true)
	@Email(message = "{kundenverwaltung.kunde.email}")
	@NotNull(message = "{kundenverwaltung.kunde.email.notNull}")
	@Size(max = EMAIL_LENGTH_MAX, message = "{kundenverwaltung.kunde.email.length}")
	private String email = "";

	@OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "file_fk")
	@XmlTransient
	private File file;

	private boolean newsletter = false;

	@Column(length = PASSWORD_LENGTH_MAX)
	private String password;

	@Transient
	private String passwordWdh;

	// siehe @ScriptAssert
	// @AssertTrue(groups = PasswordGroup.class, message =
	// "{kunde.password.notEqual}")
	// public boolean isPasswortEqual() {
	// if (password == null) {
	// return passwordWdh == null;
	// }
	// return password.equals(passwordWdh);
	// }

	@OneToOne(cascade = { PERSIST, REMOVE }, mappedBy = "kunde")
	@Valid
	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
	private Adresse adresse;

	// Default: fetch=LAZY
	@OneToMany(fetch = LAZY)
	@JoinColumn(name = "kunde_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@XmlTransient
	private List<Bestellung> bestellungen;

	@Transient
	private URI bestellungenUri;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "kunde_rolle", joinColumns = @JoinColumn(name = "kunde_fk", nullable = false), uniqueConstraints = @UniqueConstraint(columnNames = {
			"kunde_fk", "rolle" }))
	@Column(table = "kunde_rolle", name = "rolle", length = 32, nullable = false)
	private Set<RolleType> rollen;

	

	@Basic(optional = false)
	// @Column(nullable = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date erzeugt;

	@Basic(optional = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date aktualisiert;

	public Kunde() {
		super();
	}

	public Kunde(String nachname, String vorname, String email, Date seit) {
		super();
		this.nachname = nachname;
		this.vorname = vorname;
		this.email = email;
		this.seit = seit == null ? null : (Date) seit.clone();
	}

	@PrePersist
	protected void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}

	@PostPersist
	protected void postPersist() {
		LOGGER.debugf("Neuer Kunde mit ID=%d", id);
	}

	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}

	@PostUpdate
	protected void postUpdate() {
		LOGGER.debugf("Kunde mit ID=%d aktualisiert: version=%d", id, version);
	}

	@PostLoad
	protected void postLoad() {
		passwordWdh = password;
	}

	public void setValues(Kunde k) {
		version = k.version;
		nachname = k.nachname;
		vorname = k.vorname;
		umsatz = k.umsatz;
		geschlecht = k.geschlecht;
		rabatt = k.rabatt;
		seit = k.seit;
		email = k.email;
		password = k.password;
		passwordWdh = k.password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public GeschlechtType getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(GeschlechtType geschlecht) {
		this.geschlecht = geschlecht;
	}

	public Date getSeit() {
		return seit == null ? null : (Date) seit.clone();
	}

	public void setSeit(Date seit) {
		this.seit = seit == null ? null : (Date) seit.clone();
	}

	public BigDecimal getUmsatz() {
		return umsatz;
	}

	public void setUmsatz(BigDecimal umsatz) {
		this.umsatz = umsatz;
	}

	public BigDecimal getRabatt() {
		return rabatt;
	}

	public void setRabatt(BigDecimal rabatt) {
		this.rabatt = rabatt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setNewsletter(boolean newsletter) {
		this.newsletter = newsletter;
	}

	public boolean isNewsletter() {
		return newsletter;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordWdh() {
		return passwordWdh;
	}

	public void setPasswordWdh(String passwordWdh) {
		this.passwordWdh = passwordWdh;
	}

	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}

	public List<Bestellung> getBestellungen() {
		if (bestellungen == null) {
			return null;
		}
		return Collections.unmodifiableList(bestellungen);
	}

	public void setBestellungen(List<Bestellung> bestellungen) {
		if (this.bestellungen == null) {
			this.bestellungen = bestellungen;
			return;
		}

		// Wiederverwendung der vorhandenen Collection
		this.bestellungen.clear();
		if (bestellungen != null) {
			this.bestellungen.addAll(bestellungen);
		}
	}

	public Kunde addBestellung(Bestellung bestellung) {
		if (bestellungen == null) {
			bestellungen = new ArrayList<>();
		}
		bestellungen.add(bestellung);
		return this;
	}

	public URI getBestellungenUri() {
		return bestellungenUri;
	}

	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}

	public Set<RolleType> getRollen() {
		if (rollen == null) {
			return null;
		}

		return Collections.unmodifiableSet(rollen);
	}

	public void setRollen(Set<RolleType> rollen) {
		if (this.rollen == null) {
			this.rollen = rollen;
			return;
		}

		// Wiederverwendung der vorhandenen Collection
		this.rollen.clear();
		if (rollen != null) {
			this.rollen.addAll(rollen);
		}
	}

	public Date getAktualisiert() {
		return aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert
				.clone();
	}

	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public Kunde addRollen(Collection<RolleType> rollen) {
		LOGGER.tracef("neue Rollen: %s", rollen);
		if (this.rollen == null) {
			this.rollen = new HashSet<>();
		}
		this.rollen.addAll(rollen);
		LOGGER.tracef("Rollen nachher: %s", this.rollen);
		return this;
	}

	public Kunde removeRollen(Collection<RolleType> rollen) {
		LOGGER.tracef("zu entfernende Rollen: %s", rollen);
		if (this.rollen == null) {
			return this;
		}
		this.rollen.removeAll(rollen);
		LOGGER.tracef("Rollen nachher: %s", this.rollen);
		return this;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}



	@Override
	public String toString() {
		return "Kunde [id=" + id + ", version=" + version + ", nachname="
				+ nachname + ", vorname=" + vorname + ", seit=" + seit
				+ ", email=" + email + ", rabatt=" + rabatt
				+ ", bestellungenUri=" + bestellungenUri + ", rollen=" + rollen
				+ ", password=" + password + ", passwordWdh=" + passwordWdh
				+ ", erzeugt=" + erzeugt + ", aktualisiert=" + aktualisiert
				+ ", geschlecht=" + geschlecht + "]";
	}

	/**
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	/**
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Kunde other = (Kunde) obj;

		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}

		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		final Kunde neuesObjekt = Kunde.class.cast(super.clone());
		neuesObjekt.id = id;
		neuesObjekt.version = version;
		neuesObjekt.nachname = nachname;
		neuesObjekt.vorname = vorname;
		neuesObjekt.geschlecht = geschlecht;
		neuesObjekt.umsatz = umsatz;
		neuesObjekt.email = email;
		neuesObjekt.newsletter = newsletter;
		neuesObjekt.password = password;
		neuesObjekt.passwordWdh = passwordWdh;
		neuesObjekt.adresse = adresse;

		neuesObjekt.erzeugt = erzeugt;
		neuesObjekt.aktualisiert = aktualisiert;
		return neuesObjekt;
	}
}
