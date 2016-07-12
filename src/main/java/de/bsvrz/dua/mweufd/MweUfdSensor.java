/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Messwertersetzung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.mweufd.
 * 
 * de.bsvrz.dua.mweufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.mweufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.mweufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.mweufd;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.util.WeakIdentityHashMap;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.AbstraktOnlineUfdSensor;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Allgemeiner Umfelddatensensor fuer die Messwertersetzung mit aktuellen Werten.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class MweUfdSensor extends AbstraktOnlineUfdSensor<ResultData> {

	private static final Map<SystemObject, MweUfdSensor> _instances = new IdentityHashMap<>();

	/**
	 * Erfragt eine Instanz dieser Klasse.
	 * 
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param objekt
	 *            ein Systemobjekt eines Umfelddatensensors (<code>!= null</code>)
	 * @return eine statische Instanz dieser Klasse
	 * @throws UmfeldDatenSensorUnbekannteDatenartException 
	 */
	public static synchronized MweUfdSensor getInstanz(final ClientDavInterface dav,
			final SystemObject objekt) throws UmfeldDatenSensorUnbekannteDatenartException {
		if (objekt == null) {
			throw new NullPointerException("Sensor-Objekt ist <<null>>"); //$NON-NLS-1$
		}
		MweUfdSensor instance = _instances.get(objekt);
		if(instance != null) return instance;
		instance = new MweUfdSensor();
		instance.initialisiere(dav, objekt, dav.getDataModel().getAspect(
					DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));

		_instances.put(objekt, instance);
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void berechneOnlineWert(final ResultData resultat) {
		this.onlineWert = resultat;
	}

}
