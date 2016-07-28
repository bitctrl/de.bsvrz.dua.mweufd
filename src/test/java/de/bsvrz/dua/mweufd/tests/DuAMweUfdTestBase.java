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

import de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD;
import de.bsvrz.dua.tests.DuATestBase;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenMessStelle;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.modell.DUAUmfeldDatenSensor;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Method;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class DuAMweUfdTestBase extends DuATestBase {
	protected VerwaltungMesswertErsetzungUFD _messwertErsetzungUFD;

	protected static String[] getUfdArgs() {
		return new String[]{"-KonfigurationsBereichsPid=kb.duaTestUfd"};
	}


	@Override
	protected String[] getConfigurationAreas() {
		return new String[]{"kb.duaTestUfd"};
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		_messwertErsetzungUFD = new VerwaltungMesswertErsetzungUFD();
		_messwertErsetzungUFD.parseArguments(new ArgumentList(DuAMweUfdTestBase.getUfdArgs()));
		_messwertErsetzungUFD.initialize(_connection);
	}

	@After
	public void tearDown() throws Exception {
		_messwertErsetzungUFD.getVerbindung().disconnect(false, "");
		super.tearDown();
	}
}
