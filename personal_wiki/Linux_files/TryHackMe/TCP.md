# TCP
Angelegt Freitag 21 Oktober 2022


* Ist ein Protokoll...


tcpdump
-------

* Startet „tcpdump listener“, insbesondere

	sudo tcpdump ip proto \\icmp -i eth0

für ICMP-Packete, dh. wir erhalten eine Meldung, falls uns jemand [:Linux:TryHackMe:**ping**](./ping.md)-Packete schickt.

* Kann dazu benutzt werden, um festzustellen, ob man vom ZIelrechner aus, den eigenen erreicht (``telnet``-Aufgabe in [Network Services − TryHackMe](https://tryhackme.com/room/networkservices), s. auch [:Linux:TryHackMe:**Netzwerk-Dienste**](./Netzwerk-Dienste.md)), um bspw. eine ``Reverse Shell`` zu starten.



