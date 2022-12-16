# ps (Prozesse)
Angelegt Dienstag 27 September 2022


* ``ps`` Prozesse anzeigen
* ``ps aux`` Prozesse aller Nutzer/des gesamten Systems anzeigen
* ``top`` ausführliche tabellarische Übersicht

``kill [SIGNAL] <PID>`` beendet Prozess mit Prozess-ID ``<PID>``
Man kann Signale (``SIG…`` = ``Sig``nal) verwenden, um Beenden weiter zu spezifizieren (mit ``-`` angeben, dh ``-SIGTERM``):

* ``SIGTERM`` Prozess beenden aber davor aufräumen („clean up“)
* ``SIGKILL`` Prozess ohne aufräumen („clean up“) beenden
* ``SIGSTOP`` Prozess stoppen/suspendieren


* ``STRG + Z``: Prozess in Hintergrund verschieben
	* ``fg`` Holt Prozess wieder in Vordergrund


Namensräume („Namespaces“)
--------------------------

* Sind dazu da, Resourcen aufzuteilen und Prozesse abzuschirmen
* Nur Prozesse im selben Namensraum sehen sich gegenseitig


``systemd`` und ``systemctl``
-----------------------------

* Alle Prozesse sind Kindprozesse von ``systemd`` (ist auch ein Prozess; sogar ein ``d``eamon)
* Per ``systemctl`` (``system c``on``t``ro``l``) kann man mit ``systemd`` interagieren
	* Syntax: ``systemctl [option] [service]``
		* ``option`` = {
			* ``start`` Started Prozess/Service
			* ``stop`` Stoppt Prozess/Service
			* ``enable`` Sorgt dafür, dass Prozess/Service beim nächsten Hochfahren des Rechners automatisch gestartet wird. Prozess/Service wird **nicht** gestartet, muss entweder manuell durch ``start``, Neustart oder per ``--now`` am Ende, dh nach ``[service]`` geschehen
			* ``disable`` Beendet das automatische Starten beim Hochfahren. Prozess/Service wird **nicht** beendet, muss per ``stop``, Herunterfahren oder ``--now`` am Ende geschehen 
	* Bsp: ``systemctl start apache2`` um einen Apache-Webserver zu starten



