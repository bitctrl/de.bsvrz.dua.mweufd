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

import java.util.HashSet;
import java.util.Set;

import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.MweTptLtNsFbzSensor;
import de.bsvrz.dua.mweufd.fbt.MweFbtSensor;
import de.bsvrz.dua.mweufd.ni.MweNiSensor;
import de.bsvrz.dua.mweufd.sw.MweSwSensor;
import de.bsvrz.dua.mweufd.wfd.MweWfdSensor;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Das Modul Verwaltung ist die zentrale Steuereinheit der SWE Messwertersetzung
 * UFD. Seine Aufgabe besteht in der Auswertung der Aufrufparameter, der
 * Anmeldung beim Datenverteiler und der entsprechenden Initialisierung aller
 * Auswertungsmodule. Weiter ist das Modul Verwaltung für die Anmeldung der zu
 * prüfenden Daten zuständig. Die Verwaltung gibt ein Objekt des Moduls
 * Niederschlagsintensität als Beobachterobjekt an, an das die zu überprüfenden
 * Daten durch den Aktualisierungsmechanismus weitergeleitet werden.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class VerwaltungMesswertErsetzungUFD extends AbstraktVerwaltungsAdapterMitGuete {

	/**
	 * Datenflusssteuerung.
	 */
	public static MweDatenFlussSteuerung dieDfs;

	@Override
	protected void initialisiere() throws DUAInitialisierungsException {
		super.initialisiere();

		VerwaltungMesswertErsetzungUFD.dieDfs = new MweDatenFlussSteuerung(this,
				new MweUfdStandardAspekteVersorger(this).getStandardPubInfos());
		UmfeldDatenArt.initialisiere(getVerbindung());

		/**
		 * UFD-Messstellen ermitteln
		 */
		setSystemObjekte(DUAUtensilien
				.getBasisInstanzen(getVerbindung().getDataModel().getType(DUAKonstanten.TYP_UFD_MESSSTELLE),
						getVerbindung(), this.getKonfigurationsBereiche()));

		DUAUmfeldDatenMessStelle.initialisiere(getVerbindung(), this.getSystemObjekte());

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
					new MweNiSensor(this, messStelle, hauptSensorNI);
				}
				if (hauptSensorNS != null) {
					new MweTptLtNsFbzSensor(this, messStelle, hauptSensorNS);
				}
				if (hauptSensorFBZ != null) {
					new MweTptLtNsFbzSensor(this, messStelle, hauptSensorFBZ);
				}
				if (hauptSensorLT != null) {
					new MweTptLtNsFbzSensor(this, messStelle, hauptSensorLT);
				}
				if (hauptSensorTPT != null) {
					new MweTptLtNsFbzSensor(this, messStelle, hauptSensorTPT);
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

			for (final UmfeldDatenArt datenArt : rest) {
				final DUAUmfeldDatenSensor restSensor = messStelle.getHauptSensor(datenArt);
				if (restSensor != null) {
					try {
						RestDatenVersender.getInstanz(getVerbindung()).add(restSensor.getObjekt());
					} catch (final OneSubscriptionPerSendData e) {
						throw new DUAInitialisierungsException("Daten von Umfelddatensensor " + restSensor.getObjekt()
						+ " koennen nicht weitergeleitet werden", e);
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
		StandardApplicationRunner.run(new VerwaltungMesswertErsetzungUFD(), argumente);
	}

	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}

	@Override
	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_UFD;
	}

	@Override
	public void update(final ResultData[] resultate) {
		// Daten werden von den Untermodulen selbst entgegen genommen
	}

}
