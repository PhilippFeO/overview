# HackerSploit – Linux Essentials For Hackers
Angelegt Freitag 25 Februar 2022
@linux @hacking

[HackerSploit: Linux Essentials For Hackers – YouTube-Playlist](https://www.youtube.com/playlist?list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by)

Tastenkombinationen für das Terminal
------------------------------------
<https://www.youtube.com/watch?v=44H-D1t2OCw&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=2>

* ``STRG + L  ``löscht Terminal (gleichbedeutend zur Eingabe ``clear``)
* ``STRG + A  ``Cursor an Zeilenanfang
* ``STRG + E  ``Cursor an Zeilenende
* ``STRG + U  ``löschen vom Anfang bis zum Cursor
* ``STRG + K  ``löschen vom Cursor bis zum Ende
* ``STRG + Z  ``unterbrechen (suspend) laufenden Prozess
* ``STRG + SHIFT + N  ``Neues Terminal an selber Stelle


Datei-Management & Manipulation
-------------------------------
<https://www.youtube.com/watch?v=oGj5JBBQLHs&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=3>

* ``ls -lh  h`` = ``h``uman readable; Zeigt Dateigrößen nicht in Bytes sondern Kilo-, Mega-, Giga-, ... Bytes an
* ``ls -lR  R`` = ``R``ecursively; Zeigt auch Inhalte der Unterordner


Datei- & Verzeichnis-Rechte
---------------------------
<https://www.youtube.com/watch?v=qIVR1OaIUpA&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=4>
``-rw-rw-r--  1 philipp philipp  96896 Feb 25 14:36  test.sh``
``permissions   owner   group``
Die ``permissions`` gliedern sich in drei dreier Gruppen:

* ``rw-  permissions`` des Owners
* ``rw-  permissions`` der Gruppe
* ``r--  permissions`` für alle anderen Nutzer

Wenn ich nicht als ``owner`` eingeloggt bin und zu einer Datei einer anderen Person navigiere (bspw. indem ich in sein Home-Verzeichnis wechsle, dann gelten für mich die letzten drei Permissions: ``r--``.


### Ändern der Permissions
Zum Ändern der Permission: ``chmod [a|g|o|u] ((+|-) | =) [r|w|x] <file>``
Zum rekursiven Ändern (Verzeichnis und alles, was es beinhaltet): ``-R``

#### Rechte hinzufügen
	chmod +x test.sh

``-rw-rw-r--`` ⇒ ``-rwxrwxr-x  ``**Alle** Nutzer können nun ausführen
	chmod u+x test.sh

``-rw-rw-r--`` ⇒  ``-rwxrw-r--  ``Nur gegenwärtiger Nutzer (``u``ser) (wenn er auch Owner ist?) kann ausführen
oder:
	chmod u=rwx test.sh

Gleich alles auf einmal spezifizieren
Statt ``u`` kann auch ``g``roup, ``a``ll oder ``o``ther bzw. mehrere aufeinmal ``chmod ua=rw test.sh`` verwendet werden.

#### Rechte entfernen
	chmod go-wx test.sh
	

#### Unterschied zwischen ``+``, ``-`` und ``=``

* ``=`` überschreibt gegenwärtige Rechte mit Neuen
* ``+``,``−`` Entfernen/Hinzufügen von Rechten auf Grundlage der gegenwärtigen Rechte


#### Rechte per Zahlen setzen („Octal-Mode“)

* ``r = 4``
* ``w = 2``
* ``x = 1``


	chmod 444 test.sh

Setzt für Owner, Group und Andere die ``r``-Erlaubnis

Möchte man mehrere Rechte auf einmal setzen, so **addiert** man die Werte einfach
	chmod 731 test.sh

setzt für
Owner ``r``,``w``, ``x``
Group ``w``, ``x``
Other ``x``

#### Sonderrechte
``s``/``S`` Datei läuft **immer** mit Rechten des Besitzers/der Gruppe (``SUID``/``SGID``-Bit); Kann für Privilege Escalation verwendet werden, indem man ein ``bash``-Executable mit ``SUID``/``SGID``-Bit hochlädt (bspw. per ``FTP`` oder ``NFS`` ([:Linux:TryHackMe:**Netzwerk-Dienste**](./TryHackMe/Netzwerk-Dienste.md))) und an anderer Stelle, bspw. nach einem [:Linux:**ssh**](./ssh.md)-Login ausführt.
Mehr dazu unter [:Linux:TryHackMe:**SUID**](./TryHackMe/SUID.md)


Datei- & Verzeichnis-Ownership
------------------------------
<https://www.youtube.com/watch?v=uK3HnkrhCW8&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=5>
``-rw-rw-r--  1 philipp philipp  96896 Feb 25 14:36  test.sh``
``permissions   owner   group``
Zum Ändern von ``owner``: ``chown <new owner> <file>``
Zum Ändern von ``group``: ``chgrp <new group> <file>``

grep & piping
-------------
<https://www.youtube.com/watch?v=U9SI-wYRD1M&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=6>

19. [Linux.grep](./grep.md)


locate-Befehl
-------------
<https://www.youtube.com/watch?v=C6E0kl1U0Vc&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=7>
``locate <file>``

* am besten mit [Linux.grep](./grep.md) nutzen


Enumerating Distribution & Kernel Information
---------------------------------------------
<https://www.youtube.com/watch?v=OIxu1TiXArQ&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=8>

* Rudimentäre Informationen über Betriebssystem-Version

``lsb_release -a``
![](./HackerSploit_–_Linux_Essentials_For_Hackers/pasted_image002.png)
oder
``cat /etc/issue``
``cat /etc/os-release``
``cat /etc/*release``

* Informationen über CPU

``lscpu``

* Informationen über den Kernel

``uname  u``nix ``name``
``-a  ``alle/allgemeine Informationen
``-s  ``Kernel
``-r  ``Kernel-Version
``-p  ``Instruction Set (x86
``-o  o``perating system

Find + OverTheWire Bandit Challenge
-----------------------------------
<https://www.youtube.com/watch?v=egnq3LlBpgI&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=9>
``[sudo]`` ``find <dir> -type (f | d | l) -name <name>``

* ``-iname  ``ignoriere Groß- & Kleinschreibung (statt ``-name``)
* ``-size <n>(c | k | M | G)  ``Dateigröße angeben (``c`` steht dabei für Bytes)
	* ``+<n>  ``Dateien, die größer sind
	* ``-<n>  ``Dateien, die kleiner sind
* ``-perm <Octal-Notation>  ``Nur Dateien mit bestimmten ``perm``issions
* ``-user <user>  ``Dateien, die Nutzer ``<user>`` gehören (``-group <group>`` analog)
* ``-executable  ``Nur ausführbare Dateien
* ``! <Suchmuster>  ``Kehrt Suchmuster um, dh, alle Dateien, die ``<Suchmuster> ``nicht entsprechen

Mögliche Suchmuster: Alles, was drüber steht, also ``-name``, ``-size``, ``-perm``, ``-executable``, ...

* ``-regex <Regex>`` Verwendet ``<Regex>`` zur Suche (im Vergleich zu ``ll | egrep <Regex>`` wird hier wirklich nur der Dateiname ausgegeben)
* ``-delete`` Löscht gefundene Dateien
	* Sollte am Ende stehen
	* Ggfl. erst ohne ``-delete`` ausführen und gefundene Dateien kontrollieren


Disk Usage
----------
<https://www.youtube.com/watch?v=fUJBmIIsqxk&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=11>

### du [dir ..]

* ``du`` steht für „``d``isk ``u``sage“

``du`` Größe aller Dateien im aktuellen Verzeichnis
``du -sh *`` Alle Ordner im aktuellen Verzeichni	
``du -sh .[^.]*`` wie oben nur für versteckte Dateien

### Optionen
``-s  ``(``s``ummarize) Zeigt nicht alle Dateien sondern nur die Verzeichnisse an
``-h  ``(``h``uman readable)
``-c``,`` --total  ``Zeile am Ende mit Summe aller Dateigrößen

### df
Zeigt Speicherverbrauch in Relation zum gesamten verfügbaren Speicher

* ``-h  ``(``h``uman readable)


File Compression & Archiving With tar
-------------------------------------
<https://www.youtube.com/watch?v=7kNDYaurgXA&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=12>
``tar [options] -f <archive name> [file | dir | ...]``

* ``-c  ``Erstelle ein Archiv (``c``ompress)
* ``-f <file>  ``Archiv-Date bennen; **muss** letzte Option sein, da alles Darauffolgende als Dateiname interpretiert wird (``f``ile)
* ``-p  ``Archiviere auch die ``p``ermissions
* ``-v  ``Zeigt zusätzliche Informationen an (``v``erbose)
* ``-x  ``Entpackt Archiv (e``x``tract); **überschreibt ohne zu fragen** vorhandene Datei mit selbem Namen
* ``-z  ``Komprimiere Archiv mit ``gzip``; Benennung des Archivs mit ``.tar.gz``


### Beispiele:
	tar -cvf Music.tar ~/Music/

	tar -xvf Music.tar ~/Schreibtisch/Musik

	tar -czf Music.tar.gz ~/Music/


* (Unter)Verzeichnisse ausschließen:

	tar --exclude='masterarbeit/.git' -czf masterarbeit.tar.gz masterarbeit


Users And Groups & Permissions With Visudo
------------------------------------------
<https://www.youtube.com/watch?v=XvyVjAvZ41c&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=13>

### Nutzer hinzufügen
``useradd``

* ``-m  ``Home-Verzeichnis mit Namen wird angelegt
* ``-c <comment>  ``Kommentar angeben
* ``-s /bin/<shell>  ``Angabe einer Standard-Shell


### Nutzer löschen
``userdel``

### Passwort ändern
``passwd <user>``

* Muss va. bei neuen Nutzern hinzugefügt werden
* Möchte man Passwort für aktuellen Nutzer ändern, kann ``<user>`` auch weggelassen werden


### visudo-Befehl
Hier steht irgendetwas mit Gruppen, Rechten; nicht genau verstanden
``root    ALL=(ALL:ALL) ALL``

1. ``ALL``: Man kann aus allen Sitzungen (lokal, remote) heraus ``root``-User werden
2. ``ALL``: Egal als welcher Nutzer man eingeloggt ist, man kann ``root``-Nutzer werden
3. ``ALL``: Egal in welcher Gruppe man sich befindet, man kann ``root``-Nutzer werden
4. ``ALL``: ``root``-Nutzer kann alle Befehlt ausführen

Anstatt dem 4. ``ALL`` kann man auch ``/usr/bin/apt-get`` und weitere Befehle peer LEerzeichen getrennt angeben, dann sind nur die PRogramme in diesem Verzeichnis möglich


Networking (ifconfig, netstat & netdiscover)
--------------------------------------------
<https://www.youtube.com/watch?v=8tgEsVdy4a8&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=14>

In diesem Video habe ich nicht so viel verstanden, weil ich mich mit dem ganzen Internet-Zeug noch nicht so gut auskenne. Bei 16:30 erwähnt HackerSploit weiterführende Tutorials.

	ip route show

``default via ``^``192.168.178.1``^`` dev ``^``wlp0s20f3``^`` proto dhcp metric 600 ``
``169.254.0.0/16 dev wlp0s20f3 scope link metric 1000 ``
``192.168.178.0/24 dev wlp0s20f3 proto kernel scope link src ``**~^{``192.168.178.46``~}**`` metric 600``

* ``192.168.178.1  ``IP-Adresse vom Router; Gateway
* ``wlp0s20f3  ``Interface
* **``192.168.178.46  ``**IP-Adresse des Rechners

	> ip addr
	
	# Ist irgendwie für Localhost
	1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
	    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
	    inet 127.0.0.1/8 scope host lo
	       valid_lft forever preferred_lft forever
	    inet6 ::1/128 scope host 
	       valid_lft forever preferred_lft forever
	
	# Ethernet-Schnittstelle
	2: enp43s0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc fq_codel state DOWN group default qlen 1000
	    link/ether 7c:8a:e1:b3:d4:43 brd ff:ff:ff:ff:ff:ff
	
	# WLAN-Schnittstelle
	3: wlp0s20f3: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default qlen 1000
	    link/ether 1c:c1:0c:f3:27:c1 brd ff:ff:ff:ff:ff:ff
	    inet 192.168.178.46/24 brd 192.168.178.255 scope global dynamic noprefixroute wlp0s20f3
	    	#^^^^^^^^^^^^^^ IPv4-Adresse
	       valid_lft 853307sec preferred_lft 853307sec
	    inet6 2001:16b8:b980:4300:fb4c:a7e7:3189:d2f0/64 scope global temporary dynamic 
	       valid_lft 6894sec preferred_lft 3294sec
	    inet6 2001:16b8:b980:4300:c876:f888:9a92:df9c/64 scope global dynamic mngtmpaddr noprefixroute 
	       valid_lft 6894sec preferred_lft 3294sec
	    inet6 fe80::26dd:73a1:d8d2:786d/64 scope link noprefixroute
	    	# ^^^^^^^^^^^^^^^^^^^^^^^^^ IPv6-Adresse
	       valid_lft forever preferred_lft forever


TOR & Proxychains
-----------------
<https://www.youtube.com/watch?v=tX053IqrHT0&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=15>

Dieses Video habe ich nicht so gut verstanden, weil ich mich nicht so gut mit TOR und Proxys auskenne.


* Es gibt einen Unterschied zwischen dem tor-Service und dem tor-Browser. Ersteren installiert man per

``sudo apt-get install tor``

Service And Process Management (HTOP & systemctl)
-------------------------------------------------
<https://www.youtube.com/watch?v=BBp8iyE8Mus&list=PLBf0hzazHTGMh2fe2MFf3lCgk0rKmS2by&index=16>

Ein **Dienst** startet sich unter bestimmten Umständen neu, ein **Prozess** nicht.

### top

* Listet laufende Prozesse mit Id (``PID``), Nutzer, CPU- & Speicherverbrauch, etc. auf
* interaktive Version von ``top``: ``htop``, muss aber nachinstalliert werden


### Prozesse

* Unterschied zw. **Prozessen **und **Diensten** (s. oben)
* **Prozesse** können mit ``ps`` inspiziert werden
* Ohne Argumente werden nur die der gegenwärtigen Shell-Sitzung angezeigt

	$ ps
	    PID TTY          TIME CMD
	  14363 pts/0    00:00:00 bash
	  14502 pts/0    00:00:00 ps
	

* für alle: ``ps -e`` (oder ``ps axu``)
* ``sudo kill <PID>  ``Beendet Prozess mit PID ``<PID>``


### Dienste
@service @dienst @systemctl 

* Unterschied zw. **Prozessen **und **Diensten** (s. oben)
* ``systemctl  ``listet alle Dienste
* werden mit ``systemctl`` (``system c``on``t``ro``l`` gestartet) bzw. ``sudo systemctl start <service>``
* ``systemctl status <service>  ``Status eines Dienstes anzeigen
* ``systemctl is-enabled <service>  ``Abfrage, ob ``<service>`` beim Hochfahren gestartet wird
	* Um dies zu realisieren: ``systemctl enable <service>``
	* Beim Hochfahren nicht (mehr) starten: ``systemctl disable <service>``
* ``systemctl reload <service>  ``Konfiguration wird neu geladen
* ``systemctl restart <service>``
* ``systemctl stop <service>``




