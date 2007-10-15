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

import java.util.ArrayList;
import java.util.Collection;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.dua.mweufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.dua.mweufd.vew.MweUfdStandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVObjektAnmeldung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DFSKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerungFuerModul;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorDatum;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Allgemeiner Rahmen fuer eine Umfelddatenmessstelle, wie sie im Zusammenhang mit
 * der Messwertersetzung benoetigt wird 
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public abstract class AbstraktMweUfdsSensor 
extends AbstraktBearbeitungsKnotenAdapter
implements ClientSenderInterface, IMweUfdSensorListener{
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();

	/**
	 * Parameter zur Datenflusssteuerung für diese
	 * SWE und dieses Modul
	 */
	private IDatenFlussSteuerungFuerModul iDfsMod = DFSKonstanten.STANDARD;
	
	/**
	 * statische Datenverteiler-Verbindung
	 */
	protected static IVerwaltungMitGuete VERWALTUNG = null;
	
	/**
	 * die Umfelddatenmessstelle, die in Bezug auf einen bestimmten
	 * Hauptsensor messwertersetzt werden soll
	 */
	protected DUAUmfeldDatenMessStelle messStelle = null;
	
	/**
	 * Der MWE-Sensor selbst mit aktuellen Daten
	 */
	protected MweUfdSensor sensorSelbst = null;
	
	/**
	 * Der Vorgaenger des MWE-Sensors mit aktuellen Daten
	 */
	protected MweUfdSensor vorgaenger = null;
	
	/**
	 * Der Nachfolger des MWE-Sensors mit aktuellen Daten
	 */
	protected MweUfdSensor nachfolger = null;
	
	/**
	 * Der Ersatz des MWE-Sensors mit aktuellen Daten
	 */
	protected MweUfdSensor ersatz = null;
	
	/**
	 * letztes fuer diesen Umfelddatensensor emfangenes Datum
	 */
	protected ResultData letztesEmpangenesDatum = null;
	
	/**
	 * letztes fuer diesen Umfelddatensensor veröffentlichtes
	 * Datum
	 */
	protected ResultData letztesPubDatum = null;
	
	/**
	 * Zeitpunkt, seit dem ununterbrochen Messwertersetzung stattfindet
	 */
	protected long messWertErsetzungStart = -1;
	
	/**
	 * Hier untersuchter Umfelddatensensor mit aktuellen Parametern
	 */
	private DUAUmfeldDatenSensor sensorMitParametern = null;
	

	/**
	 * Standardkonstruktor
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param messStelle die Umfelddatenmessstelle, die in Bezug auf einen bestimmten
	 * Hauptsensor messwertersetzt werden soll (muss <code> != null</code> sein)
	 * @param sensor der Umfelddatensensor der messwertersetzt werden soll
	 * (muss <code> != null</code> sein)
	 * @throws DUAInitialisierungsException wenn die Initialisierung des Bearbeitungsknotens
	 * fehlgeschlagen ist
	 */
	public AbstraktMweUfdsSensor(IVerwaltungMitGuete verwaltung, 
								 DUAUmfeldDatenMessStelle messStelle,
								 DUAUmfeldDatenSensor sensor)
	throws DUAInitialisierungsException{
		this.sensorMitParametern = sensor;
		
		/**
		 * implizite Initialisierung der Datenflusssteuerung
		 */
		this.initialisiere(verwaltung);
		this.standardAspekte = new MweUfdStandardAspekteVersorger(verwaltung).getStandardPubInfos();
		this.publikationsAnmeldungen.modifiziereObjektAnmeldung(this.standardAspekte.
				getStandardAnmeldungen(this.verwaltung.getSystemObjekte()));
		
		
		this.messStelle = messStelle;
		this.sensorSelbst = MweUfdSensor.getInstanz(verwaltung.getVerbindung(),
													sensor.getObjekt());
		this.sensorSelbst.addListener(this, true);

		if(sensor.getVorgaenger() != null){
			DUAUmfeldDatenMessStelle vorgaengerMSt = DUAUmfeldDatenMessStelle.getInstanz(sensor.getVorgaenger());
			if(vorgaengerMSt != null){
				DUAUmfeldDatenSensor vorgaengerSensor = vorgaengerMSt.getHauptSensor(sensor.getDatenArt());
				if(vorgaengerSensor != null){
					this.vorgaenger = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), vorgaengerSensor.getObjekt());
				}
			}
		}

		if(sensor.getNachfolger() != null){
			DUAUmfeldDatenMessStelle nachfolgerMSt = DUAUmfeldDatenMessStelle.getInstanz(sensor.getVorgaenger());
			if(nachfolgerMSt != null){
				DUAUmfeldDatenSensor nachfolgerSensor = nachfolgerMSt.getHauptSensor(sensor.getDatenArt());
				if(nachfolgerSensor != null){
					this.nachfolger = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), nachfolgerSensor.getObjekt());
				}
			}
		}
		
		if(sensor.getErsatzSensor() != null){
			this.ersatz = MweUfdSensor.getInstanz(verwaltung.getVerbindung(), sensor.getErsatzSensor());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiere(ResultData resultat) {
		if(resultat.getData() != null){
			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
			if(datum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA){
				/**
				 * messwertersetze die Daten
				 */
				if(this.messWertErsetzungStart == -1){
					this.messWertErsetzungStart = resultat.getDataTime();
				}else{
					if(resultat.getDataTime() - this.messWertErsetzungStart > 
							this.sensorMitParametern.getMaxZeitMessWertErsetzung()){
						/**
						 * Für implausible Messwerte wird nur für einen je Umfeldmessstelle und
						 * Sensortyp parametrierbaren Zeitbereich ein Ersatzwert berechnet. Nach
						 * Ablauf dieses Zeitbereichs ist eine Berechnung nicht mehr sinnvoll, der
						 * entsprechende Sensorwert ist dann als nicht ermittelbar zu kennzeichnen.
						 */
						//this.publiziere(resultat, ...);
					}
				}
			}else{
				this.messWertErsetzungStart = -1;
				this.publiziere(resultat, resultat.getData().createModifiableCopy());	
			}
		}else{
			this.messWertErsetzungStart = -1;
			this.publiziere(resultat, null);
		}
//		AbstraktMweUfdsSensor.this.letztesEmpangenesDatum = resultat;
//		trigger();
	}
	

	/**
	 * Wird aufgerufen, wenn ein fuer diese spezielle MWE relevantes Datum
	 * empfangen wurde 
	 */
	protected abstract void trigger();
	

	/**
	 * Publiziert ein Datum nach den Vorgaben der Datenflusssteuerung
	 * 
	 * @param resultat ein Originaldatum, so wie es empfangen wurde
	 * @param nutzDatum die ggf. messwertersetzen Nutzdaten
	 */
	protected final void publiziere(final ResultData original,
									final Data nutzDatum){
		boolean publiziereDatensatz = false;
		
		if(nutzDatum == null){
			/**
			 * keine Daten wird nur publiziert, wenn das Objekt vorher
			 * nicht auch schon auf keine Daten stand
			 */
			if(this.letztesPubDatum != null){
				publiziereDatensatz = true;
			}
		}else{
			publiziereDatensatz = true;
		}
		
		if(publiziereDatensatz){
			ResultData publikationsDatum = 
				iDfsMod.getPublikationsDatum(original,
						nutzDatum, standardAspekte.getStandardAspekt(original));
			if(publikationsDatum != null){
				this.publikationsAnmeldungen.sende(publikationsDatum);							
			}else{
				LOGGER.error("Datenflusssteuerung konnte kein Publikationsdatum ermitteln fuer:\n" //$NON-NLS-1$
						+ original);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung iDfs) {
		this.iDfsMod = iDfs.getDFSFuerModul(this.verwaltung.getSWETyp(),
				this.getModulTyp());
		
		Collection<DAVObjektAnmeldung> anmeldungenStd =
			new ArrayList<DAVObjektAnmeldung>();

		if(this.standardAspekte != null){
			anmeldungenStd = this.standardAspekte.
			getStandardAnmeldungen(this.verwaltung.getSystemObjekte());
		}

		Collection<DAVObjektAnmeldung> anmeldungen = 
			this.iDfsMod.getDatenAnmeldungen(this.verwaltung.getSystemObjekte(), 
					anmeldungenStd);

		synchronized(this){
			this.publikationsAnmeldungen.modifiziereObjektAnmeldung(anmeldungen);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return ModulTyp.MESSWERTERSETZUNG_UFD;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// mache nichts
	}
	

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}
}
