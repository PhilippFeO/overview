# sed
Angelegt Donnerstag 27 Oktober 2022

Mit *sed* können Text-Datenströme, bspw. Ausgaben (per ``|``) von Programmen oder Dateien, bearbeitet werden, insbesondere wird *sed* für „Suchen und Ersetzen“ (mit [:Programmieren:**regex**]()) verwendet.

* An sich ist *sed* eine SKript-Programmiersprache, dh. es gibt (sowas wie) Schleifen, Verzweigungen und „Speicher für Variablen“ (*Hold Space*)
* verwendet „(POSIX-) Basic Regular Expressions (BRE)“, die GNU-Implementierung GNU-BREs
* [sed-Tutorium (bei UbuntuUsers gefunden)](https://tty1.net/sed-tutorium/sed-tutorium.html)
* [sed − Wikipedia](https://de.wikipedia.org/wiki/Sed_(Unix))


Funktionsweise
--------------

* Daten werden zeilenweise eingelesen und im *Pattern Space* gespeichert, dh. es findet sich immer nur **eine** Zeile im *Pattern Space*.
* Im *Pattern Space* wird nacheinander die Anweisungskette ausgeführt und am Ende ausgegeben/in Datei geschrieben
* Man kann per ``sed '/BEGINN/,/ENDE/ …``' einen Suchbereich definieren
	* ``,/ENDE/`` kann weggelassen werden
* Beim Ersetzen-Kommando ``s/<REGEX>/<ERSATZTEXT>/`` kann man (Teile der) Treffer in ``<REGEX>`` in ``<ERSATZTEXT>`` wiederverwenden, indem man die jeweilige Zeichkette in ``\(…\)`` einschließt. Das klappt 9 mal und auf die Treffer kann man per ``\1``, …, ``\9`` zugreifen. Beispiel:

	echo "I like cat and dog" | sed 's/\(cat\|dog\)/\1s/g'
	# I like cats and dogs


Basic Regular Expressions
-------------------------
@regex @regularexpression

* *sed* benutzt Basic Regular Expressions, dh
	* ``|``, ``+``, ``?`` sind **Zeichen**, **keine** Operatoren; *GNUsed* **kennt** diese Operatoren, wenn man vor sie ein ``\`` stellt **oder** man verwendet ``-r``
	* Dasselbe gilt für ``{``, ``}``, ``(``, ``)``, heißt, sie müssen mit ``\`` geschrieben werden, um bspw. als Multiplikator zu dienen
	* ``^``, ``$``, ``*`` sind **Zeichen**, wenn sie **nicht am Beginn**/**Ende**/**Beginn** einer Zeile oder Klammerausdrucks, bspw ``[^a-c]`` stehen
* *Erweiterte Reguläre Ausdrücke* sind die „Normalen“ und können per ``-r`` verwendet werden (davon ist aber abzuraten)


Syntax
------

1. ``sed '[BEREICH] { ANWEISUNG_1 … ANWEISUNG_n }' EINGABEDATEI > AUSGABEDATEI``
2. ``STREAM | sed 'ANWEISUNG_1 … ANWEISUNG_n``'


* Eine ``ANWEISUNG`` besteht meist aus einem Ersetzen-Kommando:

``s/<REGEX>/<ERSATZTEXT>/[gc…]``
Man kann statt ``/`` jedes beliebige Sonderzeichen verwenden, solange es in ``<REGEX>`` und ``<ERSATZTEXT>`` nicht vorkommt, bspw. ``&``, dh. ``:s/<REGEX>/<ERSATZTEXT>/g == :s&<REGEX>&<ERSATZTEXT>&g`` sind gleichbedeutend. Das ist vorallem dann sinnvoll, wenn in einem der beiden ``/`` vorkommt, bspw. bei Dateipfaden, da man diese sonst per ``\`` escapen müsste, was schnell zu einem unleserlichem ``<REGEX>`` führt.

Beispiele
---------

* Viele Beispiele unter [sed − Wikipedia](https://de.wikipedia.org/wiki/Sed_(Unix))
* ``sed -ne '/<RE>/p``' Gibt alle Zeichenketten, die ``<RE>`` beschreibt aus
* ``sed '/^[yz]/ { s/^\([yz]\)/(\1)/ s/alt/NEU/ }' EINGABEDATEI``

x alt		x alt
Beginn	Beginn
y alt		(y) NEU
Ende	Ende
z alt		(z) NEU

Operationen
-----------
Der eigentliche Bearbeitungsschritt steht am **Ende**, bspw. bei ``s/alt/NEU/g``. Es soll „gierig“ (``g``reedy) ersetzt werden.
Es gibt:

* ``g``
* ``p`` gibt *Pattern Space* aus (``p``rint)
	* Kann dazu führen, dass bearbeitete Zeilen doppel ausgegeben werden. Das kann mit ``-n`` verhindert werden. ``-n`` unterbindet das finale Ausgeben des *Pattern Spaces* (s. „Funktionsweise“).
* ``d`` löschen (``d``elete)



