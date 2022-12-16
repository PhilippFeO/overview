# JohnTheRipper
Angelegt Samstag 22 Oktober 2022

Beschreibung
------------

* Prinzip: Man übergibt dem Programm einen Passwort-Hash und eine Wortliste. Mit dieser probiert dann JohnTheRipper das Passwort zu brute-forcen
* [JohnTheRipper − Wikipedia [de]](https://de.wikipedia.org/wiki/John_the_Ripper)
* [JohnTheRipper − Wikipedia [en]](https://en.wikipedia.org/wiki/John_the_Ripper)



* Ist **nicht** für Login-Prozesse oder irgendetwas, das über das Internet läuft gedacht (dafür ist [:Linux:TryHackMe:Werkzeuge:**Hydra**](./Hydra.md) gedacht). Man benötigt den Passwort-Hash lokal bei sich, da er als Argument dient!
* ``ssh4john`` wandelt Passwort-geschützte [:Linux:**ssh**](../../ssh.md)-Schlüssel so um, dass man sie mit JohnTheRipper brechen kann
* Kann mit verteiltem Rechnen verwendet werden

Beispiel
--------
	cat pass.txt # user:AZl.zWwxIh15Q
	john -w:password.lst pass.txt
		# Loaded 1 password hash (Traditional DES [24/32 4K])
		# example         (user)
		# guesses: 1  time: 0:00:00:00 100%  c/s: 752  trying: 12345 - pookie

(Das Passwort lautet ``12345``)

