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
import de.bsvrz.dua.mweufd.fbt.MweFbtSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweFbtSensor zu Testen
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class MweFbtSensorTest extends MweFbtSensor {


	/**
	 * Testdaten
	 */
	static double [] prueflingDaten;
	static double [] ersatzQuerrschnittDaten;	
	static double [] ersetzteAusgabeDaten;
	static long   [] time;
	
	/**
	 * Periode der Datensendung
	 */
	static long ZEIT_INTERVALL;	
	
	/**
	 * Letzter index der gesendeten und empfangenen Daten
	 */
	static protected int indexEmpf = 0;
	static protected int indexSend = 0;
	static protected boolean initialisiert = false;
	
	/**
	 * Getesteter Sensor
	 */
	static protected SystemObject zentralSensor;
	/**
	 * Ersatzsensor
	 */
	static protected SystemObject ersatzSensor;
	/**
	 * Niederschlagintensitaetsensor
	 */
	static protected SystemObject niSensor;
	/**
	 * Wasserfilmdickesensor
	 */
	static protected SystemObject wfdSensor;
	
	/**
	 * Der Wert der NI und WFD Sensore ist Konstant
	 */
	static double niDaten = 0.0;
	static double wfdDaten = 0.0;
	
	/**
	 * Verbindung zum DAV
	 */
	static protected ClientDavInterface dav;
	
	/**
	 * Datenbeschreibung der geschickten daten
	 */
	static protected DataDescription DD_MESSWERTE, DD_WFDMESSWERTE, DD_NIMESSWERTE, DD_MESSWERT_ERSETZUNG;
	/**
	 * Datensender
	 */
	static protected  MweTestDatenSender sender;


	/**
	 * Standardkonstruktor
	 * @param verwaltung 
	 * @param messStelle
	 * @param sensor
	 * @throws DUAInitialisierungsException 
	 */
	public MweFbtSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(initialisiert) return;
		
		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);
		
		zentralSensor = dav.getDataModel().getObject("ufdSensor.testFBT.fbt.zentral");
		ersatzSensor = dav.getDataModel().getObject("ufdSensor.testFBT.fbt.ersatz");
		
		niSensor = dav.getDataModel().getObject("ufdSensor.testFBT.ni");
		wfdSensor = dav.getDataModel().getObject("ufdSensor.testFBT.wfd");
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsFahrBahnOberFlächenTemperatur"),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	 	
		DD_WFDMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		
		DD_NIMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	
		
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		
		list.add(zentralSensor);
		list.add(ersatzSensor);

		sender.anmeldeQuelle(list, DD_MESSWERTE);
		sender.anmeldeParametrierung(zentralSensor);
	
		sender.anmeldeQuelle(niSensor, DD_NIMESSWERTE);
		sender.anmeldeQuelle(wfdSensor, DD_WFDMESSWERTE);

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
	 * Sendet die Daten des naechsten Schrittes
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst false
	 */
	static public boolean naechsterCyklus() {
		if(indexSend>= ersetzteAusgabeDaten.length) return false;
		
		sender.sendeDatenSatz(zentralSensor, DD_MESSWERTE, "FahrBahnOberFlächenTemperatur", prueflingDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(ersatzSensor, DD_MESSWERTE, "FahrBahnOberFlächenTemperatur", ersatzQuerrschnittDaten[indexSend], time[indexSend]);	
		sender.sendeDatenSatz(niSensor, DD_NIMESSWERTE, "NiederschlagsIntensität", niDaten, time[indexSend]);
		sender.sendeDatenSatz(wfdSensor, DD_WFDMESSWERTE, "WasserFilmDicke", wfdDaten, time[indexSend]);

		indexSend++;
		return true;
	}
	
	/**
	 * Generiert die Testdaten nach der Pruefspezifikation
	 * @param t1 Messwertfortsetzungsintervall
	 * @param tE Messwertersetzungsintervall
	 * @param T Periode
	 */
	static public void generiereTestDatenNachPruefSpez_1(long t1, long tE, long T) {
		
		double w1 = 4.0;
		double w2 = 1.2;
		double w3 = 0.4;
		
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		
		ZEIT_INTERVALL = T;
		time = new long [length];
		time[0] = 0;
		
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
			if(!original.getObject().getPid().equals(zentralSensor.getPid()) ) return;
			
			double sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur").getItem("Wert").asUnscaledValue().doubleValue();
			if(sw>=0) sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur").getItem("Wert").asScaledValue().doubleValue();
			else sw = -1;
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[indexEmpf] + " Berechnetes datum: " + sw + " index " + (indexEmpf), Math.abs(ersetzteAusgabeDaten[indexEmpf]- sw)<0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", indexEmpf, ersetzteAusgabeDaten[indexEmpf], sw));
			indexEmpf++;
			synchronized (VERWALTUNG) {
				if(indexEmpf >= ersetzteAusgabeDaten.length) MweFbtSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}
}
