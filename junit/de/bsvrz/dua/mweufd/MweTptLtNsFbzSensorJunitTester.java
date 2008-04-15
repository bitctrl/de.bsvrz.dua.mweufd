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

import org.junit.Test;

import com.bitctrl.Constants;

import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;

/**
 * Testet die Mwe_Tpt_Lt_Ns_Fbz_Sensor Klasse.
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 * @version $Id$
 */
public class MweTptLtNsFbzSensorJunitTester {

	/**
	 * soll gewartet werden?
	 */
	public static boolean warten = true;

	/**
	 * Der Test.
	 * 
	 * @param sensorName
	 *            Name des zentralen Sensors
	 * @param ersatzSensorName
	 *            Name des Ersatzsensors
	 * @param attributName
	 *            Name des getesteten Attributes
	 * @param w1
	 *            Wert w1
	 * @param w2
	 *            Wert w2
	 * @param w3
	 *            Wert w3
	 */
	public void test(String sensorName, String ersatzSensorName,
			String attributName, double w1, double w2, double w3) {

		final long messwertErsetzungMax = 120 * Constants.MILLIS_PER_MINUTE;
		final long messwertFortFuehrungMax = 3 * Constants.MILLIS_PER_MINUTE;
		final long periode = 30 * Constants.MILLIS_PER_SECOND;

		MweTptLtNsFbzSensorTest.setInititalisiert(false);
		MweTptLtNsFbzSensorTest.setSensorUndAttribut(sensorName,
				ersatzSensorName, attributName);
		MweTptLtNsFbzSensorTest.setTestWerte(w1, w2, w3);
		MweTptLtNsFbzSensorTest.generiereTestDatenNachPruefSpez1(
				messwertFortFuehrungMax, messwertErsetzungMax, periode);
		DatenFlussSteuerungVersorgerTest.reset();
		DUAUmfeldDatenSensorTest.reset();
		MweUfdSensorTest.reset();

		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();

		String[] connData = new String[Verbindung.getConData().length];
		for (int i = 0; i < connData.length; i++) {
			connData[i] = new String(Verbindung.getConData()[i]);
		}

		StandardApplicationRunner.run(verw, connData);

		MweTptLtNsFbzSensorTest.parametriereSensor(
				messwertFortFuehrungMax, messwertErsetzungMax, periode);

		warten = true;
		while (MweTptLtNsFbzSensorTest.naechsterZyklus()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				//
			}
		}
		synchronized (verw) {
			try {
				while (warten) {
					verw.wait();
				}
			} catch (Exception e) {
				//
			}
		}
		((VerwaltungMesswertErsetzungUFDTest) verw).disconnect();
		MweTptLtNsFbzSensorTest.reset();

		try {
			Thread.sleep(500);
		} catch (Exception e) {
			//
		}
	}

	@Test
	public void test1() {
		test("ufdSensor.testFBZ.fbz.zentral", "ufdSensor.testFBZ.fbz.ersatz",
				"FahrBahnOberFl�chenZustand", 1.0, 32.0, 64.0);
	}

	@Test
	public void test2() {
		test("ufdSensor.testNS.ns.zentral", "ufdSensor.testNS.ns.ersatz",
				"NiederschlagsArt", 1.0, 2.0, 3.0);
	}

	@Test
	public void test3() {
		test("ufdSensor.testTPT.tpt.zentral", "ufdSensor.testTPT.tpt.ersatz",
				"TaupunktTemperatur", 0.1, 1.2, 2.4);
	}

	@Test
	public void test4() {
		test("ufdSensor.testLT.lt.zentral", "ufdSensor.testLT.lt.ersatz",
				"LuftTemperatur", 0.1, 1.2, 2.4);
	}
	
}