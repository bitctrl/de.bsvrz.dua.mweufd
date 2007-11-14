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
 * Testet die MweNiSensor Klasse
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class MweNiSensorJunitTester {
		
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
	 */
	@Test
	public void test1() {		
		final long MIN_IN_MS = 1000 * 60;		
		final long S_IN_MS = 1000;
		
		final long messwertErsetzungMax = 120*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
		
		MweNiSensorTest.generiereTestDatenNachPruefSpezNI_1(messwertFortFuehrungMax,messwertErsetzungMax , periode);
		DatenFlussSteuerungVersorgerTest.reset();
		DUAUmfeldDatenSensorTest.reset();
		MweUfdSensorTest.reset();
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		MweNiSensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax, periode);
		
		while(MweNiSensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
		((VerwaltungMesswertErsetzungUFDTest)verw).disconnect();
		Mwe_Tpt_Lt_Ns_Fbz_SensorTest.reset();
		
		try { Thread.sleep(1000); } catch (Exception e) { }
	}
}
