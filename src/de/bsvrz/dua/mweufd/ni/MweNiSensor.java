/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.mweufd.ni;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.AbstraktMweUfdsSensor;
import de.bsvrz.dua.mweufd.IMweUfdSensorListener;
import de.bsvrz.dua.mweufd.MweMethodenErgebnis;
import de.bsvrz.dua.mweufd.MweUfdSensor;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Implementierung der Messwertersetzung nach folgendem Verfahren:<br><br>
 * 
 * Ersatzwerte sind in der Reihenfolge der Beschreibung zu bestimmen. Ist �ber keines der
 * Ersatzwertverfahren ein g�ltiger Ersatzwert ermittelbar, ist der Sensorwert als nicht ermittelbar
 * zukennzeichnen:<br><br>
 * 
 * - f�r eine parametrierbare Zeit (Ersteinstellung = 3 Minuten) ist der letzte plausible
 * Messwert ma�gebend,<br>
 * - sonst, wenn die zugeordneten beiden benachbarten Umfelddatenmessstellen (vor und
 * nach) eine Niederschlagsintensit�t > 0 oder beide = 0 plausibel gemessen haben, nehme
 * als Ersatzwert den Mittelwert aus beiden benachbarten MQ-Werten,<br>
 * - sonst, wenn die Wasserfilmdicke gemessen wurde, wird kein Ersatzwert f�r die Niederschalgsintensit�t
 * bestimmt, Der Sensorwert ist als nicht ermittelbar zu kennzeichnen<br>
 * - sonst werden die plausiblen Messwerte des Ersatzquerschnittes �bernommen,<br>
 * - sonst Sensorwert als nicht ermittelbar kennzeichnen<br>
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweNiSensor
extends AbstraktMweUfdsSensor{
	
	/**
	 * Der WFD-Sensor mit aktuellen Daten
	 */
	protected MweUfdSensor wfdDatenSensor = null;

	/**
	 * letzter empfangener WFD-Datensatz
	 */
	private ResultData letzterWfdDatensatz = null;
		
	/**
	 * letzter empfangener Datensatz des Nachfolgersensors
	 */
	private ResultData letzterNachfolgerDatensatz = null;
	
	/**
	 * letzter empfangener Datensatz des Vorgaengersensors
	 */
	private ResultData letzterVorgaengerDatensatz = null;

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param messStelle die Umfelddatenmessstelle, die in Bezug auf einen bestimmten
	 * Hauptsensor messwertersetzt werden soll (muss <code> != null</code> sein)
	 * @param sensor der Umfelddatensensor der messwertersetzt werden soll
	 * (muss <code> != null</code> sein)
	 * @throws DUAInitialisierungsException wenn die Initialisierung des Bearbeitungsknotens
	 * fehlgeschlagen ist
	 */
	public MweNiSensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
				
		if(this.nachfolger != null){
			this.nachfolger.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweNiSensor.this.letzterNachfolgerDatensatz = resultat;
					MweNiSensor.this.trigger();
				}
				
			}, true);
		}
		
		if(this.vorgaenger != null){
			this.vorgaenger.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweNiSensor.this.letzterVorgaengerDatensatz = resultat;
					MweNiSensor.this.trigger();
				}
				
			}, true);
		}

		DUAUmfeldDatenSensor wfdSensor = messStelle.getHauptSensor(UmfeldDatenArt.WFD);
		if(wfdSensor == null){
			if(messStelle.getNebenSensoren(UmfeldDatenArt.WFD).size() > 0){
				wfdSensor = messStelle.getNebenSensoren(UmfeldDatenArt.WFD).iterator().next();
				LOGGER.warning("An Umfelddatenmessstelle " + messStelle + //$NON-NLS-1$
						" ist kein WFD-Hauptsensor konfiguriert. Nehme Nebensensor " + wfdSensor); //$NON-NLS-1$
			}
		}
		
		if(wfdSensor != null){
			this.wfdDatenSensor = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), wfdSensor.getObjekt());
			this.wfdDatenSensor.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweNiSensor.this.letzterWfdDatensatz = resultat;
					MweNiSensor.this.trigger();
				}
				
			}, true);	
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized void trigger() {
		if(this.letztesEmpangenesImplausiblesDatum != null){
			UmfeldDatenSensorDatum datumImpl = new UmfeldDatenSensorDatum(this.letztesEmpangenesImplausiblesDatum);
						
			if(this.letztesEmpangenesPlausiblesDatum == null ||
				(this.messWertFortschreibungStart != -1 &&
				this.letztesEmpangenesImplausiblesDatum.getDataTime() - this.messWertFortschreibungStart >
				this.sensorMitParametern.getMaxZeitMessWertFortschreibung())){
				
				/**
				 * naechster Punkt:
				 * wenn die zugeordneten beiden benachbarten Umfelddatenmessstellen (vor und
				 * nach) eine Niederschlagsintensit�t > 0 oder beide = 0 plausibel gemessen haben, nehme
				 * als Ersatzwert den Mittelwert aus beiden benachbarten MQ-Werten
				 */
				if(this.vorgaenger != null && this.nachfolger != null &&
					this.letzterVorgaengerDatensatz != null && this.letzterNachfolgerDatensatz != null &&
					this.letzterVorgaengerDatensatz.getData() != null && this.letzterNachfolgerDatensatz.getData() != null){
					
					UmfeldDatenSensorDatum datumVor = new UmfeldDatenSensorDatum(this.letzterVorgaengerDatensatz);
					UmfeldDatenSensorDatum datumNach = new UmfeldDatenSensorDatum(this.letzterNachfolgerDatensatz);
					
					if(datumVor.getT() == datumImpl.getT() &&
							datumNach.getT() == datumImpl.getT()){
						if(datumVor.getDatenZeit() == datumImpl.getDatenZeit() &&
								datumNach.getDatenZeit() == datumImpl.getDatenZeit()){
							if(this.isMittelWertErrechenbar(datumImpl, datumVor, datumNach)){
								this.letztesEmpangenesImplausiblesDatum = null;
								return;
							}
						}else if(datumVor.getDatenZeit() < datumImpl.getDatenZeit() ||
								datumNach.getDatenZeit() < datumImpl.getDatenZeit()){
							return;
						}
					}
				}
				
				/**
				 * naechster Punkt:
				 * wenn die Wasserfilmdicke gemessen wurde, wird kein Ersatzwert f�r die Niederschalgsintensit�t
				 * bestimmt, Der Sensorwert ist als nicht ermittelbar zu kennzeichnen
				 */
				if(this.wfdDatenSensor != null && this.letzterWfdDatensatz != null){
					UmfeldDatenSensorDatum datumWfd = new UmfeldDatenSensorDatum(this.letzterWfdDatensatz);
					
					if(datumWfd.getT() == datumImpl.getT()) {
						if(datumWfd.getDatenZeit() == datumImpl.getDatenZeit()){
							if(datumWfd.getWert().getWert() >= 0){
								datumImpl.getWert().setNichtErmittelbarAn();
								this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
										this.getNutzdatenKopieVon(this.letztesEmpangenesImplausiblesDatum));						
								this.letztesEmpangenesImplausiblesDatum = null;
								return;
							}
						}else{
							return;
						}
					}
				}
				
				/**
				 * naechster Punkt:
				 * sonst werden die plausiblen Messwerte des Ersatzquerschnittes �bernommen
				 */
				MweMethodenErgebnis ergebnisErsatzSensorErsetzung = this.versucheErsatzWertErsetzung(datumImpl);
				if(ergebnisErsatzSensorErsetzung == MweMethodenErgebnis.JA){
					this.letztesEmpangenesImplausiblesDatum = null;
					return;
				}else
				if(ergebnisErsatzSensorErsetzung == MweMethodenErgebnis.WARTE){
					return;
				}				
				
				datumImpl.getWert().setNichtErmittelbarAn();
				this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
						datumImpl.getDatum());
				this.letztesEmpangenesImplausiblesDatum = null;				
			}else{
				/**
				 * f�r eine parametrierbare Zeit (Ersteinstellung = 3 Minuten) ist der letzte plausible
				 * Messwert massgebend
				 */
				if(this.messWertFortschreibungStart == -1){
					this.messWertFortschreibungStart = this.letztesEmpangenesImplausiblesDatum.getDataTime();
				}
				this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
						this.getNutzdatenKopieVon(this.letztesEmpangenesPlausiblesDatum));						
				this.letztesEmpangenesImplausiblesDatum = null;
			}
		}
	}	
}
