package de.bsvrz.dua.mweufd;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import junit.framework.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.sw.MweSwSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class MweSwSensorTest extends MweSwSensor {


	static long [] prueflingDaten;
	static long [] nachfolgerDaten;
	static long [] ersetzteAusgabeDaten;
	static long [] time;
	static long ZEIT_INTERVALL;	
	static int index = 0;
	static int indexSend = 0;

	static protected SystemObject swSensor;
	static protected SystemObject nachfolgerSensor;
	
	static protected ClientDavInterface dav;
	static protected DataDescription DD_MESSWERTE, DD_MESSWERT_ERSETZUNG;
	
	static public void generiereTestDatenNachPruefSpezSW_1(long t1, long tE, long T) {
		
		long w1 =  40;
		long w2 =  60;
		long w3 =  80;
		
		
		ZEIT_INTERVALL = T;
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new long [length];
		nachfolgerDaten = new long [length];
		ersetzteAusgabeDaten = new  long [length];
		
		time = new long [length];
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Intervalle
		long t[] = new long [5];
		long t_int = ( tE - t1) / 3;
		
		t[0] = T;
		t[1] = t[0] +  t_int;
		t[2] = t[1] + t1;
		t[3] = t[2] + t_int;
		t[4] = t[3] + t_int;
	
	
		
		// Nachfolgerdaten
		for(int i=0; i<length; i++)
			if(time[i]<t[1])
				nachfolgerDaten[i] = w2;
			else if(time[i]>=t[1] && time[i]<t[3])
				nachfolgerDaten[i] = -1;
			else 
				nachfolgerDaten[i] = w3;
		
				
		// Pruefling
		for(int i=0; i<length; i++)
			if(time[i]<t[0])
				prueflingDaten[i] = w1;
			else prueflingDaten[i] = -1;
		
		
		
		// Ausgabewerte
		long letzterWert = w1;
		for(int i=0; i<length; i++)
			if(time[i] < t[0]) {
				ersetzteAusgabeDaten[i] = prueflingDaten[i];
			}
			else if(time[i] >= t[0] && time[i] < t[1]) {
				ersetzteAusgabeDaten[i] = nachfolgerDaten[i];
				letzterWert = ersetzteAusgabeDaten[i];
			}
			else if(time[i] >= t[1] && time[i] < t[2]) {
				ersetzteAusgabeDaten[i] = letzterWert;
			}
			else if(time[i] >= t[2] && time[i] < t[3]) {
				ersetzteAusgabeDaten[i] = -1; 
			}
			else if(time[i] >= t[3] && time[i] < t[4]) {
				ersetzteAusgabeDaten[i] = nachfolgerDaten[i]; 
			}
			else ersetzteAusgabeDaten[i] = -1;
				
		System.out.print(' ');
	}
	

	
	
	public MweSwSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		if(dav != null) return;
		
		dav = verwaltung.getVerbindung();
		swSensor = dav.getDataModel().getObject("ufdSensor.testSW.sw.zentral");
		nachfolgerSensor = dav.getDataModel().getObject("ufdSensor.testSW.sw.nach");
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsSichtWeite"),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	 	
		DD_MESSWERT_ERSETZUNG =  new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				dav.getDataModel().getAspect("asp.parameterVorgabe"));
		
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		
		list.add(swSensor);
		list.add(nachfolgerSensor);
		
		try {
			dav.subscribeSender(this, list, DD_MESSWERTE, SenderRole.source());
			dav.subscribeSender(this, list, DD_MESSWERT_ERSETZUNG, SenderRole.sender());
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

		ResultData result = new ResultData(swSensor, DD_MESSWERT_ERSETZUNG, System.currentTimeMillis(), data);
		try {
			dav.sendData(result);
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
			e.printStackTrace();
		}
	}
	

	static public boolean naechsterCyklus() {
		if(indexSend>= nachfolgerDaten.length) return false;
		
		sendeDatenSatz(swSensor, prueflingDaten[indexSend], time[indexSend]);
		sendeDatenSatz(nachfolgerSensor, nachfolgerDaten[indexSend], time[indexSend]);

		indexSend++;
		return true;
	}
	

	static  public void sendeDatenSatz(SystemObject sensor, double messwert, long zeitStempel) {
		
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsSichtWeite"));

		String att = "SichtWeite";
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
			if(!original.getObject().getPid().equals("ufdSensor.testSW.sw.zentral") ) return;
			
			long sw = nutzDatum.getItem("SichtWeite").getItem("Wert").asUnscaledValue().longValue();
			if(sw>=0) sw = nutzDatum.getItem("SichtWeite").getItem("Wert").asScaledValue().longValue();
			else sw = -1;
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[index] + " Berechnetes datum: " + sw + " index " + (index), Math.abs(ersetzteAusgabeDaten[index]- sw)<0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3d == %3d", index, ersetzteAusgabeDaten[index], sw) + new Date(original.getDataTime()));
			index++;
			synchronized (VERWALTUNG) {
				if(index >= ersetzteAusgabeDaten.length) MweWfdSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}
}
