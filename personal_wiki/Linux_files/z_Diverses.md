# z Diverses
Angelegt Dienstag 25 Oktober 2022

Portfreigabe
------------

* in Fritzbox Port X freigeben (nur ipv6)
* im Terminal:
	1. ``sudo ufw allow <ANWENDUNG>`` oder ``ufw allow <PORT>/<PROTOKOLL>``
	2. ``sudo ufw reload`` ([ufw − UbuntuUsers-](https://wiki.ubuntuusers.de/ufw/)

Bsp:
	sudo ufw allow ssh
	sudo ufw allow 8080/tcp

...benötigen beide davor eine Freigabe in Fritzbox

* Port schließen:
	1. ``sudo ufw disable <ANWENDUNG>`` oder ``sudo disable <PORT>/<PROTOKOLL>``

Bsp: wie bei „sudo allow [...]“


