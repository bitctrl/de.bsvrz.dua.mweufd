package de.bsvrz.dua.mweufd;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.wfd.MweWfdSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class MweWfdSensorTest extends MweWfdSensor {
	
	public MweWfdSensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
	}

	static double [] prueflingDaten;
	static double [] direkterNachbarDaten;
	static double [] vorherigeNachbarDaten;
	static double [] nachfolgeneNachbarDaten;
	static double [] ersatzQuerrschnittDaten;
	static double [] niederschlagIntensitaet;
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
	static protected DataDescription DD_MESSWERTE, DD_NIMESSWERTE, DD_MESSWERT_ERSETZUNG;
	
	static public void generiereTestDatenNachPruefSpezNI_1(long t1, long tE, long T) {
		
		double w1 =  1.0;
		double w2 =  0.8;
		double w3 =  2.5;
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
			if(time[i] < t[4])
				niederschlagIntensitaet[i] = wni;
			else niederschlagIntensitaet[i] = -1;
		
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
}
