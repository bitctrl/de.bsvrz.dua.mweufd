#!/bin/bash
source ../../../skripte-bash/einstellungen.sh

echo =================================================
echo =
echo =       Pruefungen SE4 - DUA, SWE 4.12 
echo =
echo =================================================
echo 

index=0
declare -a tests
declare -a testTexts

#########################
# Name der Applikation #
#########################
appname=mweufd

########################
#     Testroutinen     #
########################

testTexts[$index]="Testet die Mwe_Tpt_Lt_Ns_Fbz_Sensor Klasse"
tests[$index]="Mwe_Tpt_Lt_Ns_Fbz_SensorJunitTester"
index=$(($index+1))

testTexts[$index]="Testet die MweFbtSensor Klasse"
tests[$index]="MweFbtSensorJunitTester"
index=$(($index+1))

testTexts[$index]="Testet die MweNiSensor Klasse"
tests[$index]="MweNiSensorJunitTester"
index=$(($index+1))

testTexts[$index]="Testet die MweSwSensor Klasse"
tests[$index]="MweSwSensorJunitTester"
index=$(($index+1))

testTexts[$index]="Testet die MweWfdSensor Klasse"
tests[$index]="MweWfdSensorJunitTester"
index=$(($index+1))

########################
#      ClassPath       #
########################
cp="../../de.bsvrz.sys.funclib.bitctrl/de.bsvrz.sys.funclib.bitctrl-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-runtime.jar"
cp=$cp":../de.bsvrz.dua.plformal-runtime.jar"
cp=$cp":../de.bsvrz.dua.pllogufd-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-test.jar"
cp=$cp":../../junit-4.1.jar"

########################
#     Ausfuehrung      #
########################

for ((i=0; i < ${#tests[@]}; i++));
do
	echo "================================================="
	echo "="
	echo "= Test Nr. "$(($i+1))":"
	echo "="
	echo "= "${testTexts[$i]}
	echo "="
	echo "================================================="
	echo 
	java -cp $cp $jvmArgs org.junit.runner.JUnitCore "de.bsvrz.dua."$appname"."${tests[$i]}
	sleep 10
done


