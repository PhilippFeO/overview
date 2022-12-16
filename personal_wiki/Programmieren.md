# Programmieren
Angelegt Samstag 23 Juli 2022

LISP
----
Listen sind eigentlich cons-Zellen:
	(list 3 4) == (cons 3 (cons 4 nil))

<https://www.youtube.com/watch?v=EyhL1DNrSME>
<https://github.com/norvig/paip-lisp>

Big und Little Endian
---------------------

* Geben an, ob das niederwertigste Byte am Anfang oder am Ende stehen
* **Ende = Links**, da die Zahl von rechts nach links verarbeitet/gelesen wird!
* Im Beispiel wird die Ganzzahl ``16.909.060`` als 32-Bit-Integer-Wert gespeichert (hexadezimal: ``01020304``~``h``~)
* *Litte Endian* wird heutzutage hauptsächlich im ``x86``-Kontext verwendet


### Little Endian

* Das niederwertigste Byte steht am „Ende“ (s. oben)
* ``16.909.060`` = ``0x 04 03 02 01`` = ``01``~``h``~``02``~``h``~``03``~``h``~``04``~``h``~
* Bezeichnet das Ende der Zahldarstellung, das als erstes genannt wird (das „Kleine“)
* entspricht dem umgedrehten alltäglichen Zahlenaufbau, bzw. Tag:Monat:Jahr
* bessere Bennenung: „Little Startian“, weil die Zahl mit dem niederwertigen Byte startet
* Gut für Addition, Subtraktion & Multiplikation, da diese Operationen „hinten“, dh. an der kleinsten Stelle einer Zahl, starten und die Zahl also in der kanonischen Verarbeitungsrichtung gespeichert wird
* Division, Vergleich dagegen langsamer, diese starten vorne


### Big Endian

* Das höchstwertige Byte steht am „Ende“ (s. oben); entspricht dem natürlichen Zahlenaufbau
* ``16.909.060`` = ``0x 01 02 03 04`` = ``01``~``h``~``02``~``h``~``03``~``h``~``04``~``h``~
* Bezeichnet das Ende der Zahldarstellung, das als erstes genannt wird (das „Große“)
* entspricht dem alltäglichen Zahlenaufbau, bzw. Stunde:Minute:Sekunde
* bessere Bennenung: „Big Startian“, weil die Zahl mit dem höchstwertigen Byte startet
* Addition, Subtraktion & Multiplikation langsamer, da diese Operation an der kleinsten Zelle einer Zahl starten. Die Position zum Lesen der Zahl muss also davor (um ``#Bytes−1``) verschoben werden
* Division, Vergleich schneller, da diese mit dem höchstwertigen Byte starten
* leichter zu lesen, da Zahl in natürlicher Leserichtung gespeichert wird


