# regex
Angelegt Montag 06 Juni 2022


* Sehr ausführliche Dokumentation: <https://www.regular-expressions.info/tutorial.html>
* [Regex üben − regexr.com](https://regexr.com/)
* [Regular Expression – Wikipedia](https://en.wikipedia.org/wiki/Regular_expression)
* [what-is-a-non-capturing-group-in-regular-expressions − stackoverflow](https://stackoverflow.com/questions/3512471/what-is-a-non-capturing-group-in-regular-expressions)



Aus: [Regular Expression HOWTO – Python-Doku](https://docs.python.org/3/howto/regex.html)

* ``\w  ``Alphanumerische Zeichen, ``\w == [a-zA-Z0-9_]``
	* Alle Wörter: ``\w*``
* ``\W  ``Invertiert ``\w``, dh. alles **ohne** alphanumerische Zeichen, dh. Sonderzeichen wie ``=``, ``\``, ``.``, ``{``, etc., ``\W == [^a-zA-Z0-9_]``
* ``\d  ``Zahl, ``\d == [0-9]``
* ``\D  ``invertiert ``\d``, dh. alles ohne Zahlen, ``\d == [^0-9]``
* ``\s  ``Leerzeichen, Tabulatoren („White spaces“, also „weißem Raum“, dh Feldern in Monospace-Schriftarten, die „leer“ sind und deswegen „weiß“ erscheinen, weil auf weißem Grund geschrieben wird), ``\s == [ \t\n\r\f\v]``
* ``\S  ``Invertiert ``\s``, dh keine Leerzeichen, Tabulatoren bzw. „Weißraum“, also einfach alle Zeichen, sozusagen „``\w`` + Sonderzeichen“, ``\S == [^ \t\n\r\f\v]``
* ``.  ``Alle möglichen Zeichen
* ``^  ``invertiert Nachfolgendes innerhalb ``[...]``, wenn an **erster** Stelle, bspw. ``[^0-9]`` ist alles **außer** eine Zahl
	* Steht es nicht an der ersten, dann wird es als normales Zeichen behandelt


Look Arounds
------------
[Look Arounds – regular-expressions](https://www.regular-expressions.info/lookaround.html)
**Look Arounds** sind dazu da ein Muster unter Randbedingungen zu finden. Möchte man bspw. in einem „Suchen und Ersetzen“-Szenario alle „``\\onehot{} vector(?=(\.| ))``“ („vector“ gefolgt von „.“ oder „Leerzeichen“) durch „``\\onehot``“ ersetzen, würde man mit einer Suche, die „``.``“ oder „Leerzeichen“ inkludiert auch dieses Zeichen ersetzen und bräuchte zwei Schritte (einmal für „``.``“, einmal für „Leerzeichen“). Mit Look Arounds geht's in einem, weil der Teil innerhalb von ``(?=...)`` nicht bei der Suche markiert wird.
Allgemeine Syntax:
``MUSTER(?...)`` (Runde Klammern mit Fragezeichen

### Look Ahead
Syntax:
``MUSTER(?(=|!)MUSTER)`` (Runde Klammern mit ``?`` gefolgt von ``=`` od. ``!``)

* **negativ** „``q`` nicht gefolgt von ``u``“: ``q(?!u)``

Eselsbrücke: Ist das, was dem ``q`` folgt „nicht u (``!u``)“? Das ``!`` negiert of Ausdrücke, bspw. ``!true == false``, so auch hier: ``q`` nicht gefolgt von ``u``

* **positiv** „``q`` gefolgt von ``u``“: ``q(?=u)``

Eselsbrücke: Ist das, was dem ``q`` folgt „gleich ``u`` (``=u``)“?

### Look behind
Syntax:
``MUSTER(?<(=|!)MUSTER)`` (Runde Klammern mit ``?<`` gefolgt von ``=`` od. ``!``)
Rest wie bei „Look Ahead“

### Beispiele

* ``ADP(?=.*\n\d*\s*\w*\s*\w*\s*VERB)  ``markiert alle ``ADP`` in einer ``coNLL-U``-Datei, wenn das Wort danach vom Typ ``VERB`` ist („danach“ ist im Sinne einer ``coNLL-U``-Datei zu verstehen, die Tabellen-artig aufgebaut ist.

Etwas allgemeiner: ``NOUN(?=.*\n\d*\s*.*\s*.*\s*ADP)``, da auch „``_``“ als Tabelleneintrag zugelassen ist.


