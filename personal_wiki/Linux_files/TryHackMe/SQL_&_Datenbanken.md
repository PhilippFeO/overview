# SQL & Datenbanken
Angelegt Samstag 22 Oktober 2022

Allgemeines
-----------

Syntax
------
``SELECT * FROM <TABELLE>`` Gibt die ganze Tabelle aus

MySQL
-----

* Normalerweise greift man nicht per Bruteforse direkt ``MySQL`` an sondern probiert Nutzername und Passwort an anderer Stelle zu finden
* In ``MySQL`` sind die Begrifft ``schema`` & ``database`` Synonyme, bspw bei Oracle Database-Produkten nicht (dort ist ``scheme`` sowas wie eine Teilmenge einer ``database``)
* In [Network Services 2 − TryHackMe](https://tryhackme.com/room/networkservices2) wird ein beispielhafter Exploit mit Metasploit beschrieben


SQLite
------

* Wird per ``sqlite3 DATENBANK`` gestartet

``.tables`` Zeigt alle Tabellen(namen-) an
``PRAGMA table_info(TABELLENNAME);`` (Semikolon!) Zeigt *Spaltenname*, *Datentypen* *und noch 2 Infos, die ich gerade nicht zuordnen kann* an



