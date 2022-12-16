# vim Skripte
Angelegt Donnerstag 09 Juni 2022

Selbstgeschriebene Skripte liegen in: [~/programmieren/skripte](file:///home/philipp/programmieren/skripte)

Debug
-----
Folgender Code hilft beim Debuggen ([Check if file exists bash – Stackoverflow](https://stackoverflow.com/questions/22720064/check-if-file-exists-bash)):
	export PS4="\$LINENO: "
	set -xv



* [execute a command before shutdown – unix.Stackexchange](https://unix.stackexchange.com/questions/48973/execute-a-command-before-shutdown) (Skript abhängig vom [Runlevel – Wikipedia](http://en.wikipedia.org/wiki/Runlevel) (``0`` = Shutdown, ``6`` = Reboot) ausführen). Dazu Symlink in ``/etc/rc[0-6].d/`` auf Skript, das in ``/etc/init.d/`` liegt, platzieren (dort liegen nur Symlinks). Das Skript **darf keine **``.sh``-Endung enthalten!

Weitere Erläuterung zu den ``Runlevel``s: [the rc0 d rc1 d directories in etc – unix.Stackexchange](https://unix.stackexchange.com/questions/83748/the-rc0-d-rc1-d-directories-in-etc):

* Bennennung: ``(K|S)[0-9]{2}\w*`` (Beginn mit ``K`` oder ``S``, dann eine Zahl, dann Name, **keine** Dateiendung wie ``.sh``)
	* ``K  ``Kill, dh. es wird beendet
	* ``S  ``Start, dh. es wird gestartet
	* Zahl: Gibt Reihenfolge an
	* In ``/etc/rc6.d/`` sollte nichts mit ``S``, bzw. nur ``K``-Skript stehen, da beim herunterfahren alles beendet und Nichts mehr gestartet werden soll


if-else, Conditions
-------------------
	if [ BEDINGUNG ]; then  # Die Leerzeichen sind wichtig!
		...
	else
		...
	fi


* Test, ob Datei oder Verzeichnis existiert:

	if [ -f DATEI ]; then
		...
	
	if [ -d VERZEICHNIS ]; then
		...
	


