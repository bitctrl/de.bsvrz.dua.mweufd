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
import de.bsvrz.dua.mweufd.fbt.MweFbtSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

/**
 * Ermoeglicht die Klasse MweFbtSensor zu Testen.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class MweFbtSensorTest extends MweFbtSensor {

	/**
	 * Testdaten.
	 */
	static double[] prueflingDaten;

	/**
	 * ersatzQuerrschnittDaten.
	 */
	static double[] ersatzQuerrschnittDaten;

	/**
	 * ersetzteAusgabeDaten.
	 */
	static double[] ersetzteAusgabeDaten;

	/**
	 * time.
	 */
	static long[] time;

	/**
	 * Periode der Datensendung.
	 */
	static long zeitIntervall;

	/**
	 * Letzter index der gesendeten und empfangenen Daten.
	 */
	protected static int indexEmpf = 0;

	/**
	 * indexSend.
	 */
	protected static int indexSend = 0;

	/**
	 * Getesteter Sensor.
	 */
	protected static SystemObject zentralSensor;
	/**
	 * Ersatzsensor.
	 */
	protected static SystemObject ersatzSensor;
	/**
	 * Niederschlagintensitaetsensor.
	 */
	protected static SystemObject niSensor;
	/**
	 * Wasserfilmdickesensor.
	 */
	protected static SystemObject wfdSensor;

	/**
	 * Der Wert der NI und WFD Sensore ist Konstant.
	 */
	static double niDaten = 0.0;

	/**
	 * Der Wert der NI und WFD Sensore ist Konstant.
	 */
	static double wfdDaten = 0.0;

	/**
	 * Verbindung zum DAV.
	 */
	protected static ClientDavInterface dav;

	/**
	 * Datenbeschreibung der geschickten daten.
	 */
	protected static DataDescription ddMesswerte, ddWfdMesswerte, ddNiMesswerte, ddMesswertErsetzung;
	/**
	 * Datensender.
	 */
	protected static MweTestDatenSender sender;

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
	 *             wird weitergerechts
	 * @throws UmfeldDatenSensorUnbekannteDatenartException
	 */
	public MweFbtSensorTest(final IVerwaltungMitGuete verwaltung, final DUAUmfeldDatenMessStelle messStelle,
			final DUAUmfeldDatenSensor sensor)
					throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);

		if (!sensor.getObjekt().getPid().equals("ufdSensor.testFBT.fbt.zentral")) {
			return;
		}
		// if(dav != null) return;
		MweFbtSensorTest.dav = verwaltung.getVerbindung();
		MweFbtSensorTest.sender = new MweTestDatenSender(MweFbtSensorTest.dav);

		MweFbtSensorTest.zentralSensor = MweFbtSensorTest.dav.getDataModel().getObject("ufdSensor.testFBT.fbt.zentral");
		MweFbtSensorTest.ersatzSensor = MweFbtSensorTest.dav.getDataModel().getObject("ufdSensor.testFBT.fbt.ersatz");

		MweFbtSensorTest.niSensor = MweFbtSensorTest.dav.getDataModel().getObject("ufdSensor.testFBT.ni");
		MweFbtSensorTest.wfdSensor = MweFbtSensorTest.dav.getDataModel().getObject("ufdSensor.testFBT.wfd");

		MweFbtSensorTest.ddMesswerte = new DataDescription(
				MweFbtSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsFahrBahnOberFlächenTemperatur"),
				MweFbtSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		MweFbtSensorTest.ddWfdMesswerte = new DataDescription(
				MweFbtSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				MweFbtSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		MweFbtSensorTest.ddNiMesswerte = new DataDescription(
				MweFbtSensorTest.dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
				MweFbtSensorTest.dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		final Collection<SystemObject> list = new LinkedList<SystemObject>();

		list.add(MweFbtSensorTest.zentralSensor);
		list.add(MweFbtSensorTest.ersatzSensor);

		MweFbtSensorTest.sender.anmeldeQuelle(list, MweFbtSensorTest.ddMesswerte);
		MweFbtSensorTest.sender.anmeldeParametrierung(MweFbtSensorTest.zentralSensor);

		MweFbtSensorTest.sender.anmeldeQuelle(MweFbtSensorTest.niSensor, MweFbtSensorTest.ddNiMesswerte);
		MweFbtSensorTest.sender.anmeldeQuelle(MweFbtSensorTest.wfdSensor, MweFbtSensorTest.ddWfdMesswerte);

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
		MweFbtSensorTest.sender.parametriereSensor(MweFbtSensorTest.zentralSensor, messwertFortschreibungsIntervall,
				messWertErsetzungIntervall, periode);
	}

	/**
	 * Sendet die Daten des naechsten Schrittes.
	 *
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	public static boolean naechsterCyklus() {
		if (MweFbtSensorTest.indexSend >= MweFbtSensorTest.ersetzteAusgabeDaten.length) {
			return false;
		}

		MweFbtSensorTest.sender.sendeDatenSatz(MweFbtSensorTest.zentralSensor, MweFbtSensorTest.ddMesswerte,
				"FahrBahnOberFlächenTemperatur", MweFbtSensorTest.prueflingDaten[MweFbtSensorTest.indexSend],
				MweFbtSensorTest.time[MweFbtSensorTest.indexSend]);
		MweFbtSensorTest.sender.sendeDatenSatz(MweFbtSensorTest.ersatzSensor, MweFbtSensorTest.ddMesswerte,
				"FahrBahnOberFlächenTemperatur", MweFbtSensorTest.ersatzQuerrschnittDaten[MweFbtSensorTest.indexSend],
				MweFbtSensorTest.time[MweFbtSensorTest.indexSend]);
		MweFbtSensorTest.sender.sendeDatenSatz(MweFbtSensorTest.niSensor, MweFbtSensorTest.ddNiMesswerte,
				"NiederschlagsIntensität", MweFbtSensorTest.niDaten, MweFbtSensorTest.time[MweFbtSensorTest.indexSend]);
		MweFbtSensorTest.sender.sendeDatenSatz(MweFbtSensorTest.wfdSensor, MweFbtSensorTest.ddWfdMesswerte,
				"WasserFilmDicke", MweFbtSensorTest.wfdDaten, MweFbtSensorTest.time[MweFbtSensorTest.indexSend]);

		MweFbtSensorTest.indexSend++;
		return true;
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

		final double w1 = 4.0;
		final double w2 = 1.2;
		final double w3 = 0.4;

		final int length = (int) (tE / tT) + 5;

		MweFbtSensorTest.prueflingDaten = new double[length];
		MweFbtSensorTest.ersatzQuerrschnittDaten = new double[length];
		MweFbtSensorTest.ersetzteAusgabeDaten = new double[length];

		MweFbtSensorTest.zeitIntervall = tT;
		MweFbtSensorTest.time = new long[length];
		MweFbtSensorTest.time[0] = 0;

		final long[] t = new long[5];
		final long tInt = (tE - t1) / 3;
		t[0] = tT;
		t[1] = t[0] + t1;
		t[2] = t[1] + tInt;
		t[3] = t[2] + tInt;
		t[4] = t[3] + tInt;

		// Zeit
		for (int i = 0; i < length; i++) {
			MweFbtSensorTest.time[i] = i * tT;
		}

		// Ersatzquerrschnittdaten
		for (int i = 0; i < length; i++) {
			if (MweFbtSensorTest.time[i] < t[2]) {
				MweFbtSensorTest.ersatzQuerrschnittDaten[i] = w2;
			} else if ((MweFbtSensorTest.time[i] >= t[2]) && (MweFbtSensorTest.time[i] < t[3])) {
				MweFbtSensorTest.ersatzQuerrschnittDaten[i] = -1;
			} else {
				MweFbtSensorTest.ersatzQuerrschnittDaten[i] = w3;
			}
		}

		// Pruefling
		for (int i = 0; i < length; i++) {
			if (MweFbtSensorTest.time[i] < t[0]) {
				MweFbtSensorTest.prueflingDaten[i] = w1;
			} else {
				MweFbtSensorTest.prueflingDaten[i] = -1;
			}
		}

		// Ausgabewerte
		double letzterWert = w1;
		for (int i = 0; i < length; i++) {
			if (MweFbtSensorTest.time[i] < t[0]) {
				MweFbtSensorTest.ersetzteAusgabeDaten[i] = MweFbtSensorTest.prueflingDaten[i];
				letzterWert = MweFbtSensorTest.prueflingDaten[i];
			} else if ((MweFbtSensorTest.time[i] >= t[0]) && (MweFbtSensorTest.time[i] < t[1])) {
				MweFbtSensorTest.ersetzteAusgabeDaten[i] = letzterWert;
			} else if ((MweFbtSensorTest.time[i] >= t[1]) && (MweFbtSensorTest.time[i] < t[2])) {
				MweFbtSensorTest.ersetzteAusgabeDaten[i] = MweFbtSensorTest.ersatzQuerrschnittDaten[i];
			} else if ((MweFbtSensorTest.time[i] >= t[2]) && (MweFbtSensorTest.time[i] < t[3])) {
				MweFbtSensorTest.ersetzteAusgabeDaten[i] = -1;
			} else if ((MweFbtSensorTest.time[i] >= t[3]) && (MweFbtSensorTest.time[i] < t[4])) {
				MweFbtSensorTest.ersetzteAusgabeDaten[i] = MweFbtSensorTest.ersatzQuerrschnittDaten[i];
			} else {
				MweFbtSensorTest.ersetzteAusgabeDaten[i] = -1;
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
			if (!original.getObject().getPid().equals(MweFbtSensorTest.zentralSensor.getPid())) {
				return;
			}

			double sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur").getItem("Wert").asUnscaledValue()
					.doubleValue();
			if (sw >= 0) {
				sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur").getItem("Wert").asScaledValue().doubleValue();
			} else {
				sw = -1;
			}
			Assert.assertTrue(
					"Erwartetes datum: " + MweFbtSensorTest.ersetzteAusgabeDaten[MweFbtSensorTest.indexEmpf]
							+ " Berechnetes datum: " + sw + " index " + (MweFbtSensorTest.indexEmpf),
							Math.abs(MweFbtSensorTest.ersetzteAusgabeDaten[MweFbtSensorTest.indexEmpf] - sw) < 0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", MweFbtSensorTest.indexEmpf,
					MweFbtSensorTest.ersetzteAusgabeDaten[MweFbtSensorTest.indexEmpf], sw));
			MweFbtSensorTest.indexEmpf++;
			synchronized (AbstraktMweUfdsSensor.dieVerwaltung) {
				if (MweFbtSensorTest.indexEmpf >= MweFbtSensorTest.ersetzteAusgabeDaten.length) {
					MweFbtSensorJunitTester.setWarten(false);
				}
				AbstraktMweUfdsSensor.dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs.publiziere(original, nutzDatum);
		}
	}
}
