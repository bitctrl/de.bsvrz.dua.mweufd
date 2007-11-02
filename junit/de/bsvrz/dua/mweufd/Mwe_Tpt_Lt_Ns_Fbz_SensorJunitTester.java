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
	
	@Test
	public void test1() {		
		final long MIN_IN_MS = 1000 * 60;
		final long S_IN_MS = 1000;
		
		
		final long messwertErsetzungMax = 20*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
			
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setSensorUndAttribut("ufdSensor.testNS.ns.zentral", "ufdSensor.testNS.ns.ersatz", "NiederschlagsArt");
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setTestWerte(1.0, 2.0, 3.0);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.generiereTestDatenNachPruefSpez_1(messwertFortFuehrungMax, messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax, periode);
		
		while(Mwe_Tpt_Lt_Ns_Fbz_SensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
	}	
	
	public void test2() {		
		final long MIN_IN_MS = 1000 * 60;
		final long S_IN_MS = 1000;
		
		
		final long messwertErsetzungMax = 20*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
			
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setSensorUndAttribut("ufdSensor.testNS.ns.zentral", "ufdSensor.testNS.ns.ersatz", "NiederschlagsArt");
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setTestWerte(1.0, 2.0, 3.0);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.generiereTestDatenNachPruefSpez_1(messwertFortFuehrungMax, messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax, periode);
		
		while(Mwe_Tpt_Lt_Ns_Fbz_SensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
	}	
	
	public void test3() {		
		final long MIN_IN_MS = 1000 * 60;
		final long S_IN_MS = 1000;
		
		
		final long messwertErsetzungMax = 20*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
			
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setSensorUndAttribut("ufdSensor.testNS.ns.zentral", "ufdSensor.testNS.ns.ersatz", "NiederschlagsArt");
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setTestWerte(1.0, 2.0, 3.0);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.generiereTestDatenNachPruefSpez_1(messwertFortFuehrungMax, messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax, periode);
		
		while(Mwe_Tpt_Lt_Ns_Fbz_SensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
	}	
	
	public void test4() {		
		final long MIN_IN_MS = 1000 * 60;
		final long S_IN_MS = 1000;
		
		
		final long messwertErsetzungMax = 20*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
			
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setSensorUndAttribut("ufdSensor.testNS.ns.zentral", "ufdSensor.testNS.ns.ersatz", "NiederschlagsArt");
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.setTestWerte(1.0, 2.0, 3.0);
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.generiereTestDatenNachPruefSpez_1(messwertFortFuehrungMax, messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax, periode);
		
		while(Mwe_Tpt_Lt_Ns_Fbz_SensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
	}	
}
