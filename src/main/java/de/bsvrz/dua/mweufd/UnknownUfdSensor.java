/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Messwertersetzung UFD
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.mweufd.
 * 
 * de.bsvrz.dua.mweufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.mweufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.mweufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.mweufd;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Implementierung für unbekannte Sensoren. Alle Daten werden einfach weitergeleitet.
 *
 * @author Kappich Systemberatung
 */
public class UnknownUfdSensor extends AbstractMweSensor {

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param messStelle die Umfelddatenmessstelle, die in Bezug auf einen bestimmten Hauptsensor messwertersetzt werden soll (muss <code> != null</code> sein)
	 * @param sensor     der Umfelddatensensor der messwertersetzt werden soll (muss <code> != null</code> sein)
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public UnknownUfdSensor(final IVerwaltung verwaltung, final DUAUmfeldDatenMessStelle messStelle, final DUAUmfeldDatenSensor sensor) throws UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);
	}

	@Override
	protected void messwertErsetzung() {
		// Keine MWE
	}

	@Override
	public void aktualisiereDaten(final ResultData resultat) {
		// Daten unverändert weitersenden
		publiziere(resultat, resultat.getData());
	}
}
