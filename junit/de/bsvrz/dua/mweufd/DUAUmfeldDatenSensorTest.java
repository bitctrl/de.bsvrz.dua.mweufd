package de.bsvrz.dua.mweufd;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;

public class DUAUmfeldDatenSensorTest extends DUAUmfeldDatenSensor {

	protected DUAUmfeldDatenSensorTest(final ClientDavInterface dav, final SystemObject objekt) {
		super(dav, objekt);
	}
	
	public static void reset() {
		INSTANZEN.clear();
	}
}
