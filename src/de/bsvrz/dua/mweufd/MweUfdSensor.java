/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Allgemeiner Umfelddatensensor fuer die Messwertersetzung mit aktuellen Werten
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweUfdSensor
implements ClientReceiverInterface{

	/**
	 * statische Instanzen dieser Klasse
	 */
	private static final Map<SystemObject, MweUfdSensor> INSTANZEN = new HashMap<SystemObject, MweUfdSensor>();
	
	/**
	 * Beobachterobjekte
	 */
	private Set<IMweUfdSensorListener> listenerMenge = new HashSet<IMweUfdSensorListener>();
	
	/**
	 * letzter empfangener Datensatz
	 */
	private ResultData letztesDatum = null;
	
	/**
	 * das Systemobjekt
	 */
	private SystemObject objekt = null;

	
	/**
	 * Erfragt eine statische Instanz dieser Klasse
	 * 
	 * @param dav Datenverteiler-Verbindung
	 * @param objekt ein Systemobjekt eines Umfelddatensensors (<code>!= null</code>)
	 * @return eine statische Instanz dieser Klasse
	 */
	public static final MweUfdSensor getInstanz(final ClientDavInterface dav, 
												final SystemObject objekt){
		if(objekt == null){
			throw new NullPointerException("Sensos-Objekt ist <<null>>"); //$NON-NLS-1$
		}
		MweUfdSensor instanz = INSTANZEN.get(objekt);
		
		if(instanz == null){
			instanz = new MweUfdSensor(dav, objekt);
			INSTANZEN.put(objekt, instanz);
		}
		
		return instanz;
	}
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param dav Datenverteiler-Verbindung
	 * @param objekt ein Systemobjekt eines Umfelddatensensors (muss <code>!= null</code> sein)
	 */
	private MweUfdSensor(final ClientDavInterface dav, 
						final SystemObject objekt){
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(objekt);
		
		DataDescription datenBeschreibung = new DataDescription(
				dav.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
				dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
				(short)0);
		
		dav.subscribeReceiver(this, objekt, datenBeschreibung, ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	
	/**
	 * Erfragt das letzte von diesem Umfelddatensensor empfangene Datum
	 * 
	 * @return das letzte von diesem Umfelddatensensor empfangene Datum oder
	 * <code>null</code>, wenn noch keines empfangen wurde
	 */
	public final synchronized ResultData getLetztesDatum(){
		return this.letztesDatum;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null){
					synchronized (this) {
						this.letztesDatum = resultat;
						for(IMweUfdSensorListener listener:this.listenerMenge){
							listener.aktualisiere(resultat);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Fuegt diesem Umfelddatensensor einen Beobachter hinzu
	 * 
	 * @param listener ein Beobachter
	 * @param informiereInitial zeigt an, ob der Beobachter initial ueber das 
	 * letzte empfangene Datum informiert werden soll (so ueberhaupt schon 
	 * eines empfangen wurde)
	 */
	public final void addListener(IMweUfdSensorListener listener, boolean informiereInitial){
		if(informiereInitial){
			synchronized (this) {
				if(this.listenerMenge.add(listener) && this.letztesDatum != null){
					listener.aktualisiere(this.letztesDatum);
				}
			}
		}else{
			synchronized (this) {
				this.listenerMenge.add(listener);	
			}
		}
	}
	
	
	/**
	 * Erfragt das Systemobjekt
	 * 
	 * @return das Systemobjekt
	 */
	public final SystemObject getObjekt(){
		return this.objekt;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ergebnis = false;
		
		if(obj != null && obj instanceof MweUfdSensor){
			MweUfdSensor that = (MweUfdSensor)obj;
			ergebnis = this.objekt.equals(that.objekt);
		}
			
		return ergebnis;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.letztesDatum == null?"<<null>>":this.letztesDatum.toString(); //$NON-NLS-1$
	}

}
