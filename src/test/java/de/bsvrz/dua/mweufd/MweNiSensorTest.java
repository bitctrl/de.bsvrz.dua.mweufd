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
import de.bsvrz.dua.mweufd.ni.MweNiSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweNiSensor zu testen.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class MweNiSensorTest extends MweNiSensor {

	/**
	 * Testdaten.
	 */
	static double[] prueflingDaten;

	/**
	 * Testdaten.
	 */
	static double[] vorherigeNachbarDaten;

	/**
	 * Testdaten.
	 */
	static double[] nachfolgeneNachbarDaten;

	/**
	 * Testdaten.
	 */
	static double[] ersatzQuerrschnittDaten;

	/**
	 * Testdaten.
	 */
	static double[] wasserFilmDicke;

	/**
	 * Testdaten.
	 */
	static double[] ersetzteAusgabeDaten;

	/**
	 * Testdaten.
	 */
	static long[] time;

	/**
	 * Periode der Datensendung.
	 */
	static long zeitIntervall;

	/**
	 * Letzter index der gesendeten und empfangenen Daten
	 */
	protected static int indexEmpf = 0;

	protected static int indexSend = 0;
	protected static boolean initialisiert = false;

	/**
	 * Getesteter Sensor
	 */
	protected static SystemObject zentralSensor;
	/**
	 * Ersatzsensor
	 */
	protected static SystemObject ersatzSensor;
	/**
	 * Vorsensor
	 */
	protected static SystemObject vorSensor;
	/**
	 * Nachsensor
	 */
	protected static SystemObject nachSensor;
	/**
	 * Wasserfilmdickesensor
	 */
	protected static SystemObject wfdSensor;
	/**
	 * Verbindung zum DAV
	 */
	protected static ClientDavInterface dav;

	/**
	 * Datenbeschreibung der geschickten daten
	 */
	protected static DataDescription ddMessWerte, ddWfdMessWerte, ddMessWertErsetzung;
	/**
	 * Datensender
	 */
	protected static MweTestDatenSender sender;

	/**
	 * Standardkonstruktor
	 *
	 * @param verwaltung
	 * @param messStelle
	 * @param sensor
	 * @throws DUAInitialisierungsException
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public MweNiSensorTest(final IVerwaltungMitGuete verwaltung, final DUAUmfeldDatenMessStelle messStelle,
			final DUAUmfeldDatenSensor sensor)
					throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);

		if (!sensor.getObjekt().getPid().equals("ufdSensor.testNI.ni.zentral")) {
			return;
		}
		// if(dav != null) return;
		MweNiSensorTest.dav = verwaltung.getVerbindung();
		MweNiSensorTest.sender = new MweTestDatenSender(MweNiSensorTest.dav);

		MweNiSensorTest.zentralSensor = MweNiSensorTest.dav.getDataModel().getObject("ufdSensor.testNI.ni.zentral");
		MweNiSensorTest.vorSensor = MweNiSensorTest.dav.getDataModel().getObject("ufdSensor.testNI.ni.vor");
		MweNiSensorTest.nachSensor = MweNiSensorTest.dav.getDataModel().getObject("ufdSensor.testNI.ni.nach");
		MweNiSensorTest.ersatzSensor = MweNiSensorTest.dav.getDataModel().getObject("ufdSensor.testNI.ni.ersatz");
		MweNiSensorTest.wfdSensor = MweNiSensorTest.dav.getDataModel().getObject("ufdSensor.testNI.wfd");

		MweNiSensorTest.ddMessWerte = new DataDescription(
				MweNiSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
				MweNiSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		MweNiSensorTest.ddWfdMessWerte = new DataDescription(
				MweNiSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				MweNiSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		MweNiSensorTest.ddMessWertErsetzung = new DataDescription(
				MweNiSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				MweNiSensorTest.dav.getDataModel().getAspect("asp.parameterVorgabe"));

		final Collection<SystemObject> list = new LinkedList<SystemObject>();
		list.add(MweNiSensorTest.zentralSensor);
		list.add(MweNiSensorTest.ersatzSensor);
		list.add(MweNiSensorTest.nachSensor);
		list.add(MweNiSensorTest.vorSensor);

		MweNiSensorTest.sender.anmeldeQuelle(list, MweNiSensorTest.ddMessWerte);
		MweNiSensorTest.sender.anmeldeParametrierung(MweNiSensorTest.zentralSensor);

		MweNiSensorTest.sender.anmeldeQuelle(MweNiSensorTest.wfdSensor, MweNiSensorTest.ddWfdMessWerte);
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
		MweNiSensorTest.sender.parametriereSensor(MweNiSensorTest.zentralSensor, messwertFortschreibungsIntervall,
				messWertErsetzungIntervall, periode);
	}

	/**
	 * Sendet die Daten des naechsten Schrittes
	 *
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	static public boolean naechsterCyklus() {
		if (MweNiSensorTest.indexSend >= MweNiSensorTest.ersatzQuerrschnittDaten.length) {
			return false;
		}

		MweNiSensorTest.sender.sendeDatenSatz(MweNiSensorTest.zentralSensor, MweNiSensorTest.ddMessWerte,
				"NiederschlagsIntensität", MweNiSensorTest.prueflingDaten[MweNiSensorTest.indexSend],
				MweNiSensorTest.time[MweNiSensorTest.indexSend]);
		MweNiSensorTest.sender.sendeDatenSatz(MweNiSensorTest.vorSensor, MweNiSensorTest.ddMessWerte,
				"NiederschlagsIntensität", MweNiSensorTest.vorherigeNachbarDaten[MweNiSensorTest.indexSend],
				MweNiSensorTest.time[MweNiSensorTest.indexSend]);
		MweNiSensorTest.sender.sendeDatenSatz(MweNiSensorTest.nachSensor, MweNiSensorTest.ddMessWerte,
				"NiederschlagsIntensität", MweNiSensorTest.nachfolgeneNachbarDaten[MweNiSensorTest.indexSend],
				MweNiSensorTest.time[MweNiSensorTest.indexSend]);
		MweNiSensorTest.sender.sendeDatenSatz(MweNiSensorTest.ersatzSensor, MweNiSensorTest.ddMessWerte,
				"NiederschlagsIntensität", MweNiSensorTest.ersatzQuerrschnittDaten[MweNiSensorTest.indexSend],
				MweNiSensorTest.time[MweNiSensorTest.indexSend]);
		MweNiSensorTest.sender.sendeDatenSatz(MweNiSensorTest.wfdSensor, MweNiSensorTest.ddWfdMessWerte,
				"WasserFilmDicke", MweNiSensorTest.wasserFilmDicke[MweNiSensorTest.indexSend],
				MweNiSensorTest.time[MweNiSensorTest.indexSend]);

		MweNiSensorTest.indexSend++;
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
	static public void generiereTestDatenNachPruefSpezNi1(final long t1, final long tE, final long periode) {

		final double w1 = 2.0;
		final double w2 = 1.5;
		final double w3 = 5.5;
		final double w4 = 0.5;
		final double wfd = 1.8;

		MweNiSensorTest.zeitIntervall = periode;
		final int length = (int) (tE / periode) + 5;

		MweNiSensorTest.prueflingDaten = new double[length];
		MweNiSensorTest.vorherigeNachbarDaten = new double[length];
		MweNiSensorTest.nachfolgeneNachbarDaten = new double[length];
		MweNiSensorTest.ersatzQuerrschnittDaten = new double[length];
		MweNiSensorTest.ersetzteAusgabeDaten = new double[length];
		MweNiSensorTest.wasserFilmDicke = new double[length];

		MweNiSensorTest.time = new long[length];
		// Zeit
		for (int i = 0; i < length; i++) {
			MweNiSensorTest.time[i] = i * periode;
		}

		// Intervalle
		final long[] t = new long[9];
		final long tInt = (tE - t1) / 7;
		t[0] = periode;
		t[1] = t[0] + t1;

		for (int i = 2; i < 9; i++) {
			t[i] = t[i - 1] + tInt;
		}

		// Ersatzquerrschnittdaten
		for (int i = 0; i < length; i++) {
			if ((MweNiSensorTest.time[i] >= t[6]) && (MweNiSensorTest.time[i] < t[7])) {
				MweNiSensorTest.ersatzQuerrschnittDaten[i] = -1;
			} else {
				MweNiSensorTest.ersatzQuerrschnittDaten[i] = w4;
			}
		}

		// Nachbar Sensor
		for (int i = 0; i < length; i++) {
			if (MweNiSensorTest.time[i] < t[2]) {
				MweNiSensorTest.vorherigeNachbarDaten[i] = w2;
			} else if (MweNiSensorTest.time[i] < t[5]) {
				MweNiSensorTest.vorherigeNachbarDaten[i] = w3;
			} else {
				MweNiSensorTest.vorherigeNachbarDaten[i] = -1;
			}
		}

		// Pruefling
		for (int i = 0; i < length; i++) {
			if (MweNiSensorTest.time[i] < t[0]) {
				MweNiSensorTest.prueflingDaten[i] = w1;
			} else {
				MweNiSensorTest.prueflingDaten[i] = -1;
			}
		}

		// Nachbar Sensor
		for (int i = 0; i < length; i++) {
			if (MweNiSensorTest.time[i] < t[3]) {
				MweNiSensorTest.nachfolgeneNachbarDaten[i] = w3;
			} else {
				MweNiSensorTest.nachfolgeneNachbarDaten[i] = 0.0;
			}
		}

		// WFD
		for (int i = 0; i < length; i++) {
			if (MweNiSensorTest.time[i] < t[4]) {
				MweNiSensorTest.wasserFilmDicke[i] = wfd;
			} else {
				MweNiSensorTest.wasserFilmDicke[i] = -1;
			}
		}

		// Ausgabewerte
		double letzterWert = w1;
		for (int i = 0; i < length; i++) {
			if (MweNiSensorTest.time[i] < t[0]) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = MweNiSensorTest.prueflingDaten[i];
				letzterWert = MweNiSensorTest.prueflingDaten[i];
			} else if ((MweNiSensorTest.time[i] >= t[0]) && (MweNiSensorTest.time[i] < t[1])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = letzterWert;
			} else if ((MweNiSensorTest.time[i] >= t[1]) && (MweNiSensorTest.time[i] < t[2])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = (MweNiSensorTest.vorherigeNachbarDaten[i]
						+ MweNiSensorTest.nachfolgeneNachbarDaten[i]) / 2.0;
			} else if ((MweNiSensorTest.time[i] >= t[2]) && (MweNiSensorTest.time[i] < t[3])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = (MweNiSensorTest.vorherigeNachbarDaten[i]
						+ MweNiSensorTest.nachfolgeneNachbarDaten[i]) / 2.0;
			} else if ((MweNiSensorTest.time[i] >= t[3]) && (MweNiSensorTest.time[i] < t[4])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = -1;
			} else if ((MweNiSensorTest.time[i] >= t[4]) && (MweNiSensorTest.time[i] < t[5])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = MweNiSensorTest.ersatzQuerrschnittDaten[i];
			} else if ((MweNiSensorTest.time[i] >= t[5]) && (MweNiSensorTest.time[i] < t[6])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = MweNiSensorTest.ersatzQuerrschnittDaten[i];
			} else if ((MweNiSensorTest.time[i] >= t[6]) && (MweNiSensorTest.time[i] < t[7])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = -1;
			} else if ((MweNiSensorTest.time[i] >= t[7]) && (MweNiSensorTest.time[i] < t[8])) {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = MweNiSensorTest.ersatzQuerrschnittDaten[i];
			} else {
				MweNiSensorTest.ersetzteAusgabeDaten[i] = -1;
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
			if (!original.getObject().getPid().equals(MweNiSensorTest.zentralSensor.getPid())) {
				return;
			}
			double ni = nutzDatum.getItem("NiederschlagsIntensität").getItem("Wert").asUnscaledValue().doubleValue();
			if (ni >= 0) {
				ni = nutzDatum.getItem("NiederschlagsIntensität").getItem("Wert").asScaledValue().doubleValue();
			} else {
				ni = -1.0;
			}

			Assert.assertTrue(
					"Erwartetes datum: " + MweNiSensorTest.ersetzteAusgabeDaten[MweNiSensorTest.indexEmpf]
							+ " Berechnetes datum: " + ni + " index " + (MweNiSensorTest.indexEmpf),
					MweNiSensorTest.ersetzteAusgabeDaten[MweNiSensorTest.indexEmpf] == ni);
			if (MweNiSensorTest.ersetzteAusgabeDaten[MweNiSensorTest.indexEmpf] == ni) {
				System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", MweNiSensorTest.indexEmpf,
						MweNiSensorTest.ersetzteAusgabeDaten[MweNiSensorTest.indexEmpf], ni));
			} else {
				System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f != %3f", MweNiSensorTest.indexEmpf,
						MweNiSensorTest.ersetzteAusgabeDaten[MweNiSensorTest.indexEmpf], ni));
			}

			MweNiSensorTest.indexEmpf++;
			synchronized (AbstraktMweUfdsSensor.dieVerwaltung) {
				if (MweNiSensorTest.indexEmpf >= MweNiSensorTest.ersetzteAusgabeDaten.length) {
					MweNiSensorJunitTester.warten = false;
				}
				AbstraktMweUfdsSensor.dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs.publiziere(original, nutzDatum);
		}
	}
}
