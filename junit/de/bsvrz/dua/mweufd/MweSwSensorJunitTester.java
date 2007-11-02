package de.bsvrz.dua.mweufd;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class MweSwSensorJunitTester {
	
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
	
		
		MweSwSensorTest.generiereTestDatenNachPruefSpezSW_1(messwertFortFuehrungMax,messwertErsetzungMax , periode);
		
		AbstraktVerwaltungsAdapter verw = new VerwaltungMesswertErsetzungUFDTest();
		StandardApplicationRunner.run(verw, CON_DATA);
		
		MweSwSensorTest.parametriereSensor( messwertFortFuehrungMax, messwertErsetzungMax);
		
		while(MweSwSensorTest.naechsterCyklus()) 
		{ 
			try { Thread.sleep(50); } catch (Exception e) { }
		}
		synchronized (verw) {
			try {
				while(warten)  verw.wait();
			} catch (Exception e) {	}
		}
	}	
}
