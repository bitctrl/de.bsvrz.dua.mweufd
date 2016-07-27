[![Build Status](https://travis-ci.org/bitctrl/de.bsvrz.dua.mweufd.svg?branch=master)](https://travis-ci.org/bitctrl/de.bsvrz.dua.mweufd)
[![Build Status](https://api.bintray.com/packages/bitctrl/maven/de.bsvrz.dua.mweufd/images/download.svg)](https://bintray.com/bitctrl/maven/de.bsvrz.dua.mweufd)

# Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.12 Messwertersetzung UFD

Version: ${version}

## Übersicht

Aufgabe dieser Softwareeinheit ist es, die als implausibel gekennzeichneten logisch
plausibilisierten Messwerte von Umfelddatenmessstellen zu ersetzen (Details siehe [AFo-4.0],
S. 112f). Danach werden die Daten unter dem Aspekt MessWertErsetzung publiziert.


## Versionsgeschichte

### 2.0.0

Release-Datum: 31.05.2016

#### Neue Abhängigkeiten

Die SWE benötigt nun das Distributionspaket de.bsvrz.sys.funclib.bitctrl.dua in
Mindestversion 1.5.0 und de.bsvrz.sys.funclib.bitctrl in Mindestversion 1.4.0.

#### Änderungen

Folgende Änderungen gegenüber vorhergehenden Versionen wurden durchgeführt:

- Die SWE setzt keine Betriebsmeldungen mehr ab.
- Der Ersetzungsalgorithmus wurde vollständig überarbeitet. Für die einzelnen Datenarten
  werden die Ersatzwerte jetzt nach folgender Prioritätenabfolge bestimmt:
  
  – NI: Fortschreiben, Mittelwert, Ersatzsensor
  – NS: Fortschreiben, Ersatzsensor
  – FBZ: Fortschreiben, Ersatzsensor
  – WFD: Nebensensor, Fortschreiben, Mittelwert, Ersatzsensor
  – SW: Fortschreiben, Ersatzsensor
  – TPT: Fortschreiben, Ersatzsensor
  – LT: Fortschreiben, Mittelwert, Ersatzsensor
  – FBT: Fortschreiben, Ersatzsensor
 
  Für alle anderen Umfelddatenarten findet keine Messwertersetzung statt, alle Werte
  werden unverändert weitergeleitet. 
  Erklärung zu den einzelnen Verfahren:

  – Fortschreiben: Den letzen plausiblen Messwert mit 95% Güte maximal die
    parametrierte Zeit wiederholen
  – Mittelwert: Von den Messwerten von Vorgänger- und Nachfolgesensor den
    Mittelwert bilden und mit 90% der Güte publizieren
  – Ersatzsensor: Des plausiblen Messwert des Ersatzsensors mit 90% der Güte
    übernehmen
  – Nebensensor: Den Messwert eines beliebigen Nebensensors mit 100% der Güte
    übernehmen.

  Für Details und Randbedingungen zu den einzelnen Verfahren siehe Anwenderforderungen.

  - Die Werte von Nebensensoren werden jetzt unverändert weitergeleitet.
  - Werte des Ersatzsensors sowie von Vorgängern und Nachfolgern werden nur noch
    zum Ersetzen benutzt, wenn diese nicht selbst interpoliert sind.

- Die Gütefaktoren der Ersatzwerte wurde gemäß Anwenderforderungen fest kodiert.

#### Fehlerkorrekturen

Folgende Fehler gegenüber vorhergehenden Versionen wurden korrigiert:

- Das Flag Implausibel wird nun von den gebildeten Ersatzwerten in jedem Fall
  gelöscht bzw. nicht gesetzt.
- Beim Vergleich der ErfassungsIntervallDauer T wird jetzt nur noch mit Werten
  verglichen, die denselben Zeitstempel haben, wie der (implausible) Hauptsensorwert.

### 1.4.0

- Umstellung auf Java 8 und UTF-8

### 1.3.1

- Kompatibilität zu DuA-2.0 hergestellt

### 1.3.0

- Umstellung auf Funclib-BitCtrl-Dua

### 1.2.0

- Behandlung nicht unterstützter Sensorarten über die 'UmfeldDatenSensorUnbekannteDatenartException'
- benötigt SWE de.bsvrz.sys.funclib.bitctrl_FREI_V1.2.3.zip oder höher 
- Umstellung auf Maven-Build  
  
### 1.1.4

- Bug 1441 behoben.
  
### 1.1.3

- RuntimeException bei nicht freigegebenes implausibles Daten entfernt

### 1.1.2

- Überarbeitung
  
### 1.1.1
  
- Bash-Startskript hinzu

### 1.1.0

- Aenderungen an der Teststruktur

### 1.0.0

- Erste Auslieferung

## Bemerkungen

Diese SWE ist eine eigenständige Datenverteiler-Applikation, welche über die Klasse
de.bsvrz.dua.mweufd.vew.VerwaltungMesswertErsetzungUFD mit folgenden Parametern
gestartet werden kann (zusaetzlich zu den normalen Parametern jeder
Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)


## Kontakt

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
