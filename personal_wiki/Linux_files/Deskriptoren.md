# Deskriptoren
Angelegt Montag 31 Oktober 2022

[File descriptor − Wikipedia [en]](https://en.wikipedia.org/wiki/File_descriptor)

* Ausführliche Erläuterungen: [what-does-2>=&1-mean − stackoverflow](https://stackoverflow.com/questions/818255/what-does-21-mean)



* Es gibt immer mindestens 3 Deskriptoren:
	* [id: stdin]``0`` = ``stdin`` ([stdin − Wikipedia [en]](https://en.wikipedia.org/wiki/Stdin))
	* [id: stdout]``1`` = ``stdout`` ([stdout − Wikipedia [en]](https://en.wikipedia.org/wiki/Stdout))
	* [id: stderr]``2`` = ``stderr`` ([stderr − Wikipedia [en]](https://en.wikipedia.org/wiki/Stderr))


Operationen mit Deskriptoren
----------------------------
``>&`` leitet Datenströme um, bspw.
``>`` ist ein Alias für ``1>``
	echo "Test" 1>&2 # stdout nach stderr
	echo "Test" 2>&1 # stderr nach stdout

In der Praxis bedeutet das (Datei ``A`` existiert **nicht**):

* Fehlermeldungen werden nicht weitergeleitet, weil der falsche Strom spezifiziert wurde

	cat A.txt > B.txt
	# cat: A.txt: Datei oder Verzeichnis nicht gefunden
	cat B.txt # keine Ausgabe, B.txt ist leer

Möchte man die Fehlermeldung speicher, benötigt man:
	cat A.txt 2> B.txt # keine Ausgabe
	cat B.txt
	# cat: A.txt: Datei oder Verzeichnis nicht gefunden


* Eine Umleitung auf ``stdin`` ist dann notwendig, wenn die Ausgabe (von ``stderr``) als Eingabe für ein anderes Programm dienen soll:



