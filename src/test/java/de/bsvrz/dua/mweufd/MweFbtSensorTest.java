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
 * 
 * @version $Id$
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
	protected static DataDescription ddMesswerte, ddWfdMesswerte,
			ddNiMesswerte, ddMesswertErsetzung;
	/**
	 * Datensender.
	 */
	protected static MweTestDatenSender sender;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param messStelle die Messstelle 
	 * @param sensor der Sensor
	 * @throws DUAInitialisierungsException wird weitergerechts
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	public MweFbtSensorTest(final IVerwaltungMitGuete verwaltung,
			final DUAUmfeldDatenMessStelle messStelle, final DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException, UmfeldDatenSensorUnbekannteDatenartException {
		super(verwaltung, messStelle, sensor);

		if (!sensor.getObjekt().getPid()
				.equals("ufdSensor.testFBT.fbt.zentral")) {
			return;
		}
		// if(dav != null) return;
		dav = verwaltung.getVerbindung();
		sender = new MweTestDatenSender(dav);

		zentralSensor = dav.getDataModel().getObject(
				"ufdSensor.testFBT.fbt.zentral");
		ersatzSensor = dav.getDataModel().getObject(
				"ufdSensor.testFBT.fbt.ersatz");

		niSensor = dav.getDataModel().getObject("ufdSensor.testFBT.ni");
		wfdSensor = dav.getDataModel().getObject("ufdSensor.testFBT.wfd");

		ddMesswerte = new DataDescription(dav.getDataModel()
				.getAttributeGroup("atg.ufdsFahrBahnOberFlächenTemperatur"),
				dav.getDataModel()
						.getAspect("asp.plausibilitätsPrüfungLogisch"));

		ddWfdMesswerte = new DataDescription(dav.getDataModel()
				.getAttributeGroup("atg.ufdsWasserFilmDicke"), dav
				.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		ddNiMesswerte = new DataDescription(dav.getDataModel()
				.getAttributeGroup("atg.ufdsNiederschlagsIntensität"), dav
				.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));

		final Collection<SystemObject> list = new LinkedList<SystemObject>();

		list.add(zentralSensor);
		list.add(ersatzSensor);

		sender.anmeldeQuelle(list, ddMesswerte);
		sender.anmeldeParametrierung(zentralSensor);

		sender.anmeldeQuelle(niSensor, ddNiMesswerte);
		sender.anmeldeQuelle(wfdSensor, ddWfdMesswerte);

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
	public static void parametriereSensor(
			final long messwertFortschreibungsIntervall,
			final long messWertErsetzungIntervall, final long periode) {
		sender.parametriereSensor(zentralSensor,
				messwertFortschreibungsIntervall, messWertErsetzungIntervall,
				periode);
	}

	/**
	 * Sendet die Daten des naechsten Schrittes.
	 * 
	 * @return <code>true</code> wenn man mit dem Test fortsetzen soll, sonst
	 *         false
	 */
	public static boolean naechsterCyklus() {
		if (indexSend >= ersetzteAusgabeDaten.length) {
			return false;
		}

		sender.sendeDatenSatz(zentralSensor, ddMesswerte,
				"FahrBahnOberFlächenTemperatur", prueflingDaten[indexSend],
				time[indexSend]);
		sender.sendeDatenSatz(ersatzSensor, ddMesswerte,
				"FahrBahnOberFlächenTemperatur",
				ersatzQuerrschnittDaten[indexSend], time[indexSend]);
		sender.sendeDatenSatz(niSensor, ddNiMesswerte,
				"NiederschlagsIntensität", niDaten, time[indexSend]);
		sender.sendeDatenSatz(wfdSensor, ddWfdMesswerte, "WasserFilmDicke",
				wfdDaten, time[indexSend]);

		indexSend++;
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
	public static void generiereTestDatenNachPruefSpez1(final long t1, final long tE,
			final long tT) {

		final double w1 = 4.0;
		final double w2 = 1.2;
		final double w3 = 0.4;

		final int length = (int) (tE / tT) + 5;

		prueflingDaten = new double[length];
		ersatzQuerrschnittDaten = new double[length];
		ersetzteAusgabeDaten = new double[length];

		zeitIntervall = tT;
		time = new long[length];
		time[0] = 0;

		final long[] t = new long[5];
		final long tInt = (tE - t1) / 3;
		t[0] = tT;
		t[1] = t[0] + t1;
		t[2] = t[1] + tInt;
		t[3] = t[2] + tInt;
		t[4] = t[3] + tInt;

		// Zeit
		for (int i = 0; i < length; i++) {
			time[i] = i * tT;
		}

		// Ersatzquerrschnittdaten
		for (int i = 0; i < length; i++) {
			if (time[i] < t[2]) {
				ersatzQuerrschnittDaten[i] = w2;
			} else if (time[i] >= t[2] && time[i] < t[3]) {
				ersatzQuerrschnittDaten[i] = -1;
			} else {
				ersatzQuerrschnittDaten[i] = w3;
			}
		}

		// Pruefling
		for (int i = 0; i < length; i++) {
			if (time[i] < t[0]) {
				prueflingDaten[i] = w1;
			} else {
				prueflingDaten[i] = -1;
			}
		}

		// Ausgabewerte
		double letzterWert = w1;
		for (int i = 0; i < length; i++) {
			if (time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
				letzterWert = prueflingDaten[i];
			} else if (time[i] >= t[0] && time[i] < t[1]) {
				ersetzteAusgabeDaten[i] = letzterWert;
			} else if (time[i] >= t[1] && time[i] < t[2]) {
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			} else if (time[i] >= t[2] && time[i] < t[3]) {
				ersetzteAusgabeDaten[i] = -1;
			} else if (time[i] >= t[3] && time[i] < t[4]) {
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			} else {
				ersetzteAusgabeDaten[i] = -1;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void publiziere(final ResultData original, final Data nutzDatum) {
		boolean publiziereDatensatz = false;

		if (nutzDatum == null) {
			if (this.letztesPubDatum != null
					&& this.letztesPubDatum.getData() != null) {
				publiziereDatensatz = true;
			}
		} else {
			publiziereDatensatz = true;
		}

		if (publiziereDatensatz) {
			if (!original.getObject().getPid().equals(zentralSensor.getPid())) {
				return;
			}

			double sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur")
					.getItem("Wert").asUnscaledValue().doubleValue();
			if (sw >= 0) {
				sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur")
						.getItem("Wert").asScaledValue().doubleValue();
			} else {
				sw = -1;
			}
			Assert.assertTrue("Erwartetes datum: "
					+ ersetzteAusgabeDaten[indexEmpf] + " Berechnetes datum: "
					+ sw + " index " + (indexEmpf), Math
					.abs(ersetzteAusgabeDaten[indexEmpf] - sw) < 0.001);
			System.out.println(String.format(
					"[ %4d ] Ersatzwert OK: %3f == %3f", indexEmpf,
					ersetzteAusgabeDaten[indexEmpf], sw));
			indexEmpf++;
			synchronized (dieVerwaltung) {
				if (indexEmpf >= ersetzteAusgabeDaten.length) {
					MweFbtSensorJunitTester.warten = false;
				}
				dieVerwaltung.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.dieDfs
					.publiziere(original, nutzDatum);
		}
	}
}
