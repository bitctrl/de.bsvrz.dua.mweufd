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
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse Mwe_Tpt_Lt_Ns_Fbz_Sensor zu Testen Generiert Testdaten
 * und vergleicht mit den Ausgabedaten der originalen Klasse.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class MweTptLtNsFbzSensorTest extends MweTptLtNsFbzSensor {

	/**
	 * Testdaten.
	 */
	protected static double[] prueflingDaten;

	/**
	 * Testdaten.
	 */
	protected static double[] ersatzQuerrschnittDaten;

	/**
	 * Testdaten.
	 */
	protected static double[] ersetzteAusgabeDaten;

	/**
	 * Testdaten.
	 */
	protected static long[] time;

	/**
	 * Letzter index der gesendeten und empfangenen Daten.
	 */
	protected static int indexEmpf = 0;

	/**
	 * Testdaten.
	 */
	protected static int indexSend = 0;

	/**
	 * <code>true</code>, wenn alle Daten initialisiert sind.
	 */
	protected static boolean initialisiert = false;

	/**
	 * Hauptsensor.
	 */
	protected static SystemObject zentralSensor;

	/**
	 * Ersatzsensor.
	 */
	protected static SystemObject ersatzSensor;

	/**
	 * Verbindung zum dav.
	 */
	protected static ClientDavInterface dav;

	/**
	 * Datenbeschreibung der Messwert-Attributgruppe.
	 */
	protected static DataDescription ddMesswerte;

	/**
	 * Die Sensore und getestete Attribute.
	 */
	protected static String zentralSensorName = "ufdSensor.testFBZ.fbz.zentral";

	/**
	 * Die Sensore und getestete Attribute.
	 */
	protected static String ersatzSensorName = "ufdSensor.testFBZ.fbz.ersatz";

	/**
	 * Die Sensore und getestete Attribute.
	 */
	protected static String attribut = "FahrBahnOberFlächenZustand";

	/**
	 * Datensender.
	 */
	protected static MweTestDatenSender sender;

	/**
	 * Die Testwerte.
	 */
	protected static double w1 = 4;

	/**
	 * Die Testwerte.
	 */
	protected static double w2 = 1;

	/**
	 * Die Testwerte.
	 */
	protected static double w3 = 2;

	/**
	 * Setzt die Testwerte.
	 *
	 * @param w1X
	 *            W1
	 * @param w2P
	 *            W2
	 * @param w3X
	 *            W3
	 */
	public static void setTestWerte(final double w1X, final double w2P, final double w3X) {
		MweTptLtNsFbzSensorTest.w1 = w1X;
		MweTptLtNsFbzSensorTest.w2 = w2P;
		MweTptLtNsFbzSensorTest.w3 = w3X;
	}

	/**
	 * Setzt die Namen des Des Sensors und seiner Attribute.
	 *
	 * @param zentralSensor1
	 *            Hauptsensor
	 * @param ersatzSensor1
	 *            Ersatzsensor
	 * @param attribut1
	 *            Getesteter Attribut
	 */
	public static void setSensorUndAttribut(final String zentralSensor1, final String ersatzSensor1,
			final String attribut1) {
		MweTptLtNsFbzSensorTest.zentralSensorName = zentralSensor1;
		MweTptLtNsFbzSensorTest.ersatzSensorName = ersatzSensor1;
		MweTptLtNsFbzSensorTest.attribut = attribut1;
	}

	/**
	 * Setzt den Flag initialsisiert.
	 *
	 * @param init
	 *            true, wenn schon initialisiert ist
	 */
	public static void setInititalisiert(final boolean init) {
		MweTptLtNsFbzSensorTest.initialisiert = init;
	}

	/**
	 * Initialisiert die Testklasse ins Ausgangzustand.
	 */
	public static void reset() {
		MweTptLtNsFbzSensorTest.setSensorUndAttribut(null, null, null);
		MweTptLtNsFbzSensorTest.initialisiert = false;
		MweTptLtNsFbzSensorTest.zentralSensor = null;
		MweTptLtNsFbzSensorTest.ersatzSensor = null;
	}

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param messStelle
	 *            die Messstelle
	 * @param sensor
	 *            der Sensor
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public MweTptLtNsFbzSensorTest(final IVerwaltungMitGuete verwaltung, final DUAUmfeldDatenMessStelle messStelle,
			final DUAUmfeldDatenSensor sensor)
					throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);

		if (MweTptLtNsFbzSensorTest.initialisiert || (MweTptLtNsFbzSensorTest.attribut == null)) {
			return;
		}
		MweTptLtNsFbzSensorTest.dav = verwaltung.getVerbindung();
		MweTptLtNsFbzSensorTest.sender = new MweTestDatenSender(MweTptLtNsFbzSensorTest.dav);

		MweTptLtNsFbzSensorTest.ddMesswerte = new DataDescription(
				MweTptLtNsFbzSensorTest.dav.getDataModel()
						.getAttributeGroup("atg.ufds" + MweTptLtNsFbzSensorTest.attribut),
				MweTptLtNsFbzSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		MweTptLtNsFbzSensorTest.zentralSensor = MweTptLtNsFbzSensorTest.dav.getDataModel()
				.getObject(MweTptLtNsFbzSensorTest.zentralSensorName);
		MweTptLtNsFbzSensorTest.ersatzSensor = MweTptLtNsFbzSensorTest.dav.getDataModel()
				.getObject(MweTptLtNsFbzSensorTest.ersatzSensorName);

		final Collection<SystemObject> list = new LinkedList<SystemObject>();

		list.add(MweTptLtNsFbzSensorTest.zentralSensor);
		list.add(MweTptLtNsFbzSensorTest.ersatzSensor);

		MweTptLtNsFbzSensorTest.sender.anmeldeQuelle(list, MweTptLtNsFbzSensorTest.ddMesswerte);
		MweTptLtNsFbzSensorTest.sender.anmeldeParametrierung(MweTptLtNsFbzSensorTest.zentralSensor);

		MweTptLtNsFbzSensorTest.indexSend = 0;
		MweTptLtNsFbzSensorTest.initialisiert = true;
	}

	/**
	 * Sendet die Daten des naechsten Schrittes.
	 *
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	public static boolean naechsterZyklus() {
		if (MweTptLtNsFbzSensorTest.indexSend >= MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten.length) {
			return false;
		}

		MweTptLtNsFbzSensorTest.sender.sendeDatenSatz(MweTptLtNsFbzSensorTest.zentralSensor,
				MweTptLtNsFbzSensorTest.ddMesswerte, MweTptLtNsFbzSensorTest.attribut,
				MweTptLtNsFbzSensorTest.prueflingDaten[MweTptLtNsFbzSensorTest.indexSend],
				MweTptLtNsFbzSensorTest.time[MweTptLtNsFbzSensorTest.indexSend]);
		MweTptLtNsFbzSensorTest.sender.sendeDatenSatz(MweTptLtNsFbzSensorTest.ersatzSensor,
				MweTptLtNsFbzSensorTest.ddMesswerte, MweTptLtNsFbzSensorTest.attribut,
				MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten[MweTptLtNsFbzSensorTest.indexSend],
				MweTptLtNsFbzSensorTest.time[MweTptLtNsFbzSensorTest.indexSend]);

		MweTptLtNsFbzSensorTest.indexSend++;
		return true;
	}

	/**
	 * Parametreirt den gestesteten Sensor.
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
		MweTptLtNsFbzSensorTest.sender.parametriereSensor(MweTptLtNsFbzSensorTest.zentralSensor,
				messwertFortschreibungsIntervall, messWertErsetzungIntervall, periode);
	}

	/**
	 * Generiert die Testdaten nach der Pruefspezifikation.
	 *
	 * @param t1
	 *            Messwertfortsetzungsintervall
	 * @param tE
	 *            Messwertersetzungsintervall
	 * @param tT
	 *            Periode
	 */
	public static void generiereTestDatenNachPruefSpez1(final long t1, final long tE, final long tT) {

		final int length = (int) (tE / tT) + 5;

		MweTptLtNsFbzSensorTest.prueflingDaten = new double[length];
		MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten = new double[length];
		MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten = new double[length];

		MweTptLtNsFbzSensorTest.time = new long[length];
		MweTptLtNsFbzSensorTest.time[0] = 0;
		MweTptLtNsFbzSensorTest.indexEmpf = MweTptLtNsFbzSensorTest.indexSend = 0;

		final long[] t = new long[5];
		final long tInt = (tE - t1) / 3;
		t[0] = tT;
		t[1] = t[0] + t1;
		t[2] = t[1] + tInt;
		t[3] = t[2] + tInt;
		t[4] = t[3] + tInt;

		// Zeit
		for (int i = 0; i < length; i++) {
			MweTptLtNsFbzSensorTest.time[i] = i * tT;
		}

		// Ersatzquerrschnittdaten
		for (int i = 0; i < length; i++) {
			if (MweTptLtNsFbzSensorTest.time[i] < t[2]) {
				MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten[i] = MweTptLtNsFbzSensorTest.w2;
			} else if ((MweTptLtNsFbzSensorTest.time[i] >= t[2]) && (MweTptLtNsFbzSensorTest.time[i] < t[3])) {
				MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten[i] = -1;
			} else {
				MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten[i] = MweTptLtNsFbzSensorTest.w3;
			}
		}

		// Pruefling
		for (int i = 0; i < length; i++) {
			if (MweTptLtNsFbzSensorTest.time[i] < t[0]) {
				MweTptLtNsFbzSensorTest.prueflingDaten[i] = MweTptLtNsFbzSensorTest.w1;
			} else {
				MweTptLtNsFbzSensorTest.prueflingDaten[i] = -1;
			}
		}

		// Ausgabewerte
		double letzterWert = MweTptLtNsFbzSensorTest.w1;
		for (int i = 0; i < length; i++) {
			if (MweTptLtNsFbzSensorTest.time[i] < t[0]) {
				MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[i] = MweTptLtNsFbzSensorTest.prueflingDaten[i];
				letzterWert = MweTptLtNsFbzSensorTest.prueflingDaten[i];
			} else if ((MweTptLtNsFbzSensorTest.time[i] >= t[0]) && (MweTptLtNsFbzSensorTest.time[i] < t[1])) {
				MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[i] = letzterWert;
			} else if ((MweTptLtNsFbzSensorTest.time[i] >= t[1]) && (MweTptLtNsFbzSensorTest.time[i] < t[2])) {
				MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[i] = MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten[i];
			} else if ((MweTptLtNsFbzSensorTest.time[i] >= t[2]) && (MweTptLtNsFbzSensorTest.time[i] < t[3])) {
				MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[i] = -1;
			} else if ((MweTptLtNsFbzSensorTest.time[i] >= t[3]) && (MweTptLtNsFbzSensorTest.time[i] < t[4])) {
				MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[i] = MweTptLtNsFbzSensorTest.ersatzQuerrschnittDaten[i];
			} else {
				MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[i] = -1;
			}
		}
	}

	@Override
	protected void publiziere(final ResultData original, final Data nutzDatum) {
		boolean publiziereDatensatz = false;

		if (nutzDatum == null) {
			if ((this.letztesPubDatum != null) && (this.letztesPubDatum.getData() != null)) {
				publiziereDatensatz = true;
			}
		} else {
			publiziereDatensatz = true;
		}

		if (publiziereDatensatz) {

			if ((MweTptLtNsFbzSensorTest.attribut == null) || (nutzDatum == null)) {
				return;
			}
			if (!original.getObject().getPid().equals(MweTptLtNsFbzSensorTest.zentralSensor.getPid())) {
				return;
			}

			double sw = nutzDatum.getItem(MweTptLtNsFbzSensorTest.attribut).getItem("Wert").asUnscaledValue()
					.doubleValue();
			if (sw >= 0) {
				try {
					sw = nutzDatum.getItem(MweTptLtNsFbzSensorTest.attribut).getItem("Wert").asScaledValue()
							.doubleValue();
				} catch (final ArithmeticException e) {
					//
				}
			} else {
				sw = -1;
			}

			Assert.assertTrue(
					"Erwartetes datum: "
							+ MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[MweTptLtNsFbzSensorTest.indexEmpf]
							+ " Berechnetes datum: " + sw + " index " + (MweTptLtNsFbzSensorTest.indexEmpf),
					Math.abs(MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[MweTptLtNsFbzSensorTest.indexEmpf]
							- sw) < 0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", MweTptLtNsFbzSensorTest.indexEmpf,
					MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten[MweTptLtNsFbzSensorTest.indexEmpf], sw));
			if (++MweTptLtNsFbzSensorTest.indexEmpf >= MweTptLtNsFbzSensorTest.ersetzteAusgabeDaten.length) {
				synchronized (AbstraktMweUfdsSensor.dieVerwaltung) {
					MweTptLtNsFbzSensorJunitTester.warten = false;
					AbstraktMweUfdsSensor.dieVerwaltung.notifyAll();
				}
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs.publiziere(original, nutzDatum);
		}
	}
}
