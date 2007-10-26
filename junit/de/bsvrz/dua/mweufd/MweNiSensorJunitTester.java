package de.bsvrz.dua.mweufd;

import org.junit.Test;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;
import junit.framework.Assert;

public class MweNiSensorJunitTester {
		
	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083",  
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd", 
			"-debugLevelStdErrText=WARNING", 
			"-debugLevelFileText=WARNING",
			"-KonfigurationsBereichsPid=kb.mweUfdTestModell" }; 

	public static boolean warten = true;
	
	@Test
	public void test1() {		
		final long MIN_IN_MS = 1000 * 60;
		final long H_IN_MS = 1000 * 60 *60;
		final long S_IN_MS = 1000;
		
		MweNiSensorTest.generiereTestDatenNachPruefSpezNI_1(3*MIN_IN_MS, 20*MIN_IN_MS, 30* S_IN_MS);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		MweNiSensorTest.parametriereSensor( 20*MIN_IN_MS);
		
		while(MweNiSensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(10); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
	}
	
	 double [] prueflingDaten;
	 double [] vorherigeNachbarDaten;
	 double [] nachfolgeneNachbarDaten;
	 double [] ersatzQuerrschnittDaten;
	 double [] wasserFilmDicke;
	 double [] ersetzteAusgabeDaten;
	 long   [] time;
	 long ZEIT_INTERVALL;
	 int index = 0;
	 int indexSend = 0;
	 
	 public void generiereTestDatenNachPruefSpezNI_1(long t1, long tE, long T) {
			
			double w1 = 2.0;
			double w2 = 1.5;
			double w3 = 5.5;
			double w4 = 0.5;
			double wfd = 1.75;
			
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
		
}
