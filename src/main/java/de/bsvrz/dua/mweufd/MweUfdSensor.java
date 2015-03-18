/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.mweufd;

import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.AbstraktOnlineUfdSensor;

/**
 * Allgemeiner Umfelddatensensor fuer die Messwertersetzung mit aktuellen Werten.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class MweUfdSensor extends AbstraktOnlineUfdSensor<ResultData> {

	/**
	 * statische Instanzen dieser Klasse.
	 */
	protected static final Map<SystemObject, MweUfdSensor> INSTANZEN = new HashMap<SystemObject, MweUfdSensor>();

	/**
	 * Erfragt eine statische Instanz dieser Klasse.
	 * 
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param objekt
	 *            ein Systemobjekt eines Umfelddatensensors (<code>!= null</code>)
	 * @return eine statische Instanz dieser Klasse
	 */
	public static final MweUfdSensor getInstanz(final ClientDavInterface dav,
			final SystemObject objekt) {
		if (objekt == null) {
			throw new NullPointerException("Sensos-Objekt ist <<null>>"); //$NON-NLS-1$
		}
		MweUfdSensor instanz = INSTANZEN.get(objekt);

		if (instanz == null) {
			instanz = new MweUfdSensor();
			instanz.initialisiere(dav, objekt, dav.getDataModel().getAspect(
					DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));
			INSTANZEN.put(objekt, instanz);
		}

		return instanz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void berechneOnlineWert(final ResultData resultat) {
		this.onlineWert = resultat;
	}

}
