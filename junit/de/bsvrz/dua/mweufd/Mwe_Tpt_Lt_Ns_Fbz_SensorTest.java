package de.bsvrz.dua.mweufd;

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class Mwe_Tpt_Lt_Ns_Fbz_SensorTest extends Mwe_Tpt_Lt_Ns_Fbz_Sensor {

	public Mwe_Tpt_Lt_Ns_Fbz_SensorTest(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
	}
	
	
	static double [] prueflingDaten;
	static double [] ersatzQuerrschnittDaten;	
	static double [] ersetzteAusgabeDaten;
	static long   [] time;
	
	void generiereTestDatenNachPruefSpezNS_1(long t1, long tE, long T) {
		
		double w1 = 3.5;
		double w2 = 1.5;
		double w3 = 5.5;
		double w4 = 0.5;
		double wfd = 1.75;
		
		int length = (int)(tE/T) + 1;
		
		prueflingDaten = new double [length];
		ersatzQuerrschnittDaten = new double [length];
		ersetzteAusgabeDaten = new double [length];
		
		time = new long [length];
		time[0] = 0;
		
		long t[] = new long [5];
		
		// Zeit
		for(int i=0; i<length; i++)
			time[i] = i*T;
		
		// Ersatzquerrschnittdaten
		for(int i=0; i<length; i++)
			if(time[i]>=t[6] && time[i]<t[7])
				ersatzQuerrschnittDaten[i] = -1;
			else
				ersatzQuerrschnittDaten[i] = w4;
		
				
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
			else ersetzteAusgabeDaten[i] = -1;
				
			
	}

}
