# Eigenwerte und -vektoren
Angelegt Dienstag 31 Mai 2022


* [Wissenswertes zur Eigenwertzerlegung – Wikipedia](https://en.wikipedia.org/wiki/Eigendecomposition_of_a_matrix#Useful_facts_regarding_eigenvalues)
* s. auch [Lineare Algebra](../Lineare_Algebra.md), um mehr über Basiswechsel zu erfahren.



* Da (vollbesetzte) Matrizen umständliche Objekte sind, möchte man sie vereinfachen, um mit ihnen besser arbeiten zu können. Dabei sind (Block-)Diagonalformen das Leichteste, was man erreichen kann. Um eine Matrix ``A``, die in diesem Fall als „normale“ lineare Abbildung interpretiert wird (vgl. oben!), zu transformieren, benötigt man ihre *Eigenwerte* und *Eigenvektoren*.
* [Eigenschaften von Eigenwerte & -vektoren − Wikipedia [de]](https://de.wikipedia.org/wiki/Eigenwertproblem#Eigenschaften)


### Berechnung

* ``Av = λv <=> (A-λE)v = 0 <=> det(A-λE) = 0``. Die Lösungen werden *Eigenwerte* genannt.
	* ``det(A-λE)`` beschreibt ein Polynom in ``λ`` und ist deswegen immer lösbar
* mit den *EW* können per ``(A-λE)v = 0`` die *EV* ``α``~``i``~ berechnet werden


### Basiswechsel

* die *EV* ``α``~``i ``~bilden eine Basis (ich glaube nicht immer, muss ich noch mal recherchieren aber vorerst tun sie das). Um Vektoren ``w`` bzgl. dieser Basis darzustellen berechnet man einfach (wie oben erklärt) ``w``~``neu``~`` = (α``~``1``~`` … α``~``n``~``) * w``.
* Erinnerung: für Matrix ``A`` gilt, da/wenn sie als „normale“ lineare Abblidung interpretiert wird:

``A``: Vek. in alter Basis → Vek. in alter Basis

* Mit den *EV* ``α``~``i``~ von ``A`` bildet man nun zwei Matrizen, die als **Transformationsmatrizen** aufgefasst werden und eine Abbildung bzgl. der Basis bestehend aus *EV* darstellt:

``T := Mat(α``~``1``~, ``…``, ``α``~``n``~``)``: Vek. in *EV*-Basis → Vek. in alter Basis (von ``A``)
``T``^``-1``^`` := Mat``^``-1``^``(α``~``1``~, ``…``, ``α``~``n``~``)``: Vek. in alter Basis (von ``A``) → Vek. in  *EV*-Basis
(Hier war ich verwirrt, warum in beiden Definitionen die *EV* ``α``~``i``~ auftauchen, während ich oben noch ``e``~``i``~ und ``f``~``i``~, bzw. „``2`` und ``1/2``“ verwende, und ich hatte die Vermutung einen Fehler gemacht zu haben aber dem ist nicht so. Sowohl oben als auch hier ist alles richtig. Leider ist es mir gerade zu kompliziert zu erläutern, wie ich meine Verwirrung legen konnte. Wenn man es in Ruhe durchdenkt und die Basen richtig einsetzt, man hat nur ``e``~``i``~ und ``f``~``i``~, bzw. „``2`` und ``1/2``“ zu Auswahl, kommt darauf.)
⇒ ``A`` kann man nun mit diesen Verknüpfen (Urbilder und Basen passen überall):
``D := T``^``-1``^`` * A * T``: Vek. in *EV*-Basis → Vek. in *EV*-Basis

* ``D`` ist dabei eine **Diagonalmatrix**, wobei die *EW* auf der Diagonale (entsprechend der Reihenfolge der *EV*) stehen.
* Um mit ``D`` zu arbeiten, muss man das „Umfeld“ in dem ``A`` gegeben und „bearbeitet“ werden soll in die *EV*-Basis transformieren. Das macht man indem man jeden Vek. ``w`` mit ``T``^``-1``^ multipliziert: ``w``~``neu``~`` := T``^``-1``^`` * w``. Dann kann man die neuen Vek. ``w``~``neu``~ „ganz normal“ mit ``D`` multiplizieren − da ``D`` eine **Diagonalmatrix** ist, klappt das auch besonders einfach und schnell 😊️.


