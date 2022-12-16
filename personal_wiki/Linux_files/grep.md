# grep
Angelegt Mittwoch 16 Februar 2022


* Es können mehrere Dateien zum Durchsuchen auf einmal angegeben werden



``-n  ``Gibt Zeilennummer mit aus
``-A <num>  ``Gibt die folgenden ``<num>`` Zeilen nach einem Treffer aus
``-B <NUM>`` Gibt die vorhergehenden ``<NUM>`` Zeilen eines Treffers aus
``-C <NUM>`` Kombiniert ``-A`` und ``-B``, dh. Zeilen davor und danach
``-H  ``Ausgabe Dateiname/n
``-i  ``Groß-/Kleinschreibung wird ignoriert
``-m <num>``,`` --max-count=<num>`` Gibt die ersten ``<num>`` Treffer aus 
``-r  ``Alle Dateien in einem Verzeichnis rekursiv durchsuchen
Äquivalent: ``-d recurse``, bzw. ``--directories=recurse``
``-d <ACTION>, --directories=<ACTION>  ``Aktion, die für Verzeichnisse ausgeführt werden soll
``<ACTION>  ``read, skip, recurse
``-E``, ``--extended-regexp`` *Erweiterte reguläre Ausdrücke* können verwendet werden, s. [:Linux:**sed**](./sed.md)
``-P``, ``--perl-regexp`` Verwendung von ``PERL``*-reguläre Ausdrücke*, dazu gehört bspw. ``\d`` für ``[0-9] == [[:digit:]]`` 


regex
-----
@regex

19. auch [regex – MeinWiki.Python]()



* ``|  ``oder-Verknüpfung für reguläre Ausdrücke (s. Beispiele)
* ``\w  ``Irgendein „Wort“-Character TODO nachschauen, welche genau
* ``\s  ``ein Leerzeichen


### Beispiele
	 egrep "[aei]r$|se$" Spanisch.\ Vokabeln.csv
 
findet alle Wörter, die auf „ar“, „er“, „ir“ (``[aei]r``) enden (``$`` am Schluss) oder die auf „se“ enden, in Datei „Spanisch. Vokabeln.csv“ (de facto fast alle spanischen Verben in einer CSV-Datei)
OPTIONAL Datei verlinken.

	 egrep "([aei]r|se)($|\s)" Spanisch.\ Vokabeln.csv 
Etwas allgemeiner aber findet auch Formulierungen wie „einer“ oder „über“, also alles, was auf „er + <Leerzeichen> endet“. Notwendig, da obige Formulierung nicht „enfardarse con alguien“ trifft.


