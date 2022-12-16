# z Diverses
Angelegt Samstag 29 Oktober 2022

Insecure Deserialization
------------------------

* Wie in *Verteilte Systeme* & *Middleware − Cloud Computing*: Zum Versenden von Objekten werden diese *serialisiert*, abgeschickt und beim Empfänger *deserialisiert*.
	* So bspw. mit Passwörtern, um sie abzuspeichern
* Sicherheitslücke entsteht dadurch, dass die *Deserialisierung* schlecht gestaltet wurde und bspw. der Nutzereingabe blind vertraut wird. Wird das deserialisierte Objekt irgendwann abgerufen, wird dann der Schadcode ausgeführt.


