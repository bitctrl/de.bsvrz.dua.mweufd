/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.mweufd;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.IOnlineUfdSensorListener;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Allgemeiner Rahmen fuer eine Umfelddatenmessstelle, wie sie im Zusammenhang
 * mit der Messwertersetzung benoetigt wird.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public abstract class AbstraktMweUfdsSensor implements ClientSenderInterface,
		IOnlineUfdSensorListener<ResultData> {

	/**
	 * statische Datenverteiler-Verbindung.
	 */
	protected static IVerwaltungMitGuete dieVerwaltung = null;

	/**
	 * die Umfelddatenmessstelle, die in Bezug auf einen bestimmten Hauptsensor
	 * messwertersetzt werden soll.
	 */
	protected DUAUmfeldDatenMessStelle messStelle = null;

	/**
	 * Der MWE-Sensor selbst mit aktuellen Daten.
	 */
	protected MweUfdSensor sensorSelbst = null;

	/**
	 * Der Vorgaenger des MWE-Sensors mit aktuellen Daten.
	 */
	protected MweUfdSensor vorgaenger = null;

	/**
	 * Der Nachfolger des MWE-Sensors mit aktuellen Daten.
	 */
	protected MweUfdSensor nachfolger = null;

	/**
	 * Der Ersatz des MWE-Sensors mit aktuellen Daten.
	 */
	protected MweUfdSensor ersatz = null;

	/**
	 * letzter empfangener Datensatz des Ersatzsensors.
	 */
	protected ResultData letzterErsatzDatensatz = null;

	/**
	 * letztes fuer diesen (den messwertzuersetzenden) Umfelddatensensor
	 * emfangenes implausibles Datum.
	 */
	protected ResultData letztesEmpangenesImplausiblesDatum = null;

	/**
	 * letztes fuer diesen (den messwertzuersetzenden) Umfelddatensensor
	 * emfangenes plausibles Datum.
	 */
	protected ResultData letztesEmpangenesPlausiblesDatum = null;

	/**
	 * letztes fuer diesen Umfelddatensensor veröffentlichtes Datum.
	 */
	protected ResultData letztesPubDatum = null;

	/**
	 * Zeitpunkt, seit dem ununterbrochen Messwertersetzung stattfindet.
	 */
	protected long messWertErsetzungStart = -1;

	/**
	 * Zeitpunkt, seit dem ununterbrochen Messwerte fortgeschrieben werden.
	 */
	protected long messWertFortschreibungStart = -1;

	/**
	 * Hier untersuchter Umfelddatensensor mit aktuellen Parametern.
	 */
	protected DUAUmfeldDatenSensor sensorMitParametern = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param messStelle
	 *            die Umfelddatenmessstelle, die in Bezug auf einen bestimmten
	 *            Hauptsensor messwertersetzt werden soll (muss
	 *            <code> != null</code> sein)
	 * @param sensor
	 *            der Umfelddatensensor der messwertersetzt werden soll (muss
	 *            <code> != null</code> sein)
	 * @throws DUAInitialisierungsException
	 *             wenn die Initialisierung des Bearbeitungsknotens
	 *             fehlgeschlagen ist
	 */
	public AbstraktMweUfdsSensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		if (messStelle == null || sensor == null) {
			throw new NullPointerException("Messstelle/Sensor ist <<null>>"); //$NON-NLS-1$
		}
		dieVerwaltung = verwaltung;
		VerwaltungMesswertErsetzungUFD.dieDfs.addObjekt(sensor.getObjekt());
		this.sensorMitParametern = sensor;

		this.messStelle = messStelle;
		this.sensorSelbst = MweUfdSensor.getInstanz(verwaltung.getVerbindung(),
				sensor.getObjekt());
		this.sensorSelbst.addListener(this, true);

		if (sensor.getVorgaenger() != null) {
			DUAUmfeldDatenMessStelle vorgaengerMSt = DUAUmfeldDatenMessStelle
					.getInstanz(sensor.getVorgaenger());
			if (vorgaengerMSt != null) {
				DUAUmfeldDatenSensor vorgaengerSensor = vorgaengerMSt
						.getHauptSensor(sensor.getDatenArt());
				if (vorgaengerSensor != null) {
					this.vorgaenger = MweUfdSensor.getInstanz(verwaltung
							.getVerbindung(), vorgaengerSensor.getObjekt());
				}
			}
		}

		if (sensor.getNachfolger() != null) {
			DUAUmfeldDatenMessStelle nachfolgerMSt = DUAUmfeldDatenMessStelle
					.getInstanz(sensor.getNachfolger());
			if (nachfolgerMSt != null) {
				DUAUmfeldDatenSensor nachfolgerSensor = nachfolgerMSt
						.getHauptSensor(sensor.getDatenArt());
				if (nachfolgerSensor != null) {
					this.nachfolger = MweUfdSensor.getInstanz(verwaltung
							.getVerbindung(), nachfolgerSensor.getObjekt());
				}
			}
		}

		if (sensor.getErsatzSensor() != null) {
			this.ersatz = MweUfdSensor.getInstanz(verwaltung.getVerbindung(),
					sensor.getErsatzSensor());
			this.ersatz.addListener(new IOnlineUfdSensorListener<ResultData>() {

				public void aktualisiereDaten(ResultData resultat) {
					AbstraktMweUfdsSensor.this.letzterErsatzDatensatz = resultat;
					AbstraktMweUfdsSensor.this.trigger();
				}

			}, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData resultat) {
		if (this.letztesEmpangenesImplausiblesDatum != null) {
			Debug.getLogger().error(
					"Nicht freigegebenes implausibles Datum:\n" + //$NON-NLS-1$
							this.letztesEmpangenesImplausiblesDatum
							+ "\nNachfolger:\n" + resultat); //$NON-NLS-1$

			UmfeldDatenSensorDatum datumImpl = new UmfeldDatenSensorDatum(
					this.letztesEmpangenesImplausiblesDatum);
			datumImpl.getWert().setNichtErmittelbarAn();
			this.publiziere(this.letztesEmpangenesImplausiblesDatum, datumImpl
					.getDatum());

			this.letztesEmpangenesImplausiblesDatum = null;

			/**
			 * TODO: raus
			 */
			throw new RuntimeException(
					"Nicht freigegebenes implausibles Datum:\n" + //$NON-NLS-1$
							this.letztesEmpangenesImplausiblesDatum
							+ "\nNachfolger:\n" + resultat); //$NON-NLS-1$
		}

		if (resultat.getData() != null) {

			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
			if (datum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA) {
				if (this.messWertErsetzungStart == -1) {
					this.messWertErsetzungStart = resultat.getDataTime();
				}

				if (resultat.getDataTime() - this.messWertErsetzungStart >= this.sensorMitParametern
						.getMaxZeitMessWertErsetzung()) {
					/**
					 * Für implausible Messwerte wird nur für einen je
					 * Umfeldmessstelle und Sensortyp parametrierbaren
					 * Zeitbereich ein Ersatzwert berechnet. Nach Ablauf dieses
					 * Zeitbereichs ist eine Berechnung nicht mehr sinnvoll, der
					 * entsprechende Sensorwert ist dann als nicht ermittelbar
					 * zu kennzeichnen.
					 */
					datum.getWert().setNichtErmittelbarAn();
					this.publiziere(resultat, datum.getDatum());
				} else {
					/**
					 * messwertersetze die Daten
					 */
					this.letztesEmpangenesImplausiblesDatum = resultat;
					this.trigger();
				}

			} else {
				this.messWertErsetzungStart = -1;
				this.messWertFortschreibungStart = -1;
				this.letztesEmpangenesPlausiblesDatum = resultat;
				this.publiziere(resultat, resultat.getData()
						.createModifiableCopy());
			}
		} else {
			this.publiziere(resultat, null);
		}
	}

	/**
	 * Wird aufgerufen, wenn ein fuer diese spezielle MWE relevantes Datum
	 * empfangen wurde.
	 */
	protected abstract void trigger();

	/**
	 * Publiziert ein Datum nach den Vorgaben der Datenflusssteuerung (Es werden
	 * hier keine zwei Datensaetze nacheinander mit der Kennzeichnung "keine
	 * Daten" versendet).
	 * 
	 * @param original
	 *            ein Originaldatum, so wie es empfangen wurde
	 * @param nutzDatum
	 *            die ggf. messwertersetzen Nutzdaten
	 */
	protected void publiziere(final ResultData original, final Data nutzDatum) {
		boolean publiziereDatensatz = false;

		if (nutzDatum == null) {
			/**
			 * "keine Daten" wird nur publiziert, wenn das Objekt vorher nicht
			 * auch schon auf keine Daten stand
			 */
			if (this.letztesPubDatum != null
					&& this.letztesPubDatum.getData() != null) {
				publiziereDatensatz = true;
			}
		} else {
			publiziereDatensatz = true;
		}

		if (publiziereDatensatz) {
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs
					.publiziere(original, nutzDatum);
			if (this.letztesPubDatum == null) {
				Debug.getLogger().error(
						"Datenflusssteuerung konnte kein Publikationsdatum ermitteln fuer:\n" //$NON-NLS-1$
								+ original);
			}
		}
	}

	/**
	 * Errechnet <b>wenn moeglich</b> den Durchschnitt der beiden uebergebenen
	 * benachbarten Sensorwerte unter Anpassung der Guete und publiziert diesen.
	 * 
	 * @param datumImpl
	 *            der implausible Sensorwert des zentralen Sensors
	 * @param datumVor
	 *            ein Sensorwert des Vorgaengers
	 * @param datumNach
	 *            ein Sensorwert des Nachfolgers
	 * @return ob der Mittelwert publiziert wurde
	 */
	protected final boolean isMittelWertErrechenbar(
			UmfeldDatenSensorDatum datumImpl, UmfeldDatenSensorDatum datumVor,
			UmfeldDatenSensorDatum datumNach) {
		boolean erfolg = false;

		if (datumVor.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& datumNach.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& ((datumVor.getWert().getWert() > 0 && datumNach.getWert()
						.getWert() > 0) || (datumVor.getWert().getWert() == 0 && datumNach
						.getWert().getWert() == 0))) {

			long durchschnitt = Math.round(((double) datumVor.getWert()
					.getWert() + (double) datumNach.getWert().getWert()) / 2.0);

			if (DUAUtensilien.isWertInWerteBereich(datumImpl.getOriginalDatum()
					.getData().getItem(
							UmfeldDatenArt.getUmfeldDatenArtVon(
									this.sensorSelbst.getObjekt()).getName())
					.getItem("Wert"), durchschnitt)) { //$NON-NLS-1$
				GWert gueteWert1 = new GWert(
						datumVor.getGueteIndex(),
						GueteVerfahren.getZustand(datumVor.getGueteVerfahren()),
						false);
				GWert gueteWert2 = new GWert(datumNach.getGueteIndex(),
						GueteVerfahren
								.getZustand(datumNach.getGueteVerfahren()),
						false);
				GWert gueteGesamt = GWert
						.getNichtErmittelbareGuete(GueteVerfahren
								.getZustand(datumImpl.getGueteVerfahren()));

				try {
					gueteGesamt = GueteVerfahren.gewichte(GueteVerfahren.summe(
							gueteWert1, gueteWert2), dieVerwaltung
							.getGueteFaktor());
				} catch (GueteException e) {
					Debug.getLogger().error(
							"Guete kann nicht angepasst werden\n" + //$NON-NLS-1$
									"Wert1: " + datumVor + //$NON-NLS-1$
									"\nWert2: " + datumNach); //$NON-NLS-1$
					e.printStackTrace();
				}

				datumImpl.setGueteIndex(gueteGesamt
						.getIndexUnskaliertGewichtet());
				datumImpl
						.setStatusMessWertErsetzungInterpoliert(DUAKonstanten.JA);
				datumImpl.getWert().setWert(durchschnitt);

				this.publiziere(this.letztesEmpangenesImplausiblesDatum,
						datumImpl.getDatum());

				this.letztesEmpangenesImplausiblesDatum = null;
				erfolg = true;
			}
		}

		return erfolg;
	}

	/**
	 * Erfragt eine Kopie der Nutzdaten des uebergebenen Result-Datensatzes mit
	 * angepasster Guete und Flag <code>interpoliert</code>.
	 * 
	 * @param resultat
	 *            ein Result-Datensatz mit Nutzdaten, der kopiert werden soll
	 * @return eine Kopie der Nutzdaten des uebergebenen Result-Datensatzes mit
	 *         angepasster Guete und Flag <code>interpoliert</code>
	 */
	protected final Data getNutzdatenKopieVon(ResultData resultat) {
		UmfeldDatenSensorDatum kopie = new UmfeldDatenSensorDatum(resultat);

		kopie.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
		kopie.setStatusMessWertErsetzungInterpoliert(DUAKonstanten.JA);
		GWert guete = new GWert(kopie.getGueteIndex(), GueteVerfahren
				.getZustand(kopie.getGueteVerfahren()), false);
		GWert neueGuete = GWert.getNichtErmittelbareGuete(GueteVerfahren
				.getZustand(kopie.getGueteVerfahren()));
		try {
			neueGuete = GueteVerfahren.gewichte(guete, dieVerwaltung
					.getGueteFaktor());
		} catch (GueteException e) {
			Debug.getLogger().error(
					"Guete von kopiertem Wert kann nicht angepasst werden: " + //$NON-NLS-1$
							kopie);
			e.printStackTrace();
		}
		kopie.setGueteIndex(neueGuete.getIndexUnskaliertGewichtet());

		return kopie.getDatum();
	}

	/**
	 * Implementiert die Ersetzungsmethode:<br>
	 * Es werden die plausiblen Messwerte des Ersatzquerschnittes übernommen<br>.
	 * 
	 * @param datumImpl
	 *            das implausible Datum, das ersetzt werden soll
	 * @return das Ergebnis des Ersetzungsversuchs<br> - <code><b>JA</b></code>:
	 *         Es existiert ein Ersatzquerschnitt mit aktuellen, plausiblen
	 *         Daten. Der implausible Messwert wurde ersetzt und publiziert<br> -
	 *         <code><b>NEIN</b></code>: entweder gibt es keinen
	 *         Ersatzquerschnitt, oder der Ersatzquerschnitt liefert keine
	 *         Nutzdaten oder im falschen Intervall oder im richtigen Intervall
	 *         und hat nur implausible aktuelle Nutzdaten<br> -
	 *         <code><b>WARTE</b></code>: es gibt Ersatzquerschnitt mit
	 *         Nutzdaten, die im richtigen Intervall aber noch nicht aktuell
	 *         vorliegen
	 */
	protected final MweMethodenErgebnis versucheErsatzWertErsetzung(
			UmfeldDatenSensorDatum datumImpl) {
		MweMethodenErgebnis ergebnis = MweMethodenErgebnis.NEIN;

		if (this.ersatz != null && this.letzterErsatzDatensatz != null
				&& this.letzterErsatzDatensatz.getData() != null) {
			UmfeldDatenSensorDatum datumErsatz = new UmfeldDatenSensorDatum(
					this.letzterErsatzDatensatz);

			if (datumErsatz.getT() == datumImpl.getT()) {
				if (datumErsatz.getDatenZeit() == datumImpl.getDatenZeit()) {
					if (datumErsatz.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
						this
								.publiziere(
										this.letztesEmpangenesImplausiblesDatum,
										this
												.getNutzdatenKopieVon(this.letzterErsatzDatensatz));
						this.letztesEmpangenesImplausiblesDatum = null;
						ergebnis = MweMethodenErgebnis.JA;
					}
				} else if (datumErsatz.getDatenZeit() < datumImpl
						.getDatenZeit()) {
					/**
					 * wir koennen noch auf das aktuelle Datum warten
					 */
					ergebnis = MweMethodenErgebnis.WARTE;
				}
			}
		}

		return ergebnis;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// mache nichts
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}

}
