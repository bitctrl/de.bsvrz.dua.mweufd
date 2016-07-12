/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Messwertersetzung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

package de.bsvrz.dua.mweufd.vew;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Aktuelle Datenflusssteuerung der Messwertersetzung UFD.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class MweDatenFlussSteuerung implements IDatenFlussSteuerungsListener {

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	protected IVerwaltung dieVerwaltung = null;

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
	private Collection<SystemObject> objekte = Collections
			.synchronizedCollection(new HashSet<SystemObject>());

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
	public MweDatenFlussSteuerung(final IVerwaltung verwaltung,
			final IStandardAspekte standardAspekte)
			throws DUAInitialisierungsException {
		dieVerwaltung = verwaltung;
		this.standardAspekte = standardAspekte;
		this.publikationsAnmeldungen = new DAVSendeAnmeldungsVerwaltung(
				verwaltung.getVerbindung(), SenderRole.source());

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
	public final synchronized ResultData publiziere(final ResultData original,
			final Data nutzDatum) {
		ResultData letztesPubDatum = null;

		final ResultData publikationsDatum = iDfsMod.getPublikationsDatum(original,
				nutzDatum, standardAspekte.getStandardAspekt(original));
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

	/**
	 * {@inheritDoc}
	 */
	public synchronized void aktualisierePublikation(final IDatenFlussSteuerung iDfs) {
		this.iDfsMod = iDfs.getDFSFuerModul(SWETyp.SWE_MESSWERTERSETZUNG_UFD,
				ModulTyp.MESSWERTERSETZUNG_UFD);
		this.aktualisiereObjektAnmeldungen();
	}

	/**
	 * Triggert die Aktualisierung der Objektanmeldungen mit allen aktuellen
	 * Objekten unter der aktuellen Datenflusssteuerung.
	 */
	private synchronized void aktualisiereObjektAnmeldungen() {
		Collection<DAVObjektAnmeldung> anmeldungenStd = new ArrayList<DAVObjektAnmeldung>();

		final SystemObject[] objekteBisJetzt = this.objekte
				.toArray(new SystemObject[0]);

		if (this.standardAspekte != null) {
			anmeldungenStd = this.standardAspekte
					.getStandardAnmeldungen(objekteBisJetzt);
		}

		final Collection<DAVObjektAnmeldung> anmeldungen = this.iDfsMod
				.getDatenAnmeldungen(objekteBisJetzt, anmeldungenStd);

		this.publikationsAnmeldungen.modifiziereObjektAnmeldung(anmeldungen);
	}
}
