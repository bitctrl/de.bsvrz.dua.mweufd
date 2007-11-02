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
		
		final long messwertErsetzungMax = 120*MIN_IN_MS;
		final long messwertFortFuehrungMax = 3*MIN_IN_MS;
		final long periode = 30* S_IN_MS;
		
		MweNiSensorTest.generiereTestDatenNachPruefSpezNI_1(messwertFortFuehrungMax,messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		MweNiSensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax);
		
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
}
