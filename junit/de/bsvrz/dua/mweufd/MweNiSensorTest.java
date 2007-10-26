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
import de.bsvrz.dua.mweufd.ni.MweNiSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class MweNiSensorTest extends MweNiSensor {
	

	static double [] prueflingDaten;
	static double [] vorherigeNachbarDaten;
	static double [] nachfolgeneNachbarDaten;
	static double [] ersatzQuerrschnittDaten;
	static double [] wasserFilmDicke;
	static double [] ersetzteAusgabeDaten;
	static long   [] time;
	static long ZEIT_INTERVALL;
	static int index = 0;
	static int indexSend = 0;

	static protected SystemObject niSensor;
	static protected SystemObject ersatzSensor;
	static protected SystemObject vorSensor;
	static protected SystemObject nachSensor;
	
	static protected SystemObject wfdSensor;
	
	static protected ClientDavInterface dav;

	static protected DataDescription DD_MESSWERTE, DD_WFDMESSWERTE, DD_MESSWERT_ERSETZUNG;
	
	public MweNiSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(dav != null) return;
		
		dav = verwaltung.getVerbindung();
		niSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.zentral");
		vorSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.vor");
		nachSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.nach");
		ersatzSensor = dav.getDataModel().getObject("ufdSensor.testNI.ni.ersatz");
		wfdSensor = dav.getDataModel().getObject("ufdSensor.testNI.wfd");
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		DD_WFDMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		
		DD_MESSWERT_ERSETZUNG =  new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				dav.getDataModel().getAspect("asp.parameterVorgabe"));
		
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		list.add(niSensor);
		list.add(ersatzSensor);
		list.add(nachSensor);
		list.add(vorSensor);
		try {
			dav.subscribeSender(this, list, DD_MESSWERTE, SenderRole.source());
			dav.subscribeSender(this, list, DD_MESSWERT_ERSETZUNG, SenderRole.sender());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}
		
		list.clear();
		list.add(wfdSensor);
		try {
			dav.subscribeSender(this, list, DD_WFDMESSWERTE, SenderRole.source());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}
	}
	
	public static void parametriereSensor(long messWertErsetzungIntervall) {
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"));
		data.getItem("maxZeitMessWertErsetzung").asTimeValue().setMillis(messWertErsetzungIntervall);
		ResultData result = new ResultData(niSensor, DD_MESSWERT_ERSETZUNG, System.currentTimeMillis(), data);
		try {
			dav.sendData(result);
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	static public boolean naechsterCyklus() {
		if(indexSend>= ersatzQuerrschnittDaten.length) return false;
		
		sendeDatenSatz(niSensor, prueflingDaten[indexSend], time[indexSend]);
		sendeDatenSatz(vorSensor, vorherigeNachbarDaten[indexSend], time[indexSend]);
		sendeDatenSatz(nachSensor, nachfolgeneNachbarDaten[indexSend], time[indexSend]);
		sendeDatenSatz(ersatzSensor, ersatzQuerrschnittDaten[indexSend], time[indexSend]);
		sendeWfdDatenSatz(wfdSensor, wasserFilmDicke[indexSend], time[indexSend]);
		
		indexSend++;
		return true;
	}
	
	static  public void sendeDatenSatz(SystemObject sensor, double messwert, long zeitStempel) {
		
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
		
		ResultData result = new ResultData(sensor, DD_MESSWERTE, zeitStempel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	static  public void sendeWfdDatenSatz(SystemObject sensor, double messwert, long zeitStempel) {
		
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
		
		ResultData result = new ResultData(sensor, DD_WFDMESSWERTE, zeitStempel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	static public void generiereTestDatenNachPruefSpezNI_1(long t1, long tE, long T) {
		
		double w1 = 2.0;
		double w2 = 1.5;
		double w3 = 5.5;
		double w4 = 0.5;
		double wfd = 1.8;
		
		ZEIT_INTERVALL = T;
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		vorherigeNachbarDaten = new double [length];
		nachfolgeneNachbarDaten  = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		wasserFilmDicke  = new double [length];
		
		time = new long [length];
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Intervalle
		long t[] = new long [9];
		long t_int = ( tE - t1 ) / 7;
		t[0] = T;
		t[1] = t[0] + t1;
		
		for(int i=2; i<9; i++)
			t[i] = t[i-1] + t_int;
	
		
		// Ersatzquerrschnittdaten
		for(int i=0; i<length; i++)
			if(time[i]>=t[6] && time[i]<t[7])
				ersatzQuerrschnittDaten[i] = -1;
			else
				ersatzQuerrschnittDaten[i] = w4;
		
		// Nachbar Sensor
		for(int i=0; i<length; i++)
			if(time[i]<t[2])
				vorherigeNachbarDaten[i] = w2;
			else if(time[i]<t[5])
				vorherigeNachbarDaten[i] = w3;
			else
				vorherigeNachbarDaten[i] = -1;
				
		// Pruefling
		for(int i=0; i<length; i++)
			if(time[i]<t[0])
				prueflingDaten[i] = w1;
			else prueflingDaten[i] = -1;
		
		// Nachbar Sensor
		for(int i=0; i<length; i++)
			if(time[i]<t[3])
				nachfolgeneNachbarDaten[i] = w3;
			else nachfolgeneNachbarDaten[i] = 0.0;
		
		// WFD
		for(int i=0; i<length; i++)
			if(time[i] < t[4])
				wasserFilmDicke[i] = wfd;
			else wasserFilmDicke[i] = -1;
		
		// Ausgabewerte
		double letzterWert = w1;
		for(int i=0; i<length; i++)
			if(time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
				letzterWert = prueflingDaten[i];
			}
			else if(time[i] >= t[0] && time[i] < t[1])
				ersetzteAusgabeDaten[i] = letzterWert;
			else if(time[i] >= t[1] && time[i] < t[2])
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i])/2.0;
			else if(time[i] >= t[2] && time[i] < t[3])
				ersetzteAusgabeDaten[i] = (vorherigeNachbarDaten[i] + nachfolgeneNachbarDaten[i])/2.0;
			else if(time[i] >= t[3] && time[i] < t[4])
				ersetzteAusgabeDaten[i] = -1;
			else if(time[i] >= t[4] && time[i] < t[5])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else if(time[i] >= t[5] && time[i] < t[6])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else if(time[i] >= t[6] && time[i] < t[7])
				ersetzteAusgabeDaten[i] = -1;
			else if(time[i] >= t[7] && time[i] < t[8])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else ersetzteAusgabeDaten[i] = -1;
				
		System.out.print(' ');
	}
	
	/**
	 * Publiziert ein Datum nach den Vorgaben der Datenflusssteuerung
	 * (Es werden hier keine zwei Datensaetze nacheinander mit der Kennzeichnung
	 * "keine Daten" versendet)
	 * 
	 * @param resultat ein Originaldatum, so wie es empfangen wurde
	 * @param nutzDatum die ggf. messwertersetzen Nutzdaten
	 */
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
		
		if(publiziereDatensatz){
			if(!original.getObject().getPid().equals("ufdSensor.testNI.ni.zentral") ) return;
			double ni = nutzDatum.getItem("NiederschlagsIntensität").getItem("Wert").asUnscaledValue().doubleValue();
			if(ni>=0) ni = nutzDatum.getItem("NiederschlagsIntensität").getItem("Wert").asScaledValue().doubleValue();
			else ni = -1.0;
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[index] + " Berechnetes datum: " + ni + " index " + (index),ersetzteAusgabeDaten[index] == ni);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", index, ersetzteAusgabeDaten[index], ni));
			index++;
			synchronized (VERWALTUNG) {
				if(index >= ersetzteAusgabeDaten.length) MweNiSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
		}
	}
}
