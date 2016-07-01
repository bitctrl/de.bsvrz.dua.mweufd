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
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.dua.mweufd.wfd.MweWfdSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweWfdSensor zu testen
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class MweWfdSensorTest extends MweWfdSensor {

	/**
	 * Testdaten
	 */
	static double[] prueflingDaten;
	static double[] direkterNachbarDaten;
	static double[] vorherigeNachbarDaten;
	static double[] nachfolgeneNachbarDaten;
	static double[] ersatzQuerrschnittDaten;
	static double[] niederschlagIntensitaet;
	static double[] ersetzteAusgabeDaten;
	static long[] time;
	static int[] bereich;

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
	 * Ersatzsensor
	 */
	static protected SystemObject ersatzSensor;
	/**
	 * Vorsensor
	 */
	static protected SystemObject vorSensor;
	/**
	 * Nachsensor
	 */
	static protected SystemObject nachSensor;
	/**
	 * Niederschlagintensitaetssensor
	 */
	static protected SystemObject niSensor;
	/**
	 * Die Nebensensoren - direkte Nachbarn
	 */
	static protected SystemObject nebenSensor1, nebenSensor2, nebenSensor3;
	/**
	 * Verbindung zum DAV
	 */
	static protected ClientDavInterface dav;

	/**
	 * Datenbeschreibung der geschickten daten
	 */
	static protected DataDescription ddMessWerte, ddNiMessWerte, ddMessWertErsetzung;
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
	public MweWfdSensorTest(final IVerwaltungMitGuete verwaltung, final DUAUmfeldDatenMessStelle messStelle,
			final DUAUmfeldDatenSensor sensor)
					throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);

		if (!sensor.getObjekt().getPid().equals("ufdSensor.testWFD.wfd.zentral")) {
			return;
		}

		MweWfdSensorTest.dav = verwaltung.getVerbindung();
		MweWfdSensorTest.sender = new MweTestDatenSender(MweWfdSensorTest.dav);

		MweWfdSensorTest.zentralSensor = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.zentral");
		MweWfdSensorTest.vorSensor = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.vor");
		MweWfdSensorTest.nachSensor = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.nach");
		MweWfdSensorTest.ersatzSensor = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.ersatz");
		MweWfdSensorTest.niSensor = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.ni");
		MweWfdSensorTest.nebenSensor1 = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.neben1");
		MweWfdSensorTest.nebenSensor2 = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.neben2");
		MweWfdSensorTest.nebenSensor3 = MweWfdSensorTest.dav.getDataModel().getObject("ufdSensor.testWFD.wfd.neben3");

		MweWfdSensorTest.ddMessWerte = new DataDescription(
				MweWfdSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				MweWfdSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		MweWfdSensorTest.ddNiMessWerte = new DataDescription(
				MweWfdSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
				MweWfdSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		final Collection<SystemObject> list = new LinkedList<SystemObject>();

		list.add(MweWfdSensorTest.zentralSensor);
		list.add(MweWfdSensorTest.ersatzSensor);
		list.add(MweWfdSensorTest.nachSensor);
		list.add(MweWfdSensorTest.vorSensor);
		list.add(MweWfdSensorTest.nebenSensor1);
		list.add(MweWfdSensorTest.nebenSensor2);
		list.add(MweWfdSensorTest.nebenSensor3);

		MweWfdSensorTest.sender.anmeldeQuelle(list, MweWfdSensorTest.ddMessWerte);
		MweWfdSensorTest.sender.anmeldeParametrierung(MweWfdSensorTest.zentralSensor);

		MweWfdSensorTest.sender.anmeldeQuelle(MweWfdSensorTest.niSensor, MweWfdSensorTest.ddNiMessWerte);

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
		MweWfdSensorTest.sender.parametriereSensor(MweWfdSensorTest.zentralSensor, messwertFortschreibungsIntervall,
				messWertErsetzungIntervall, periode);
	}

	/**
	 * Sendet die Daten des naechsten Schrittes
	 *
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	static public boolean naechsterCyklus() {
		if (MweWfdSensorTest.indexSend >= MweWfdSensorTest.ersatzQuerrschnittDaten.length) {
			return false;
		}

		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.zentralSensor, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.prueflingDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.vorSensor, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.vorherigeNachbarDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.nachSensor, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.nachfolgeneNachbarDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.ersatzSensor, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.ersatzQuerrschnittDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.nebenSensor1, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.direkterNachbarDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.nebenSensor2, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.direkterNachbarDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.nebenSensor3, MweWfdSensorTest.ddMessWerte,
				"WasserFilmDicke", MweWfdSensorTest.direkterNachbarDaten[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);
		MweWfdSensorTest.sender.sendeDatenSatz(MweWfdSensorTest.niSensor, MweWfdSensorTest.ddNiMessWerte,
				"NiederschlagsIntensität", MweWfdSensorTest.niederschlagIntensitaet[MweWfdSensorTest.indexSend],
				MweWfdSensorTest.time[MweWfdSensorTest.indexSend]);

		MweWfdSensorTest.indexSend++;
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
	static public void generiereTestDatenNachPruefSpezWfd1(final long t1, final long tE, final long periode) {

		final double w1 = 1.0;
		final double w2 = 0.8;
		final double w3 = 2.6;
		final double w4 = 0.3;
		final double wni = 2.8;
		final double wd = 0.2;

		MweWfdSensorTest.zeitIntervall = periode;
		final int length = (int) (tE / periode) + 5;

		MweWfdSensorTest.prueflingDaten = new double[length];
		MweWfdSensorTest.direkterNachbarDaten = new double[length];
		MweWfdSensorTest.vorherigeNachbarDaten = new double[length];
		MweWfdSensorTest.nachfolgeneNachbarDaten = new double[length];
		MweWfdSensorTest.ersatzQuerrschnittDaten = new double[length];
		MweWfdSensorTest.ersetzteAusgabeDaten = new double[length];
		MweWfdSensorTest.niederschlagIntensitaet = new double[length];
		MweWfdSensorTest.bereich = new int[length];

		MweWfdSensorTest.time = new long[length];
		// Zeit
		for (int i = 0; i < length; i++) {
			MweWfdSensorTest.time[i] = i * periode;
		}

		// Intervalle
		final long[] t = new long[10];
		final long tInt = (tE - t1) / 8;
		t[0] = periode;
		t[1] = t[0] + t1;

		for (int i = 2; i < 10; i++) {
			t[i] = t[i - 1] + tInt;
		}

		// Ersatzquerrschnittdaten
		for (int i = 0; i < length; i++) {
			if ((MweWfdSensorTest.time[i] >= t[7]) && (MweWfdSensorTest.time[i] < t[8])) {
				MweWfdSensorTest.ersatzQuerrschnittDaten[i] = -1;
			} else {
				MweWfdSensorTest.ersatzQuerrschnittDaten[i] = w4;
			}
		}

		// Nachbar Sensor
		for (int i = 0; i < length; i++) {
			if (MweWfdSensorTest.time[i] < t[3]) {
				MweWfdSensorTest.vorherigeNachbarDaten[i] = w2;
			} else if (MweWfdSensorTest.time[i] < t[6]) {
				MweWfdSensorTest.vorherigeNachbarDaten[i] = w3;
			} else {
				MweWfdSensorTest.vorherigeNachbarDaten[i] = -1;
			}
		}

		// Pruefling
		for (int i = 0; i < length; i++) {
			if (MweWfdSensorTest.time[i] < t[0]) {
				MweWfdSensorTest.prueflingDaten[i] = w1;
			} else {
				MweWfdSensorTest.prueflingDaten[i] = -1;
			}
		}

		// Direkter Nachbar
		for (int i = 0; i < length; i++) {
			if (MweWfdSensorTest.time[i] < t[1]) {
				MweWfdSensorTest.direkterNachbarDaten[i] = -1;
			} else if ((MweWfdSensorTest.time[i] >= t[1]) && (MweWfdSensorTest.time[i] < t[2])) {
				MweWfdSensorTest.direkterNachbarDaten[i] = wd;
			} else if ((MweWfdSensorTest.time[i] >= t[2]) && (MweWfdSensorTest.time[i] < t[5])) {
				MweWfdSensorTest.direkterNachbarDaten[i] = -1;
			} else if ((MweWfdSensorTest.time[i] >= t[5]) && (MweWfdSensorTest.time[i] < t[6])) {
				MweWfdSensorTest.direkterNachbarDaten[i] = wd;
			} else if ((MweWfdSensorTest.time[i] >= t[6]) && (MweWfdSensorTest.time[i] < t[8])) {
				MweWfdSensorTest.direkterNachbarDaten[i] = -1;
			} else {
				MweWfdSensorTest.direkterNachbarDaten[i] = wd;
			}
		}

		// Nachbar Sensor
		for (int i = 0; i < length; i++) {
			if (MweWfdSensorTest.time[i] < t[4]) {
				MweWfdSensorTest.nachfolgeneNachbarDaten[i] = w3;
			} else {
				MweWfdSensorTest.nachfolgeneNachbarDaten[i] = 0.0;
			}
		}

		// NI
		for (int i = 0; i < length; i++) {
			if (MweWfdSensorTest.time[i] < t[5]) {
				MweWfdSensorTest.niederschlagIntensitaet[i] = wni;
			} else {
				MweWfdSensorTest.niederschlagIntensitaet[i] = -1;
			}
		}

		// Ausgabewerte
		double letzterWert = w1;
		for (int i = 0; i < length; i++) {
			if (MweWfdSensorTest.time[i] < t[0]) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = MweWfdSensorTest.prueflingDaten[i];
				letzterWert = MweWfdSensorTest.prueflingDaten[i];
				MweWfdSensorTest.bereich[i] = 0;
			} else if ((MweWfdSensorTest.time[i] >= t[0]) && (MweWfdSensorTest.time[i] < t[1])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = letzterWert;
				MweWfdSensorTest.bereich[i] = 1;
			} else if ((MweWfdSensorTest.time[i] >= t[1]) && (MweWfdSensorTest.time[i] < t[2])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = MweWfdSensorTest.direkterNachbarDaten[i];
				MweWfdSensorTest.bereich[i] = 1; // 1b
			} else if ((MweWfdSensorTest.time[i] >= t[2]) && (MweWfdSensorTest.time[i] < t[3])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = (MweWfdSensorTest.vorherigeNachbarDaten[i]
						+ MweWfdSensorTest.nachfolgeneNachbarDaten[i]) / 2.0;
				MweWfdSensorTest.bereich[i] = 2;
			} else if ((MweWfdSensorTest.time[i] >= t[3]) && (MweWfdSensorTest.time[i] < t[4])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = (MweWfdSensorTest.vorherigeNachbarDaten[i]
						+ MweWfdSensorTest.nachfolgeneNachbarDaten[i]) / 2.0;
				MweWfdSensorTest.bereich[i] = 3;
			} else if ((MweWfdSensorTest.time[i] >= t[4]) && (MweWfdSensorTest.time[i] < t[5])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = -1;
				MweWfdSensorTest.bereich[i] = 4;
			} else if ((MweWfdSensorTest.time[i] >= t[5]) && (MweWfdSensorTest.time[i] < t[6])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = MweWfdSensorTest.direkterNachbarDaten[i];
				MweWfdSensorTest.bereich[i] = 4; // 4b
			} else if ((MweWfdSensorTest.time[i] >= t[6]) && (MweWfdSensorTest.time[i] < t[7])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = MweWfdSensorTest.ersatzQuerrschnittDaten[i];
				MweWfdSensorTest.bereich[i] = 6;
			} else if ((MweWfdSensorTest.time[i] >= t[7]) && (MweWfdSensorTest.time[i] < t[8])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = -1;
				MweWfdSensorTest.bereich[i] = 7;
			} else if ((MweWfdSensorTest.time[i] >= t[8]) && (MweWfdSensorTest.time[i] < t[9])) {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = MweWfdSensorTest.direkterNachbarDaten[i];
				MweWfdSensorTest.bereich[i] = 7; // 7b
			} else {
				MweWfdSensorTest.ersetzteAusgabeDaten[i] = -1;
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
			if (!original.getObject().getPid().equals("ufdSensor.testWFD.wfd.zentral")) {
				return;
			}
			double wfd = nutzDatum.getItem("WasserFilmDicke").getItem("Wert").asUnscaledValue().doubleValue();
			if (wfd >= 0) {
				wfd = nutzDatum.getItem("WasserFilmDicke").getItem("Wert").asScaledValue().doubleValue();
			} else {
				wfd = -1.0;
			}
			Assert.assertTrue(
					"Erwartetes datum: " + MweWfdSensorTest.ersetzteAusgabeDaten[MweWfdSensorTest.indexEmpf]
							+ " Berechnetes datum: " + wfd + " index " + (MweWfdSensorTest.indexEmpf) + " Bereich "
							+ MweWfdSensorTest.bereich[MweWfdSensorTest.indexEmpf],
					Math.abs(MweWfdSensorTest.ersetzteAusgabeDaten[MweWfdSensorTest.indexEmpf] - wfd) < 0.001);
			System.out.println(String.format("[ %4d ] Bereich %2d Ersatzwert OK: %3f == %3f",
					MweWfdSensorTest.indexEmpf, MweWfdSensorTest.bereich[MweWfdSensorTest.indexEmpf],
					MweWfdSensorTest.ersetzteAusgabeDaten[MweWfdSensorTest.indexEmpf], wfd));
			MweWfdSensorTest.indexEmpf++;
			synchronized (AbstraktMweUfdsSensor.dieVerwaltung) {
				if (MweWfdSensorTest.indexEmpf >= MweWfdSensorTest.ersetzteAusgabeDaten.length) {
					MweWfdSensorJunitTester.warten = false;
				}
				AbstraktMweUfdsSensor.dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs.publiziere(original, nutzDatum);
		}
	}
}
