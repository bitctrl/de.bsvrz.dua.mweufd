/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.mweufd.tests.
 * 
 * de.bsvrz.dua.mweufd.tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.mweufd.tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.mweufd.tests.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.mweufd.tests;

import com.google.common.collect.ImmutableList;
import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.tests.ColumnLayout;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuAMweUfd extends DuAMweUfdTestBase implements ClientSenderInterface {

	private SystemObject _ni;
	private SystemObject _ns;
	private SystemObject _wfd;
	private SystemObject _fbz;
	private SystemObject _lt;
	private SystemObject _fbt;
	private SystemObject _sw;
	private SystemObject _tpt;
	
	private SystemObject _haupt_ni;
	private SystemObject _haupt_ni2;
	private SystemObject _haupt_ni3;
	private SystemObject _haupt_ns;
	private SystemObject _haupt_wfd;
	private SystemObject _haupt_wfd2;
	private SystemObject _haupt_wfd3;
	private SystemObject _haupt_fbz;
	private SystemObject _haupt_lt;
	private SystemObject _haupt_lt2;
	private SystemObject _haupt_lt3;
	private SystemObject _haupt_fbt;
	private SystemObject _haupt_sw;
	private SystemObject _haupt_tpt;
	
	private Aspect _aspSend;
	private Aspect _aspReceive;
	
	private DataDescription _ddniSend;
	private DataDescription _ddnsSend;
	private DataDescription _ddwfdSend;
	private DataDescription _ddfbzSend;
	private DataDescription _ddltSend;
	private DataDescription _ddtptSend;
	private DataDescription _ddswSend;
	private DataDescription _ddfbtSend;
	private DataDescription _ddswReceive;
	private DataDescription _ddtptReceive;
	private DataDescription _ddltReceive;
	private DataDescription _ddfbzReceive;
	private DataDescription _ddwfdReceive;
	private DataDescription _ddnsReceive;
	private DataDescription _ddniReceive;
	private DataDescription _ddfbtReceive;
	
	private SystemObject _vor_ni;
	private SystemObject _vor_wfd;
	private SystemObject _vor_lt;

	private SystemObject _nach_ni;
	private SystemObject _nach_wfd;
	private SystemObject _nach_lt;
	
	private SystemObject _ersatz_ni;
	private SystemObject _ersatz_ns;
	private SystemObject _ersatz_fbz;
	private SystemObject _ersatz_wfd;
	private SystemObject _ersatz_sw;
	private SystemObject _ersatz_tpt;
	private SystemObject _ersatz_fbt;
	private SystemObject _ersatz_lt;
	@Before
	public void setUp() throws Exception {
		super.setUp();

		// Ohne Ersatzsensoren
		_ni =  _dataModel.getObject("ufd.ni");
		_ns =  _dataModel.getObject("ufd.na");
		_fbz = _dataModel.getObject("ufd.fbz");
		_wfd = _dataModel.getObject("ufd.wfd");
		_sw =  _dataModel.getObject("ufd.sw");
		_tpt = _dataModel.getObject("ufd.tpt");
		_lt =  _dataModel.getObject("ufd.lt");
		_fbt = _dataModel.getObject("ufd.fbt");

		// Mit Ersatzsensoren und Vorgänger/nachfolger
		_haupt_ni =  _dataModel.getObject("ufd.haupt.ni");
		_haupt_wfd = _dataModel.getObject("ufd.haupt.wfd");
		_haupt_lt =  _dataModel.getObject("ufd.haupt.lt");
		

		// Mit Ersatzsensoren
		_haupt_ni2 =  _dataModel.getObject("ufd.haupt.ni2");
		_haupt_ns =  _dataModel.getObject("ufd.haupt.na");
		_haupt_fbz = _dataModel.getObject("ufd.haupt.fbz");
		_haupt_wfd2 = _dataModel.getObject("ufd.haupt.wfd2");
		_haupt_sw =  _dataModel.getObject("ufd.haupt.sw");
		_haupt_tpt = _dataModel.getObject("ufd.haupt.tpt");
		_haupt_lt2 =  _dataModel.getObject("ufd.haupt.lt2");
		_haupt_fbt = _dataModel.getObject("ufd.haupt.fbt");
		
		// Mit Vorgänger & Nachfolger
		_haupt_ni3 =  _dataModel.getObject("ufd.haupt.ni3");
		_haupt_wfd3 = _dataModel.getObject("ufd.haupt.wfd3");
		_haupt_lt3 =  _dataModel.getObject("ufd.haupt.lt3");
		
		_vor_ni =  _dataModel.getObject("ufd.vor.ni");
		_vor_wfd = _dataModel.getObject("ufd.vor.wfd");
		_vor_lt =  _dataModel.getObject("ufd.vor.lt");
		
		_nach_ni =  _dataModel.getObject("ufd.nach.ni");
		_nach_wfd = _dataModel.getObject("ufd.nach.wfd");
		_nach_lt =  _dataModel.getObject("ufd.nach.lt");
		
		_ersatz_ni =  _dataModel.getObject("ufd.ersatz.ni");
		_ersatz_ns =  _dataModel.getObject("ufd.ersatz.na");
		_ersatz_fbz = _dataModel.getObject("ufd.ersatz.fbz");
		_ersatz_wfd = _dataModel.getObject("ufd.ersatz.wfd");
		_ersatz_sw =  _dataModel.getObject("ufd.ersatz.sw");
		_ersatz_tpt = _dataModel.getObject("ufd.ersatz.tpt");
		_ersatz_lt =  _dataModel.getObject("ufd.ersatz.lt");
		_ersatz_fbt = _dataModel.getObject("ufd.ersatz.fbt");

		_aspSend = _dataModel.getAspect("asp.plausibilitätsPrüfungLogisch");
		_aspReceive = _dataModel.getAspect("asp.messWertErsetzung");
		AttributeGroup atgni = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsIntensität");
		AttributeGroup atgns = _dataModel.getAttributeGroup("atg.ufds" + "NiederschlagsArt");
		AttributeGroup atgwfd = _dataModel.getAttributeGroup("atg.ufds" + "WasserFilmDicke");
		AttributeGroup atgfbz = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenZustand");
		AttributeGroup atglt = _dataModel.getAttributeGroup("atg.ufds" + "LuftTemperatur");
		AttributeGroup atgtpt = _dataModel.getAttributeGroup("atg.ufds" + "TaupunktTemperatur");
		AttributeGroup atgsw = _dataModel.getAttributeGroup("atg.ufds" + "SichtWeite");
		AttributeGroup atgfbt = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenTemperatur");
		_ddniSend = new DataDescription(atgni, _aspSend);
		_ddnsSend = new DataDescription(atgns, _aspSend);
		_ddwfdSend = new DataDescription(atgwfd, _aspSend);
		_ddfbzSend = new DataDescription(atgfbz, _aspSend);
		_ddltSend = new DataDescription(atglt, _aspSend);
		_ddtptSend = new DataDescription(atgtpt, _aspSend);
		_ddswSend = new DataDescription(atgsw, _aspSend);
		_ddfbtSend = new DataDescription(atgfbt, _aspSend);
		_ddniReceive = new DataDescription(atgni, _aspReceive);
		_ddnsReceive = new DataDescription(atgns, _aspReceive);
		_ddwfdReceive = new DataDescription(atgwfd, _aspReceive);
		_ddfbzReceive = new DataDescription(atgfbz, _aspReceive);
		_ddltReceive = new DataDescription(atglt, _aspReceive);
		_ddtptReceive = new DataDescription(atgtpt, _aspReceive);
		_ddswReceive = new DataDescription(atgsw, _aspReceive);
		_ddfbtReceive = new DataDescription(atgfbt, _aspReceive);
	}

	@Override
	public void sendData(final ResultData... resultDatas) throws SendSubscriptionNotConfirmed {
		for(ResultData resultData : resultDatas) {
			try {
				super.sendData(resultData);
			}
			catch(DataNotSubscribedException e){
				try {
					_connection.subscribeSource(this, resultData);
				}
				catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
					throw new AssertionError(oneSubscriptionPerSendData);
				}
				super.sendData(resultData);
			}
		}
	}

	@Test
	public void testNI1() throws Exception {
		fakeParamApp.publishParam(_haupt_ni.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-NI1.csv", new SystemObject[]{_haupt_ni, _vor_ni, _nach_ni, _ersatz_ni}, new SystemObject[]{_haupt_ni}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}
	
	@Test
	public void testNI2() throws Exception {
		fakeParamApp.publishParam(_haupt_ni2.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-NI2.csv", new SystemObject[]{_haupt_ni2, _ersatz_ni}, new SystemObject[]{_haupt_ni2}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testNI3() throws Exception {
		fakeParamApp.publishParam(_haupt_ni3.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-NI3.csv", new SystemObject[]{_haupt_ni3, _vor_ni, _nach_ni}, new SystemObject[]{_haupt_ni3}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}
	
	@Test
	public void testNI4() throws Exception {
		fakeParamApp.publishParam(_ni.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-NI4.csv", new SystemObject[]{_ni}, new SystemObject[]{_ni}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}

	@Test
	public void testNI5() throws Exception {
		fakeParamApp.publishParam(_haupt_ni.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-NI5.csv", new SystemObject[]{_haupt_ni, _vor_ni, _nach_ni, _ersatz_ni}, new SystemObject[]{_haupt_ni}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}
	
	@Test
	public void testNI6() throws Exception {
		fakeParamApp.publishParam(_haupt_ni.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-NI6.csv", new SystemObject[]{_haupt_ni, _vor_ni, _nach_ni, _ersatz_ni}, new SystemObject[]{_haupt_ni}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}
		
	@Test
	public void testNI7() throws Exception {
		fakeParamApp.publishParam(_ni.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-NI7.csv", new SystemObject[]{_ni}, new SystemObject[]{_ni}, _ddniSend, _ddniReceive, new DuaUfdLayout());
	}
	
	@Test
	public void testNA1() throws Exception {
		fakeParamApp.publishParam(_haupt_ns.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-NS1.csv", new SystemObject[]{_haupt_ns, _ersatz_ns}, new SystemObject[]{_haupt_ns}, _ddnsSend, _ddnsReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testNA2() throws Exception {
		fakeParamApp.publishParam(_ns.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-NS2.csv", new SystemObject[]{_ns}, new SystemObject[]{_ns}, _ddnsSend, _ddnsReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testNA3() throws Exception {
		fakeParamApp.publishParam(_haupt_ns.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-NS3.csv", new SystemObject[]{_haupt_ns, _ersatz_ns}, new SystemObject[]{_haupt_ns}, _ddnsSend, _ddnsReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testNA4() throws Exception {
		fakeParamApp.publishParam(_haupt_ns.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-NS4.csv", new SystemObject[]{_haupt_ns, _ersatz_ns}, new SystemObject[]{_haupt_ns}, _ddnsSend, _ddnsReceive, new DuaUfdLayout());
	}		
	
	@Test
	public void testFBZ1() throws Exception {
		fakeParamApp.publishParam(_haupt_fbz.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-FBZ1.csv", new SystemObject[]{_haupt_fbz, _ersatz_fbz}, new SystemObject[]{_haupt_fbz}, _ddfbzSend, _ddfbzReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testFBZ2() throws Exception {
		fakeParamApp.publishParam(_fbz.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-FBZ2.csv", new SystemObject[]{_fbz}, new SystemObject[]{_fbz}, _ddfbzSend, _ddfbzReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testFBZ3() throws Exception {
		fakeParamApp.publishParam(_haupt_fbz.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-FBZ3.csv", new SystemObject[]{_haupt_fbz, _ersatz_fbz}, new SystemObject[]{_haupt_fbz}, _ddfbzSend, _ddfbzReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testFBZ4() throws Exception {
		fakeParamApp.publishParam(_haupt_fbz.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-FBZ4.csv", new SystemObject[]{_haupt_fbz, _ersatz_fbz}, new SystemObject[]{_haupt_fbz}, _ddfbzSend, _ddfbzReceive, new DuaUfdLayout());
	}

	@Test
	public void testFBT1() throws Exception {
		fakeParamApp.publishParam(_haupt_fbt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-FBT1.csv", new SystemObject[]{_haupt_fbt, _ersatz_fbt}, new SystemObject[]{_haupt_fbt}, _ddfbtSend, _ddfbtReceive, new DuaUfdLayout());
	}

	@Test
	public void testFBT2() throws Exception {
		fakeParamApp.publishParam(_fbt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-FBT2.csv", new SystemObject[]{_fbt}, new SystemObject[]{_fbt}, _ddfbtSend, _ddfbtReceive, new DuaUfdLayout());
	}

	@Test
	public void testFBT3() throws Exception {
		fakeParamApp.publishParam(_haupt_fbt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-FBT3.csv", new SystemObject[]{_haupt_fbt, _ersatz_fbt}, new SystemObject[]{_haupt_fbt}, _ddfbtSend, _ddfbtReceive, new DuaUfdLayout());
	}

	@Test
	public void testFBT4() throws Exception {
		fakeParamApp.publishParam(_haupt_fbt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-FBT4.csv", new SystemObject[]{_haupt_fbt, _ersatz_fbt}, new SystemObject[]{_haupt_fbt}, _ddfbtSend, _ddfbtReceive, new DuaUfdLayout());
	}


	@Test
	public void testLT1() throws Exception {
		fakeParamApp.publishParam(_haupt_lt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-LT1.csv", new SystemObject[]{_haupt_lt, _vor_lt, _nach_lt, _ersatz_lt}, new SystemObject[]{_haupt_lt}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}

	@Test
	public void testLT2() throws Exception {
		fakeParamApp.publishParam(_haupt_lt2.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-LT2.csv", new SystemObject[]{_haupt_lt2, _ersatz_lt}, new SystemObject[]{_haupt_lt2}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}

	@Test
	public void testLT3() throws Exception {
		fakeParamApp.publishParam(_haupt_lt3.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-LT3.csv", new SystemObject[]{_haupt_lt3, _vor_lt, _nach_lt}, new SystemObject[]{_haupt_lt3}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}

	@Test
	public void testLT4() throws Exception {
		fakeParamApp.publishParam(_lt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-LT4.csv", new SystemObject[]{_lt}, new SystemObject[]{_lt}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}

	@Test
	public void testLT5() throws Exception {
		fakeParamApp.publishParam(_haupt_lt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-LT5.csv", new SystemObject[]{_haupt_lt, _vor_lt, _nach_lt, _ersatz_lt}, new SystemObject[]{_haupt_lt}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}

	@Test
	public void testLT6() throws Exception {
		fakeParamApp.publishParam(_haupt_lt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-LT6.csv", new SystemObject[]{_haupt_lt, _vor_lt, _nach_lt, _ersatz_lt}, new SystemObject[]{_haupt_lt}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}

	@Test
	public void testLT7() throws Exception {
		fakeParamApp.publishParam(_lt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-LT7.csv", new SystemObject[]{_lt}, new SystemObject[]{_lt}, _ddltSend, _ddltReceive, new DuaUfdLayout());
	}


	@Test
	public void testSW1() throws Exception {
		fakeParamApp.publishParam(_haupt_sw.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-SW1.csv", new SystemObject[]{_haupt_sw, _ersatz_sw}, new SystemObject[]{_haupt_sw}, _ddswSend, _ddswReceive, new DuaUfdLayout());
	}

	@Test
	public void testSW2() throws Exception {
		fakeParamApp.publishParam(_sw.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-SW2.csv", new SystemObject[]{_sw}, new SystemObject[]{_sw}, _ddswSend, _ddswReceive, new DuaUfdLayout());
	}

	@Test
	public void testSW3() throws Exception {
		fakeParamApp.publishParam(_haupt_sw.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-SW3.csv", new SystemObject[]{_haupt_sw, _ersatz_sw}, new SystemObject[]{_haupt_sw}, _ddswSend, _ddswReceive, new DuaUfdLayout());
	}

	@Test
	public void testSW4() throws Exception {
		fakeParamApp.publishParam(_haupt_sw.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-SW4.csv", new SystemObject[]{_haupt_sw, _ersatz_sw}, new SystemObject[]{_haupt_sw}, _ddswSend, _ddswReceive, new DuaUfdLayout());
	}
	
	@Test
	public void testTPT1() throws Exception {
		fakeParamApp.publishParam(_haupt_tpt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-TPT1.csv", new SystemObject[]{_haupt_tpt, _ersatz_tpt}, new SystemObject[]{_haupt_tpt}, _ddtptSend, _ddtptReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testTPT2() throws Exception {
		fakeParamApp.publishParam(_tpt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-TPT2.csv", new SystemObject[]{_tpt}, new SystemObject[]{_tpt}, _ddtptSend, _ddtptReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testTPT3() throws Exception {
		fakeParamApp.publishParam(_haupt_tpt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'4 Minuten'}");
		startTestCase("DUA69-TPT3.csv", new SystemObject[]{_haupt_tpt, _ersatz_tpt}, new SystemObject[]{_haupt_tpt}, _ddtptSend, _ddtptReceive, new DuaUfdLayout());
	}	
	
	@Test
	public void testTPT4() throws Exception {
		fakeParamApp.publishParam(_haupt_tpt.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'10 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-TPT4.csv", new SystemObject[]{_haupt_tpt, _ersatz_tpt}, new SystemObject[]{_haupt_tpt}, _ddtptSend, _ddtptReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD1() throws Exception {
		fakeParamApp.publishParam(_haupt_wfd.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-WFD1.csv", new SystemObject[]{_haupt_wfd, _vor_wfd, _nach_wfd, _ersatz_wfd}, new SystemObject[]{_haupt_wfd}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD2() throws Exception {
		fakeParamApp.publishParam(_haupt_wfd2.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-WFD2.csv", new SystemObject[]{_haupt_wfd2, _ersatz_wfd}, new SystemObject[]{_haupt_wfd2}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD3() throws Exception {
		fakeParamApp.publishParam(_haupt_wfd3.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-WFD3.csv", new SystemObject[]{_haupt_wfd3, _vor_wfd, _nach_wfd}, new SystemObject[]{_haupt_wfd3}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD4() throws Exception {
		fakeParamApp.publishParam(_wfd.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-WFD4.csv", new SystemObject[]{_wfd}, new SystemObject[]{_wfd}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD5() throws Exception {
		fakeParamApp.publishParam(_haupt_wfd.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'0 Minuten',maxZeitMessWertFortschreibung:'3 Minuten'}");
		startTestCase("DUA69-WFD5.csv", new SystemObject[]{_haupt_wfd, _vor_wfd, _nach_wfd, _ersatz_wfd}, new SystemObject[]{_haupt_wfd}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD6() throws Exception {
		fakeParamApp.publishParam(_haupt_wfd.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-WFD6.csv", new SystemObject[]{_haupt_wfd, _vor_wfd, _nach_wfd, _ersatz_wfd}, new SystemObject[]{_haupt_wfd}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}

	@Test
	public void testWFD7() throws Exception {
		fakeParamApp.publishParam(_wfd.getPid(), "atg.ufdsMessWertErsetzung", "{maxZeitMessWertErsetzung:'9 Minuten',maxZeitMessWertFortschreibung:'0 Minuten'}");
		startTestCase("DUA69-WFD7.csv", new SystemObject[]{_wfd}, new SystemObject[]{_wfd}, _ddwfdSend, _ddwfdReceive, new DuaUfdLayout());
	}


	@Override
	public void dataRequest(final SystemObject object, final DataDescription dataDescription, final byte state) {
	}

	@Override
	public boolean isRequestSupported(final SystemObject object, final DataDescription dataDescription) {
		return false;
	}

	private class DuaUfdLayout extends ColumnLayout {
		@Override
		public int getColumnCount(final boolean in) {
			return 1;
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			try {
				item.getTextValue("Wert").setText(row.get(realCol));
			}
			catch(Exception e){}
			if(!item.isDefined()) {
				item.getUnscaledValue("Wert").set(Integer.parseInt(row.get(realCol)));
			}
			item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 1));
			item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Interpoliert").setText(row.get(realCol + 2));
			String percent = row.get(realCol + 3);
			if(percent.endsWith("%")) {
				percent = percent.substring(0, percent.length() - 1);
			}
			percent = percent.replace(',', '.');
			item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
		}

		@Override
		public Collection<String> getIgnored() {
			return ImmutableList.of("T");
		}
	}
}
