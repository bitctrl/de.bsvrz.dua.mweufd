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

package de.bsvrz.dua.mweufd.sw;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.AbstraktMweUfdsSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.IOnlineUfdSensorListener;

/**
 * Implementierung der Messwertersetzung nach folgendem Verfahren:<br><br>
 * 
 * Ersatzwerte sind in der Reihenfolge der Beschreibung zu bestimmen. Ist über keines der
 * Ersatzwertverfahren ein gültiger Ersatzwert ermittelbar, ist der Sensorwert als nicht
 * ermittelbar zukennzeichnen:<br><br>
 * 
 * - nehme die Werte der Nachfolger-Umfeldmessstelle<br>
 * - wenn keine Nachfolger-Umfeldmessstelle vorhanden ist, nehme für eine dynamisch parametrierbare
 * Zeit (Ersteinstellung = 3 Minuten) den letzten plausiblen Messwert,<br>
 * - sonst Sensorwert als nicht ermittelbar kennzeichnen. 
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweSwSensor
extends AbstraktMweUfdsSensor {
	
	/**
	 * letzter empfangener Datensatz des Nachfolgersensors
	 */
	private ResultData letzterNachfolgerDatensatz = null;
	
	/**
	 * letzter empfangener plausible Datensatz des Nachfolgersensors
	 */
	private ResultData letzterPlausibleNachfolgerDatensatz = null;
	
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
	public MweSwSensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);

		if(this.nachfolger != null){
			this.nachfolger.addListener(new IOnlineUfdSensorListener<ResultData>(){

				public void aktualisiereDaten(ResultData resultat) {
					if(resultat.getData() != null) {
						UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
						if(datum.getStatusMessWertErsetzungImplausibel() != DUAKonstanten.JA)
							MweSwSensor.this.letzterPlausibleNachfolgerDatensatz = resultat;
					}
					
					MweSwSensor.this.letzterNachfolgerDatensatz = resultat;
					MweSwSensor.this.trigger();
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

			if(this.nachfolger != null && this.letzterNachfolgerDatensatz != null &&
				this.letzterNachfolgerDatensatz.getData() != null){
				
				UmfeldDatenSensorDatum datumNach = new UmfeldDatenSensorDatum(this.letzterNachfolgerDatensatz);
				if(datumNach.getT() == datumImpl.getT()){
					if(datumNach.getDatenZeit() == datumImpl.getDatenZeit()){
						if(datumNach.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.NEIN){
							this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
									this.getNutzdatenKopieVon(this.letzterNachfolgerDatensatz));
							this.letztesEmpangenesImplausiblesDatum = null;
							return;
						}
					}else{
						return;
					}
				}
			}
				
			if(this.messWertFortschreibungStart == -1 ||
			   this.letztesEmpangenesImplausiblesDatum.getDataTime() - this.messWertFortschreibungStart <
			   this.sensorMitParametern.getMaxZeitMessWertFortschreibung()){
				if(this.letzterPlausibleNachfolgerDatensatz!= null){
					if(this.messWertFortschreibungStart == -1){
						this.messWertFortschreibungStart = this.letztesEmpangenesImplausiblesDatum.getDataTime();
					}
					this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
							this.getNutzdatenKopieVon(this.letzterPlausibleNachfolgerDatensatz));
					this.letztesEmpangenesImplausiblesDatum = null;
					return;
				}
			}
						
			datumImpl.getWert().setNichtErmittelbarAn();
			this.publiziere(this.letztesEmpangenesImplausiblesDatum, 
					datumImpl.getDatum());
			this.letztesEmpangenesImplausiblesDatum = null;
		}
	}

}
