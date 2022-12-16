# Markov-Ketten
Angelegt Donnerstag 03 Februar 2022


* Können gut per Übergangsmatrix beschrieben werden ⇒ Durch Potenzierung kann zukünftige Entwicklung vorausberechnet werden.
	* [Übergangsmatrix](https://de.wikipedia.org/wiki/Übergangsmatrix):
		* Zeilen- oder Spaltensumme = 1, oBdA: Zeilsumme = 1, dh **(zeilen-)stochastisch**
		* alle Einträge zw. 0 & 1
		* Hat EW 1 (und damit eine *stationäre Verteilung*)
		* T^*^ := lim~k → ∞~ (T^k^)~i,j~ = π~j~, dh. alle Elemente einer Zeile i von T^*^ sind gleich π~j~, wobei π eine *stationäre Verteilung* ist.
* (Links-)EV zum EW 1 heißen [stationäre Verteilung](https://de.wikipedia.org/wiki/Stationäre_Verteilung) und sind [Wahrscheinlichkeitsvektoren](https://de.wikipedia.org/wiki/Wahrscheinlichkeitsvektor), dh. (Zeilen-)Vektoren, Summe der Komponenten = 1, alle Komponenten zw. 0 & 1
	* [stationäre Verteilung](https://de.wikipedia.org/wiki/Stationäre_Verteilung): Es gibt eine Startverteilung π (Zeilenvektor), der sich im Zeitverlauf nicht ändert, dh. π * T^t^ = π für alle t \in N

⇒ π ist (Links-)EV zum EW 1 (denk kurz darüber nach, warum das so ist!).

* Eindeutig, wenn Markov-Kette **irreduzibel** (gibt ggfl. mehrere Bedingungen, lieber Wikipedia-Artikel durchlesen, um sicher zu gehen)


* Zeilenvektoren sind typisch in der Wahrscheinlichkeitstheorie


* Im Kontext von Markov-Ketten geben WS-Vektoren π~0~ an, mit welcher WS man sich in dem jeweilgen Zustand befindet, dh. i-ter Eintrag von π~0~ entspricht WS sich (am Anfang zum Zeitpunkt t=0) in Zustand i zu befinden.
	* Aufenthalts-WS zum Zeitpunkt t=1: Linksmultiplikation π~0~ * T = π~1~
	* Aufenthalts-WS zum Zeitpunkt t=k: Linksmultiplikation π~0~ * T^k^ = π~k~
* (Rechts-)EV zum EW 1: Absorptionswahrscheinlichkeiten im [absorbierenden Zustand](https://de.wikipedia.org/wiki/Absorbierender_Zustand) (= wird nach dem Betreten nie wieder verlassen)


Unterschiede zwischen Successor Representation und Markov-Ketten
----------------------------------------------------------------

* Beide bedienen sich des Konzepts der Übergangsmatrix.
* Bei Markov-Ketten wird durch Potenzierung die Vorhersage getroffen
* Bei SR durch M = ∑~t=1, ..., ∞~ γ^t^ * T^t^ = (E~n~ − γ * T )^-1^ (geometrische Reihe), also durch eine gewichtete Summe der Übergangsmatrizen


### Wichtige Wikipedia-Artikel

* <https://de.wikipedia.org/wiki/Übergangskern>
* <https://de.wikipedia.org/wiki/Übergangsmatrix>
* <https://de.wikipedia.org/wiki/Stationäre_Verteilung>


@Übergangsmatrix @Übergangswahrscheinlichkeit @Markovkette @Wahrscheinlichkeit @Wahrscheinlichkeitstheorie @StationäreVerteilung @SuccessorRepresentation

