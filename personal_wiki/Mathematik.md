# Mathematik
Angelegt Donnerstag 31 März 2022

Unterseiten
-----------

* [+3Blue1Brown](./Mathematik_files/3Blue1Brown.md)
* [+Deep Learning](./Mathematik_files/Deep_Learning.md)
* [+Funktionalanalysis](./Mathematik_files/Funktionalanalysis.md)
* [+Lebesgue-Integral](./Mathematik_files/Lebesgue-Integral.md)
* [+Lineare Algebra](./Mathematik_files/Lineare_Algebra.md)
* [+Markov-Ketten](./Mathematik_files/Markov-Ketten.md)
* [+Maßtheorie](./Mathematik_files/Maßtheorie.md)
* [+Numerik](./Mathematik_files/Numerik.md)
* [+Optimierung](./Mathematik_files/Optimierung.md)
* [+Wahrscheinlichkeitstheorie](./Mathematik_files/Wahrscheinlichkeitstheorie.md)
* [+Weitz](./Mathematik_files/Weitz.md)


Gescannte Niederschriften
-------------------------
[./Differential und Linienintegral.pdf](./Mathematik_files/Differential und Linienintegral.pdf) 
[./Linienintegral und Skalarfeld.pdf](./Mathematik_files/Linienintegral und Skalarfeld.pdf)

* [Kurvenintegral 1. & 2. Art – YouTube > Weitz](https://www.youtube.com/watch?v=7mrsZzXmibg)
* [Gebietsintegral – YouTube > Weitz](https://www.youtube.com/watch?v=u3qYaKv0Ffo)
* [Playlist Differentialgeometrie – YouTube > Weitz](https://www.youtube.com/watch?v=dFrSAXwDtlk&list=PLb0zKSynM2PD3i3xMuWrUF9_txMrJMGEZ)

[./Flächenelement.pdf](./Mathematik_files/Flächenelement.pdf) 
[./Integralrechnung Papula.pdf](./Mathematik_files/Integralrechnung Papula.pdf) 


* [Playlist Maßtheorie – YouTube > The Bright Side of Mathematics](https://www.youtube.com/watch?v=4DHP8cBcg_o&list=PLBh2i93oe2qskb2hCIR2HfO4ZFF1LXO1d)


Diverses
--------

* f:D → R, x → f(x), dann
	* f(x **+** 1): Verschieben des **Ursprungs nach rechts **um 1, bzw. des **Graphen nach links** um 1
	* f(x **-** 1): Analog nur nach links (Ursprung) und rechts (Graph)
	* f(x) **+** a: Verschieben des **Ursprungs nach unten** um 1, bzw. des **Graphen nach oben** um a
	* f(x) **-** a: Analog nur nach oben (Ursprung) und unten (Graph)

In der Praxis, bspw. Differentialquotient (Variable ist h **nicht** x!):

* f(x~0~ + h) - f(x~0~):
	* f wird um x~0~ nach links und um f(x~0~) nach unten verschoben. Das bedeutet im Prinzip, dass der Punkt (x~0~, f(x~0~)) nun der neue Ursprung ist
	* Das Koordinatensystem wird um x~0~ nach rechts und f(x~0~) nach oben verschoben.


* **Potenzreihen** sind diff'bar, wenn sie ∀ x∈(-r, r) konvergiert, dh. ∑~n∈N~ a~n~x^n^ ist endlich. Sie wird dann gliedweise (per Summenregel) differenziert, bspw. kann man so die e-Funktion ableiten


Allgemeines zu Ableitungen
--------------------------
@ableitung @differenzieren @ableiten @differenzierbar

* f: R^n^ → R^m^, x → f(x)
* Df: R^n^ → R^m×n^, x → Df|~x~ = Df(x)
	* In Df stehen die jeweiligen partiellen Ableitungen (mit Variablen)
	* In Df(x) stehen die jeweiligen partiellen Ableitungen **ausgewertet am Punkt x**, dh. Df(x) ist eine „normale Matrix“
	* Df(x) ist die **Linearisierung** von f an der Stelle x, dh. die lineare Abbildung, die f am Punkt x bestmöglich beschreibt
* Df(x) = Df|~x~: R^n^ → R^m^, x' → Df(x) * x'
	* Df(x) * x' ist ein **Matrix-Vektor-Produkt**, da in Df(x) „ganz normal“ Zahlen stehen
* Für m = 1: ∇f(x): R^n^ → R, x' → ∇f(x) * x' = <∇f(x), x'>
	* ∇f(x) ist (je nach Definition und Anwendung) ein Spalten- oder Zeilenvektor


Integration
-----------

### Substitutionsregel und Koordinatentransformation

#### Koordinatentransformation
[Integration durch Substitution (Substitutionsregel) und Transformationssatz – Weitz (YouTube)](https://www.youtube.com/watch?v=geJ-36mnZ1I)
![](./Mathematik_files/pasted_image.png)

* Der Ausdruck φ'(x) dient als „Korrekturfaktor“ (s. Bild unten)
* Auf der rechten Seite muss man sich y als etwas vorstellen, dass von einer Variablen, hier x, abhängt.
* Man kann die Gleichung auch anderweitig formulieren:

∫~a~^b^ f(y) dy = ∫~φ⁻¹(a)~^φ⁻¹(b) ^f(φ(x)) · φ'(x) dx
(In diesem Fall muss φ^-1^ existieren. Das ist bei der Formulierung auf der Aufnahme nicht notwendig.)

* Die Umformulierung entspricht dann einer „Koordinatentransformation“ und wird bspw. bei Wegintegralen, dh. φ ist ein Weg γ, verwendet.

![](./Mathematik_files/pasted_image002.png)

* Würde man die Integrale ohne den Korrekturfaktor φ'(x) notieren, wäre das entsprechende Integral zu klein. Das kann man an dem Beispiel der Bildschirmaufnahme gut erkennen.

Hier wäre φ(x) = 2x (und φ^-1^(y) = 1/2y).
Um von „oben“ nach „unten“ zu gelangen, werden die Rechtecke der Riemann-Summen um den Faktor 1/2 gestaucht (bzw. um von „unten“ nach „oben“ zu gelangen mit 2 gestreckt). Das bedeutet, dass das ursprüngliche Rechteck mit der Fläche f(2x) * **2** * dx nun die Fläche f(2x) * dx hat, also kleiner ist. Um dieses Missstand zu beheben, ist ein Korrekturfaktor nötig und der lautet in diesem Fall 2, bzw. φ'(x). Durch die Multiplikation mit 2 bzw. φ'(x) wird die Funktion einfach doppelt so hoch.
Wäre φ(x) anders, wäre der Flächenzuwachs nicht so schön linear

* Eine andere Herangehensweise: Ausgangspunkt ist ∫~a²~^b²^ f(y) dy, wobei nun y durch x² ersetzt wird, dh. man hat nun ∫~a~^b ^f(x²) dx. Allerdings fehlt noch der Korrekturfaktor bei den Rechtecken (s. Bild unten).

![](./Mathematik_files/pasted_image003.png)
Unten haben wir die Höhe f(x²) und die Breite dx, ergo f(x²) dx. Oben haben wir die Höhe f(y) = f(x²) aber **nicht** die Breite dy. Wegen φ(x) = x² verändert sich diese in Abhängigkeit von x. (Im Video wurde an dieser Stelle eine Animation abgespielt, in der man sah, dass die unteren Rechtecke immer gleichbreit waren, während sich die oben in ihrer Breite kontinuierlich veränderten.)
Wir wüssten nun gerne in welchem Verhältnis die Breite oben zu der unten steht, dh. wie stehen dy/dx zu einander?
Durch die suggestive Leibnizschreibweise ist das nun leicht herauszufinden:
dy/dx = dx²/dx = 2x ⇔ dy = dx² = 2x dx 
Damit haben wir nun dy und für die obere Rechtecksfläche ergibt sich: f(y) dy = f(x²) 2x dx.

#### Transformationssatz
Dieser ist mit der obigen Ausführung hoffentlich leicht zu verstehen:
∫~A~ f(Φ(v)) · |det(DΦ(x))| dx = ∫~B~ f(w) dw
wobei v, w Vektoren und D der mehrdimensionale Ableitungsoperator ist, dh. DΦ ist die Jacobi-Matrix des Diffeomorphismus' Φ. Als Bild:
![](./Mathematik_files/pasted_image001.png)
Eine typische Anwendung für den Transformationsatz ist eine Integration über Kreis-förmigen Gebieten, indem man zu Polarkoordinaten übergeht. Wird auch in obigem Video (<https://www.youtube.com/watch?v=geJ-36mnZ1I>) erläutert.

* Man kann den Transformationssatz auch maßtheoretisch formulieren. s. dazu [Maßtheorie#**Maßtheoretischer Transformationssatz**](./Mathematik_files/Maßtheorie.md).
* [Transformationssatz − Wikipedia](https://de.wikipedia.org/wiki/Transformationssatz)


### Wegintegrale und Transformationssatz
Das Wegintegral ist für f: ℝ^n^ → ℝ und γ: [a, b] → ℝ^n^ wie folgt definiert:
∫~γ([a,b])~ f ds := ∫~a~^b^ f(γ(t)) · ||γ'(t)||~2~ dt
Das ist im Prinzip **fast** der *Transformationssatz* für Dimension 1 bzw. *Integration durch Substitution* − nur passt || · ||~2~ nicht so recht zu beidem. **Das liegt daran**, dass γ kein [Diffeomorphismus − Wikipedia](https://de.wikipedia.org/wiki/Diffeomorphismus) ist (γ bildet bspw. nicht in sich selbst ab).
Letztenendes passt aber doch alles unter einen Hut, da man beim *Transformationssatz* allgemeiner von der [(verallgemeinerte) Funktionaldeterminante − Wikipedia](https://de.wikipedia.org/wiki/Funktionaldeterminante) spricht. Man kann dann auch die Selbstabbildungseigenschaft von Φ, dh A,B ⊆R^m^, fallen lassen. Diese ist wie folgt für f: R^n^ → R^m^ (n,m nicht unbedingt gleich) definiert:
Ff(x) := √(det(Df(x)^T^ · Df(x)))
(A^T^A ist eine quadratische symmetrische Matrix für alle A)
(In Df(x) stehen die partiellen Ableitungen als Funktion, keine Zahlen, wie bei f'(x))
Die [(verallgemeinerte) Funktionaldeterminante − Wikipedia](https://de.wikipedia.org/wiki/Funktionaldeterminante) wird auch [Gramsche Determinante − Wikipedia](https://de.wikipedia.org/wiki/Gramsche_Determinante) genannt.
Mit diesem Hintergrund ergibt sich für das obige Wegintegral, wenn man den *Transformationssatz* mit γ anwendet:
∫~γ([a,b])~ f ds = ∫~a~^b^ f(γ(t)) · Fγ(t) dt = ∫~a~^b^ f(γ(t)) · ||γ'(t)||~2~ dt,
wobei Fγ(t) = ||γ'(t)||~2~, wie unten stehende Rechnung zeigt:
γ'(t) = Dγ(t) = [ x'(t) y'(t) ]^T^
⇒ Dγ(t)^T^ · Dγ(t) = x'^2^ + y'^2^
⇒ det(Dγ(t)^T^ · Dγ(t)) = x'^2^ + y'^2^
⇒ √(det(Dγ(t)^T^ · Dγ(t))) = √(x'^2^ + y'^2^) = ||γ'(t)||~2~

* [Wegintegral − Wikipedia](https://de.wikipedia.org/wiki/Kurvenintegral)


#### Beispiel, bzw. Substitutionsregel

* ∫~a~^b^ f(x) dx = ∫~u⁻¹(a)~^u⁻¹(b) ^f(x(u)) * x'(u) du

Die Variable x wird als Funktion von u ausgedrückt.
**Ziel** ist es durch eine geschickte Wahl von x(u) den Integranden zu vereinfachen

* Beispiel: ∫~a~^b^ √(1-x²) dx

x(u) := sin(u)
u(y) := arcsin(y)
x'(u) = dx/du ⇔ cos(u) = dx/du ⇔ dx = cos(u) du
⇒ ∫~a~^b^ √(1-x²) dx
= ∫~arcsin(a)~^arcsin(b)^ √(1-x(u)²) * x'(u) du
= ∫~arcsin(a)~^arcsin(b)^ √(1-sin²(u)) * cos(u) du
= ∫~arcsin(a)~^arcsin(b)^ √cos²(u) * cos(u) du
= ∫~arcsin(a)~^arcsin(b)^ cos²(u) du
![](./Mathematik_files/pasted_image005.png)

Faltung
-------
Gegeben seien zwei Funktionen f,g: ℤ/ℝ → ℤ/ℝ/ℂ. Die (diskrete) Faltung ist definiert als

* f*g(k) := ∑~n∈ℤ~ f(n)g(−n+k) = ∑~n∈ℤ~ f(n)g(k−n)
* f*g(x) := ∫~ℝ~ f(t)g(−t+x) dt = ∫~ℝ~ f(t)g(x−t) dt (Die Summen werden im Kontinuierlichen einfach Integralek)

Dabei wird g als *Faltungskern* oder *Filter* bezeichnet. Der Ausdruck −n+k=k−n, bzw. −t+x=x−t, im Argument von g bedeutet dabei:

* −n: Spiegelung [[Begründung für Spiegelung](#Mathematik)] an der y-Achse
	* Damit die Spiegelung nicht ins Gewicht fällt, verwendet man als Filter meist symmetrische Filter.
* +k: intuitives „Durchschieben” des Filters von links nach rechts.

[id: begründung-spiegelung][Begründung für Spiegelung]:

* Die Spiegelung hat ihren Ursprung in der Signalverarbeitung, da sie dort automatisch auftritt.
* Im diskreten 2d-Fall mit einer *Filtermatrix* kann man es auch erläutern (s. unten)
* „Spiegelung und + (dh. das eigentliche Verschieben des Graphen nach links)“ wird zu einem Verschieben nach rechts, vor allem bei symmetrischen Faltungskernen


* Mit einer Faltung möchte man verschiedenes erreichen:
	* Übertragen der Eigenschaften des *Faltungskerns* auf die Funktion, bspw. um eine nicht Stetige glatt zu machen
	* Extraktion von Merkmalen (Feature extraction), dh. man möchte bestimmte Elemente, bspw. ein Smiley, eines Bildes finden. Dazu nimmt man eine Matrix und beschreibt dieses Element in dieser, bspw. indem man es mit 1en und 0en Pixel-Art-mäßig nachbaut und diese Matrix dann über das Bild schiebt. Hohe Werte (in Relation zu den anderen Einträgen der Ergebnismatrix; in Relation deswegen, weil ein Bild/eine Matrix, die überall große Werte aufweist, dann diese auch im Ergebnis hat und dann bringen objektive hohe Zahlen nichts) sprechen dann dafür, dass dort das Element gut getroffen wurde.
		* Wenn man die *Filtermatrix* baut, muss man noch darauf achten, dass sie im Zuge der Faltung gespiegelt wird, s. [#2d-faltung-mit-filtermatrix](#Mathematik) oder man führt **bewusst** eine [Korrelation](#Mathematik) durch.
		* Dieses Prinzip funktioniert auch im Kontinuierlichen 1d-Szenario aber hier fällt mir kein anschauliches Beispiel ein.


### Gleitender Durchschnitt

* Mittels g = 1/μ(A) * 1~A~ (Indikator auf Menge A, durch x−t kann man A um x verschieben, s. unten) kann man per Faltung den gleitenden Durchschnitt einer Funktion f bzgl. A berechnen, dh. f*g(x) hat an jeder Stelle x den durchschnittlichen Wert, den f hat, wenn man f nur auf der Menge A um x herum betrachtet und dort den Durchschnitt (Fläche/Integral durch Maß der Menge) berechnet. Den Indikator kann man per x−t verschieben. Man kann A um x herum legen, indem man 1~A~(x-t) rechnet.

Algorithmisch ausgedrückt:

* Nimm f und x
* Nimm A, das für die Anschauung am besten symmetrisch ist, bspw. A = [x−1/3, x+1/3] oder eine Kugel B~x~(r), weil dann μ(A+x) = μ(A) gilt
* [id: indikator-umformung]Multipliziere f und g(x−t) = 1/μ(A) * 1~A~(x−t) = 1/μ(A+x) * 1~A+x~(−t) = 1/μ(A+x) * 1~A+x~(t) (Die Menge verschiebt man mit + nicht wie Graphen mit −; aus −t wird t wegen der Achsensymmetrie)

⇒ f ist also nur „um“ die Stelle x ungleich 0 (Die Anführungszeichen, weil „um“ nur dann Sinn macht, wenn die Menge in Abhängigkeit von x definiert wurde). Bspw. wird f mit A+x = [x−1/3, x+1/3] „links und rechts“ abgeschnitten.

* Berechne den Durchschnitt von f auf dieser Menge, dh Fläche/Integral durch Maß der Menge


* Als Integral, wobei 
	* 1~[−a, a]~(x−t) ≡ 1~[x−a, x+a]~(t) für eine Variable t und ein festes x (s. [#indikator-umformung](#Mathematik))
	* μ(A+x) = μ(A) (Translationsinvarianz von Maßen)

f*g(x) = ∫~ℝ~ f(t) * g(x−t) dt
  = ∫~ℝ~ f(t) * 1/μ(A+x) * 1~A+x~(t) dt, [#indikator-umformung](#Mathematik) & wenn A von einem Punkt abhängt
[id: formel]= 1/μ(A) * ∫~A+x~ f(t) dt
⇒ Gleitende Durchschnitte sind Faltungen mit dem entsprechenden Indikator

#### Verallgemeinerung

* nimmt man ein allgemeines g aber mit endlichem Träger supp(g) (dh. Menge der Nicht-Nullstellen hat Maß > 1), berechnet man auch einen gleitenden Durchschnitt aber gewichtet f gemäß g in diesem Fenster.
	* Man kann g wie folgt darstellen, um obiger [#formel](#Mathematik) näher zu kommen: g = 1/μ(supp(g)) * 1~supp(g)~ * g

f * g(x) = ∫~ℝ~ f(t) * 1/μ(supp(g)) * 1~supp(g)~(x−t) * g(x−t) dt
  = 1/μ(supp(g)) * ∫~supp(g)+x~ f(t) * g(x−t) dt

* Man kann sich ein allgemeines g wie eine Multiplikation mit dem Indikator vorstellen, um beliebige Kurven zu erhalten
* Um die Rechnung „schöner“ zu gestalten, nimmt man meist ein symmetrisches g mit Träger 1, dh. μ(supp(g)) = 1.


### 2d-Faltung mit Filtermatrix

* im 2d-Fall wird eine *(gespiegelte)* *Filtermatrix* von links nach rechts und oben nach unten über das Bild geschoben, überlappende Punkte multipliziert und die Produkte addiert (vgl. @cnn-Schicht in einem Neuralen Netz)
* Die Matrix wird aus folgendem Grund horizontal und vertikal gespiegelt: Angenommen, man würde das nicht machen, ergäbe sich folgende Rechnung, [id: korrelation]*Korrelation* genannt:

![](./Mathematik_files/Korrelation Filtermatrix Beispiel.jpg)
Hierbei wird die *Filtermatrix*, wie oben beschrieben, über das Bild geschoben aber **ohne** Spiegelung. Das Resultat entspricht der horizontal und vertikal gespiegelten *Filtermatrix*. Das ist jedoch unpraktisch (und auch unintuitiv), da man an sich möchte, dass sich der Filter „direkt“ im Resultat wiederfindet, vor allem wenn die *Filtermatrix* eine bestimmte Form, bspw. ein ✘ oder ein Smiley, beschreibt. Das kann man dadurch beheben, indem man die *Filtermatrix* erst horizontal und vertikal spiegelt und dann über das Bild schiebt. Diese Operation wird *Konvolution* oder *Faltung* genannt und entspricht dann einer Spiegelung mit anschließender *Korrelation*:
![](./Mathematik_files/Konvolution Faltung Filtermatrix Beispiel.jpg)
⇒ Die h. & v. Spiegelung nimmt man eher aus „Bequemlichkeit“ vor, damit das Resultat am Ende „schöner aussieht“.

* In einem Neuralen Netz mit @cnn-Schicht muss man während der Implementierung nicht auf die Spiegelung achten, weil das Netz die Gewichte, dh. die *Filtermatrix-Einträge* lernt. Erst, wann man eine Konfiguration lädt, muss die Spiegelung vornehmen, bevor man das Netz laufen lässt.


Stencil-Methoden
----------------

* Mit Hilfe von Taylor-Reihen kann man Ableitungen von Funktionen in Abhängigkeit von dieser selbst darstellen (vgl. [Two_Point_Stencil_Example − Wikipedia [en]](https://en.wikipedia.org/wiki/Compact_stencil#Two_Point_Stencil_Example)).
* f(x~0~∓h) bedeutet, dass man f auf einem Gitter mit Schrittweite h auswertet


