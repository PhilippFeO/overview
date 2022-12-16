# SUID
Angelegt Freitag 21 Oktober 2022


* Wenn Datei ``SUID``-Bit gesetzt hat, wird Datei (wenn sie ausführbar ist) mit den Rechten des Besitzers ausgeführt, auch wenn der, der sie ausführt, diese Rechte nicht hat, zB. bei einer Datei, die ``root`` gehört und das ``SUID``-Bit hat, kann ein normaler Nutzer sie praktisch mit ``root``-Rechten ausführen.
	* Kann man dazu nutzen eine Shell mit diesen Rechten zu erhalten
	* (Ein) Grobes Vorgehen: Wenn man bspw. bei ``NFS``-Servern Dateien hochladen kann, dann kann man a priori die Permissions/Rechte dieser kontrollieren (ist ja auf dem eigenen Rechner)

⇒ Man kann ein ``bash``-Executable, das ``root`` gehört, mit gesetztem ``SUID``-Bit, dh ``s``-Bit, hochladen❗️Dann meldet man sich, bspw. per [:Linux:**ssh**](../ssh.md), an und führt diese Datei aus und erhält, wegen der Besitzers ``root`` und des gesetzten ``SUID``-Bits, eine ``root``-Shell.

* Das ``SUID``-Bit kann man wie folgt setzen:

	chmod +s BASH-EXECUTABLE
		

* wird ``S`` (und nicht ``s``) angezeigt

![](./SUID/pasted_image.png)
wurde das ``SUID``-Bit gesetzt aber das Recht für die Ausführung fehlt, dh. es ist sinnlos, da man die Datei nicht (bspw. mit ``root``-Rechten) ausführen kann. Das ``S`` weist also auf ein „sinnloses“ ``SUID``-Bit hin.
Lösung: „``x``“ hinzufügen:
	chmod +x DATEI # Setzt „x“ für alle
	chmod o=x DATEI # Setzt „x“ nur für „andere“ („other“)
		

* **Ausführung** einer ``SUID``-Datei, bzw. eines ``SUID``-``bash``-Executables, am besten mit ``-p``. Diese Option erhält die Rechte, also das ``s``/``S``, bzw. das ``SUID``-Bit.


