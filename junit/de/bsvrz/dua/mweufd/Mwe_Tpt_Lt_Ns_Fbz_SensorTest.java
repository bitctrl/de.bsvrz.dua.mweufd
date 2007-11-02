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
import java.util.Date;
import java.util.LinkedList;

import junit.framework.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
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


	static double [] prueflingDaten;
	static double [] ersatzQuerrschnittDaten;	
	static double [] ersetzteAusgabeDaten;
	static long   [] time;
	
	static int index = 0;
	static int indexSend = 0;
	
	
	static protected boolean initialisiert = false;
	
	static protected SystemObject zentralSensor;
	static protected SystemObject ersatzSensor;
	
	static protected ClientDavInterface dav;
	static protected DataDescription DD_MESSWERTE;
	
	static  String zentralSensorName = "ufdSensor.testNS.ns.zentral";
	static  String ersatzSensorName = "ufdSensor.testNS.ns.ersatz";
	static  String attribut = "NiederschlagsArt";
	
	static MweTestDatenSender sender;
	
	static double w1 = 4;
	static double w2 = 1;
	static double w3 = 2;
	
	static public void setTestWerte(double w1, double w2, double w3) {
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.w1 = w1;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.w2 = w2;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.w3 = w3;
	}
	
	static public void setSensorUndAttribut(String zentralSensor, String ersatzSensor, String attribut) {
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.zentralSensorName = zentralSensor;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.ersatzSensorName = ersatzSensor;
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.attribut = attribut;
	}
	
	public Mwe_Tpt_Lt_Ns_Fbz_SensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(initialisiert) return;
		
		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);
		
		zentralSensor = dav.getDataModel().getObject(zentralSensorName);
		ersatzSensor = dav.getDataModel().getObject(ersatzSensorName);
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufds" + attribut),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	 	
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		
		list.add(zentralSensor);
		list.add(ersatzSensor);
		
		sender.anmeldeQuelle(list, DD_MESSWERTE);
		sender.anmeldeParametrierung(zentralSensor);
		
		initialisiert = true;
	}
	
	static public boolean naechsterCyklus() {
		if(indexSend>= ersetzteAusgabeDaten.length) return false;
		
		sender.sendeDatenSatz(zentralSensor, DD_MESSWERTE, attribut, prueflingDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(ersatzSensor, DD_MESSWERTE, attribut, ersatzQuerrschnittDaten[indexSend], time[indexSend]);

		indexSend++;
		return true;
	}
	
	
	public static void parametriereSensor(long messwertFortschreibungsIntervall, long messWertErsetzungIntervall, long periode) {
		sender.parametriereSensor(zentralSensor, messwertFortschreibungsIntervall, messWertErsetzungIntervall, periode);
	}

	static public void generiereTestDatenNachPruefSpez_1(long t1, long tE, long T) {
		
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		
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
			
			double sw = nutzDatum.getItem(attribut).getItem("Wert").asUnscaledValue().doubleValue();
			if(sw>=0) sw = nutzDatum.getItem(attribut).getItem("Wert").asScaledValue().doubleValue();
			else sw = -1;
			
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[index] + " Berechnetes datum: " + sw + " index " + (index), Math.abs(ersetzteAusgabeDaten[index]- sw)<0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", index, ersetzteAusgabeDaten[index], sw));
			index++;
			synchronized (VERWALTUNG) {
				if(index >= ersetzteAusgabeDaten.length) MweWfdSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}

}
