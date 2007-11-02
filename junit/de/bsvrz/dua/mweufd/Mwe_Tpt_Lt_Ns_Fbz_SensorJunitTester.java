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

import org.junit.Test;

import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;

/**
 * Testet die Mwe_Tpt_Lt_Ns_Fbz_Sensor Klasse
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class Mwe_Tpt_Lt_Ns_Fbz_SensorJunitTester {
	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083",  
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd", 
			"-debugLevelStdErrText=WARNING", 
			"-debugLevelFileText=WARNING",
			"-KonfigurationsBereichsPid=kb.mweUfdTestModell" }; 

	public static boolean warten = true;
		
	/**
	 * Der Test
	 * @param sensorName	 Name des zentralen Sensors
	 * @param ersatzSensorName Name des Ersatzsensors
	 * @param attributName Name des getesteten Attributes
	 * @param w1 Wert w1
	 * @param w2 Wert w2
	 * @param w3 Wert w3
	 */
	public void test(String sensorName, String ersatzSensorName, String attributName, double w1, double w2, double w3) {		
		final long MIN_IN_MS = 1000 * 60;
		final long S_IN_MS = 1000;
		
		final long messwertErsetzungMax = 20*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
		
		System.out.println("###\nGetestete Sensor: " + sensorName + " Attribut: " + attributName + "\n###");

		
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setInititalisiert(false);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setSensorUndAttribut(sensorName, ersatzSensorName, attributName);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setTestWerte(w1, w2, w3);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.generiereTestDatenNachPruefSpez_1(messwertFortFuehrungMax, messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		
		String [] conn_data = new String [CON_DATA.length];
		for(int i=0; i<conn_data.length; i++)
			conn_data[i] = new String(CON_DATA[i]);
		
		StandardApplicationRunner.run(verw, conn_data);
		
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax, periode);
		
		while(Mwe_Tpt_Lt_Ns_Fbz_SensorTest.naechsterZyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
		((VerwaltungMesswertErsetzungUFDTest)verw).disconnect();
	}	
	
	@Test
	public void test1() {
		test("ufdSensor.testFBZ.fbz.zentral", "ufdSensor.testFBZ.fbz.ersatz", "FahrBahnOberFlächenZustand", 1.0, 32.0, 64.0);
	}

	public void test2() {
		test("ufdSensor.testNS.ns.zentral", "ufdSensor.testNS.ns.ersatz", "NiederschlagsArt", 1.0, 2.0, 3.0);
	}
	
	public void test3() {
		test("ufdSensor.testTPT.tpt.zentral", "ufdSensor.testTPT.tpt.ersatz", "TaupunktTemperatur", 0.1, 1.2, 2.4);
	}
	
	public void test4() {
		test("ufdSensor.testLT.lt.zentral", "ufdSensor.testLT.lt.ersatz", "LuftTemperatur", 0.1, 1.2, 2.4);
	}
}
