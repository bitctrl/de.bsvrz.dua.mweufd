/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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

import junit.framework.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.dua.mweufd.wfd.MweWfdSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweWfdSensor zu testen
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 * @version $Id$
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
	static long ZEIT_INTERVALL;

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
	static protected DataDescription DD_MESSWERTE, DD_NIMESSWERTE,
			DD_MESSWERT_ERSETZUNG;
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
	 */
	public MweWfdSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);

		if (!sensor.getObjekt().getPid()
				.equals("ufdSensor.testWFD.wfd.zentral"))
			return;

		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);

		zentralSensor = dav.getDataModel().getObject(
				"ufdSensor.testWFD.wfd.zentral");
		vorSensor = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.vor");
		nachSensor = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.nach");
		ersatzSensor = dav.getDataModel().getObject(
				"ufdSensor.testWFD.wfd.ersatz");
		niSensor = dav.getDataModel().getObject("ufdSensor.testWFD.ni");
		nebenSensor1 = dav.getDataModel().getObject(
				"ufdSensor.testWFD.wfd.neben1");
		nebenSensor2 = dav.getDataModel().getObject(
				"ufdSensor.testWFD.wfd.neben2");
		nebenSensor3 = dav.getDataModel().getObject(
				"ufdSensor.testWFD.wfd.neben3");

		DD_MESSWERTE = new DataDescription(dav.getDataModel()
				.getAttributeGroup("atg.ufdsWasserFilmDicke"), dav
				.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		DD_NIMESSWERTE = new DataDescription(dav.getDataModel()
				.getAttributeGroup("atg.ufdsNiederschlagsIntensität"), dav
				.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		Collection<SystemObject> list = new LinkedList<SystemObject>();

		list.add(zentralSensor);
		list.add(ersatzSensor);
		list.add(nachSensor);
		list.add(vorSensor);
		list.add(nebenSensor1);
		list.add(nebenSensor2);
		list.add(nebenSensor3);

		sender.anmeldeQuelle(list, DD_MESSWERTE);
		sender.anmeldeParametrierung(zentralSensor);

		sender.anmeldeQuelle(niSensor, DD_NIMESSWERTE);

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
	public static void parametriereSensor(
			long messwertFortschreibungsIntervall,
			long messWertErsetzungIntervall, long periode) {
		sender.parametriereSensor(zentralSensor,
				messwertFortschreibungsIntervall, messWertErsetzungIntervall,
				periode);
	}

	/**
	 * Sendet die Daten des naechsten Schrittes
	 * 
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	static public boolean naechsterCyklus() {
		if (indexSend >= ersatzQuerrschnittDaten.length)
			return false;

		sender.sendeDatenSatz(zentralSensor, DD_MESSWERTE, "WasserFilmDicke",
				prueflingDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(vorSensor, DD_MESSWERTE, "WasserFilmDicke",
				vorherigeNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(nachSensor, DD_MESSWERTE, "WasserFilmDicke",
				nachfolgeneNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(ersatzSensor, DD_MESSWERTE, "WasserFilmDicke",
				ersatzQuerrschnittDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(nebenSensor1, DD_MESSWERTE, "WasserFilmDicke",
				direkterNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(nebenSensor2, DD_MESSWERTE, "WasserFilmDicke",
				direkterNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(nebenSensor3, DD_MESSWERTE, "WasserFilmDicke",
				direkterNachbarDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(niSensor, DD_NIMESSWERTE,
				"NiederschlagsIntensität", niederschlagIntensitaet[indexSend],
				time[indexSend]);

		indexSend++;
		return true;
	}

	/**
	 * Generiert die Testdaten nach der Pruefspezifikation
	 * 
	 * @param t1
	 *            Messwertfortsetzungsintervall
	 * @param tE
	 *            Messwertersetzungsintervall
	 * @param T
	 *            Periode
	 */
	static public void generiereTestDatenNachPruefSpezWFD_1(long t1, long tE,
			long T) {

		double w1 = 1.0;
		double w2 = 0.8;
		double w3 = 2.6;
		double w4 = 0.3;
		double wni = 2.8;
		double wd = 0.2;

		ZEIT_INTERVALL = T;
		int length = (int) (tE / T) + 5;

		prueflingDaten = new double[length];
		direkterNachbarDaten = new double[length];
		vorherigeNachbarDaten = new double[length];
		nachfolgeneNachbarDaten = new double[length];
		ersatzQuerrschnittDaten = new double[length];
		ersetzteAusgabeDaten = new double[length];
		niederschlagIntensitaet = new double[length];
		bereich = new int[length];

		time = new long[length];
		// Zeit
		for (int i = 0; i < length; i++)
			time[i] = i * T;

		// Intervalle
		long t[] = new long[10];
		long t_int = (tE - t1) / 8;
		t[0] = T;
		t[1] = t[0] + t1;

		for (int i = 2; i < 10; i++)
			t[i] = t[i - 1] + t_int;

		// Ersatzquerrschnittdaten
		for (int i = 0; i < length; i++)
			if (time[i] >= t[7] && time[i] < t[8])
				ersatzQuerrschnittDaten[i] = -1;
			else
				ersatzQuerrschnittDaten[i] = w4;

		// Nachbar Sensor
		for (int i = 0; i < length; i++)
			if (time[i] < t[3])
				vorherigeNachbarDaten[i] = w2;
			else if (time[i] < t[6])
				vorherigeNachbarDaten[i] = w3;
			else
				vorherigeNachbarDaten[i] = -1;

		// Pruefling
		for (int i = 0; i < length; i++)
			if (time[i] < t[0])
				prueflingDaten[i] = w1;
			else
				prueflingDaten[i] = -1;

		// Direkter Nachbar
		for (int i = 0; i < length; i++)
			if (time[i] < t[1])
				direkterNachbarDaten[i] = -1;
			else if (time[i] >= t[1] && time[i] < t[2])
				direkterNachbarDaten[i] = wd;
			else if (time[i] >= t[2] && time[i] < t[5])
				direkterNachbarDaten[i] = -1;
			else if (time[i] >= t[5] && time[i] < t[6])
				direkterNachbarDaten[i] = wd;
			else if (time[i] >= t[6] && time[i] < t[8])
				direkterNachbarDaten[i] = -1;
			else
				direkterNachbarDaten[i] = wd;

		// Nachbar Sensor
		for (int i = 0; i < length; i++)
			if (time[i] < t[4])
				nachfolgeneNachbarDaten[i] = w3;
			else
				nachfolgeneNachbarDaten[i] = 0.0;

		// NI
		for (int i = 0; i < length; i++)
			if (time[i] < t[5])
				niederschlagIntensitaet[i] = wni;
			else
				niederschlagIntensitaet[i] = -1;

		// Ausgabewerte
		double letzterWert = w1;
		for (int i = 0; i < length; i++)
			if (time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
				letzterWert = prueflingDaten[i];
				bereich[i] = 0;
			} else if (time[i] >= t[0] && time[i] < t[1]) {
				ersetzteAusgabeDaten[i] = letzterWert;
				bereich[i] = 1;
			} else if (time[i] >= t[1] && time[i] < t[2]) {
				ersetzteAusgabeDaten[i] = direkterNachbarDaten[i];
				bereich[i] = 1; // 1b
			} else if (time[i] >= t[2] && time[i] < t[3]) {
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i]) / 2.0;
				bereich[i] = 2;
			} else if (time[i] >= t[3] && time[i] < t[4]) {
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i]) / 2.0;
				bereich[i] = 3;
			} else if (time[i] >= t[4] && time[i] < t[5]) {
				ersetzteAusgabeDaten[i] = -1;
				bereich[i] = 4;
			} else if (time[i] >= t[5] && time[i] < t[6]) {
				ersetzteAusgabeDaten[i] = direkterNachbarDaten[i];
				bereich[i] = 4; // 4b
			} else if (time[i] >= t[6] && time[i] < t[7]) {
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
				bereich[i] = 6;
			} else if (time[i] >= t[7] && time[i] < t[8]) {
				ersetzteAusgabeDaten[i] = -1;
				bereich[i] = 7;
			} else if (time[i] >= t[8] && time[i] < t[9]) {
				ersetzteAusgabeDaten[i] = direkterNachbarDaten[i];
				bereich[i] = 7; // 7b
			} else
				ersetzteAusgabeDaten[i] = -1;

		System.out.print(' ');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void publiziere(final ResultData original, final Data nutzDatum) {
		boolean publiziereDatensatz = false;

		if (nutzDatum == null) {
			/**
			 * "keine Daten" wird nur publiziert, wenn das Objekt vorher nicht
			 * auch schon auf keine Daten stand
			 */
			if (this.letztesPubDatum != null
					&& this.letztesPubDatum.getData() != null) {
				publiziereDatensatz = true;
			}
		} else {
			publiziereDatensatz = true;
		}

		if (publiziereDatensatz) {
			if (!original.getObject().getPid().equals(
					"ufdSensor.testWFD.wfd.zentral"))
				return;
			double wfd = nutzDatum.getItem("WasserFilmDicke").getItem("Wert")
					.asUnscaledValue().doubleValue();
			if (wfd >= 0)
				wfd = nutzDatum.getItem("WasserFilmDicke").getItem("Wert")
						.asScaledValue().doubleValue();
			else
				wfd = -1.0;
			Assert.assertTrue("Erwartetes datum: "
					+ ersetzteAusgabeDaten[indexEmpf] + " Berechnetes datum: "
					+ wfd + " index " + (indexEmpf) + " Bereich "
					+ bereich[indexEmpf], Math
					.abs(ersetzteAusgabeDaten[indexEmpf] - wfd) < 0.001);
			System.out.println(String.format(
					"[ %4d ] Bereich %2d Ersatzwert OK: %3f == %3f", indexEmpf,
					bereich[indexEmpf], ersetzteAusgabeDaten[indexEmpf], wfd));
			indexEmpf++;
			synchronized (dieVerwaltung) {
				if (indexEmpf >= ersetzteAusgabeDaten.length)
					MweWfdSensorJunitTester.warten = false;
				dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs
					.publiziere(original, nutzDatum);
		}
	}
}
