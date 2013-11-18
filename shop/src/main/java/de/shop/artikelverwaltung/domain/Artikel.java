package de.shop.artikelverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.KEINE_ID;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.logging.Logger;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;


@Entity
@Table(indexes = @Index(columnList = "bezeichnung"))
@NamedQueries({
	@NamedQuery(name  = Artikel.FIND_ARTIKEL,
        	query = "SELECT      a"
        	        + " FROM     Artikel a"),
	@NamedQuery(name  = Artikel.FIND_VERFUEGBARE_ARTIKEL,
            	query = "SELECT      a"
            	        + " FROM     Artikel a"
						+ " WHERE    a.ausgesondert = FALSE"
                        + " ORDER BY a.id ASC"),
	@NamedQuery(name  = Artikel.FIND_ARTIKEL_BY_BEZ,
            	query = "SELECT      a.bezeichnung"
                        + " FROM     Artikel a"
						+ " WHERE    a.bezeichnung LIKE :" + Artikel.PARAM_BEZEICHNUNG
						+ "          AND a.ausgesondert = FALSE"
			 	        //+ " ORDER BY a.id ASC"
						),
   	@NamedQuery(name  = Artikel.FIND_ARTIKEL_MAX_PREIS,
            	query = "SELECT      a"
                        + " FROM     Artikel a"
						+ " WHERE    a.preis < :" + Artikel.PARAM_PREIS
			 	        + " ORDER BY a.id ASC"),
  	@NamedQuery(name  = Artikel.FIND_ARTIKEL_BY_ID,
  				query = "SELECT      a"
  						+ " FROM     Artikel a"
  						+ " WHERE    a.id = :" + Artikel.PARAM_ID
  						+ " ORDER BY a.id ASC"
  						),
  	@NamedQuery(name  = Artikel.FIND_ARTIKEL_BY_BEZEICHNUNG,
  		  		query = "SELECT      a"
  		  				+ " FROM     Artikel a"
  		  				+ " WHERE    a.bezeichnung LIKE :" + Artikel.PARAM_ARTIKEL_BEZEICHNUNG
  		  				)					
//	@NamedQuery(name  = Artikel.HINZUFUEGEN,
//		  		query = "INSERT INTO Artikel(id,ausgesondert,bezeichnung,preis,aktualisiert,erzeugt)"
// 		  				+ "values(1,0,"Tisch",52,'20/05/2004 00:00:00,0','20/05/2004 00:00:00,0'),
//  		TEST  				
  						
})
@Cacheable
@XmlRootElement
public class Artikel implements Serializable {
	private static final long serialVersionUID = -3700579190995722151L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final int BEZEICHNUNG_LENGTH_MAX = 32;
	
	private static final String PREFIX = "Artikel.";
	public static final String FIND_ARTIKEL = PREFIX + "findArtikel";
	public static final String FIND_VERFUEGBARE_ARTIKEL = PREFIX + "findVerfuegbareArtikel";
	public static final String FIND_ARTIKEL_BY_BEZ = PREFIX + "findArtikelByBez";
	public static final String FIND_ARTIKEL_MAX_PREIS = PREFIX + "findArtikelByMaxPreis";
	public static final String FIND_ARTIKEL_BY_ID = PREFIX + "findArtikelById";
	public static final String FIND_ARTIKEL_BY_BEZEICHNUNG = PREFIX + "findArtikelByBezeichnung";

	public static final String PARAM_BEZEICHNUNG = "bezeichnung";
	public static final String PARAM_ARTIKEL_BEZEICHNUNG = "bezeichnung";
	public static final String PARAM_PREIS = "preis";
	public static final String PARAM_ID = "id";

	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	//@Min(value = MIN_ID, message = "{artikelverwaltung.artikel.id.min}", groups = IdGroup.class)
	private Long id = KEINE_ID;
	
	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column(length = BEZEICHNUNG_LENGTH_MAX, nullable = false)
	@NotNull(message = "{artikelverwaltung.artikel.bezeichnung.notNull}")
	@Size(max = BEZEICHNUNG_LENGTH_MAX, message = "{artikelverwaltung.artikel.bezeichnung.length}")
	private String bezeichnung = "";
	
	
	
	private double preis;
	

	private boolean ausgesondert = false;
	
	@Basic(optional = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date erzeugt;

	@Basic(optional = false)
	@Temporal(TIMESTAMP)
	@XmlTransient
	private Date aktualisiert;
	
	public Artikel() {
		super();
	}
	
	public Artikel(String bezeichnung, double preis) {
		super();
		this.bezeichnung = bezeichnung;
		this.preis = preis;
	}

	@PrePersist
	private void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}
	
	@PostPersist
	private void postPersist() {
		LOGGER.debugf("Neuer Artikel mit ID=%d", id);
	}
	
	@PostUpdate
	private void postUpdate() {
		LOGGER.debugf("Artikel mit ID=%s aktualisiert: version=%d", id, version);
	}
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}

	public void setValues(Artikel a) {
		bezeichnung = a.bezeichnung;
		preis = a.preis;
		ausgesondert = a.ausgesondert;
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
	
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public void setPreis(double preis) {
		this.preis = preis;
	}

	public double getPreis() {
		return preis;
	}

	public boolean isAusgesondert() {
		return ausgesondert;
	}

	public void setAusgesondert(boolean ausgesondert) {
		this.ausgesondert = ausgesondert;
	}

	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public Date getAktualisiert() {
		return aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (ausgesondert ? 1231 : 1237);
		result = prime * result
				+ ((bezeichnung == null) ? 0 : bezeichnung.hashCode());
		long temp;
		temp = Double.doubleToLongBits(preis);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	

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
		final Artikel other = (Artikel) obj;
		
		if (ausgesondert != other.ausgesondert) {
			return false;
		}
		
		if (bezeichnung == null) {
			if (other.bezeichnung != null) {
				return false;
			}
		}
		else if (!bezeichnung.equals(other.bezeichnung)) {
			return false;
		}
		
		if (Double.doubleToLongBits(preis) != Double.doubleToLongBits(other.preis)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Artikel [id=" + id + ", bezeichnung=" + bezeichnung
		       + ", preis=" + preis + ", ausgesondert=" + ausgesondert
		       + ", erzeugt=" + erzeugt
			   + ", aktualisiert=" + aktualisiert + "]";
	}

}
