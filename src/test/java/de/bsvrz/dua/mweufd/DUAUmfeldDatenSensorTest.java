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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Wegen Test-zwecken muss die Klasse DUAUmfeldDatenSensor neuinitialisiert
 * werden reset() ermoeglicht die Singleton-Instanzen zu loeschen.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class DUAUmfeldDatenSensorTest extends DUAUmfeldDatenSensor {

	/**
	 * Standardkonstruktor.
	 *
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param objekt
	 *            das Systemobjekt des Umfelddatensensors
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	protected DUAUmfeldDatenSensorTest(final ClientDavInterface dav,
			final SystemObject objekt) throws UmfeldDatenSensorUnbekannteDatenartException {
		super(dav, objekt);
	}

	/**
	 * Ermoeglicht dass die Klasse neu initialisiert wird.
	 */
	public static void reset() {
		resetCache();
	}
}
