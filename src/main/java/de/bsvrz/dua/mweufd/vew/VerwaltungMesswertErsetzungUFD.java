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

package de.bsvrz.dua.mweufd.vew;

import java.util.HashSet;
import java.util.Set;

import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
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
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Das Modul Verwaltung ist die zentrale Steuereinheit der SWE Messwertersetzung
 * UFD. Seine Aufgabe besteht in der Auswertung der Aufrufparameter, der
 * Anmeldung beim Datenverteiler und der entsprechenden Initialisierung aller
 * Auswertungsmodule. Weiter ist das Modul Verwaltung f�r die Anmeldung der zu
 * pr�fenden Daten zust�ndig. Die Verwaltung gibt ein Objekt des Moduls
 * Niederschlagsintensit�t als Beobachterobjekt an, an das die zu �berpr�fenden
 * Daten durch den Aktualisierungsmechanismus weitergeleitet werden.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class VerwaltungMesswertErsetzungUFD extends
		AbstraktVerwaltungsAdapterMitGuete {

	/**
	 * Datenflusssteuerung.
	 */
	public static MweDatenFlussSteuerung dieDfs = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere() throws DUAInitialisierungsException {
		super.initialisiere();

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

		DUAUmfeldDatenMessStelle.initialisiere(this.verbindung, this
				.getSystemObjekte());
		
		/**
		 * die Datenarten, die nicht messwertersetzt werden, aber dennoch
		 * weitergereicht werden sollen
		 */
		Set<UmfeldDatenArt> rest = new HashSet<UmfeldDatenArt>();
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
			DUAUmfeldDatenSensor hauptSensorNI = messStelle
					.getHauptSensor(UmfeldDatenArt.ni);
			DUAUmfeldDatenSensor hauptSensorNS = messStelle
					.getHauptSensor(UmfeldDatenArt.ns);
			DUAUmfeldDatenSensor hauptSensorFBZ = messStelle
					.getHauptSensor(UmfeldDatenArt.fbz);
			DUAUmfeldDatenSensor hauptSensorWFD = messStelle
					.getHauptSensor(UmfeldDatenArt.wfd);
			DUAUmfeldDatenSensor hauptSensorSW = messStelle
					.getHauptSensor(UmfeldDatenArt.sw);
			DUAUmfeldDatenSensor hauptSensorTPT = messStelle
					.getHauptSensor(UmfeldDatenArt.tpt);
			DUAUmfeldDatenSensor hauptSensorLT = messStelle
					.getHauptSensor(UmfeldDatenArt.lt);
			DUAUmfeldDatenSensor hauptSensorFBT = messStelle
					.getHauptSensor(UmfeldDatenArt.fbt);
			
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

			for (UmfeldDatenArt datenArt : rest) {
				DUAUmfeldDatenSensor restSensor = messStelle
						.getHauptSensor(datenArt);
				if (restSensor != null) {
					try {
						RestDatenVersender.getInstanz(this.verbindung).add(
								restSensor.getObjekt());
					} catch (OneSubscriptionPerSendData e) {
						throw new DUAInitialisierungsException(
								"Daten von Umfelddatensensor "
										+ restSensor.getObjekt()
										+ " koennen nicht weitergeleitet werden",
								e);
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
	public static void main(String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungMesswertErsetzungUFD(),
				argumente);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}

	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_UFD;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		// Daten werden von den Untermodulen selbst entgegen genommen
	}

}