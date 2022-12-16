# ssh
Angelegt Freitag 04 Februar 2022
[UbuntuUsers/SSH](https://wiki.ubuntuusers.de/SSH/)

ssh-Schlüsselpaar erstellen
---------------------------
``ssh-keygen -t rsa -b 2048 -C "<comment>"``

* ``-b ``Länge in Bits
* ``rsa`` Algorithmus (?)
* ``-C ``Kommentar


ssh-Schlüssel auf Server kopieren
---------------------------------
``ssh-copy-id -i ~/.ssh/<SCHLÜSSEL> <BENUTZER@SERVER-ADRESSE>``

.ssh/config bzw. Server speichern
---------------------------------
[UbuntuUsers/ssh-config](https://wiki.ubuntuusers.de/SSH/#ssh-config)
In [~/.ssh/config](file:///home/philipp/.ssh/config) kann man Server abspeichern, sodass man nicht mehr
``ssh BENUTZER@SERVER-ADRESSE``
eingeben muss, sondern nur noch
``ssh SERVER-NAME``,
bspw.
``ssh uni``.

* ``IdentityFile ``spezifiziert zu verwendende(n) Schlüssel
* ``IdentitiesOnly ``Es wird/werden **nur** der/die angegebene/n Schlüssel verwendent (dadurch ist es sicherer, weil der ssh-client sonst alle Schlüssel durchprobiert)

ssh-Server
----------

* Wenn man sich „openssh-server“ installiert, kann man per ssh auf den jeweiligen Rechner zugreifen. Dazu muss einfach

	ssh <Nutzer>@<IP-Adresse>

in das Terminal eines anderen Rechners eingegeben werden.

* Links:

<https://wiki.ubuntuusers.de/SSH/#Der-SSH-Server>
<https://www.youtube.com/watch?v=JZ7h5FxlTVg>



