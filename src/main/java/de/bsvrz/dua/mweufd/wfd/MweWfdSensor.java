/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

package de.bsvrz.dua.mweufd.wfd;

import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.AbstraktMweUfdsSensor;
import de.bsvrz.dua.mweufd.MweMethodenErgebnis;
import de.bsvrz.dua.mweufd.MweUfdSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.IOnlineUfdSensorListener;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Implementierung der Messwertersetzung nach folgendem Verfahren:<br>
 * <br>
 * 
 * Ersatzwerte sind in der Reihenfolge der Beschreibung zu bestimmen. Ist über
 * keines der Ersatzwertverfahren ein gültiger Ersatzwert ermittelbar, ist der
 * Sensorwert als nicht ermittelbar zukennzeichnen:<br>
 * <br>
 * - wenn am gleichen Umfeldmessstellen ein weiterer Bodensensor (Nebensensor)
 * plausible Werte liefert, so sind diese zu übernehmen, - sonst ist für eine
 * parametrierbare Zeit (Ersteinstellung = 3 Minuten) der letzte plausible
 * Messwert maßgebend,<br>
 * - sonst, wenn die zugeordneten beiden benachbarten Umfeldmessstellen (vor und
 * nach) eine Wasserfilmdicke > 0 oder beide = 0 plausibel gemessen haben, nehme
 * als Ersatzwert den Mittelwert aus beiden benachbarten
 * Umfeldmessstellen-Werten,<br>
 * - sonst, wenn die Niederschlagsintensität plausibel gemessen wurde, wird kein
 * Ersatzwert für die Wasserfilmdicke bestimmt. Der Sensorwert ist als nicht
 * ermittelbar zu kennzeichnen. - sonst werden die plausiblen Messwerte des
 * Ersatzquerschnittes übernommen,<br>
 * - sonst Sensorwert als nicht ermittelbar kennzeichnen.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class MweWfdSensor extends AbstraktMweUfdsSensor {

	/**
	 * Der Nebensensor mit aktuellen Daten.
	 */
	protected Map<SystemObject, ResultData> nebenSensorenMitDaten = new HashMap<SystemObject, ResultData>();

	/**
	 * letzter empfangener Datensatz des Nachfolgersensors.
	 */
	private ResultData letzterNachfolgerDatensatz = null;

	/**
	 * letzter empfangener Datensatz des Vorgaengersensors.
	 */
	private ResultData letzterVorgaengerDatensatz = null;

	/**
	 * Der WFD-Sensor mit aktuellen Daten.
	 */
	protected MweUfdSensor niDatenSensor = null;

	/**
	 * letzter empfangener WFD-Datensatz.
	 */
	private ResultData letzterNiDatensatz = null;

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
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	public MweWfdSensor(final IVerwaltungMitGuete verwaltung,
			final DUAUmfeldDatenMessStelle messStelle, final DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);

		if (this.nachfolger != null) {
			this.nachfolger.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							MweWfdSensor.this.letzterNachfolgerDatensatz = resultat;
							MweWfdSensor.this.trigger();
						}

					}, true);
		}

		if (this.vorgaenger != null) {
			this.vorgaenger.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							MweWfdSensor.this.letzterVorgaengerDatensatz = resultat;
							MweWfdSensor.this.trigger();
						}

					}, true);
		}

		DUAUmfeldDatenSensor niSensor = messStelle
				.getHauptSensor(UmfeldDatenArt.ni);
		if (niSensor == null) {
			if (messStelle.getNebenSensoren(UmfeldDatenArt.ni).size() > 0) {
				niSensor = messStelle.getNebenSensoren(UmfeldDatenArt.ni)
						.iterator().next();
			}
		}

		if (niSensor != null) {
			this.niDatenSensor = MweUfdSensor.getInstanz(verwaltung
					.getVerbindung(), niSensor.getObjekt());
			this.niDatenSensor.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							MweWfdSensor.this.letzterNiDatensatz = resultat;
							MweWfdSensor.this.trigger();
						}

					}, true);
		}

		for (DUAUmfeldDatenSensor nebenSensor : messStelle
				.getNebenSensoren(UmfeldDatenArt.wfd)) {

			// Der ErsatzSensor iast auch in der Menge der Nebensensoren, aber
			// wird anders behandelt
			if (this.ersatz != null
					&& nebenSensor.getObjekt() == this.ersatz.getObjekt()) {
				continue;
			}

			final MweUfdSensor datenNebenSensor = MweUfdSensor.getInstanz(verwaltung
					.getVerbindung(), nebenSensor.getObjekt());

			datenNebenSensor.addListener(
					new IOnlineUfdSensorListener<ResultData>() {

						public void aktualisiereDaten(final ResultData resultat) {
							synchronized (this) {
								MweWfdSensor.this.nebenSensorenMitDaten.put(
										resultat.getObject(), resultat);
							}
							MweWfdSensor.this.trigger();
						}

					}, true);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized void trigger() {
		if (this.letztesEmpangenesImplausiblesDatum != null) {
			final UmfeldDatenSensorDatum datumImpl = new UmfeldDatenSensorDatum(
					this.letztesEmpangenesImplausiblesDatum);

			if (this.messWertFortschreibungStart == -1
					|| this.letztesEmpangenesImplausiblesDatum.getDataTime()
							- this.messWertFortschreibungStart < this.sensorMitParametern
							.getMaxZeitMessWertFortschreibung()) {
				if (this.letztesEmpangenesPlausiblesDatum != null) {
					if (this.messWertFortschreibungStart == -1) {
						this.messWertFortschreibungStart = this.letztesEmpangenesImplausiblesDatum
								.getDataTime();
					}
					this
							.publiziere(
									this.letztesEmpangenesImplausiblesDatum,
									this
											.getNutzdatenKopieVon(letztesEmpangenesPlausiblesDatum));
					this.letztesEmpangenesImplausiblesDatum = null;
					return;
				}
			}

			final MweMethodenErgebnis ergebnisNebenSensorErsetzung = this
					.versucheErsetzungDurchNebenSensoren(datumImpl);
			if (ergebnisNebenSensorErsetzung == MweMethodenErgebnis.JA) {
				this.letztesEmpangenesImplausiblesDatum = null;
				return;
			} else if (ergebnisNebenSensorErsetzung == MweMethodenErgebnis.WARTE) {
				return;
			}

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

				if (datumVor.getT() == datumImpl.getT()
						&& datumNach.getT() == datumImpl.getT()) {
					if (datumVor.getDatenZeit() == datumImpl.getDatenZeit()
							&& datumNach.getDatenZeit() == datumImpl
									.getDatenZeit()) {
						if (this.isMittelWertErrechenbar(datumImpl, datumVor,
								datumNach)) {
							this.letztesEmpangenesImplausiblesDatum = null;
							return;
						}
					} else if (datumVor.getDatenZeit() < datumImpl
							.getDatenZeit()
							|| datumNach.getDatenZeit() < datumImpl
									.getDatenZeit()) {
						return;
					}
				}

			}

			if (this.niDatenSensor != null && this.letzterNiDatensatz != null
					&& this.letzterNiDatensatz.getData() != null) {
				final UmfeldDatenSensorDatum datumNi = new UmfeldDatenSensorDatum(
						this.letzterNiDatensatz);
				if (datumNi.getT() == datumImpl.getT()) {
					if (datumNi.getDatenZeit() == datumImpl.getDatenZeit()) {
						if (datumNi.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
							datumImpl.getWert().setNichtErmittelbarAn();
							this.publiziere(
									this.letztesEmpangenesImplausiblesDatum,
									datumImpl.getDatum());
							this.letztesEmpangenesImplausiblesDatum = null;
							return;
						}
					} else {
						return;
					}
				}
			}

			final MweMethodenErgebnis ergebnisErsatzSensorErsetzung = this
					.versucheErsatzWertErsetzung(datumImpl);
			if (ergebnisErsatzSensorErsetzung == MweMethodenErgebnis.JA) {
				this.letztesEmpangenesImplausiblesDatum = null;
				return;
			} else if (ergebnisErsatzSensorErsetzung == MweMethodenErgebnis.WARTE) {
				return;
			}

			datumImpl.getWert().setNichtErmittelbarAn();
			this.publiziere(this.letztesEmpangenesImplausiblesDatum, datumImpl
					.getDatum());
			this.letztesEmpangenesImplausiblesDatum = null;
		}
	}

	/**
	 * Implementiert die erste Ersetzungsmethode aus Afo-4.0 (6.6.2.5.5.4,
	 * S.113):<br>
	 * Wenn am gleichen Umfeldmessstellen ein weiterer Bodensensor (Nebensensor)
	 * plausible Werte liefert, so sind diese zu übernehmen.<br>
	 * 
	 * @param datumImpl
	 *            das implausible Datum, das ersetzt werden soll
	 * @return das Ergebnis des Ersetzungsversuchs<br>
	 *         - <code><b>JA</b></code>: es existiert ein Nebensensor mit
	 *         aktuellen, plausiblen Daten. Der implausible Messwert wurde
	 *         ersetzt und publiziert<br>
	 *         - <code><b>NEIN</b></code>: entweder gibt es keine Nebensensoren,
	 *         oder alle Nebensensoren, die Daten liefern haben keine Nutzdaten
	 *         oder senden im falschen Intervall oder senden im richtigen
	 *         Intervall und haben nur implausible aktuelle Nutzdaten<br>
	 *         - <code><b>WARTE</b></code>: es gibt Nebensensoren mit Nutzdaten,
	 *         die im richtigen Intervall senden, von denen aber noch keine
	 *         aktuellen Daten vorliegen
	 */
	private MweMethodenErgebnis versucheErsetzungDurchNebenSensoren(
			final UmfeldDatenSensorDatum datumImpl) {
		MweMethodenErgebnis ergebnis = MweMethodenErgebnis.NEIN;

		/**
		 * Nutzbare Nebensensoren sind Sensoren, die Nutzdaten im gleichen
		 * Intervall wie der zu ersetztende Sensor liefern
		 */
		int nutzbareNebenSensorenGesamt = 0;
		/**
		 * Aktuelle nutzbare Sensoren, sind nutzbare Sensoren, die ein Nutzdatum
		 * fuer das Intervall bereitstellen, fuer das die Messwertersetzung
		 * stattfinden soll
		 */
		int nutzbareNebenSensorenAktuellGesamt = 0;
		if (nebenSensorenMitDaten != null) {
			for (ResultData nebenSensorResultat : this.nebenSensorenMitDaten
					.values()) {
				if (nebenSensorResultat != null
						&& nebenSensorResultat.getData() != null) {
					final UmfeldDatenSensorDatum datumNebenSensor = new UmfeldDatenSensorDatum(
							nebenSensorResultat);

					if (datumNebenSensor.getT() == datumImpl.getT()) {
						nutzbareNebenSensorenGesamt++;
						if (datumNebenSensor.getDatenZeit() == datumImpl
								.getDatenZeit()) {
							nutzbareNebenSensorenAktuellGesamt++;

							if (datumNebenSensor
									.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN) {
								this
										.publiziere(
												this.letztesEmpangenesImplausiblesDatum,
												this
														.getNutzdatenKopieVon(datumNebenSensor
																.getOriginalDatum()));
								this.letztesEmpangenesImplausiblesDatum = null;
								ergebnis = MweMethodenErgebnis.JA;
								break;
							}
						} else if (datumNebenSensor.getDatenZeit() > datumImpl
								.getDatenZeit()) {
							nutzbareNebenSensorenGesamt--;
						}
					}
				}
			}

			if (nutzbareNebenSensorenGesamt > 0) {
				if (nutzbareNebenSensorenGesamt > nutzbareNebenSensorenAktuellGesamt) {
					ergebnis = MweMethodenErgebnis.WARTE;
				}
			}
		}

		return ergebnis;
	}

}
