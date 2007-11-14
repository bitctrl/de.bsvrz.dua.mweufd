package de.bsvrz.dua.mweufd;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DatenFlussSteuerungsVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class DatenFlussSteuerungVersorgerTest extends
		DatenFlussSteuerungsVersorger {
	
	private DatenFlussSteuerungVersorgerTest(final IVerwaltung verwaltung,
			final SystemObject dfsObjekt) throws DUAInitialisierungsException {
		super(verwaltung, dfsObjekt);
	}

	public static void reset() {
		INSTANZ = null;
	}
}
