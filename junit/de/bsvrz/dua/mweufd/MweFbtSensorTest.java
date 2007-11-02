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
import de.bsvrz.dua.mweufd.fbt.MweFbtSensor;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class MweFbtSensorTest extends MweFbtSensor {



	static double [] prueflingDaten;
	static double [] ersatzQuerrschnittDaten;	
	static double [] ersetzteAusgabeDaten;
	static long   [] time;
	static long ZEIT_INTERVALL;	
	static int index = 0;
	static int indexSend = 0;
	static protected boolean initialisiert = false;
	
	static protected SystemObject zentralSensor;
	static protected SystemObject ersatzSensor;
	
	static protected SystemObject niSensor;
	static protected SystemObject wfdSensor;
	
	static double niDaten = 0.0;
	static double wfdDaten = 0.0;
	
	static protected ClientDavInterface dav;
	static protected DataDescription DD_MESSWERTE, DD_WFDMESSWERTE, DD_NIMESSWERTE, DD_MESSWERT_ERSETZUNG;
	

	
	public MweFbtSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		
		if(initialisiert) return;
		
		dav = verwaltung.getVerbindung();
		
		zentralSensor = dav.getDataModel().getObject("ufdSensor.testFBT.fbt.zentral");
		ersatzSensor = dav.getDataModel().getObject("ufdSensor.testFBT.fbt.ersatz");
		
		niSensor = dav.getDataModel().getObject("ufdSensor.testFBT.ni");
		wfdSensor = dav.getDataModel().getObject("ufdSensor.testFBT.wfd");
		
		DD_MESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsFahrBahnOberFlächenTemperatur"),
							dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	 	
		DD_WFDMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsWasserFilmDicke"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
		
		DD_NIMESSWERTE = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsNiederschlagsIntensität"),
				dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"));
	
		DD_MESSWERT_ERSETZUNG =  new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"),
				dav.getDataModel().getAspect("asp.parameterVorgabe"));
		
		Collection<SystemObject> list = new LinkedList<SystemObject>();
		
		list.add(zentralSensor);
		list.add(ersatzSensor);
		
		try {
			dav.subscribeSender(this, list, DD_MESSWERTE, SenderRole.source());
			dav.subscribeSender(this, list, DD_MESSWERT_ERSETZUNG, SenderRole.sender());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}
		
		try {
			dav.subscribeSender(this, niSensor, DD_NIMESSWERTE, SenderRole.source());
			dav.subscribeSender(this, wfdSensor, DD_WFDMESSWERTE, SenderRole.source());
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
		}	
	}
	
	public static void parametriereSensor(long messwertFortschreibungsIntervall, long messWertErsetzungIntervall, long periode) {
		ZEIT_INTERVALL = periode;
		Data data = dav.createData(dav.getDataModel().getAttributeGroup("atg.ufdsMessWertErsetzung"));
		data.getItem("maxZeitMessWertErsetzung").asTimeValue().setMillis(messWertErsetzungIntervall);
		/**
		 * TODO Unkommnetieren
		 */
		//data.getItem("maxZeitMessWertFortschreibung").asTimeValue().setMillis(messWertFortschreibungsIntervall);

		ResultData result = new ResultData(zentralSensor, DD_MESSWERT_ERSETZUNG, System.currentTimeMillis(), data);
		try {
			dav.sendData(result);
		} catch (Exception e) {
			System.out.print("Fehler " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	static public boolean naechsterCyklus() {
		if(indexSend>= ersetzteAusgabeDaten.length) return false;
		
		sendeDatenSatz(zentralSensor, DD_MESSWERTE, "FahrBahnOberFlächenTemperatur", prueflingDaten[indexSend], time[indexSend]);
		sendeDatenSatz(ersatzSensor, DD_MESSWERTE, "FahrBahnOberFlächenTemperatur", ersatzQuerrschnittDaten[indexSend], time[indexSend]);	
		sendeDatenSatz(niSensor, DD_NIMESSWERTE, "NiederschlagsIntensität", niDaten, time[indexSend]);
		sendeDatenSatz(wfdSensor, DD_WFDMESSWERTE, "WasserFilmDicke", wfdDaten, time[indexSend]);

		indexSend++;
		return true;
	}
	

	static public void generiereTestDatenNachPruefSpez_1(long t1, long tE, long T) {
		
		double w1 = 4.0;
		double w2 = 1.2;
		double w3 = 0.4;
		
		int length = (int)(tE/T) + 5;
		
		prueflingDaten = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		
		ZEIT_INTERVALL = T;
		time = new long [length];
		time[0] = 0;
		
		long t[] = new long [5];
		long t_int = ( tE - t1) / 3;
		t[0] = T;
		t[1] = t[0] + t1;
		t[2] = t[1] + t_int;
		t[3] = t[2] + t_int;
		t[4] = t[3] + t_int;
		
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Ersatzquerrschnittdaten
		for(int i=0; i<length; i++)
			if(time[i]<t[2])
				ersatzQuerrschnittDaten[i] = w2;
			else if(time[i]>=t[2] && time[i]<t[3])
				ersatzQuerrschnittDaten[i] = -1;
			else 
				ersatzQuerrschnittDaten[i] = w3;
		
				
		// Pruefling
		for(int i=0; i<length; i++)
			if(time[i]<t[0])
				prueflingDaten[i] = w1;
			else prueflingDaten[i] = -1;

		
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
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else if(time[i] >= t[2] && time[i] < t[3])
				ersetzteAusgabeDaten[i] = -1;
			else if(time[i] >= t[3] && time[i] < t[4])
				ersetzteAusgabeDaten[i] = ersatzQuerrschnittDaten[i];
			else ersetzteAusgabeDaten[i] = -1;
				
			
	}
	

	@Override
	protected void publiziere(final ResultData original,
									final Data nutzDatum){
		boolean publiziereDatensatz = false;
		
		if(nutzDatum == null){
			if(this.letztesPubDatum != null && this.letztesPubDatum.getData() != null){
				publiziereDatensatz = true;
			}
		}else{
			publiziereDatensatz = true;
		}
		
		if(publiziereDatensatz){
			if(!original.getObject().getPid().equals(zentralSensor.getPid()) ) return;
			
			double sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur").getItem("Wert").asUnscaledValue().doubleValue();
			if(sw>=0) sw = nutzDatum.getItem("FahrBahnOberFlächenTemperatur").getItem("Wert").asScaledValue().doubleValue();
			else sw = -1;
			Assert.assertTrue("Erwartetes datum: " + ersetzteAusgabeDaten[index] + " Berechnetes datum: " + sw + " index " + (index), Math.abs(ersetzteAusgabeDaten[index]- sw)<0.001);
			System.out.println(String.format("[ %4d ] Ersatzwert OK: %3f == %3f", index, ersetzteAusgabeDaten[index], sw) + new Date(original.getDataTime()));
			index++;
			synchronized (VERWALTUNG) {
				if(index >= ersetzteAusgabeDaten.length) MweWfdSensorJunitTester.warten = false;
				VERWALTUNG.notify();
			}
			this.letztesPubDatum = VerwaltungMesswertErsetzungUFD.DFS.publiziere(original, nutzDatum);
		}
	}
}
