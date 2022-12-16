# Linux-Befehle (alte Version)

* ``find -name DATEI1 -fprint DATEI2``

Pfad von DATEI1 wird in Datei "DATEI2" geschrieben

* ``find -iname "*".pdf``

``= find -iname \*.pdf``
``= find -iname "*.pdf"``
sucht nach Dateien, die auf "pdf" enden, Majuskeln und Minuskeln werden ignoriert

* ``find -name Alge\*.pdf``

sucht nach Dateien, die mit "Alge" beginnen und auf ".pdf" enden

* ``find PFAD -name DATEI -ls``

sucht im Verzeichnis PFAD nach DATEI und listet die Ergebnisse; kein PFAD entspricht aktuelles Verzeichnis

Sonstiges
---------

* ``var1=$(COMMAND)``

schreibt Ausgabe von COMMAND in var1:
Bsp.: ``var1=$(find -name File.pdf)``
Pfad von File.pdf wird in var1 geschrieben

ip-Adresse
----------
wget <http://wieistmeineip.net> -q -O - | grep -Eo '\<:digit:{1,3}(\.:digit:{1,3}){3}\>'
	

Globale Variablen
-----------------
zum gegenwärtigen Zeitpunkt weiß ich noch nicht, ob und wann selbst angelegte Variablen nach einem Neustart noch zur Verfügung stehen

* ``env``

zeigt alle globalen Variablen an

* ``export var=DUMMY``

legt globale Variable mit Inhalt DUMMY an; kann ggfl zur Speicherung von Pfaden verwendet werden (die ein Skript/Programm starten; schöner: bash_aliases)

* ``echo $var``

anzeigen von var

* ``unset var``

löscht globale Variable

