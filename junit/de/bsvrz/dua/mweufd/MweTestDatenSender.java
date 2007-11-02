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
package de.bsvrz.dua.mweufd;

import java.util.Collection;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Implementierung einer Klasse, die generisch  Messwerte fuer Sensoren Absenden kann
 *  
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class MweTestDatenSender implements ClientSenderInterface {
	
	/**
	 * Verbindung zum dav
	 */
	private ClientDavInterface dav;
	
	/**
	 * Periote der abgeschickten Daten
	 */
	private long ZEIT_INTERVALL;
	
	/**
	 * Die Parametrierung Datenbeschreibung
	 */
	private final DataDescription DD_MESSWERT_ERSETZUNG;
	
	/**
	 * Standardkonstruktor
	 * @param dav DAV
	 */
	public MweTestDatenSender(ClientDavInterface dav) {
		this.dav = dav;
		DD_MESSWERT_ERSETZUNG = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				dav.getDataModel().getAspect("asp.parameterVorgabe"));
	}
	/**
	 * Setzt den ZeitIntervall der Messwerterzeugung
	 * @param t
	 */
	public void setZeitIntervall(long t) {
		this.ZEIT_INTERVALL = t;
	}
	
	/**
	 * Meldet sich als Sender Ann 
	 * @param so System Objekt
	 * @param dd Datenbeschreibung
	 */
	public void anmeldeSender(SystemObject so, DataDescription dd) {
		try {
			dav.subscribeSender(this, so, dd, SenderRole.sender());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}	
	}

	/**
	 * Meldet sich als Sender Ann 
	 * @param list System Objekt Liste
	 * @param dd Datenbeschreibung
	 */
	public void anmeldeSender(Collection<SystemObject> list, DataDescription dd) {
		try {
			dav.subscribeSender(this, list, dd, SenderRole.sender());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}	
	}


	/**
	 * Meldet sich als Quelle Ann 
	 * @param list System Objekt Liste
	 * @param dd Datenbeschreibung
	 */	
	public void anmeldeQuelle(Collection<SystemObject> list, DataDescription dd) {
		try {
			dav.subscribeSender(this, list, dd, SenderRole.source());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}	
	}

	/**
	 * Meldet sich als Quelle Ann 
	 * @param so System Objekt
	 * @param dd Datenbeschreibung
	 */	
	public void anmeldeQuelle(SystemObject so, DataDescription dd) {
		try {
			dav.subscribeSender(this, so, dd, SenderRole.source());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}	
	}
	
	/**
	 * Meldet sich an fuer die Parametrierung des Sensors
	 * @param so Sensor
	 */
	public void anmeldeParametrierung(SystemObject so) {
		try {
			dav.subscribeSender(this, so, DD_MESSWERT_ERSETZUNG, SenderRole.sender());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}	
	}
	/**
	 * Parametriert den Sensor
	 * @param sensor Sensor
	 * @param messwertFortschreibungsIntervall Intervall der Maximalen Messwertfortschreibung
	 * @param messWertErsetzungIntervall Intervall der Maximalen Messwertersetzung
	 * @param periode Periode
	 */
	public void parametriereSensor(SystemObject sensor, long messwertFortschreibungsIntervall, long messWertErsetzungIntervall, long periode) {
		ZEIT_INTERVALL = periode;
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"));
		data.getItem("maxZeitMessWertErsetzung").asTimeValue().setMillis(messWertErsetzungIntervall);
		//data.getItem("maxZeitMessWertFortschreibung").asTimeValue().setMillis(messwertFortschreibungsIntervall);

		ResultData result = new ResultData(sensor, DD_MESSWERT_ERSETZUNG, System.currentTimeMillis(), data);
		try {
			dav.sendData(result);
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Semdet einen Datensatz
	 * @param sensor Sensor 
	 * @param datenBeschreibung Datenbeschreibung
	 * @param att Attribut
	 * @param messwert Messwert
	 * @param zeitStempel ZeitStempel
	 */
	public void sendeDatenSatz(SystemObject sensor, DataDescription datenBeschreibung, String att, double messwert, long zeitStempel) {
		
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufds" + att));

		data.getTimeValue("T").setMillis(ZEIT_INTERVALL);
		
		if(messwert>=0) 
			try {
				data.getItem(att).getScaledValue("Wert").set(messwert);
			} catch (IllegalArgumentException e) {
				data.getItem(att).getUnscaledValue("Wert").set(messwert);
			}
		else
			data.getItem(att).getUnscaledValue("Wert").set(messwert);
	
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);			
		
		if(messwert>=0)
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		else
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(1);

		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("G�te").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("G�te").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(sensor, datenBeschreibung, zeitStempel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}
}