-- ===============================================================================
-- Jede SQL-Anweisung muss in genau 1 Zeile
-- Kommentare durch -- am Zeilenanfang
-- ===============================================================================

-- ===============================================================================
-- Tabellen fuer Enum-Werte *einmalig* anlegen und jeweils Werte einfuegen
-- Beim ALLERERSTEN Aufruf die Zeilen mit "DROP TABLE ..." durch -- auskommentieren
-- ===============================================================================
DROP TABLE geschlecht;
CREATE TABLE geschlecht(id VARCHAR2(1) NOT NULL PRIMARY KEY, txt VARCHAR2(10) NOT NULL UNIQUE) CACHE;
INSERT INTO geschlecht VALUES ('M', 'MAENNLICH');
INSERT INTO geschlecht VALUES ('W', 'WEIBLICH');

DROP TABLE transport_art;
CREATE TABLE transport_art(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(8) NOT NULL UNIQUE) CACHE;
INSERT INTO transport_art VALUES (0, 'STRASSE');
INSERT INTO transport_art VALUES (1, 'SCHIENE');
INSERT INTO transport_art VALUES (2, 'LUFT');
INSERT INTO transport_art VALUES (3, 'WASSER');

-- ===============================================================================
-- Fremdschluessel in den bereits *generierten* Tabellen auf die obigen "Enum-Tabellen" anlegen
-- ===============================================================================
--ALTER TABLE kunde ADD CONSTRAINT kunde__geschlecht_fk FOREIGN KEY (geschlecht_fk) REFERENCES geschlecht;
--ALTER TABLE lieferung ADD CONSTRAINT lieferung__transport_art_fk FOREIGN KEY (transport_art_fk) REFERENCES transport_art;

-- ===============================================================================
-- Indexe in den *generierten* Tabellen anlegen
-- ===============================================================================
--CREATE INDEX adresse__kunde_index ON adresse(kunde_fk);
--CREATE INDEX bestellung__kunde_index ON bestellung(kunde_fk);
--CREATE INDEX bestpos__bestellung_index ON bestellposition(bestellung_fk);
--CREATE INDEX bestpos__artikel_index ON bestellposition(artikel_fk);

INSERT INTO kunde (id, version, nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES(1,1,'Admin','Admin','01.01.2001','M',1,'0,1',0,'admin@hska.de','a4ayc/80/OGda4BO/1o/V0etpOqiLx1JwB5S3beHW0s=','01.08.2006 00:00:00','01.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (101,1,'Alpha','Adrian','31.01.2001','M',1,'0,1','1500,5','101@hska.de','Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=','01.08.2006 00:00:00','01.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (102,1,'Alpha','Alfred','28.02.2002','M',1,0,'500,5','102@hska.de','N4NPLyV2LyPh90pTHL5EXbc9Z2Xr5gh4p9++zX1K9uE=','02.08.2006 00:00:00','02.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (103,1,'Alpha','Anton','15.09.2003','M',0,'0,1','0,5','103@hska.de','RU9jrDDIMimX7wJe3/ar0j4NvnuKPVEmqJTkoWjBtZs=','03.08.2006 00:00:00','03.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (104,1,'Delta','Dirk','30.04.2004','M',1,'0,15','1500,5','104@hska.de','Xvb98yUTqnzRH3K+zPEyuSJNM/JxRx//QCdCiHoXHt8=','04.08.2006 00:00:00','04.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (105,1,'Epsilon','Emil','31.03.2005','M',0,'0,0','1500,5','105@hska.de','ElPpNz54G3UAJmyqVRUOCOIQvIzYzHDYmYXjYAFV6GA=','05.08.2006 00:00:00','05.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (106,1,'Jan','Jan','02.03.2005','M',0,'0,0','1500,5','106@hska.de','SC2Wc8/uXeOR+X/eTRyE+fjW8s8HhPz/uVi0Ay3nI2w=','05.08.2006 00:00:00','05.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (107,1,'Jan','Nick','02.04.2005','M',0,'0,0','1500,5','107@hska.de','M0byu/bDS9Lb4ovRu2V9Dpw3OSodXsmSnmpd9HY93C0=','05.08.2006 00:00:00','05.08.2006 00:00:00');
INSERT INTO kunde (id, version,	nachname, vorname, seit,  geschlecht_fk, newsletter, rabatt, umsatz, email, password, erzeugt, aktualisiert) VALUES (108,1,'Mueller','Marina','02.04.2005','W',0,'0,0','1500,5','108@hska.de','lTfzLsdZnhrpU69sn5Kf50f/na33mpvv8fMExVAXMBE=','05.08.2006 00:00:00','05.08.2006 00:00:00');

INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (1,'admin');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (1,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (1,'abteilungsleiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (1,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (101,'admin');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (101,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (101,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (102,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (102,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (103,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (103,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (104,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (105,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (106,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (107,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (108,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (108,'mitarbeiter');

INSERT INTO adresse (id, version, plz, ort, strasse, hausnr, kunde_fk, erzeugt, aktualisiert) VALUES (200,1,'76133','Karlsruhe','Moltkestrasse','30',1,'01.08.2006 00:00:00','01.08.2006 00:00:00');
INSERT INTO adresse (id, version, plz, ort, strasse, hausnr, kunde_fk, erzeugt, aktualisiert) VALUES (201,1,'76133','Karlsruhe','Moltkestrasse','31',101,'02.08.2006 00:00:00','02.08.2006 00:00:00');
INSERT INTO adresse (id, version, plz, ort, strasse, hausnr, kunde_fk, erzeugt, aktualisiert) VALUES (202,1,'76133','Karlsruhe','Moltkestrasse','32',102,'03.08.2006 00:00:00','03.08.2006 00:00:00');
INSERT INTO adresse (id, version, plz, ort, strasse, hausnr, kunde_fk, erzeugt, aktualisiert) VALUES (203,1,'76133','Karlsruhe','Moltkestrasse','33',103,'04.08.2006 00:00:00','04.08.2006 00:00:00');
INSERT INTO adresse (id, version, plz, ort, strasse, hausnr, kunde_fk, erzeugt, aktualisiert) VALUES (204,1,'76133','Karlsruhe','Moltkestrasse','34',104,'05.08.2006 00:00:00','05.08.2006 00:00:00');
INSERT INTO adresse (id, version, plz, ort, strasse, hausnr, kunde_fk, erzeugt, aktualisiert) VALUES (205,1,'76133','Karlsruhe','Moltkestrasse','35',105,'06.08.2006 00:00:00','06.08.2006 00:00:00');


INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (300,1,'Tisch ''Oval''',80,0,'01.08.2006 00:00:00','01.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (301,1,'Stuhl ''Sitz bequem''',10,0,'02.08.2006 00:00:00','02.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (302,1,'Tür ''Hoch und breit''',300,0,'03.08.2006 00:00:00','03.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (303,1,'Fenster ''Glasklar''',150,0,'04.08.2006 00:00:00','04.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (304,1,'Spiegel ''Mach mich schöner''',60,0,'05.08.2006 00:00:00','05.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (305,1,'Kleiderschrank ''Viel Platz''',500,0,'06.08.2006 00:00:00','06.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (306,1,'Bett ''Mit Holzwurm''',600,0,'07.08.2006 00:00:00','07.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (307,1,'Couch ''Ohne Holzwurm''',750,0,'07.08.2006 00:00:00','07.08.2006 00:00:00');
INSERT INTO artikel (id, version, bezeichnung, preis, ausgesondert, erzeugt, aktualisiert) VALUES (308,1,'Sofa ''Mit Sitzmulde''',450,0,'07.08.2006 00:00:00','07.08.2006 00:00:00');


INSERT INTO bestellung (id, version, kunde_fk, idx, erzeugt, aktualisiert) VALUES (400,1,101,0,'01.08.2006 00:00:00','01.08.2006 00:00:00');
INSERT INTO bestellung (id, version, kunde_fk, idx, erzeugt, aktualisiert) VALUES (401,1,101,1,'02.08.2006 00:00:00','02.08.2006 00:00:00');
INSERT INTO bestellung (id, version, kunde_fk, idx, erzeugt, aktualisiert) VALUES (402,1,102,0,'03.08.2006 00:00:00','03.08.2006 00:00:00');
INSERT INTO bestellung (id, version, kunde_fk, idx, erzeugt, aktualisiert) VALUES (403,1,102,1,'04.08.2006 00:00:00','04.08.2006 00:00:00');
INSERT INTO bestellung (id, version, kunde_fk, idx, erzeugt, aktualisiert) VALUES (404,1,104,0,'05.08.2006 00:00:00','05.08.2006 00:00:00');


INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (500,1,400,300,1,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (501,1,400,301,4,1);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (502,1,401,302,5,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (503,1,402,303,3,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (504,1,402,304,2,1);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (505,1,403,305,1,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (506,1,404,300,5,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (507,1,404,301,2,1);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, anzahl, idx) VALUES (508,1,404,302,8,2);


INSERT INTO lieferung (id, liefernr, transport_art_fk, erzeugt, aktualisiert) VALUES (600,'20051005-001',0,'01.08.2006 00:00:00','01.08.2006 00:00:00');
INSERT INTO lieferung (id, liefernr, transport_art_fk, erzeugt, aktualisiert) VALUES (601,'20051005-002',1,'02.08.2006 00:00:00','02.08.2006 00:00:00');
INSERT INTO lieferung (id, liefernr, transport_art_fk, erzeugt, aktualisiert) VALUES (602,'20051005-003',2,'03.08.2006 00:00:00','03.08.2006 00:00:00');
INSERT INTO lieferung (id, liefernr, transport_art_fk, erzeugt, aktualisiert) VALUES (603,'20051008-001',3,'04.08.2006 00:00:00','04.08.2006 00:00:00');


INSERT INTO bestellung_lieferung (bestellung_fk, lieferung_fk) VALUES (400,600);
INSERT INTO bestellung_lieferung (bestellung_fk, lieferung_fk) VALUES (401,600);
INSERT INTO bestellung_lieferung (bestellung_fk, lieferung_fk) VALUES (402,601);
INSERT INTO bestellung_lieferung (bestellung_fk, lieferung_fk) VALUES (402,602);
INSERT INTO bestellung_lieferung (bestellung_fk, lieferung_fk) VALUES (403,602);
INSERT INTO bestellung_lieferung (bestellung_fk, lieferung_fk) VALUES (404,603);


