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

package de.bsvrz.dua.mweufd.modell;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;

/**
 * Korrespondiert mit einem Objekt vom Typ <code>typ.umfeldDatenMessStelle</code>
 * und stellt alle Konfigurationsdaten zur Verfuegung
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class DUAUmfeldDatenMessStelle {

	/**
	 * statische Instanzen dieser Klasse
	 */
	private static Map<SystemObject, DUAUmfeldDatenMessStelle> INSTANZEN = null;
	
	/**
	 * das Systemobjekt
	 */
	private SystemObject objekt = null;
	
	/**
	 * Mapt die Umfelddatensensoren dieser Messstelle auf deren Umfelddatenarten
	 */
	private Map<UmfeldDatenArt, DatenSensorMenge> sensoren =
		new HashMap<UmfeldDatenArt, DatenSensorMenge>();
	
	
	/**
	 * Initialisiert alle Messstellen, die mit den uebergebenen Objekten
	 * assoziiert sind
	 * 
	 * @param dav die Datenverteiler-Verbindung
	 * @param messStellenObjekte Menge der zu initialisierenden Objekte
	 * (muss <code>!= null</code> sein)
	 */
	public static final void initialisiere(final ClientDavInterface dav, 
										   final SystemObject[] messStellenObjekte){
		if(messStellenObjekte == null){
			throw new NullPointerException("Menge der Umfelddaten-Messstellen ist <<null>>"); //$NON-NLS-1$
		}
		if(INSTANZEN != null){
			throw new RuntimeException("UFD-Modell darf nur einmal initialisiert werden"); //$NON-NLS-1$
		}

		INSTANZEN = new HashMap<SystemObject, DUAUmfeldDatenMessStelle>();
		for(SystemObject mStObj:messStellenObjekte){
			INSTANZEN.put(mStObj, new DUAUmfeldDatenMessStelle(dav, mStObj));
		}
	}
	
	
	/**
	 * Erfragt die statischen Instanzen dieser Klasse<br>
	 * <b>Achtung:</b> <code>initialisiere(final ClientDavInterface dav,
	 * final SystemObject[] messStellenObjekte)</code>	muss vorher aufgerufen 
	 * worden sein 
	 * 
	 * @return die statischen Instanzen dieser Klasse (ggf. leere Liste)
	 */
	public static final Collection<DUAUmfeldDatenMessStelle> getInstanzen(){
		if(INSTANZEN == null){
			throw new RuntimeException("DUAUmfeldDatenMessStelle wurde noch nicht initialisiert"); //$NON-NLS-1$
		}

		return INSTANZEN.values();
	}


	/**
	 * Erfragt die statische Instanz dieser Klasse, die mit dem uebergebenen Systemobjekt 
	 * assoziiert ist<br>
	 * <b>Achtung:</b> <code>initialisiere(final ClientDavInterface dav,
	 * final SystemObject[] messStellenObjekte)</code>	muss vorher aufgerufen 
	 * worden sein 
	 * 
	 * @param messStellenObjekt ein Systemobjekt einer Umfelddatenmessstelle
	 * @return die statische Instanz dieser Klasse, die mit dem uebergebenen Systemobjekt 
	 * assoziiert ist oder <code>null</code>, wenn keine Instanz gefunden wurde
	 */
	public static final DUAUmfeldDatenMessStelle getInstanz(final SystemObject messStellenObjekt){
		if(INSTANZEN == null){
			throw new RuntimeException("DUAUmfeldDatenMessStelle wurde noch nicht initialisiert"); //$NON-NLS-1$
		}

		return INSTANZEN.get(messStellenObjekt);
	}

	
	/**
	 * Standardkonstruktor<br>
	 * 
	 * @param dav Datenverteiler-Verbindung
	 * @param objekt das Systemobjekt der Messstelle
	 */
	private DUAUmfeldDatenMessStelle(final ClientDavInterface dav, 
									 final SystemObject objekt){
		if(objekt == null){
			throw new NullPointerException("Systemobjekt der Umfelddaten-Messstelle ist <<null>>"); //$NON-NLS-1$
		}
		this.objekt = objekt;
		
		Map<UmfeldDatenArt, Set<DUAUmfeldDatenSensor>> datenArtAufSensoren = 
						new HashMap<UmfeldDatenArt, Set<DUAUmfeldDatenSensor>>();
		for(UmfeldDatenArt datenArt:UmfeldDatenArt.getInstanzen()){
			datenArtAufSensoren.put(datenArt, new HashSet<DUAUmfeldDatenSensor>());
		}
		
		NonMutableSet sensorenMenge = ((ConfigurationObject) objekt).getNonMutableSet("UmfeldDatenSensoren"); //$NON-NLS-1$
		for (SystemObject sensorObj : sensorenMenge.getElements()) {
			DUAUmfeldDatenSensor sensor = DUAUmfeldDatenSensor.getInstanz(dav, sensorObj);
			
			Set<DUAUmfeldDatenSensor> sensorenMitDatenArt = datenArtAufSensoren.get(sensor.getDatenArt());			
			sensorenMitDatenArt.add(sensor);
		}
		
		for(UmfeldDatenArt datenArt:UmfeldDatenArt.getInstanzen()){
			this.sensoren.put(datenArt, new DatenSensorMenge(datenArtAufSensoren.get(datenArt)));
		}
	}
	
	
	/**
	 * Erfragt alle Umfelddatensensoren dieser Messstelle
	 * 
	 * @return alle Umfelddatensensoren dieser Messstelle (ggf. leere Liste)
	 */
	public final Collection<DUAUmfeldDatenSensor> getSensoren(){
		Collection<DUAUmfeldDatenSensor> alleSensoren = new HashSet<DUAUmfeldDatenSensor>();
		
		for(UmfeldDatenArt datenArt:UmfeldDatenArt.getInstanzen()){
			alleSensoren.addAll(this.sensoren.get(datenArt).getNebenSensoren());
			if(this.sensoren.get(datenArt).getHauptSensor() != null){
				alleSensoren.add(this.sensoren.get(datenArt).getHauptSensor());
			}
		}
		
		return alleSensoren; 
	}

	
	/**
	 * Erfragt alle an dieser Umfelddatenmessstelle konfigurierten Sensoren
	 * mit der uebergebenen Datenart
	 * 
	 * @param datenArt eine Umfelddatenart
	 * @return alle an dieser Umfelddatenmessstelle konfigurierten Sensoren
	 * mit der uebergebenen Datenart (ggf. leere Liste)
	 */
	public final Collection<DUAUmfeldDatenSensor> getSensoren(final UmfeldDatenArt datenArt){
		Collection<DUAUmfeldDatenSensor> alleSensorenDerDatenArt = new HashSet<DUAUmfeldDatenSensor>();
		
		alleSensorenDerDatenArt.addAll(this.sensoren.get(datenArt).getNebenSensoren());
		if(this.sensoren.get(datenArt).getHauptSensor() != null){
			alleSensorenDerDatenArt.add(this.sensoren.get(datenArt).getHauptSensor());
		}
		
		return alleSensorenDerDatenArt;
	}
	
	
	/**
	 * Erfragt den an dieser Umfelddatenmessstelle konfigurierten Hauptsensor
	 * mit der uebergebenen Datenart
	 * 
	 * @param datenArt eine Umfelddatenart
	 * @return den an dieser Umfelddatenmessstelle konfigurierten Hauptsensor
	 * mit der uebergebenen Datenart (ggf. <code>null</code>)
	 */
	public final DUAUmfeldDatenSensor getHauptSensor(final UmfeldDatenArt datenArt){
		return this.sensoren.get(datenArt).getHauptSensor();
	}
	
	
	/**
	 * Erfragt alle an dieser Umfelddatenmessstelle konfigurierten Nebensensoren
	 * mit der uebergebenen Datenart
	 * 
	 * @param datenArt eine Umfelddatenart
	 * @return alle an dieser Umfelddatenmessstelle konfigurierten Nebensensoren
	 * mit der uebergebenen Datenart (ggf. leere Liste)
	 */
	public final Collection<DUAUmfeldDatenSensor> getNebenSensoren(final UmfeldDatenArt datenArt){
		return this.sensoren.get(datenArt).getNebenSensoren();
	}		
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ergebnis = false;
		
		if(obj != null && obj instanceof DUAUmfeldDatenMessStelle){
			DUAUmfeldDatenMessStelle that = (DUAUmfeldDatenMessStelle)obj;
			ergebnis = this.objekt.equals(that.objekt);
		}
			
		return ergebnis;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = this.objekt.toString() + "\n"; //$NON-NLS-1$
		
		for(UmfeldDatenArt datenArt:UmfeldDatenArt.getInstanzen()){
			if(!this.sensoren.get(datenArt).isEmpty()){
				s += "Datenart: " + datenArt + "\nHS: " +  //$NON-NLS-1$ //$NON-NLS-2$
				(this.sensoren.get(datenArt).getHauptSensor() == null?"keiner":this.sensoren.get(datenArt).getHauptSensor()); //$NON-NLS-1$
				if(this.sensoren.get(datenArt).getNebenSensoren().size() != 0){
					for(DUAUmfeldDatenSensor nebenSensor:this.sensoren.get(datenArt).getNebenSensoren()){
						s += "\nNS: " + nebenSensor; //$NON-NLS-1$
					}
				}else{
					s += "\nNS: keine"; //$NON-NLS-1$
				}
			}
		}
		
		return s;
	}

	
	/**
	 * Speichert die Umfelddatensensoren einer Messstelle 
	 * fuer <b>eine</b> bestimmte Umfelddatenart
	 * 
	 * @author BitCtrl Systems GmbH, Thierfelder
	 * 
	 */
	private class DatenSensorMenge{
	
		/**
		 * der Hauptsensor
		 */
		private DUAUmfeldDatenSensor hauptSensor = null;
		
		/**
		 * alle Nebensensoren
		 */
		private Collection<DUAUmfeldDatenSensor> nebenSensoren = new HashSet<DUAUmfeldDatenSensor>();
		
		
		/**
		 * Standardkonstruktor
		 * 
		 * @param alleSensoren Liste aller Sensoren (muss <code>!= null</code> sein)
		 */
		protected DatenSensorMenge(Set<DUAUmfeldDatenSensor> alleSensoren){
			for(DUAUmfeldDatenSensor sensor:alleSensoren){
				if(sensor.isHauptSensor()){
					if(this.hauptSensor != null){
						throw new RuntimeException("Es darf nur ein Hauptsensor pro Messstelle konfiguriert sein "  //$NON-NLS-1$
								+ DUAUmfeldDatenMessStelle.this);
					}
					this.hauptSensor = sensor;
				}else{
					this.nebenSensoren.add(sensor);
				}
			}
		}
		
		
		/**
		 * Erfragt den Hauptsensor
		 * 
		 * @return den Hauptsensor (ggf. <code>null</code>)
		 */
		protected final DUAUmfeldDatenSensor getHauptSensor(){
			return this.hauptSensor;
		}
		
		
		/**
		 * Erfragt alle Nebensensoren
		 * 
		 * @return alle Nebensensoren (ggf. leere Liste)
		 */
		protected final Collection<DUAUmfeldDatenSensor> getNebenSensoren(){
			return this.nebenSensoren;
		}
		
		
		/**
		 * Erfragt, ob die Menge der in diesem Objekt referenzierten Umfelddatensensoren
		 * leer ist  
		 * 
		 * @return ob die Menge der in diesem Objekt referenzierten Umfelddatensensoren
		 * leer ist
		 */
		protected final boolean isEmpty(){
			return this.hauptSensor == null && this.nebenSensoren.isEmpty();
		}
	}
}
