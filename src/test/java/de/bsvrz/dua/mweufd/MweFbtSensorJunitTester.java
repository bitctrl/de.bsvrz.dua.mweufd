/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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
 * Testet die MweFbtSensor Klasse.
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 * @version $Id$
 */
public class MweFbtSensorJunitTester {

	/**
	 * warten?
	 */
	public static boolean warten = true;

	/**
	 * Der Test.
	 */
	@Test
	public void test1() {
		final long messwertErsetzungMax = 120 * Constants.MILLIS_PER_MINUTE;
		final long messwertFortFuehrungMax = 3 * Constants.MILLIS_PER_MINUTE;
		final long periode = 30 * Constants.MILLIS_PER_SECOND;

		MweFbtSensorTest.generiereTestDatenNachPruefSpez1(
				messwertFortFuehrungMax, messwertErsetzungMax, periode);
		DatenFlussSteuerungVersorgerTest.reset();
		DUAUmfeldDatenSensorTest.reset();
		MweUfdSensorTest.reset();

		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, Verbindung.getConData());

		MweFbtSensorTest.parametriereSensor(messwertFortFuehrungMax,
				messwertErsetzungMax, periode);

		while (MweFbtSensorTest.naechsterCyklus()) {
			try {
				Thread.sleep(200);
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
}