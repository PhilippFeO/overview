# Lineare Algebra
Angelegt Dienstag 31 Mai 2022

**Centering Matrix**/**Zentrierungsmatrix**: <https://en.wikipedia.org/wiki/Centering_matrix>

Matrix-Vektor-Produkt
---------------------
Gegeben:
``v = (1 1), M = (2 0  ==> M * v = (2 2)``
``		0 2)``
Das Produkt ``M * v`` kann auf zwei Arten interpretiert werden (vgl. [Koordinatentransformation − Wikipedia [de]](https://de.wikipedia.org/wiki/Koordinatentransformation)):
Kurz gesagt:

1. normale Abbildung („Alias“)
2. Darstellung desselben Objektes bzgl einer anderen Basis („Alibi“)


ad 1.) ``M`` **bildet die Basisvektoren auf neue Basisvektoren ab**. In Folge dessen wird auch ``v`` auf einen neuen Vektor linear abgebildet (so, wie eine Funktion einen Wert neu abbildet; Eine lineare Abbildung ist dadurch eindeutig bestimmt, was sie mit den Basisvektoren anstellt). In diesem Fall werden die BV ``(1 0)``, ``(0 1)`` skaliert auf ``(2 0)``, ``(0 2)`` und damit ``v`` auf ``(2 2)``.

* Läge man das alte und das neue Koords. übereinander, so würden sich ``v`` und ``Mv`` **nicht überdecken** (hier: „``Mv`` ragt weiter hinaus“).
* Da man Matrix ``M`` in dieser Interpretation als „normale“ Abbildung betrachtet gilt hier für die Basen:

``M``: Vek. in alter Basis → Vek. in alter Basis
⇒ Es findet **kein** Basiswechsel statt
ad 2.) ``M * v = (2 2)`` stellt ``v`` bzgl. der Basis ``(1/2 0)``, ``(0 1/2)`` dar, dh.
``   M = (1/2 0     = (2 0``
``        0   1\2)``^``-1``^``    0 2)``,
⇒ ``M`` wird also als Inverse der Matrix aus den neuen Basisvektoren dargestellt. Da man die Inverse und damit das entsprechende Koords. nur schlecht ablesen kann, kann man es selten so schön wie hier darstellen. In Formeln:
Vektor ``w`` bzgl. neuer Basis ``f``~``1``~, ``f``~``2``~ darstellen: ``(f``~``1``~`` f``~``2``~``)``^``-1``^`` * w`` (die kleine ^``-1``^ beachten!)

* Läge man das alte und das neue Koords. übereinander, so würden sich ``v`` und ``Mv`` **überdecken**.
* Umgekehrt: Möchte man die Koordinaten eines Vektors ``w`` in einem anderen Koords. mit den Basisvektoren ``f``~``1``~, ``f``~``2``~ haben, muss man ihn mit ``(f``~``1``~`` f``~``2``~``)``^``-1``^ multiplizieren
* Allgemein gilt für Basiswechsel (``{e``~``1``~, ``…``, ``e``~``n``~``}`` alte Basis, ``{f``~``1``~, ``…``, ``f``~``n``~``}`` neue Basis):

``Mat(e``~``1``~, ``…``, ``e``~``n``~``)``: Vek. in neuer Basis → Vek. in alter ``e``-Basis
``Mat(f``~``1``~, ``…``, ``f``~``n``~``)``^``-1``^: Vek. in alter Basis → Vek. in neuer Basis (für die neue Basis müssen deren Basisvektoren invertiert werden)

* Möchte man Vektor ``w``, gegeben in (alter) Basis ``B``, bzgl der (neuen) Basis ``C`` darstellen, so muss man

``Mat(c``~``1``~``, …, c``~``n``~``)``^``-1``^`` * w``
rechnen.


Eigenwerte & -vektoren
----------------------

19. [Eigenwerte und -vektoren](./Lineare_Algebra/Eigenwerte_und_-vektoren.md)









