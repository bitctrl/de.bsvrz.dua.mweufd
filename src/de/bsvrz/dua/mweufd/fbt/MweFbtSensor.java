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

package de.bsvrz.dua.mweufd.fbt;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.AbstraktMweUfdsSensor;
import de.bsvrz.dua.mweufd.MweUfdSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.IOnlineUfdSensorListener;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Implementierung der Messwertersetzung nach folgendem Verfahren:<br><br>
 * 
 * Ersatzwerte sind in der Reihenfolge der Beschreibung zu bestimmen. Ist über keines der Ersatzwertverfahren
 * ein gültiger Ersatzwert ermittelbar, ist der Sensorwert als nicht ermittelbar zukennzeichnen:<br><br>
 * 
 * - für eine parametrierbare Zeit (Ersteinstellung = 3 Minuten) ist der letzte plausible
 * Messwert maßgebend<br>
 * - sonst, wenn die Wasserfilmdicke und/oder die Niederschlagsintensität am betrachteten
 * Querschnitt plausibel ist, übernehme die plausible Fahrbahnoberflächentemperatur vom
 * zugeordneten Ersatzquerschnitt<br>
 * - sonst Sensorwert als nicht ermittelbar kennzeichnen.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweFbtSensor
extends AbstraktMweUfdsSensor {

	/**
	 * Der WFD-Sensor mit aktuellen Daten
	 */
	protected MweUfdSensor wfdDatenSensor = null;

	/**
	 * letzter empfangener WFD-Datensatz
	 */
	private ResultData letzterWfdDatensatz = null;

	/**
	 * Der NI-Sensor mit aktuellen Daten
	 */
	protected MweUfdSensor niDatenSensor = null;

	/**
	 * letzter empfangener NI-Datensatz
	 */
	private ResultData letzterNiDatensatz = null;
		
	
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
	public MweFbtSensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
				
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
			this.wfdDatenSensor.addListener(new IOnlineUfdSensorListener<ResultData>(){

				public void aktualisiereDaten(ResultData resultat) {
					MweFbtSensor.this.letzterWfdDatensatz = resultat;
					MweFbtSensor.this.trigger();
				}
				
			}, true);	
		}
		
		
		DUAUmfeldDatenSensor niSensor = messStelle.getHauptSensor(UmfeldDatenArt.NI);
		if(niSensor == null){
			if(messStelle.getNebenSensoren(UmfeldDatenArt.NI).size() > 0){
				niSensor = messStelle.getNebenSensoren(UmfeldDatenArt.NI).iterator().next();
				LOGGER.warning("An Umfelddatenmessstelle " + messStelle + //$NON-NLS-1$
						" ist kein NI-Hauptsensor konfiguriert. Nehme Nebensensor " + niSensor); //$NON-NLS-1$
			}
		}
		
		if(niSensor != null){
			this.niDatenSensor = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), niSensor.getObjekt());
			this.niDatenSensor.addListener(new IOnlineUfdSensorListener<ResultData>(){

				public void aktualisiereDaten(ResultData resultat) {
					MweFbtSensor.this.letzterNiDatensatz = resultat;
					MweFbtSensor.this.trigger();
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
				 * naechster Punkt
				 */
				UmfeldDatenSensorDatum datumWfd = null;
				UmfeldDatenSensorDatum datumNi = null;
				UmfeldDatenSensorDatum datumErsatz = null;

				if(this.wfdDatenSensor != null && this.letzterWfdDatensatz != null &&
					this.letzterWfdDatensatz.getData() != null){
					datumWfd = new UmfeldDatenSensorDatum(this.letzterWfdDatensatz);
				}
				if(this.niDatenSensor != null && this.letzterNiDatensatz != null &&
						this.letzterNiDatensatz.getData() != null){
					datumNi = new UmfeldDatenSensorDatum(this.letzterNiDatensatz);
				}
				if(this.ersatz != null && this.letzterErsatzDatensatz != null &&
						this.letzterErsatzDatensatz.getData() != null){
					datumErsatz = new UmfeldDatenSensorDatum(this.letzterErsatzDatensatz);
				}				

				boolean ersetze = false;
				if(datumErsatz != null && datumErsatz.getT() == datumImpl.getT()){
					if(datumErsatz.getDatenZeit() == datumImpl.getDatenZeit()){

						if(datumErsatz.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
							if(datumWfd != null && datumWfd.getT() == datumImpl.getT() &&
								datumNi != null && datumNi.getT() == datumImpl.getT()){
								if(datumNi.getDatenZeit() == datumImpl.getDatenZeit() && 
									datumWfd.getDatenZeit() == datumImpl.getDatenZeit()){
									if(datumWfd.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN ||
											datumNi.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
										ersetze = true;
									}											
								}else{
									return;
								}
							}					
							if(datumWfd != null && datumWfd.getT() == datumImpl.getT()){
								if(datumWfd.getDatenZeit() == datumImpl.getDatenZeit()){
									if(datumWfd.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
										ersetze = true;
									}								
								}else{
									return;
								}															
							}
							if(datumNi != null && datumNi.getT() == datumImpl.getT()){
								if(datumNi.getDatenZeit() == datumImpl.getDatenZeit()){
									if(datumNi.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
										ersetze = true;
									}
								}else{
									return;
								}							
							}							
						}						
						
					}else{
						return;
					}
				}
				
				if(ersetze){
					this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
							this.getNutzdatenKopieVon(datumErsatz.getOriginalDatum()));
				}else{
					datumImpl.getWert().setNichtErmittelbarAn();
					this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
							datumImpl.getDatum());
				}				
				this.letztesEmpangenesImplausiblesDatum = null;				
			}else{
				/**
				 * für eine parametrierbare Zeit (Ersteinstellung = 3 Minuten) ist der letzte plausible
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
