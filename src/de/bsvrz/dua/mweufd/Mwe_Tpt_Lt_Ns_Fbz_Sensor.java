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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;

/**
 * Implementierung der Messwertersetzung nach folgendem Verfahren:<br><br>
 * 
 * Ersatzwerte sind in der Reihenfolge der Beschreibung zu bestimmen. Ist ueber keines
 * der Ersatzwertverfahren ein gültiger Ersatzwert ermittelbar, ist der Sensorwert als
 * <code>nicht ermittelbar</code> zu kennzeichnen.<br><br>
 * 
 * - für eine parametrierbare Zeit (Ersteinstellung = 3 Minuten) ist der letzte plausible
 * Messwert maßgebend,<br>
 * - sonst wird der plausible Wert vom zugeordneten Ersatzquerschnitt übernommen,<br>
 * - sonst Sensorwert als nicht ermittelbar kennzeichnen 
 * <br><br>
 * <b>Dieses Verfahren wird fuer die Messwertersetzung von TPT, LT, NS und FBZ benutzt</b> 
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Mwe_Tpt_Lt_Ns_Fbz_Sensor
extends AbstraktMweUfdsSensor {
	
	/**
	 * letzter empfangener Datensatz des Ersatzsensors
	 */
	private ResultData letzterErsatzDatensatz = null;
	

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
	public Mwe_Tpt_Lt_Ns_Fbz_Sensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(this.ersatz != null){
			this.ersatz.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					Mwe_Tpt_Lt_Ns_Fbz_Sensor.this.letzterErsatzDatensatz = resultat;
					Mwe_Tpt_Lt_Ns_Fbz_Sensor.this.trigger();
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
				boolean setzteNichtErmittelbar = false;
				if(this.ersatz != null && this.letzterErsatzDatensatz != null){
					if(this.letzterErsatzDatensatz.getData() == null){
						setzteNichtErmittelbar = true;
					}else{
						UmfeldDatenSensorDatum datumErsatz = new UmfeldDatenSensorDatum(this.letzterErsatzDatensatz);
						if(datumErsatz.getT() == datumImpl.getT()){
							if(datumErsatz.getDatenZeit() == datumImpl.getDatenZeit()){
								if(datumErsatz.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA){
									setzteNichtErmittelbar = true;
								}else{
									this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
											this.getNutzdatenKopieVon(this.letzterErsatzDatensatz));
									this.letztesEmpangenesImplausiblesDatum = null;									
								}								
							}
						}else{
							setzteNichtErmittelbar = true;
						}
					}
				}else{
					setzteNichtErmittelbar = true;
				}
				
				if(setzteNichtErmittelbar){
					datumImpl.getWert().setNichtErmittelbarAn();
					this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
							datumImpl.getDatum());
					this.letztesEmpangenesImplausiblesDatum = null;					
				}
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
