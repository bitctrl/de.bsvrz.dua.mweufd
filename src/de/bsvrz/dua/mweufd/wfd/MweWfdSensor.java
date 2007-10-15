package de.bsvrz.dua.mweufd.wfd;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mweufd.AbstraktMweUfdsSensor;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;

public class MweWfdSensor
extends AbstraktMweUfdsSensor {

	public MweWfdSensor(IVerwaltungMitGuete verwaltung,
			DUAUmfeldDatenMessStelle messStelle, DUAUmfeldDatenSensor sensor)
			throws DUAInitialisierungsException {
		super(verwaltung, messStelle, sensor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void trigger() {
		// TODO Auto-generated method stub
		
	}

	public void aktualisiereDaten(ResultData[] resultate) {
		// TODO Auto-generated method stub
		
	}



}
