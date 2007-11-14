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

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.dua.mweufd.MweTestDatenSender;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse Mwe_Tpt_Lt_Ns_Fbz_Sensor zu Testen
 * Generiert Testdaten und vergleicht mit den 
 * Ausgabedaten der originalen Klasse 
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class Mwe_Tpt_Lt_Ns_Fbz_SensorTest extends Mwe_Tpt_Lt_Ns_Fbz_Sensor {

	/**
	 * Testdaten
	 */
	static protected double [] prueflingDaten;
	static protected double [] ersatzQuerrschnittDaten;	
	static protected double [] ersetzteAusgabeDaten;
	static protected long   [] time;
	
	/**
	 * Letzter index der gesendeten und empfangenen Daten
	 */
	static protected int indexEmpf = 0;
	static protected int indexSend = 0;
	
	/**
	 * <code>true</code>, wenn alle Daten initialisiert sind 
	 */
	static protected boolean initialisiert = false;
	
	/**
	 * Hauptsensor
	 */
	static protected SystemObject zentralSensor;
	/**
	 * Ersatzsensor
	 */
	static protected SystemObject ersatzSensor;
	
	/**
	 * Verbindung zum dav
	 */
	static protected ClientDavInterface dav;
	/**
	 * Datenbeschreibung der Messwert-Attributgruppe
	 */
	static protected DataDescription DD_MESSWERTE;
	
	/**
	 * Die Sensore und getestete Attribute
	 */
	static protected String zentralSensorName = "ufdSensor.testFBZ.fbz.zentral";
	static protected String ersatzSensorName ="ufdSensor.testFBZ.fbz.ersatz";
	static protected String attribut = "FahrBahnOberFlächenZustand";
	/**
	 * Datensender
	 */
	static protected  MweTestDatenSender sender;
	
	/**
	 * Die Testwerte
	 */
	static protected double w1 = 4;
	static protected double w2 = 1;
	static protected double w3 = 2;
	
	/**
	 * Setzt die Testwerte
	 * @param w1 W1
	 * @param w2 W2
	 * @param w3 W3
	 */
	static public void setTestWerte(double w1, double w2, double w3) {
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.w1 = w1;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.w2 = w2;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.w3 = w3;
	}
	
	/**
	 * Setzt die Namen des Des Sensors und seiner Attribute
	 * @param zentralSensor Hauptsensor 
	 * @param ersatzSensor Ersatzsensor
	 * @param attribut Getesteter Attribut
	 */
	static public void setSensorUndAttribut(String zentralSensor, String ersatzSensor, String attribut) {
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.zentralSensorName = zentralSensor;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.ersatzSensorName = ersatzSensor;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.attribut = attribut;
	}
	/**
	 * Setzt den Flag initialsisiert
	 * @param init true, wenn schon initialisiert ist
	 */
	static public void setInititalisiert(boolean init) {
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.initialisiert = init;
	}
	
	/**
	 * Initialisiert die Testklasse ins Ausgangzustand
	 */
	public static void reset() {
		setSensorUndAttribut(null, null, null);
		initialisiert = false;
		zentralSensor = null;
		ersatzSensor = null;
	}
	
	/**
	 * Standardkonstruktor
	 * @param verwaltung 
	 * @param messStelle
	 * @param sensor
	 * @throws DUAInitialisierungsException 
	 */
	public Mwe_Tpt_Lt_Ns_Fbz_SensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(initialisiert || attribut == null) return;
		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufds" + attribut),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		
		zentralSensor = dav.getDataModel().getObject(zentralSensorName);
		ersatzSensor = dav.getDataModel().getObject(ersatzSensorName);
		

		Collection<SystemObject> list = new LinkedList<SystemObject>();
		
		list.add(zentralSensor);
		list.add(ersatzSensor);
		
		sender.anmeldeQuelle(list, DD_MESSWERTE);
		sender.anmeldeParametrierung(zentralSensor);
		
		indexSend = 0;
		initialisiert = true;
	}
	
	/**
	 * Sendet die Daten des naechsten Schrittes
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst false
	 */
	static public boolean naechsterZyklus() {
		if(indexSend>= ersetzteAusgabeDaten.length) return false;
		
		sender.sendeDatenSatz(zentralSensor, DD_MESSWERTE, attribut, prueflingDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(ersatzSensor, DD_MESSWERTE, attribut, ersatzQuerrschnittDaten[indexSend], time[indexSend]);

		indexSend++;
		return true;
	}
	
	/**
	 * Parametreirt den gestesteten Sensor
	 * @param messwertFortschreibungsIntervall Maximaler MesswertFortschreibungsIntervall
	 * @param messWertErsetzungIntervall Maximaler MessWertErsetzungIntervall 
	 * @param periode Elementares Schritt
	 */
	public static void parametriereSensor(long messwertFortschreibungsIntervall, long messWertErsetzungIntervall, long periode) {
		sender.parametriereSensor(zentralSensor, messwertFortschreibungsIntervall, messWertErsetzungIntervall, periode);
	}

	/**
	 * Generiert die Testdaten nach der Pruefspezifikation
	 * @param t1 Messwertfortsetzungsintervall
	 * @param tE Messwertersetzungsintervall
	 * @param T Periode
	 */
	static public void generiereTestDatenNachPruefSpez_1(long t1, long tE, long T) {
		
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		
		time = new long [length];
		time[0] = 0;
		indexEmpf = indexSend = 0;
		
		long t[] = new long [5];
		long t_int = ( tE - t1) / 3;
		t[0] = T;
		t[1] = t[0] + t1;
		t[2] = t[1] + t_int;
		t[3] = t[2] + t_int;
		t[4] = t[3] + t_int;
		
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Ersatzquerrschnittdaten
		for(int i=0; i<length; i++)
			if(time[i]<t[2])
				ersatzQuerrschnittDaten[i] = w2;
			else if(time[i]>=t[2] && time[i]<t[3])
				ersatzQuerrschnittDaten[i] = -1;
			else 
				ersatzQuerrschnittDaten[i] = w3;
		
				
		// Pruefling
		for(int i=0; i<length; i++)
			if(time[i]<t[0])
				prueflingDaten[i] = w1;
			else prueflingDaten[i] = -1;

		
		// Ausgabewerte
		double letzterWert = w1;
		for(int i=0; i<length; i++)
			if(time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
				letzterWert = prueflingDaten[i];
			}
			else if(time[i] >= t[0] && time[i] < t[1])
				ersetzteAusgabeDaten[i] = letzterWert;
			else if(time[i] >= t[1] && time[i] < t[2])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else if(time[i] >= t[2] && time[i] < t[3])
				ersetzteAusgabeDaten[i] = -1;
			else if(time[i] >= t[3] && time[i] < t[4])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else ersetzteAusgabeDaten[i] = -1;
				
			
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void publiziere(final ResultData original,
									final Data nutzDatum){
		boolean publiziereDatensatz = false;
		
		if(nutzDatum == null){
			if(this.letztesPubDatum != null && this.letztesPubDatum.getData() != null){
				publiziereDatensatz = true;
			}
		}else{
			publiziereDatensatz = true;
		}
		
		if(publiziereDatensatz){
			
			if( attribut == null || nutzDatum == null) return;
			if(!original.getObject().getPid().equals(zentralSensor.getPid()) ) return;
			
			double sw = nutzDatum.getItem(attribut).getItem("Wert").asUnscaledValue().doubleValue();
			if(sw>=0) {
				try {
					sw = nutzDatum.getItem(attribut).getItem("Wert").asScaledValue().doubleValue();
				} catch (ArithmeticException e) {	}
			}
			else sw = -1;
			
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[indexEmpf] + " Berechnetes datum: " + sw + " index " + (indexEmpf), Math.abs(ersetzteAusgabeDaten[indexEmpf]- sw)<0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", indexEmpf, ersetzteAusgabeDaten[indexEmpf], sw));
			if(++indexEmpf >= ersetzteAusgabeDaten.length)
				synchronized (VERWALTUNG) {
					Mwe_Tpt_Lt_Ns_Fbz_SensorJunitTester.warten = false;
					VERWALTUNG.notifyAll();
				}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}
}
