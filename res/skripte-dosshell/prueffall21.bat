@echo off

call ..\..\..\skripte-dosshell\einstellungen.bat

set cp=..\..\de.bsvrz.sys.funclib.bitctrl\de.bsvrz.sys.funclib.bitctrl-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.plformal-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.pllogufd-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.mweufd-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.mweufd-test.jar
set cp=%cp%;..\..\junit-4.1.jar

title Pruefungen SE4 - DUA, SWE 4.12

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.12
echo #
echo #  Testet die Mwe_Tpt_Lt_Ns_Fbz_Sensor Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.mweufd.Mwe_Tpt_Lt_Ns_Fbz_SensorJunitTester
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.12
echo #
echo #  Testet die MweFbtSensor Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.mweufd.MweFbtSensorJunitTester
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.12
echo #
echo #  Testet die MweNiSensor Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.mweufd.MweNiSensorJunitTester
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.12
echo #
echo #  Testet die MweSwSensor Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.mweufd.MweSwSensorJunitTester
pause


echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.12
echo #
echo #  Testet die MweWfdSensor Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.mweufd.MweWfdSensorJunitTester
pause


