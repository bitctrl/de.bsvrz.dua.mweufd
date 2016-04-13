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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVObjektAnmeldung;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVSendeAnmeldungsVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DFSKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DatenFlussSteuerungsVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerungFuerModul;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerungsListener;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Aktuelle Datenflusssteuerung der Messwertersetzung UFD.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MweDatenFlussSteuerung implements IDatenFlussSteuerungsListener {

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	protected static IVerwaltung dieVerwaltung = null;

	/**
	 * Schnittstelle zu den Informationen über die Standardpublikationsaspekte.
	 */
	private IStandardAspekte standardAspekte = null;

	/**
	 * Anmeldungen zum Publizieren von verarbeiteten Daten.
	 */
	private DAVSendeAnmeldungsVerwaltung publikationsAnmeldungen = null;

	/**
	 * Parameter zur Datenflusssteuerung.
	 */
	private IDatenFlussSteuerungFuerModul iDfsMod = DFSKonstanten.STANDARD;

	/**
	 * Alle Objekte, die aktuell von der Datenflusssteuerung verwaltet werden.
	 */
	private final Collection<SystemObject> objekte = Collections.synchronizedCollection(new HashSet<SystemObject>());

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param standardAspekte
	 *            Schnittstelle zu den Informationen über die
	 *            Standardpublikationsaspekte
	 * @throws DUAInitialisierungsException
	 *             wenn die Initialisierung der Datenflusssteuerung fehlschlaegt
	 */
	public MweDatenFlussSteuerung(final IVerwaltung verwaltung, final IStandardAspekte standardAspekte)
			throws DUAInitialisierungsException {
		if (MweDatenFlussSteuerung.dieVerwaltung != null) {
			throw new RuntimeException("Datenflusssteuerung darf nur einmal initialisiert werden"); //$NON-NLS-1$
		}

		MweDatenFlussSteuerung.dieVerwaltung = verwaltung;
		this.standardAspekte = standardAspekte;
		this.publikationsAnmeldungen = new DAVSendeAnmeldungsVerwaltung(verwaltung.getVerbindung(),
				SenderRole.source());

		DatenFlussSteuerungsVersorger.getInstanz(verwaltung).addListener(this);
	}

	/**
	 * Publiziert ein Datum nach den Vorgaben der Datenflusssteuerung.
	 *
	 * @param original
	 *            das Originaldatum
	 * @param nutzDatum
	 *            die im Originaldatum auszutauschenden Nutzdaten
	 * @return das publizierte Datum oder <code>null</code>, wenn kein Datum
	 *         publiziert werden konnte
	 */
	public final synchronized ResultData publiziere(final ResultData original, final Data nutzDatum) {
		ResultData letztesPubDatum = null;

		final ResultData publikationsDatum = iDfsMod.getPublikationsDatum(original, nutzDatum,
				standardAspekte.getStandardAspekt(original));
		if (publikationsDatum != null) {
			this.publikationsAnmeldungen.sende(publikationsDatum);
			letztesPubDatum = publikationsDatum;
		}

		return letztesPubDatum;
	}

	/**
	 * Fuegt der Menge der Objekte, die aktuell von der Datenflusssteuerung
	 * verwaltet werden ein Objekt hinzu.
	 *
	 * @param objekt
	 *            ein neues Objekt
	 */
	public final synchronized void addObjekt(final SystemObject objekt) {
		this.objekte.add(objekt);
		this.aktualisiereObjektAnmeldungen();
	}

	@Override
	public synchronized void aktualisierePublikation(final IDatenFlussSteuerung iDfs) {
		this.iDfsMod = iDfs.getDFSFuerModul(SWETyp.SWE_MESSWERTERSETZUNG_UFD, ModulTyp.MESSWERTERSETZUNG_UFD);
		this.aktualisiereObjektAnmeldungen();
	}

	/**
	 * Triggert die Aktualisierung der Objektanmeldungen mit allen aktuellen
	 * Objekten unter der aktuellen Datenflusssteuerung.
	 */
	private synchronized void aktualisiereObjektAnmeldungen() {
		Collection<DAVObjektAnmeldung> anmeldungenStd = new ArrayList<DAVObjektAnmeldung>();

		final Collection<SystemObject> objekteBisJetzt = new ArrayList<>(this.objekte);

		if (this.standardAspekte != null) {
			anmeldungenStd = this.standardAspekte.getStandardAnmeldungen(objekteBisJetzt);
		}

		final Collection<DAVObjektAnmeldung> anmeldungen = this.iDfsMod.getDatenAnmeldungen(objekteBisJetzt,
				anmeldungenStd);

		this.publikationsAnmeldungen.modifiziereObjektAnmeldung(anmeldungen);
	}
}
