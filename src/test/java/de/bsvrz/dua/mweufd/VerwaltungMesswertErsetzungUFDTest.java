/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
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
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */
package de.bsvrz.dua.mweufd;

import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Ermoeglicht die mit hilfe der Klasse VerwaltungMesswertErsetzungUFD die
 * einzelne *Sensor Klassen zu testen.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class VerwaltungMesswertErsetzungUFDTest extends VerwaltungMesswertErsetzungUFD {

	@Override
	protected void initialisiere() throws DUAInitialisierungsException {

		super.initialisiere();

		for (final DUAUmfeldDatenMessStelle messStelle : DUAUmfeldDatenMessStelle.getInstanzen()) {
			try {
				final DUAUmfeldDatenSensor hauptSensorNI = messStelle.getHauptSensor(UmfeldDatenArt.ni);
				final DUAUmfeldDatenSensor hauptSensorNS = messStelle.getHauptSensor(UmfeldDatenArt.ns);
				final DUAUmfeldDatenSensor hauptSensorFBZ = messStelle.getHauptSensor(UmfeldDatenArt.fbz);
				final DUAUmfeldDatenSensor hauptSensorWFD = messStelle.getHauptSensor(UmfeldDatenArt.wfd);
				final DUAUmfeldDatenSensor hauptSensorSW = messStelle.getHauptSensor(UmfeldDatenArt.sw);
				final DUAUmfeldDatenSensor hauptSensorTPT = messStelle.getHauptSensor(UmfeldDatenArt.tpt);
				final DUAUmfeldDatenSensor hauptSensorLT = messStelle.getHauptSensor(UmfeldDatenArt.lt);
				final DUAUmfeldDatenSensor hauptSensorFBT = messStelle.getHauptSensor(UmfeldDatenArt.fbt);

				if (hauptSensorNI != null) {

					new MweNiSensorTest(this, messStelle, hauptSensorNI);

				}
				if (hauptSensorNS != null) {
					new MweTptLtNsFbzSensorTest(this, messStelle, hauptSensorNS);
				}
				if (hauptSensorFBZ != null) {
					new MweTptLtNsFbzSensorTest(this, messStelle, hauptSensorFBZ);
				}
				if (hauptSensorLT != null) {
					new MweTptLtNsFbzSensorTest(this, messStelle, hauptSensorLT);
				}
				if (hauptSensorTPT != null) {
					new MweTptLtNsFbzSensorTest(this, messStelle, hauptSensorTPT);
				}
				if (hauptSensorWFD != null) {
					new MweWfdSensorTest(this, messStelle, hauptSensorWFD);
				}
				if (hauptSensorSW != null) {
					new MweSwSensorTest(this, messStelle, hauptSensorSW);
				}
				if (hauptSensorFBT != null) {
					new MweFbtSensorTest(this, messStelle, hauptSensorFBT);
				}
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Verbindung trennen.
	 */
	public void disconnect() {
		this.verbindung.disconnect(false, "");
	}
}
