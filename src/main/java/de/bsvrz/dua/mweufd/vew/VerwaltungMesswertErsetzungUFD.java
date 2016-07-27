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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.UnknownUfdSensor;
import de.bsvrz.dua.mweufd.sensors.*;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Das Modul Verwaltung ist die zentrale Steuereinheit der SWE Messwertersetzung
 * UFD. Seine Aufgabe besteht in der Auswertung der Aufrufparameter, der
 * Anmeldung beim Datenverteiler und der entsprechenden Initialisierung aller
 * Auswertungsmodule. Weiter ist das Modul Verwaltung für die Anmeldung der zu
 * prüfenden Daten zuständig.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id: VerwaltungMesswertErsetzungUFD.java 54549 2015-04-17 13:40:51Z gieseler $
 */
public class VerwaltungMesswertErsetzungUFD extends
		AbstraktVerwaltungsAdapter {

	/**
	 * Datenflusssteuerung.
	 */
	public MweDatenFlussSteuerung dieDfs;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere() throws DUAInitialisierungsException {
		dieDfs = new MweDatenFlussSteuerung(this,
				new MweUfdStandardAspekteVersorger(this).getStandardPubInfos());
		UmfeldDatenArt.initialisiere(this.verbindung);

		/**
		 * UFD-Messstellen ermitteln
		 */
		this.objekte = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(
						DUAKonstanten.TYP_UFD_MESSSTELLE), this.verbindung,
				this.getKonfigurationsBereiche()).toArray(new SystemObject[0]);

		DUAUmfeldDatenMessStelle.initialisiere(this.verbindung,
				this.getSystemObjekte());

		/**
		 * die Datenarten, die nicht messwertersetzt werden, aber dennoch
		 * weitergereicht werden sollen
		 */
		final Set<UmfeldDatenArt> rest = new HashSet<UmfeldDatenArt>();
		rest.addAll(UmfeldDatenArt.getInstanzen());
		rest.remove(UmfeldDatenArt.ni);
		rest.remove(UmfeldDatenArt.ns);
		rest.remove(UmfeldDatenArt.fbz);
		rest.remove(UmfeldDatenArt.wfd);
		rest.remove(UmfeldDatenArt.sw);
		rest.remove(UmfeldDatenArt.tpt);
		rest.remove(UmfeldDatenArt.lt);
		rest.remove(UmfeldDatenArt.fbt);

		for (DUAUmfeldDatenMessStelle messStelle : DUAUmfeldDatenMessStelle
				.getInstanzen()) {
			try {
				final DUAUmfeldDatenSensor hauptSensorNI = messStelle
						.getHauptSensor(UmfeldDatenArt.ni);
				final DUAUmfeldDatenSensor hauptSensorNS = messStelle
						.getHauptSensor(UmfeldDatenArt.ns);
				final DUAUmfeldDatenSensor hauptSensorFBZ = messStelle
						.getHauptSensor(UmfeldDatenArt.fbz);
				final DUAUmfeldDatenSensor hauptSensorWFD = messStelle
						.getHauptSensor(UmfeldDatenArt.wfd);
				final DUAUmfeldDatenSensor hauptSensorSW = messStelle
						.getHauptSensor(UmfeldDatenArt.sw);
				final DUAUmfeldDatenSensor hauptSensorTPT = messStelle
						.getHauptSensor(UmfeldDatenArt.tpt);
				final DUAUmfeldDatenSensor hauptSensorLT = messStelle
						.getHauptSensor(UmfeldDatenArt.lt);
				final DUAUmfeldDatenSensor hauptSensorFBT = messStelle
						.getHauptSensor(UmfeldDatenArt.fbt);

				if (hauptSensorNI != null) {
					new MweNiSensor(this, messStelle, hauptSensorNI);
				}
				if (hauptSensorNS != null) {
					new MweNsSensor(this, messStelle, hauptSensorNS);
				}
				if (hauptSensorFBZ != null) {
					new MweFbzSensor(this, messStelle, hauptSensorFBZ);
				}
				if (hauptSensorLT != null) {
					new MweLtSensor(this, messStelle, hauptSensorLT);
				}
				if (hauptSensorTPT != null) {
					new MweTptSensor(this, messStelle, hauptSensorTPT);
				}
				if (hauptSensorWFD != null) {
					new MweWfdSensor(this, messStelle, hauptSensorWFD);
				}
				if (hauptSensorSW != null) {
					new MweSwSensor(this, messStelle, hauptSensorSW);
				}
				if (hauptSensorFBT != null) {
					new MweFbtSensor(this, messStelle, hauptSensorFBT);
				}
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				Debug.getLogger().warning(e.getMessage());
				continue;
			}

			// Daten von unbekannten Sensoren einfach weiterleiten
			for (UmfeldDatenArt datenArt : rest) {
				final DUAUmfeldDatenSensor restSensor = messStelle
						.getHauptSensor(datenArt);
				if (restSensor != null) {
					try {
						new UnknownUfdSensor(this, messStelle, restSensor);
					} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
						Debug.getLogger().warning(e.getMessage());
					}
				}
			}
			
			// Nebensensordaten einfach weiterleiten
			for (UmfeldDatenArt datenArt : UmfeldDatenArt.getInstanzen()) {
				final Collection<DUAUmfeldDatenSensor> restSensoren = messStelle.getNebenSensoren(datenArt);
				for(DUAUmfeldDatenSensor restSensor : restSensoren) {
					try {
						new UnknownUfdSensor(this, messStelle, restSensor);
					} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
						Debug.getLogger().warning(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Startet diese Applikation.
	 * 
	 * @param argumente
	 *            Argumente der Kommandozeile
	 */
	public static void main(final String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungMesswertErsetzungUFD(),
				argumente);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_UFD;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] resultate) {
		// Daten werden von den Untermodulen selbst entgegen genommen
	}

	public MweDatenFlussSteuerung getDFS() {
		return dieDfs;
	}
}
