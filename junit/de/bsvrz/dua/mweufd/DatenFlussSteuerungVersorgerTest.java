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

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DatenFlussSteuerungsVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 *  Wegen Test-zwecken muss die Klasse DatenFlussSteuerungVersorger neuinitialisiert werden
 *  reset() ermoeglicht die Singleton-Instanz zu loeschen
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class DatenFlussSteuerungVersorgerTest extends
		DatenFlussSteuerungsVersorger {
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param dfsObjekt
	 *            das des Objektes, das die Datenflusssteuerung für das
	 *            übergebene Verwaltungsmodul beschreibt
	 * @throws DUAInitialisierungsException
	 *             wird geworfen, wenn die übergebene Verbindung fehlerhaft ist
	 *             (nicht die geforderten Informationen bereit hält), bzw. keine
	 *             Datenanmeldungen durchgeführt werden konnten
	 */
	private DatenFlussSteuerungVersorgerTest(final IVerwaltung verwaltung,
			final SystemObject dfsObjekt) throws DUAInitialisierungsException {
		super(verwaltung, dfsObjekt);
	}

	/**
	 * Ermoeglicht dass die Klasse neu initialisiert wird
	 */
	public static void reset() {
		instanz = null;
	}
}
