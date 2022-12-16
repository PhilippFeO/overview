# mkfifo (named pipes)
Angelegt Montag 31 Oktober 2022


* *Named Pipes* sind *Pipes* für verschiedene Prozesse über Prozess-, bzw. Terminalgrenzen hinweg.
* Kann dazu verwendet werden Schreibprozesse in temporäre Dateien zu unterbinden, die sonst für die Inter-Prozess-Kommunikation angelegt werden müssen, um Daten auszutauschen. Einer schreibt in die *Named Pipe*, der andere liest.

| (unnamed pipes)
-----------------

* Folgender Aufruf ist klar, verknüpft zwei Programme und geschieht in Rutsch/Terminal

	man mkfifo | grep "\-Z"


Die Frage ist nun, wie man das über Terminals/Prozesse hinweg realisieren kann − Antwort: *Named Pipes*

mkfifo (named pipes)
--------------------

* Syntax: ``mkfifo DATEIPFAD`` (bspw. ``mkfifo mypipe``)

![](./mkfifo_(named_pipes)/pasted_image.png)

* Für die *Named Pipe* benötigt man zwei Prozesse/Terminals. Einmal muss gelesen und einmal geschrieben werden, sonst passiert Nichts
	1. ``grep "\-Z" mypipe``
	2. ``man mkfifo > mypipe``

⇒ Per *Named Pipes* können Programme über Prozess-/Terminalgrenzen hinweg miteinander kommunizieren



