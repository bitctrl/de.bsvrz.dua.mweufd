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

package de.bsvrz.dua.mweufd.wfd;

import java.util.HashMap;
import java.util.Set;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.AbstraktMweUfdsSensor;
import de.bsvrz.dua.mweufd.IMweUfdSensorListener;
import de.bsvrz.dua.mweufd.MweUfdSensor;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Implementierung der Messwertersetzung nach folgendem Verfahren:<br><br>
 * 
 * Ersatzwerte sind in der Reihenfolge der Beschreibung zu bestimmen. Ist über keines der Ersatzwertverfahren
 * ein gültiger Ersatzwert ermittelbar, ist der Sensorwert als nicht ermittelbar
 * zukennzeichnen:<br><br>
 * 
 * - wenn am gleichen Umfeldmessstellen ein weiterer Bodensensor (Nebensensor) plausible
 * Werte liefert, so sind diese zu übernehmen,
 * - sonst ist für eine parametrierbare Zeit (Ersteinstellung = 3 Minuten) der letzte plausible
 * Messwert maßgebend,<br>
 * - sonst, wenn die zugeordneten beiden benachbarten Umfeldmessstellen (vor und nach)
 * eine Wasserfilmdicke > 0 oder beide = 0 plausibel gemessen haben, nehme als Ersatzwert
 * den Mittelwert aus beiden benachbarten Umfeldmessstellen-Werten,<br>
 * - sonst, wenn die Niederschlagsintensität plausibel gemessen wurde, wird kein Ersatzwert
 * für die Wasserfilmdicke bestimmt. Der Sensorwert ist als nicht ermittelbar zu kennzeichnen.
 * - sonst werden die plausiblen Messwerte des Ersatzquerschnittes übernommen,<br>
 * - sonst Sensorwert als nicht ermittelbar kennzeichnen.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweWfdSensor
extends AbstraktMweUfdsSensor {

	/**
	 * Der Nebensensor mit aktuellen Daten
	 */
	protected HashMap<SystemObject, ResultData> nebenSensorenMitDaten = null;
	
	/**
	 * letzter empfangener Datensatz des Ersatzsensors
	 */
	private ResultData letzterErsatzDatensatz = null;
	
	/**
	 * letzter empfangener Datensatz des Nachfolgersensors
	 */
	private ResultData letzterNachfolgerDatensatz = null;
	
	/**
	 * letzter empfangener Datensatz des Vorgaengersensors
	 */
	private ResultData letzterVorgaengerDatensatz = null;
	
	/**
	 * Der WFD-Sensor mit aktuellen Daten
	 */
	protected MweUfdSensor niDatenSensor = null;

	/**
	 * letzter empfangener WFD-Datensatz
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
	public MweWfdSensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(this.ersatz != null){
			this.ersatz.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweWfdSensor.this.letzterErsatzDatensatz = resultat;
					MweWfdSensor.this.trigger();
				}
				
			}, true);
		}
		
		if(this.nachfolger != null){
			this.nachfolger.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweWfdSensor.this.letzterNachfolgerDatensatz = resultat;
					MweWfdSensor.this.trigger();
				}
				
			}, true);
		}
		
		if(this.vorgaenger != null){
			this.vorgaenger.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweWfdSensor.this.letzterVorgaengerDatensatz = resultat;
					MweWfdSensor.this.trigger();
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
			this.niDatenSensor.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					MweWfdSensor.this.letzterNiDatensatz = resultat;
					MweWfdSensor.this.trigger();
				}
				
			}, true);	
		}
		
		for(DUAUmfeldDatenSensor nebenSensor:messStelle.getNebenSensoren(UmfeldDatenArt.WFD)){
			MweUfdSensor datenNebenSensor = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), nebenSensor.getObjekt());
			
			datenNebenSensor.addListener(new IMweUfdSensorListener(){

				public void aktualisiere(ResultData resultat) {
					synchronized (this) {
						MweWfdSensor.this.nebenSensorenMitDaten.put(resultat.getObject(), resultat);
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
		// TODO Auto-generated method stub
		
	}

}
