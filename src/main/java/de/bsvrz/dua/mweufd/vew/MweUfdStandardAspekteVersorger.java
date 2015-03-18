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

package de.bsvrz.dua.mweufd.vew;

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.StandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Diese Klasse repräsentiert die Versorgung des Moduls Messwertersetzung UFD
 * (innerhalb der SWE Messwertersetzung UFD) mit
 * Standard-Publikationsinformationen (Zuordnung von
 * Objekt-Datenbeschreibung-Kombination zu Standard- Publikationsaspekt).
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class MweUfdStandardAspekteVersorger extends StandardAspekteVersorger {

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException wird weitergereicht
	 */
	public MweUfdStandardAspekteVersorger(final IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		super(verwaltung);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() throws DUAInitialisierungsException {

		this.standardAspekte = new StandardAspekteAdapter(
				new StandardPublikationsZuordnung[] {
						new StandardPublikationsZuordnung(
								"typ.ufdsFahrBahnFeuchte", //$NON-NLS-1$
								"atg.ufdsFahrBahnFeuchte", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsFahrBahnGlätte", //$NON-NLS-1$
								"atg.ufdsFahrBahnGlätte", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsFahrBahnOberFlächenTemperatur", //$NON-NLS-1$
								"atg.ufdsFahrBahnOberFlächenTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsFahrBahnOberFlächenZustand", //$NON-NLS-1$
								"atg.ufdsFahrBahnOberFlächenZustand", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsGefrierTemperatur", //$NON-NLS-1$
								"atg.ufdsGefrierTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsHelligkeit", //$NON-NLS-1$
								"atg.ufdsHelligkeit", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsLuftDruck", //$NON-NLS-1$
								"atg.ufdsLuftDruck", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsLuftTemperatur", //$NON-NLS-1$
								"atg.ufdsLuftTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsNiederschlagsArt", //$NON-NLS-1$
								"atg.ufdsNiederschlagsArt", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsNiederschlagsIntensität", //$NON-NLS-1$
								"atg.ufdsNiederschlagsIntensität", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsNiederschlagsMenge", //$NON-NLS-1$
								"atg.ufdsNiederschlagsMenge", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsRelativeLuftFeuchte", //$NON-NLS-1$
								"atg.ufdsRelativeLuftFeuchte", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsRestSalz", //$NON-NLS-1$
								"atg.ufdsRestSalz", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsSchneeHöhe", //$NON-NLS-1$
								"atg.ufdsSchneeHöhe", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsSichtWeite", //$NON-NLS-1$
								"atg.ufdsSichtWeite", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsTaupunktTemperatur", //$NON-NLS-1$
								"atg.ufdsTaupunktTemperatur", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsTemperaturInTiefe1", //$NON-NLS-1$
								"atg.ufdsTemperaturInTiefe1", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsTemperaturInTiefe2", //$NON-NLS-1$
								"atg.ufdsTemperaturInTiefe2", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsTemperaturInTiefe3", //$NON-NLS-1$
								"atg.ufdsTemperaturInTiefe3", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsWasserFilmDicke", //$NON-NLS-1$
								"atg.ufdsWasserFilmDicke", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsWindRichtung", //$NON-NLS-1$
								"atg.ufdsWindRichtung", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsWindGeschwindigkeitMittelWert", //$NON-NLS-1$
								"atg.ufdsWindGeschwindigkeitMittelWert", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								"typ.ufdsWindGeschwindigkeitSpitzenWert", //$NON-NLS-1$
								"atg.ufdsWindGeschwindigkeitSpitzenWert", //$NON-NLS-1$
								DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
								DUAKonstanten.ASP_MESSWERTERSETZUNG), });

	}

}
