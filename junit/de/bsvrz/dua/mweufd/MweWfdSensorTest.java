package de.bsvrz.dua.mweufd;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.dua.mweufd.wfd.MweWfdSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class MweWfdSensorTest extends MweWfdSensor {

	static double [] prueflingDaten;
	static double [] direkterNachbarDaten;
	static double [] vorherigeNachbarDaten;
	static double [] nachfolgeneNachbarDaten;
	static double [] ersatzQuerrschnittDaten;
	static double [] niederschlagIntensitaet;
	static double [] ersetzteAusgabeDaten;
	static long   [] time;
	static int    [] bereich;
	static long ZEIT_INTERVALL;
	static int index = 0;
	static int indexSend = 0;

	static protected SystemObject wfdSensor;
	static protected SystemObject niSensor;
	static protected SystemObject vorSensor;
	static protected SystemObject nachSensor;
	static protected SystemObject ersatzSensor;
	static protected SystemObject nebenSensor1, nebenSensor2, nebenSensor3;
	
	static protected ClientDavInterface dav;
	static protected DataDescription DD_MESSWERTE, DD_NIMESSWERTE, DD_MESSWERT_ERSETZUNG;
	
	static public void generiereTestDatenNachPruefSpezWFD_1(long t1, long tE, long T) {
		
		double w1 =  1.0;
		double w2 =  0.8;
		double w3 =  2.6;
		double w4 =  0.3;
		double wni = 2.8;
		double wd =  0.2;
		
		ZEIT_INTERVALL = T;
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		direkterNachbarDaten = new double [length];
		vorherigeNachbarDaten = new double [length];
		nachfolgeneNachbarDaten  = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		niederschlagIntensitaet  = new double [length];
		bereich = new int[length];
		
		time = new long [length];
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Intervalle
		long t[] = new long [10];
		long t_int = ( tE - t1 ) / 8;
		t[0] = T;
		t[1] = t[0] + t1;
		
		for(int i=2; i<10; i++)
			t[i] = t[i-1] + t_int;
	
		
		// Ersatzquerrschnittdaten
		for(int i=0; i<length; i++)
			if(time[i]>=t[7] && time[i]<t[8])
				ersatzQuerrschnittDaten[i] = -1;
			else
				ersatzQuerrschnittDaten[i] = w4;
		
		// Nachbar Sensor
		for(int i=0; i<length; i++)
			if(time[i]<t[3])
				vorherigeNachbarDaten[i] = w2;
			else if(time[i]<t[6])
				vorherigeNachbarDaten[i] = w3;
			else
				vorherigeNachbarDaten[i] = -1;
				
		// Pruefling
		for(int i=0; i<length; i++)
			if(time[i]<t[0])
				prueflingDaten[i] = w1;
			else prueflingDaten[i] = -1;
		
		// Direkter Nachbar
		for(int i=0; i<length; i++)
			if(time[i]<t[1])
				direkterNachbarDaten[i] = -1;
			else if(time[i] >= t[1] && time[i] < t[2]) direkterNachbarDaten[i] = wd;
			else if(time[i] >= t[2] && time[i] < t[5]) direkterNachbarDaten[i] = -1;
			else if(time[i] >= t[5] && time[i] < t[6]) direkterNachbarDaten[i] = wd;
			else if(time[i] >= t[6] && time[i] < t[8]) direkterNachbarDaten[i] = -1;
			else direkterNachbarDaten[i] = wd;
		
		// Nachbar Sensor
		for(int i=0; i<length; i++)
			if(time[i]<t[4])
				nachfolgeneNachbarDaten[i] = w3;
			else nachfolgeneNachbarDaten[i] = 0.0;
		
		// NI
		for(int i=0; i<length; i++)
			if(time[i] < t[5])
				niederschlagIntensitaet[i] = wni;
			else niederschlagIntensitaet[i] = -1;
		
		// Ausgabewerte
		double letzterWert = w1;
		for(int i=0; i<length; i++)
			if(time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
				letzterWert = prueflingDaten[i];
				bereich[i] = 0;
			}
			else if(time[i] >= t[0] && time[i] < t[1]) {
				ersetzteAusgabeDaten[i] = letzterWert;
				bereich[i] = 1;
			}
			else if(time[i] >= t[1] && time[i] < t[2]) {
				ersetzteAusgabeDaten[i] = direkterNachbarDaten[i];
				bereich[i] = 1; // 1b
			}
			else if(time[i] >= t[2] && time[i] < t[3]) {
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i])/2.0;
				bereich[i] = 2;
			}
			else if(time[i] >= t[3] && time[i] < t[4]) {
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i])/2.0;
				bereich[i] = 3;
			}
			else if(time[i] >= t[4] && time[i] < t[5]) {
				ersetzteAusgabeDaten[i] = -1;
				bereich[i] = 4;
			}
			else if(time[i] >= t[5] && time[i] < t[6]) {
				ersetzteAusgabeDaten[i] = direkterNachbarDaten[i];
				bereich[i] = 4; // 4b
			}
			else if(time[i] >= t[6] && time[i] < t[7]) {
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
				bereich[i] = 6;
			}
			else if(time[i] >= t[7] && time[i] < t[8]) {
				ersetzteAusgabeDaten[i] = -1;
				bereich[i] = 7;
			}
			else if(time[i] >= t[8] && time[i] < t[9]) {
				ersetzteAusgabeDaten[i] = direkterNachbarDaten[i];
				bereich[i] = 7; // 7b
			}
			else ersetzteAusgabeDaten[i] = -1;
				
		System.out.print(' ');
	}
	
	
	public MweWfdSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		if(dav != null) return;
		
		dav = verwaltung.getVerbindung();
		wfdSensor = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.zentral");
		vorSensor = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.vor");
		nachSensor = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.nach");
		ersatzSensor = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.ersatz");
		niSensor = dav.getDataModel().getObject("ufdSensor.testWFD.ni");
		nebenSensor1 = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.neben1");
		nebenSensor2 = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.neben2");
		nebenSensor3 = dav.getDataModel().getObject("ufdSensor.testWFD.wfd.neben3");
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	 	DD_NIMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		
		DD_MESSWERT_ERSETZUNG =  new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				dav.getDataModel().getAspect("asp.parameterVorgabe"));
		
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		
		list.add(wfdSensor);
		list.add(ersatzSensor);
		list.add(nachSensor);
		list.add(vorSensor);
		list.add(nebenSensor1);
		list.add(nebenSensor2);
		list.add(nebenSensor3);
		
		
		try {
			dav.subscribeSender(this, list, DD_MESSWERTE, SenderRole.source());
			dav.subscribeSender(this, list, DD_MESSWERT_ERSETZUNG, SenderRole.sender());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}
		
		list.clear();
		list.add(niSensor);
		try {
			dav.subscribeSender(this, list, DD_NIMESSWERTE, SenderRole.source());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}
	}
	
	public static void parametriereSensor(long messwertFortschreibungsIntervall, long messWertErsetzungIntervall) {
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"));
		data.getItem("maxZeitMessWertErsetzung").asTimeValue().setMillis(messWertErsetzungIntervall);
		/**
		 * TODO Unkommnetieren
		 */
		//data.getItem("maxZeitMessWertFortschreibung").asTimeValue().setMillis(messWertFortschreibungsIntervall);

		ResultData result = new ResultData(wfdSensor, DD_MESSWERT_ERSETZUNG, System.currentTimeMillis(), data);
		try {
			dav.sendData(result);
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	static public boolean naechsterCyklus() {
		if(indexSend>= ersatzQuerrschnittDaten.length) return false;
		
		//if(indexSend >7 ) return false;
		sendeDatenSatz(wfdSensor, prueflingDaten[indexSend], time[indexSend]);
		sendeDatenSatz(vorSensor, vorherigeNachbarDaten[indexSend], time[indexSend]);
		sendeDatenSatz(nachSensor, nachfolgeneNachbarDaten[indexSend], time[indexSend]);
		sendeDatenSatz(ersatzSensor, ersatzQuerrschnittDaten[indexSend], time[indexSend]);
		sendeDatenSatz(nebenSensor1, direkterNachbarDaten[indexSend], time[indexSend]);
		sendeDatenSatz(nebenSensor2, direkterNachbarDaten[indexSend], time[indexSend]);
		sendeDatenSatz(nebenSensor3, direkterNachbarDaten[indexSend], time[indexSend]);
		sendeNiDatenSatz(niSensor, niederschlagIntensitaet[indexSend], time[indexSend]);
		
		
		indexSend++;
		return true;
	}
	
	static  public void sendeDatenSatz(SystemObject sensor, double messwert, long zeitStempel) {
		
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"));

		String att = "WasserFilmDicke";
		data.getTimeValue("T").setMillis(ZEIT_INTERVALL);
		if(messwert>0)
			data.getItem(att).getScaledValue("Wert").set(messwert);
		else
			data.getItem(att).getUnscaledValue("Wert").set(messwert);
	
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);	
		

		if(messwert>0)
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		else
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(1);
		
		
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(sensor, DD_MESSWERTE, zeitStempel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	static  public void sendeNiDatenSatz(SystemObject sensor, double messwert, long zeitStempel) {
		
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"));

		String att = "NiederschlagsIntensität";
		data.getTimeValue("T").setMillis(ZEIT_INTERVALL);
		if(messwert>0)
			data.getItem(att).getScaledValue("Wert").set(messwert);
		else
			data.getItem(att).getUnscaledValue("Wert").set(messwert);
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
		
		if(messwert>0)
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		else
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(1);
		
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(sensor, DD_NIMESSWERTE, zeitStempel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	protected void publiziere(final ResultData original,
									final Data nutzDatum){
		boolean publiziereDatensatz = false;
		
		if(nutzDatum == null){
			/**
			 * "keine Daten" wird nur publiziert, wenn das Objekt vorher
			 * nicht auch schon auf keine Daten stand
			 */
			if(this.letztesPubDatum != null && this.letztesPubDatum.getData() != null){
				publiziereDatensatz = true;
			}
		}else{
			publiziereDatensatz = true;
		}
		
		if(publiziereDatensatz) {
			if(!original.getObject().getPid().equals("ufdSensor.testWFD.wfd.zentral") ) return;
			double wfd = nutzDatum.getItem("WasserFilmDicke").getItem("Wert").asUnscaledValue().doubleValue();
			if(wfd>=0) wfd = nutzDatum.getItem("WasserFilmDicke").getItem("Wert").asScaledValue().doubleValue();
			else wfd = -1.0;
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[index] + " Berechnetes datum: " + wfd + " index " + (index) + " Bereich " + bereich[index], Math.abs(ersetzteAusgabeDaten[index]- wfd)<0.001);
			System.out.println(String.format("[ %4d ] Bereich %2d Ersatzwert OK: %3f == %3f", index, bereich[index], ersetzteAusgabeDaten[index], wfd));
			index++;
			synchronized (VERWALTUNG) {
				if(index >= ersetzteAusgabeDaten.length) MweWfdSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}
}
