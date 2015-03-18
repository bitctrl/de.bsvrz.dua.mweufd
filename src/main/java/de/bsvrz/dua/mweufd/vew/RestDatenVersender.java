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

package de.bsvrz.dua.mweufd.vew;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Meldet sich auf alle logischen Daten der uebergebenen Umfelddatensensoren
 * an und versednet diese ungesehen als messwertersetzt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class RestDatenVersender implements ClientSenderInterface,
		ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * statische Instanz dieser Klasse.
	 */
	private static RestDatenVersender instanz = null;

	/**
	 * Verbindung zum Datenverteiler.
	 */
	private ClientDavInterface dav = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param dav
	 *            Verbindung zum Datenverteiler
	 */
	private RestDatenVersender(final ClientDavInterface dav) {
		this.dav = dav;
	}

	/**
	 * Erfragt die statische Instanz dieser Klasse.
	 * 
	 * @param dav1
	 *            Verbindung zum Datenverteiler.
	 * @return die statische Instanz dieser Klasse
	 */
	static RestDatenVersender getInstanz(final ClientDavInterface dav1) {
		if (instanz == null) {
			instanz = new RestDatenVersender(dav1);
		}
		return instanz;
	}	
	

	/**
	 * Fuegt diesem Objekt einen neuen zu behandelnden Umfelddatensensor hinzu.
	 * 
	 * @param objekt
	 *            ein Umfelddatensensor
	 * @throws OneSubscriptionPerSendData
	 *             wird geworfen, wenn bereits eine lokale Sendeanmeldung fuer
	 *             die gleichen Daten von einem anderen Anwendungsobjekt
	 *             vorliegt
	 */
	public void add(final SystemObject objekt) throws OneSubscriptionPerSendData {
		final String atgPid = "atg.ufds"
				+ UmfeldDatenArt.getUmfeldDatenArtVon(objekt).getName();

		this.dav.subscribeSender(this, objekt, new DataDescription(dav
				.getDataModel().getAttributeGroup(atgPid), dav.getDataModel()
				.getAspect(DUAKonstanten.ASP_MESSWERTERSETZUNG)), SenderRole
				.source());

		this.dav.subscribeReceiver(this, objekt, new DataDescription(dav
				.getDataModel().getAttributeGroup(atgPid), dav.getDataModel()
				.getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH)),
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// 
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(final ResultData[] results) {
		if (results != null) {
			for (ResultData logResult : results) {
				if (logResult != null) {
					final String atgPid = "atg.ufds"
							+ UmfeldDatenArt.getUmfeldDatenArtVon(
									logResult.getObject()).getName();

					final AttributeGroup atg = this.dav.getDataModel()
							.getAttributeGroup(atgPid);
					final ResultData mweResult = new ResultData(
							logResult.getObject(),
							new DataDescription(
									atg,
									dav
											.getDataModel()
											.getAspect(
													DUAKonstanten.ASP_MESSWERTERSETZUNG)),
							logResult.getDataTime(), logResult.getData());

					try {
						this.dav.sendData(mweResult);
					} catch (final DataNotSubscribedException e) {
						LOGGER.error("", e);
						e.printStackTrace();
					} catch (final SendSubscriptionNotConfirmed e) {
						LOGGER.error("", e);
						e.printStackTrace();
					}
				}
			}
		}
	}

}
