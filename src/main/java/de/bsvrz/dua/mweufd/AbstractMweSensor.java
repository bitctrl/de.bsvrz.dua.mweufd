/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Messwertersetzung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.mweufd.
 * 
 * de.bsvrz.dua.mweufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.mweufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.mweufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
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
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.IOnlineUfdSensorListener;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.HashMap;
import java.util.Map;

import static de.bsvrz.dua.mweufd.MweMethodenErgebnis.NEIN;

/**
 * Abstrakte Basisklasse für alle Sensoren der Messwertersetzung
 *
 * @author Kappich Systemberatung
 */
public abstract class AbstractMweSensor implements ClientSenderInterface, IOnlineUfdSensorListener<ResultData> {
	
	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * statische Datenverteiler-Verbindung.
	 */
	protected IVerwaltung dieVerwaltung = null;
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
	 * Der Nebensensor mit aktuellen Daten.
	 */
	protected Map<SystemObject, ResultData> nebenSensorenMitDaten = new HashMap<SystemObject, ResultData>();
	/**
	 * letzter empfangener Datensatz des Nachfolgersensors.
	 */
	protected ResultData letzterNachfolgerDatensatz = null;
	/**
	 * letzter empfangener Datensatz des Vorgaengersensors.
	 */
	protected ResultData letzterVorgaengerDatensatz = null;
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
	 * Erstellt einen neuen Sensor
	 * @param verwaltung Verwaltungsmodul
	 * @param umfeldDatenMessStelle Messstelle des Sensors
	 * @param sensor Sensorobjekt aus der Funclib Bitctrl
	 */
	public AbstractMweSensor(final IVerwaltung verwaltung, final DUAUmfeldDatenMessStelle umfeldDatenMessStelle, final DUAUmfeldDatenSensor sensor) throws UmfeldDatenSensorUnbekannteDatenartException {
		if (umfeldDatenMessStelle == null || sensor == null) {
			throw new NullPointerException("Messstelle/Sensor ist <<null>>"); //$NON-NLS-1$
		}
		dieVerwaltung = verwaltung;
		((VerwaltungMesswertErsetzungUFD) verwaltung).getDFS().addObjekt(sensor.getObjekt());
		this.sensorMitParametern = sensor;

		this.messStelle = umfeldDatenMessStelle;
		this.sensorSelbst = MweUfdSensor.getInstanz(
				verwaltung.getVerbindung(),
				sensor.getObjekt());
		this.sensorSelbst.addListener(this, true);

		if(sensor.getObjekt().getPid().equals("ufd.haupt.lt")){
			System.out.println("verwaltung = " + verwaltung);
		}
		
		if (sensor.getVorgaenger() != null) {
			final DUAUmfeldDatenMessStelle vorgaengerMSt = DUAUmfeldDatenMessStelle
					.getInstanz(sensor.getVorgaenger());
			if (vorgaengerMSt != null) {
				final DUAUmfeldDatenSensor vorgaengerSensor = vorgaengerMSt
						.getHauptSensor(sensor.getDatenArt());
				if (vorgaengerSensor != null) {
					this.vorgaenger = MweUfdSensor.getInstanz(verwaltung
							.getVerbindung(), vorgaengerSensor.getObjekt());
				}
			}
		}

		if (sensor.getNachfolger() != null) {
			final DUAUmfeldDatenMessStelle nachfolgerMSt = DUAUmfeldDatenMessStelle
					.getInstanz(sensor.getNachfolger());
			if (nachfolgerMSt != null) {
				final DUAUmfeldDatenSensor nachfolgerSensor = nachfolgerMSt
						.getHauptSensor(sensor.getDatenArt());
				if (nachfolgerSensor != null) {
					this.nachfolger = MweUfdSensor.getInstanz(verwaltung
							.getVerbindung(), nachfolgerSensor.getObjekt());
				}
			}
		}

		if (sensor.getErsatzSensor() != null) {
			this.ersatz = MweUfdSensor.getInstanz(
					verwaltung.getVerbindung(),
					sensor.getErsatzSensor());
			this.ersatz.addListener(new IOnlineUfdSensorListener<ResultData>() {

				public void aktualisiereDaten(final ResultData resultat) {
					AbstractMweSensor.this.letzterErsatzDatensatz = resultat;
					AbstractMweSensor.this.trigger();
				}

			}, true);
		}

		if (this.nachfolger != null) {
			this.nachfolger.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							AbstractMweSensor.this.letzterNachfolgerDatensatz = resultat;
							AbstractMweSensor.this.trigger();
						}

					}, true);
		}

		if (this.vorgaenger != null) {
			this.vorgaenger.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							AbstractMweSensor.this.letzterVorgaengerDatensatz = resultat;
							AbstractMweSensor.this.trigger();
						}

					}, true);
		}

		for (DUAUmfeldDatenSensor nebenSensor : umfeldDatenMessStelle
				.getNebenSensoren(UmfeldDatenArt.wfd)) {
			final MweUfdSensor datenNebenSensor = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), nebenSensor.getObjekt());
			
			datenNebenSensor.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							synchronized (this) {
								AbstractMweSensor.this.nebenSensorenMitDaten.put(
										resultat.getObject(), resultat);
							}
							AbstractMweSensor.this.trigger();
						}

					}, true);

		}
	}

	/**
	 * Wird aufgerufen, wenn ein fuer diese spezielle MWE relevantes Datum
	 * empfangen wurde.
	 */
	protected final void trigger() {
		if (this.letztesEmpangenesImplausiblesDatum != null) {
			messwertErsetzung();
		}
	}

	/**
	 * Hier sollten konkrete Sensorklassen das Verfahren der Messwertersetzung implementieren. Hierzu gibt es folgende Methoden:
	 * 
	 *      - {@link #ersetze()}
	 *      - {@link #ersetzeVorgaengerNachfolger()} 
	 *      - {@link #ersetzeNebensensor()}
	 *      - {@link #fortschreibe()} 
	 */
	protected abstract void messwertErsetzung();

	/**
	 * Ersetzte das implausible Datum durch den Wert des Ersatzsensors
	 * @return Ob die Ersetzung erfolgreich war (oder ggf. noch weitere Ersetzungsverfahren probiert werden müssen)
	 */
	protected MweMethodenErgebnis ersetze() {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(this.letztesEmpangenesImplausiblesDatum);
		if(this.ersatz != null && this.letzterErsatzDatensatz != null
				&& this.letzterErsatzDatensatz.getData() != null) {
			final UmfeldDatenSensorDatum datumErsatz = new UmfeldDatenSensorDatum(this.letzterErsatzDatensatz);

			if(datumErsatz.getT() == datum.getT()) {
				if(datumErsatz.getDatenZeit() == datum.getDatenZeit()) {
					if(datumErsatz.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
							&& datumErsatz.getStatusMessWertErsetzungInterpoliert() == DUAKonstanten.NEIN) {
						datumErsatz.setGueteIndex(Math.round(datumErsatz.getGueteIndex().getWert() * 0.9));
						datumErsatz.setStatusMessWertErsetzungInterpoliert(DUAKonstanten.JA);
						this.publiziere(
								this.letztesEmpangenesImplausiblesDatum,
								datumErsatz.getDatum()
						);
						this.letztesEmpangenesImplausiblesDatum = null;
						return MweMethodenErgebnis.JA;
					}
				}
				else if(datumErsatz.getDatenZeit() < datum
						.getDatenZeit()) {
					/**
					 * wir koennen noch auf das aktuelle Datum warten
					 */
					return MweMethodenErgebnis.WARTE;
				}
			}
		}
		this.publiziere(this.letztesEmpangenesImplausiblesDatum, datum.getDatum());
		this.letztesEmpangenesImplausiblesDatum = null;
		return MweMethodenErgebnis.JA;
	}

	/**
	 * Ersetzte das implausible Datum durch den Mittelwert von Vorgänger und Nachfolger
	 * @return Ob die Ersetzung erfolgreich war (oder ggf. noch weitere Ersetzungsverfahren probiert werden müssen)
	 */
	protected MweMethodenErgebnis ersetzeVorgaengerNachfolger() {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(this.letztesEmpangenesImplausiblesDatum);
		if (this.vorgaenger != null
				&& this.letzterVorgaengerDatensatz != null
				&& this.letzterVorgaengerDatensatz.getData() != null
				&& this.nachfolger != null
				&& this.letzterNachfolgerDatensatz != null
				&& this.letzterNachfolgerDatensatz.getData() != null) {
			final UmfeldDatenSensorDatum datumNach = new UmfeldDatenSensorDatum(
					this.letzterNachfolgerDatensatz);
			final UmfeldDatenSensorDatum datumVor = new UmfeldDatenSensorDatum(
					this.letzterVorgaengerDatensatz);

			if(datumVor.getDatenZeit() == datum.getDatenZeit()
					&& datumNach.getDatenZeit() == datum
					.getDatenZeit()) {

				if(datumVor.getT() != datum.getT()
						|| datumNach.getT() != datum.getT()) {
					return NEIN;
				}

				if(this.isMittelWertErrechenbar(datum, datumVor, datumNach)) {
					this.letztesEmpangenesImplausiblesDatum = null;
					return MweMethodenErgebnis.JA;
				}
			}
			else if(datumVor.getDatenZeit() < datum.getDatenZeit()
					|| datumNach.getDatenZeit() < datum.getDatenZeit()) {
				return MweMethodenErgebnis.WARTE;
			}
		}
		return NEIN;
	}

	/**
	 * Ersetzte das implausible Datum durch den Wert eines Nebensensors
	 * @return Ob die Ersetzung erfolgreich war (oder ggf. noch weitere Ersetzungsverfahren probiert werden müssen)
	 */
	protected MweMethodenErgebnis ersetzeNebensensor() {
		final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(this.letztesEmpangenesImplausiblesDatum);
		for(ResultData resultData : nebenSensorenMitDaten.values()) {
			
			if(resultData != null && resultData.hasData()) {

				final UmfeldDatenSensorDatum datumNeben = new UmfeldDatenSensorDatum(resultData);

				if(datumNeben.getT() == datum.getT() && datumNeben.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
					if(datumNeben.getDatenZeit() == datum.getDatenZeit()) {
						this.publiziere(this.letztesEmpangenesImplausiblesDatum, datumNeben.getDatum());
						this.letztesEmpangenesImplausiblesDatum = null;
						return MweMethodenErgebnis.JA;
					}
					else if(datumNeben.getDatenZeit() < datum.getDatenZeit()) {
						return MweMethodenErgebnis.WARTE;
					}
				}
			}
		}
		return NEIN;
	}

	/**
	 * Ersetzte das implausible Datum durch den letzen plausiblen Wert (Fortschreiben)
	 * @return Ob die Ersetzung erfolgreich war (oder ggf. noch weitere Ersetzungsverfahren probiert werden müssen)
	 */
	protected MweMethodenErgebnis fortschreibe() {
		final long dataTime = this.letztesEmpangenesImplausiblesDatum.getDataTime();
		if(this.sensorMitParametern.getMaxZeitMessWertFortschreibung() > 0
				&& this.letztesEmpangenesPlausiblesDatum != null
				&& (this.messWertFortschreibungStart == -1
				|| dataTime - this.messWertFortschreibungStart < this.sensorMitParametern.getMaxZeitMessWertFortschreibung())) {
			/**
			 * Fortschreibung
			 */
			if(this.messWertFortschreibungStart == -1) {
				this.messWertFortschreibungStart = dataTime;
			}
			final UmfeldDatenSensorDatum datumPl = new UmfeldDatenSensorDatum(this.letztesEmpangenesPlausiblesDatum);
			datumPl.setGueteIndex(Math.round(datumPl.getGueteIndex().getWert() * .95));
			datumPl.setStatusMessWertErsetzungInterpoliert(DUAKonstanten.JA);
			this.publiziere(this.letztesEmpangenesImplausiblesDatum, datumPl.getDatum());

			this.letztesEmpangenesImplausiblesDatum = null;
			return MweMethodenErgebnis.JA;
		}
		return NEIN;
	}

	public void aktualisiereDaten(final ResultData resultat) {
		if (this.letztesEmpangenesImplausiblesDatum != null) {
			LOGGER.error(
					"Nicht freigegebenes implausibles Datum:\n" + //$NON-NLS-1$
							this.letztesEmpangenesImplausiblesDatum
							+ "\nNachfolger:\n" + resultat); //$NON-NLS-1$

			final UmfeldDatenSensorDatum datumImpl = new UmfeldDatenSensorDatum(
					this.letztesEmpangenesImplausiblesDatum);
			datumImpl.getWert().setNichtErmittelbarAn();
			this.publiziere(this.letztesEmpangenesImplausiblesDatum, datumImpl
					.getDatum());

			this.letztesEmpangenesImplausiblesDatum = null;
		}

		if (resultat.getData() != null) {
			final UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
			if (datum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA) {
				if (this.messWertErsetzungStart == -1) {
					this.messWertErsetzungStart = resultat.getDataTime();
				}

				if (resultat.getDataTime() - this.messWertErsetzungStart >= this.sensorMitParametern.getMaxZeitMessWertErsetzung()) {
					/**
					 * Maximale Messwertersetzungszeit abgelaufen, unverändert publizieren
					 */
					this.publiziere(resultat, resultat.getData());
				} else {
					/**
					 * Messwertersetze die Daten
					 */
					this.letztesEmpangenesImplausiblesDatum = resultat;
					this.trigger();
				}
			} else {
				this.messWertErsetzungStart = -1;
				this.messWertFortschreibungStart = -1;
				this.letztesEmpangenesPlausiblesDatum = resultat;
				this.publiziere(resultat, resultat.getData());
			}
		} else {
			this.messWertErsetzungStart = -1;
			this.messWertFortschreibungStart = -1;
			this.letztesEmpangenesPlausiblesDatum = null;
			this.publiziere(resultat, null);
		}
	}

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
			this.letztesPubDatum = ((VerwaltungMesswertErsetzungUFD)dieVerwaltung).getDFS()
					.publiziere(original, nutzDatum);
			if (this.letztesPubDatum == null) {
				LOGGER.warning(
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
			final UmfeldDatenSensorDatum datumImpl, final UmfeldDatenSensorDatum datumVor,
			final UmfeldDatenSensorDatum datumNach) {
		boolean erfolg = false;

		if (datumVor.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& datumNach.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN
				&& datumVor.getStatusMessWertErsetzungInterpoliert() == DUAKonstanten.NEIN
				&& datumNach.getStatusMessWertErsetzungInterpoliert() == DUAKonstanten.NEIN
				&& ((datumVor.getWert().getWert() >= 0 && datumNach.getWert().getWert() >= 0))) {

			final long durchschnitt = Math.round(((double) datumVor.getWert()
					.getWert() + (double) datumNach.getWert().getWert()) / 2.0);

			UmfeldDatenArt umfeldDatenArt;
			try {
				umfeldDatenArt = UmfeldDatenArt.getUmfeldDatenArtVon(this.sensorSelbst.getObjekt());
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e1) {
				LOGGER.warning(
						"Guete kann nicht angepasst werden: " + e1.getMessage());
				return false;
			}
			
			if (DUAUtensilien.isWertInWerteBereich(datumImpl.getOriginalDatum()
					.getData().getItem(umfeldDatenArt.getName())
					.getItem("Wert"), durchschnitt)) { //$NON-NLS-1$
				final GWert gueteWert1 = new GWert(
						datumVor.getGueteIndex(),
						GueteVerfahren.getZustand(datumVor.getGueteVerfahren()),
						false);
				final GWert gueteWert2 = new GWert(datumNach.getGueteIndex(),
						GueteVerfahren
								.getZustand(datumNach.getGueteVerfahren()),
						false);
				GWert gueteGesamt = GWert
						.getNichtErmittelbareGuete(GueteVerfahren
								.getZustand(datumImpl.getGueteVerfahren()));

				try {
					gueteGesamt = GueteVerfahren.gewichte(GueteVerfahren.summe(gueteWert1, gueteWert2), 0.9); 
				} catch (final GueteException e) {
					LOGGER.warning(
							"Guete kann nicht angepasst werden\n" + //$NON-NLS-1$
									"Wert1: " + datumVor + //$NON-NLS-1$
									"\nWert2: " + datumNach); //$NON-NLS-1$
					e.printStackTrace();
				}

				datumImpl.setGueteIndex(gueteGesamt.getIndexUnskaliertGewichtet());
				datumImpl.setStatusMessWertErsetzungInterpoliert(DUAKonstanten.JA);
				datumImpl.setStatusMessWertErsetzungImplausibel(DUAKonstanten.NEIN);
				datumImpl.getWert().setWert(durchschnitt);

				this.publiziere(this.letztesEmpangenesImplausiblesDatum,
						datumImpl.getDatum());

				this.letztesEmpangenesImplausiblesDatum = null;
				erfolg = true;
			}
		}

		return erfolg;
	}

	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// mache nichts
	}

	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}
}
