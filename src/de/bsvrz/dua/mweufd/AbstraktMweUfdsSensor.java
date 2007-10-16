/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD
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
 * Wei�enfelser Stra�e 67<br>
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
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
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
	 * Parameter zur Datenflusssteuerung f�r diese
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
	 * letztes fuer diesen (den messwertzuersetzenden) Umfelddatensensor emfangenes 
	 * implausibles Datum
	 */
	protected ResultData letztesEmpangenesImplausiblesDatum = null;

	/**
	 * letztes fuer diesen (den messwertzuersetzenden) Umfelddatensensor emfangenes 
	 * plausibles Datum
	 */
	protected ResultData letztesEmpangenesPlausiblesDatum = null;
	
	/**
	 * letztes fuer diesen Umfelddatensensor ver�ffentlichtes
	 * Datum
	 */
	protected ResultData letztesPubDatum = null;
	
	/**
	 * Zeitpunkt, seit dem ununterbrochen Messwertersetzung stattfindet
	 */
	protected long messWertErsetzungStart = -1;

	/**
	 * Zeitpunkt, seit dem ununterbrochen Messwerte fortgeschrieben werden
	 */
	protected long messWertFortschreibungStart = -1;
	
	/**
	 * Hier untersuchter Umfelddatensensor mit aktuellen Parametern
	 */
	protected DUAUmfeldDatenSensor sensorMitParametern = null;
	

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
		if(messStelle == null || sensor == null){
			throw new NullPointerException("Messstelle/Sensor ist <<null>>"); //$NON-NLS-1$
		}
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
	 * {@inheritDoc}.<br>
	 * 
	 * Hier kommen die Daten an, die von dem Sensor kommen, der messwertersetzt 
	 * werden soll
	 */
	public void aktualisiere(ResultData resultat) {
		if(resultat.getData() != null){

			UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
			if(datum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA){
				if(this.messWertErsetzungStart == -1){
					this.messWertErsetzungStart = resultat.getDataTime();
				}
				
				if(resultat.getDataTime() - this.messWertErsetzungStart > 
					this.sensorMitParametern.getMaxZeitMessWertErsetzung()){
					/**
					 * F�r implausible Messwerte wird nur f�r einen je Umfeldmessstelle und
					 * Sensortyp parametrierbaren Zeitbereich ein Ersatzwert berechnet. Nach
					 * Ablauf dieses Zeitbereichs ist eine Berechnung nicht mehr sinnvoll, der
					 * entsprechende Sensorwert ist dann als nicht ermittelbar zu kennzeichnen.
					 */
					datum.getWert().setNichtErmittelbarAn();
					this.publiziere(resultat, datum.getDatum());
				}else{
					/**
					 * messwertersetze die Daten
					 */
					this.letztesEmpangenesImplausiblesDatum = resultat;
					this.trigger();
				}

			}else{
				this.messWertErsetzungStart = -1;
				this.messWertFortschreibungStart = -1;
				this.letztesEmpangenesPlausiblesDatum = resultat;
				this.publiziere(resultat, resultat.getData().createModifiableCopy());	
			}
		}else{
			this.publiziere(resultat, null);
		}
	}
	

	/**
	 * Wird aufgerufen, wenn ein fuer diese spezielle MWE relevantes Datum
	 * empfangen wurde 
	 */
	protected abstract void trigger();
	

	/**
	 * Publiziert ein Datum nach den Vorgaben der Datenflusssteuerung
	 * (Es werden hier keine zwei Datensaetze nacheinander mit der Kennzeichnung
	 * "keine Daten" versendet)
	 * 
	 * @param resultat ein Originaldatum, so wie es empfangen wurde
	 * @param nutzDatum die ggf. messwertersetzen Nutzdaten
	 */
	protected final void publiziere(final ResultData original,
									final Data nutzDatum){
		boolean publiziereDatensatz = false;
		
		if(nutzDatum == null){
			/**
			 * "keine Daten" wird nur publiziert, wenn das Objekt vorher
			 * nicht auch schon auf keine Daten stand
			 */
			if(this.letztesPubDatum != null && this.letztesPubDatum.getData() != null){
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
				this.letztesPubDatum = publikationsDatum;
			}else{
				LOGGER.error("Datenflusssteuerung konnte kein Publikationsdatum ermitteln fuer:\n" //$NON-NLS-1$
						+ original);
			}
		}
	}
	

	/**
	 * Erfragt eine Kopie der Nutzdaten des uebergebenen Result-Datensatzes
	 * mit angepasster Guete und Flag <code>interpoliert</code>
	 * 
	 * @param resultat ein Result-Datensatz mit Nutzdaten, der kopiert werden soll
	 * @return eine Kopie der Nutzdaten des uebergebenen Result-Datensatzes
	 * mit angepasster Guete und Flag <code>interpoliert</code>
	 */
	protected final Data getNutzdatenKopieVon(ResultData resultat){
		UmfeldDatenSensorDatum kopie = new UmfeldDatenSensorDatum(resultat);
		
		kopie.setStatusMessWertErsetzungInterpoliert(DUAKonstanten.JA);
		GWert guete = new GWert(kopie.getGueteIndex(), 
				GueteVerfahren.getZustand(kopie.getGueteVerfahren()), false);
		GWert neueGuete = GWert.getNichtErmittelbareGuete(
				GueteVerfahren.getZustand(kopie.getGueteVerfahren()));
		try {
			neueGuete = GueteVerfahren.gewichte(guete, VERWALTUNG.getGueteFaktor());
		} catch (GueteException e) {
			LOGGER.error("Guete von kopiertem Wert kann nicht angepasst werden: " +  //$NON-NLS-1$
					kopie);
			e.printStackTrace();
		}
		kopie.setGueteIndex(neueGuete.getIndexUnskaliert());
		
		return kopie.getDatum();
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
	public void aktualisiereDaten(ResultData[] resultate) {
		// Dieser Aktualisierungsmechanismus wird hier nicht benutzt
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
