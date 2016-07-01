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

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.sw.MweSwSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweSwSensor zu testen
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class MweSwSensorTest extends MweSwSensor {

	/**
	 * Testdaten
	 */
	static long[] prueflingDaten;
	static long[] nachfolgerDaten;
	static long[] ersetzteAusgabeDaten;
	static long[] time;

	/**
	 * Periode der Datensendung
	 */
	static long zeitIntervall;

	/**
	 * Letzter index der gesendeten und empfangenen Daten
	 */
	static protected int indexEmpf = 0;
	static protected int indexSend = 0;

	/**
	 * Getesteter Sensor
	 */
	static protected SystemObject zentralSensor;
	/**
	 * Nachfolgersensor
	 */
	static protected SystemObject nachfolgerSensor;
	/**
	 * Verbindung zum DAV
	 */
	static protected ClientDavInterface dav;

	/**
	 * Datenbeschreibung der geschickten daten
	 */
	static protected DataDescription ddMessWerte, ddMessWertErsetzung;
	/**
	 * Datensender
	 */
	static protected MweTestDatenSender sender;

	/**
	 * Standardkonstruktor
	 *
	 * @param verwaltung
	 * @param messStelle
	 * @param sensor
	 * @throws DUAInitialisierungsException
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public MweSwSensorTest(final IVerwaltungMitGuete verwaltung, final DUAUmfeldDatenMessStelle messStelle,
			final DUAUmfeldDatenSensor sensor)
					throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);
		// if(dav != null) return;
		if (!sensor.getObjekt().getPid().equals("ufdSensor.testSW.sw.zentral")) {
			return;
		}
		MweSwSensorTest.dav = verwaltung.getVerbindung();
		MweSwSensorTest.sender = new MweTestDatenSender(MweSwSensorTest.dav);

		MweSwSensorTest.zentralSensor = MweSwSensorTest.dav.getDataModel().getObject("ufdSensor.testSW.sw.zentral");
		MweSwSensorTest.nachfolgerSensor = MweSwSensorTest.dav.getDataModel().getObject("ufdSensor.testSW.sw.nach");

		MweSwSensorTest.ddMessWerte = new DataDescription(
				MweSwSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsSichtWeite"),
				MweSwSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		final Collection<SystemObject> list = new LinkedList<SystemObject>();
		list.add(MweSwSensorTest.zentralSensor);
		list.add(MweSwSensorTest.nachfolgerSensor);

		MweSwSensorTest.sender.anmeldeQuelle(list, MweSwSensorTest.ddMessWerte);
		MweSwSensorTest.sender.anmeldeParametrierung(MweSwSensorTest.zentralSensor);
	}

	/**
	 * Parametreirt den gestesteten Sensor
	 *
	 * @param messwertFortschreibungsIntervall
	 *            Maximaler MesswertFortschreibungsIntervall
	 * @param messWertErsetzungIntervall
	 *            Maximaler MessWertErsetzungIntervall
	 * @param periode
	 *            Elementares Schritt
	 */
	public static void parametriereSensor(final long messwertFortschreibungsIntervall,
			final long messWertErsetzungIntervall, final long periode) {
		MweSwSensorTest.sender.parametriereSensor(MweSwSensorTest.zentralSensor, messwertFortschreibungsIntervall,
				messWertErsetzungIntervall, periode);
	}

	/**
	 * Sendet die Daten des naechsten Schrittes
	 *
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	static public boolean naechsterCyklus() {
		if (MweSwSensorTest.indexSend >= MweSwSensorTest.nachfolgerDaten.length) {
			return false;
		}

		MweSwSensorTest.sender.sendeDatenSatz(MweSwSensorTest.zentralSensor, MweSwSensorTest.ddMessWerte, "SichtWeite",
				MweSwSensorTest.prueflingDaten[MweSwSensorTest.indexSend],
				MweSwSensorTest.time[MweSwSensorTest.indexSend]);
		MweSwSensorTest.sender.sendeDatenSatz(MweSwSensorTest.nachfolgerSensor, MweSwSensorTest.ddMessWerte,
				"SichtWeite", MweSwSensorTest.nachfolgerDaten[MweSwSensorTest.indexSend],
				MweSwSensorTest.time[MweSwSensorTest.indexSend]);

		MweSwSensorTest.indexSend++;
		return true;
	}

	/**
	 * Generiert die Testdaten nach der Pruefspezifikation
	 *
	 * @param t1
	 *            Messwertfortsetzungsintervall
	 * @param tE
	 *            Messwertersetzungsintervall
	 * @param periode
	 *            Periode
	 */
	static public void generiereTestDatenNachPruefSpezSw1(final long t1, final long tE, final long periode) {

		final long w1 = 40;
		final long w2 = 60;
		final long w3 = 80;

		MweSwSensorTest.zeitIntervall = periode;
		final int length = (int) (tE / periode) + 5;

		MweSwSensorTest.prueflingDaten = new long[length];
		MweSwSensorTest.nachfolgerDaten = new long[length];
		MweSwSensorTest.ersetzteAusgabeDaten = new long[length];

		MweSwSensorTest.time = new long[length];
		// Zeit
		for (int i = 0; i < length; i++) {
			MweSwSensorTest.time[i] = i * periode;
		}

		// Intervalle
		final long[] t = new long[5];
		final long tInt = (tE - t1) / 3;

		t[0] = periode;
		t[1] = t[0] + tInt;
		t[2] = t[1] + t1;
		t[3] = t[2] + tInt;
		t[4] = t[3] + tInt;

		// Nachfolgerdaten
		for (int i = 0; i < length; i++) {
			if (MweSwSensorTest.time[i] < t[1]) {
				MweSwSensorTest.nachfolgerDaten[i] = w2;
			} else if ((MweSwSensorTest.time[i] >= t[1]) && (MweSwSensorTest.time[i] < t[3])) {
				MweSwSensorTest.nachfolgerDaten[i] = -1;
			} else {
				MweSwSensorTest.nachfolgerDaten[i] = w3;
			}
		}

		// Pruefling
		for (int i = 0; i < length; i++) {
			if (MweSwSensorTest.time[i] < t[0]) {
				MweSwSensorTest.prueflingDaten[i] = w1;
			} else {
				MweSwSensorTest.prueflingDaten[i] = -1;
			}
		}

		// Ausgabewerte
		long letzterWert = w1;
		for (int i = 0; i < length; i++) {
			if (MweSwSensorTest.time[i] < t[0]) {
				MweSwSensorTest.ersetzteAusgabeDaten[i] = MweSwSensorTest.prueflingDaten[i];
			} else if ((MweSwSensorTest.time[i] >= t[0]) && (MweSwSensorTest.time[i] < t[1])) {
				MweSwSensorTest.ersetzteAusgabeDaten[i] = MweSwSensorTest.nachfolgerDaten[i];
				letzterWert = MweSwSensorTest.ersetzteAusgabeDaten[i];
			} else if ((MweSwSensorTest.time[i] >= t[1]) && (MweSwSensorTest.time[i] < t[2])) {
				MweSwSensorTest.ersetzteAusgabeDaten[i] = letzterWert;
			} else if ((MweSwSensorTest.time[i] >= t[2]) && (MweSwSensorTest.time[i] < t[3])) {
				MweSwSensorTest.ersetzteAusgabeDaten[i] = -1;
			} else if ((MweSwSensorTest.time[i] >= t[3]) && (MweSwSensorTest.time[i] < t[4])) {
				MweSwSensorTest.ersetzteAusgabeDaten[i] = MweSwSensorTest.nachfolgerDaten[i];
			} else {
				MweSwSensorTest.ersetzteAusgabeDaten[i] = -1;
			}
		}

		System.out.print(' ');
	}

	@Override
	protected void publiziere(final ResultData original, final Data nutzDatum) {
		boolean publiziereDatensatz = false;

		if (nutzDatum == null) {
			/**
			 * "keine Daten" wird nur publiziert, wenn das Objekt vorher nicht
			 * auch schon auf keine Daten stand
			 */
			if ((this.letztesPubDatum != null) && (this.letztesPubDatum.getData() != null)) {
				publiziereDatensatz = true;
			}
		} else {
			publiziereDatensatz = true;
		}

		if (publiziereDatensatz) {
			if (!original.getObject().getPid().equals(MweSwSensorTest.zentralSensor.getPid())) {
				return;
			}

			long sw = nutzDatum.getItem("SichtWeite").getItem("Wert").asUnscaledValue().longValue();
			if (sw >= 0) {
				sw = nutzDatum.getItem("SichtWeite").getItem("Wert").asScaledValue().longValue();
			} else {
				sw = -1;
			}
			Assert.assertTrue(
					"Erwartetes datum: " + MweSwSensorTest.ersetzteAusgabeDaten[MweSwSensorTest.indexEmpf]
							+ " Berechnetes datum: " + sw + " index " + (MweSwSensorTest.indexEmpf),
					Math.abs(MweSwSensorTest.ersetzteAusgabeDaten[MweSwSensorTest.indexEmpf] - sw) < 0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3d == %3d", MweSwSensorTest.indexEmpf,
					MweSwSensorTest.ersetzteAusgabeDaten[MweSwSensorTest.indexEmpf], sw));
			MweSwSensorTest.indexEmpf++;
			synchronized (AbstraktMweUfdsSensor.dieVerwaltung) {
				if (MweSwSensorTest.indexEmpf >= MweSwSensorTest.ersetzteAusgabeDaten.length) {
					MweSwSensorJunitTester.warten = false;
				}
				AbstraktMweUfdsSensor.dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs.publiziere(original, nutzDatum);
		}
	}
}
