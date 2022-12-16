# ftpFTP
Angelegt Freitag 21 Oktober 2022


* ``F``ile ``T``ransfer ``P``rotocol (Client-Server-Protokoll); Wird zum übertragen von Dateien über ein Netzwerk verwendet
* Zwei Kanäle (unverschlüsselt), damit man unabhängig von Datei-Transfer (wenn Datei groß ist oder Verbindung langsam) Befehle absetzen kann:
	* command/control-Kanal
	* data-Kanal
* Nach hergestellter Verbindung kann man ``FTP``-Befehle auf dem Server ausführen
	* Aktive Verb.: Client öffnet Port und wartet bis Server Verb. herstellt
	* Passive Verb.: Server öffnet Port udn wartet bis Client Verb. herstellt

ftp-Server
----------

* Per

	ftp

kann man sich mit einem FTP-Server verbinden. Es öffnet sich eine ``ftp>``-Konsole

* ``get``, ``mget DATEI`` lädt ``DATEI`` herunter

	get DATEI -
	
Zeigt den Inhalt an (wie ``cat``)

Enumeration FTP
---------------
@enumeration

