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
import de.bsvrz.dua.mweufd.sw.MweSwSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweSwSensor zu testen
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 * @version $Id$
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
	static protected DataDescription DD_MESSWERTE, DD_MESSWERT_ERSETZUNG;
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
	public MweSwSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		// if(dav != null) return;
		if (!sensor.getObjekt().getPid().equals("ufdSensor.testSW.sw.zentral"))
			return;
		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);

		zentralSensor = dav.getDataModel().getObject(
				"ufdSensor.testSW.sw.zentral");
		nachfolgerSensor = dav.getDataModel().getObject(
				"ufdSensor.testSW.sw.nach");

		DD_MESSWERTE = new DataDescription(dav.getDataModel()
				.getAttributeGroup("atg.ufdsSichtWeite"), dav.getDataModel()
				.getAspect("asp.plausibilitätsPrüfungLogisch"));

		Collection<SystemObject> list = new LinkedList<SystemObject>();
		list.add(zentralSensor);
		list.add(nachfolgerSensor);

		sender.anmeldeQuelle(list, DD_MESSWERTE);
		sender.anmeldeParametrierung(zentralSensor);
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
		if (indexSend >= nachfolgerDaten.length)
			return false;

		sender.sendeDatenSatz(zentralSensor, DD_MESSWERTE, "SichtWeite",
				prueflingDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(nachfolgerSensor, DD_MESSWERTE, "SichtWeite",
				nachfolgerDaten[indexSend], time[indexSend]);

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
	static public void generiereTestDatenNachPruefSpezSW_1(long t1, long tE,
			long T) {

		long w1 = 40;
		long w2 = 60;
		long w3 = 80;

		ZEIT_INTERVALL = T;
		int length = (int) (tE / T) + 5;

		prueflingDaten = new long[length];
		nachfolgerDaten = new long[length];
		ersetzteAusgabeDaten = new long[length];

		time = new long[length];
		// Zeit
		for (int i = 0; i < length; i++)
			time[i] = i * T;

		// Intervalle
		long t[] = new long[5];
		long t_int = (tE - t1) / 3;

		t[0] = T;
		t[1] = t[0] + t_int;
		t[2] = t[1] + t1;
		t[3] = t[2] + t_int;
		t[4] = t[3] + t_int;

		// Nachfolgerdaten
		for (int i = 0; i < length; i++)
			if (time[i] < t[1])
				nachfolgerDaten[i] = w2;
			else if (time[i] >= t[1] && time[i] < t[3])
				nachfolgerDaten[i] = -1;
			else
				nachfolgerDaten[i] = w3;

		// Pruefling
		for (int i = 0; i < length; i++)
			if (time[i] < t[0])
				prueflingDaten[i] = w1;
			else
				prueflingDaten[i] = -1;

		// Ausgabewerte
		long letzterWert = w1;
		for (int i = 0; i < length; i++)
			if (time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
			} else if (time[i] >= t[0] && time[i] < t[1]) {
				ersetzteAusgabeDaten[i] = nachfolgerDaten[i];
				letzterWert = ersetzteAusgabeDaten[i];
			} else if (time[i] >= t[1] && time[i] < t[2]) {
				ersetzteAusgabeDaten[i] = letzterWert;
			} else if (time[i] >= t[2] && time[i] < t[3]) {
				ersetzteAusgabeDaten[i] = -1;
			} else if (time[i] >= t[3] && time[i] < t[4]) {
				ersetzteAusgabeDaten[i] = nachfolgerDaten[i];
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
			if (!original.getObject().getPid().equals(zentralSensor.getPid()))
				return;

			long sw = nutzDatum.getItem("SichtWeite").getItem("Wert")
					.asUnscaledValue().longValue();
			if (sw >= 0)
				sw = nutzDatum.getItem("SichtWeite").getItem("Wert")
						.asScaledValue().longValue();
			else
				sw = -1;
			Assert.assertTrue("Erwartetes datum: "
					+ ersetzteAusgabeDaten[indexEmpf] + " Berechnetes datum: "
					+ sw + " index " + (indexEmpf), Math
					.abs(ersetzteAusgabeDaten[indexEmpf] - sw) < 0.001);
			System.out.println(String.format(
					"[ %4d ] Ersatzwert OK: %3d == %3d", indexEmpf,
					ersetzteAusgabeDaten[indexEmpf], sw));
			indexEmpf++;
			synchronized (dieVerwaltung) {
				if (indexEmpf >= ersetzteAusgabeDaten.length)
					MweSwSensorJunitTester.warten = false;
				dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs
					.publiziere(original, nutzDatum);
		}
	}
}
