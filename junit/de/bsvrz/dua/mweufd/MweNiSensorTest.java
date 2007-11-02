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
import de.bsvrz.dua.mweufd.ni.MweNiSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweNiSensor zu testen
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class MweNiSensorTest extends MweNiSensor {
	

	/**
	 * Testdaten
	 */
	static double [] prueflingDaten;
	static double [] vorherigeNachbarDaten;
	static double [] nachfolgeneNachbarDaten;
	static double [] ersatzQuerrschnittDaten;
	static double [] wasserFilmDicke;
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
	
	/**
	 * Getesteter Sensor
	 */
	static protected SystemObject zentralSensor;
	/**
	 * Ersatzsensor
	 */
	static protected SystemObject ersatzSensor;
	/**
	 * Vorsensor
	 */
	static protected SystemObject vorSensor;
	/**
	 * Nachsensor
	 */
	static protected SystemObject nachSensor;
	/**
	 * Wasserfilmdickesensor
	 */
	static protected SystemObject wfdSensor;
	/**
	 * Verbindung zum DAV
	 */
	static protected ClientDavInterface dav;
	
	/**
	 * Datenbeschreibung der geschickten daten
	 */
	static protected DataDescription DD_MESSWERTE, DD_WFDMESSWERTE, DD_MESSWERT_ERSETZUNG;
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
	public MweNiSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(dav != null) return;
		
		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);
		
		zentralSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.zentral");
		vorSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.vor");
		nachSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.nach");
		ersatzSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.ersatz");
		wfdSensor = dav.getDataModel().getObject("ufdSensor.testNI.wfd");
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		DD_WFDMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		
		DD_MESSWERT_ERSETZUNG =  new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				dav.getDataModel().getAspect("asp.parameterVorgabe"));
		
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		list.add(zentralSensor);
		list.add(ersatzSensor);
		list.add(nachSensor);
		list.add(vorSensor);

		sender.anmeldeQuelle(list, DD_MESSWERTE);
		sender.anmeldeParametrierung(zentralSensor);
	
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
		if(indexSend>= ersatzQuerrschnittDaten.length) return false;
		
		sender.sendeDatenSatz(zentralSensor, DD_MESSWERTE, "NiederschlagsIntensität", prueflingDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(vorSensor, DD_MESSWERTE, "NiederschlagsIntensität", vorherigeNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(nachSensor, DD_MESSWERTE, "NiederschlagsIntensität", nachfolgeneNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(ersatzSensor, DD_MESSWERTE, "NiederschlagsIntensität", ersatzQuerrschnittDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(wfdSensor, DD_WFDMESSWERTE, "WasserFilmDicke", wasserFilmDicke[indexSend], time[indexSend]);
		
		indexSend++;
		return true;
	}
	
	/**
	 * Generiert die Testdaten nach der Pruefspezifikation
	 * @param t1 Messwertfortsetzungsintervall
	 * @param tE Messwertersetzungsintervall
	 * @param T Periode
	 */
	static public void generiereTestDatenNachPruefSpezNI_1(long t1, long tE, long T) {
		
		double w1 = 2.0;
		double w2 = 1.5;
		double w3 = 5.5;
		double w4 = 0.5;
		double wfd = 1.8;
		
		ZEIT_INTERVALL = T;
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		vorherigeNachbarDaten = new double [length];
		nachfolgeneNachbarDaten  = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		wasserFilmDicke  = new double [length];
		
		time = new long [length];
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Intervalle
		long t[] = new long [9];
		long t_int = ( tE - t1 ) / 7;
		t[0] = T;
		t[1] = t[0] + t1;
		
		for(int i=2; i<9; i++)
			t[i] = t[i-1] + t_int;
	
		
		// Ersatzquerrschnittdaten
		for(int i=0; i<length; i++)
			if(time[i]>=t[6] && time[i]<t[7])
				ersatzQuerrschnittDaten[i] = -1;
			else
				ersatzQuerrschnittDaten[i] = w4;
		
		// Nachbar Sensor
		for(int i=0; i<length; i++)
			if(time[i]<t[2])
				vorherigeNachbarDaten[i] = w2;
			else if(time[i]<t[5])
				vorherigeNachbarDaten[i] = w3;
			else
				vorherigeNachbarDaten[i] = -1;
				
		// Pruefling
		for(int i=0; i<length; i++)
			if(time[i]<t[0])
				prueflingDaten[i] = w1;
			else prueflingDaten[i] = -1;
		
		// Nachbar Sensor
		for(int i=0; i<length; i++)
			if(time[i]<t[3])
				nachfolgeneNachbarDaten[i] = w3;
			else nachfolgeneNachbarDaten[i] = 0.0;
		
		// WFD
		for(int i=0; i<length; i++)
			if(time[i] < t[4])
				wasserFilmDicke[i] = wfd;
			else wasserFilmDicke[i] = -1;
		
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
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i])/2.0;
			else if(time[i] >= t[2] && time[i] < t[3])
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i])/2.0;
			else if(time[i] >= t[3] && time[i] < t[4])
				ersetzteAusgabeDaten[i] = -1;
			else if(time[i] >= t[4] && time[i] < t[5])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else if(time[i] >= t[5] && time[i] < t[6])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else if(time[i] >= t[6] && time[i] < t[7])
				ersetzteAusgabeDaten[i] = -1;
			else if(time[i] >= t[7] && time[i] < t[8])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else ersetzteAusgabeDaten[i] = -1;
				
		System.out.print(' ');
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void publiziere(final ResultData original,
									final Data nutzDatum){
		boolean publiziereDatensatz = false;
		
		if(nutzDatum == null){
			/**
			 * "keine Daten" wird nur publiziert, wenn das Objekt vorher
			 * nicht auch schon auf keine Daten stand
			 */
			if(this.letztesPubDatum != null && this.letztesPubDatum.getData() != null){
				publiziereDatensatz = true;
			}
		}else{
			publiziereDatensatz = true;
		}
		
		if(publiziereDatensatz){
			if(!original.getObject().getPid().equals(zentralSensor.getPid()) ) return;
			double ni = nutzDatum.getItem("NiederschlagsIntensität").getItem("Wert").asUnscaledValue().doubleValue();
			if(ni>=0) ni = nutzDatum.getItem("NiederschlagsIntensität").getItem("Wert").asScaledValue().doubleValue();
			else ni = -1.0;
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[indexEmpf] + " Berechnetes datum: " + ni + " index " + (indexEmpf),ersetzteAusgabeDaten[indexEmpf] == ni);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", indexEmpf, ersetzteAusgabeDaten[indexEmpf], ni));
			indexEmpf++;
			synchronized (VERWALTUNG) {
				if(indexEmpf >= ersetzteAusgabeDaten.length) MweNiSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}
}
