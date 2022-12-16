# Backpropagation
Angelegt Freitag 09 September 2022

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
* ∇f(x): R^n^ → R, x' → ∇f(x) * x' = <∇f(x), x'> für m = 1
	* ∇f(x) ist (je nach Definition und Anwendung) ein Spalten- oder Zeilenvektor


Definitionen
------------

* [Backpropagation – Wikipedia](https://en.wikipedia.org/wiki/Backpropagation)
	* Evtl. sind die weiterführenden Links auch interessant
	* [Learning_and_Neural_Networks – Wikiversity](https://en.wikiversity.org/wiki/Learning_and_Neural_Networks)


j := Schicht j
a^j^ := Aktivierungsfunktion
z^j^ := meist Wx + b
C, E := Kosten- oder Verlustfunktion

Letzte bzw. nur eine Schicht
----------------------------
Netzwerk: E( y, a¹( z¹(W¹, x) ) )

* ∂E/∂W¹ = ∂E/∂a¹ * ∂a¹/∂z¹ * ∂z¹/∂W¹
* Man wertet den Ausdruck an den ursprünglich für W¹ eingesetzt Gewichten W¹* aus:

∂E/∂W¹|~W¹*~
= ∂E/∂a¹|~W¹*~ * ∂a¹/∂z¹|~W¹*~ * ∂z¹/∂W¹|~W¹*~
= E'(a¹(W¹*)) * a¹'(z¹(W¹*)) * z¹'(W¹*)

Mehrere Schichten
-----------------

* Möchte man nach den Gewichten tieferer/innere Schichten ableiten, muss man bachten, dass sich eine Solche auf viele weitere Schichten auswirkt. Man kann also nicht einfach a¹ durch a^k^ ersetzten.
* Auch hier wertet man die jeweiligen Ableitungen mit den Gewichten aus, die zuvor eingesetzt wurden, dh. wenn nach W² differenziert wurde, setzt man W²* aus der Vorwärtspropagation ein.
* Netzwerk:
	1. E( y, a³( z³(W³, a²( z²(W², a¹( z¹(W¹, x) ) ) ) )
	2. E( y, [a^k^( z^k^(W^k^) )]~L~ )
		* Der Teil in eckigen Klammern wird de facto L-mal wiederholt und entspricht einer Schicht
* ad a.:
	* ∂E/∂W^3^ wie oben
	* ∂E/∂W¹ nun anders, da W¹ die nachfolgenden Schichten beeinflusst
	* ∂E/∂W¹ = ∂E/∂a¹ * ∂a¹/∂z¹ * ∂z¹/∂W¹
		* ∂a¹/∂z¹, ∂z¹/∂W¹ sind trivial/wie oben
		* ∂E/∂a**¹** =

∂E/∂a³ * ∂a³/∂z³ * ∂z³/∂a**¹** +
∂E/∂a² * ∂a²/∂z² * ∂z²/∂a**¹**

* So muss es sein, weil man nicht Schicht 1 nach seiner eigenen Ausgabe ableitet, das wäre "davor". Man möchte das Netzwerk einschließlich Verlustfunktion nach der Ausgabe von Schicht 1 ableiten und die beeinflusst die **nachfolgenden** Schichten aber nicht die eigene. (Ich hatte bis zu dieser Stelle Probleme mit den Indizes und welche Schichten nach a¹ differenziert werden, bspw. rechnete ich ∂E/∂a¹ mit der Kettenregel aus aber ich bin immer wieder auf Zirkelschlüsse gestoßen.) 


* ad b.:
	* ∂E/∂W^L ^ wie (ganz) oben ∂E/∂W¹
	* ∂E/∂W^k ^, da innere Schicht ähnlich zum obigen Beispiel
	* ∂E/∂W^k^ = ∂E/∂a^k^ * ∂a^k^/∂z^k^ * ∂z^k^/∂W^k^
		* ∂E/∂a^k^ = ∑~m = k+1, …, L~ ∂E/∂a^m^ * ∂a^m^/∂z^m^ * ∂z^m^/∂a^k^
		* **Es wird ab der folgenden Schicht aufsummiert.** Den Ausdruck ∂E/∂a^m^ kann man einfach per Kettenregel bestimmen (so wie man das bei einer Schicht gemacht hat, nämlich indem man einfach den Ausdruck „auseinanderzieht“)
* Der „Trick“ bei der Ableitung nach W^k^ ist allgemein: ∂E/∂W^k^ dahingehend auseinanderziehen, dass man
	* Ableitung bzgl Aktivierungsfunktion **von Schicht k**
	* Ableitung der Aktivierungsfunktion bzgl z^k^ **von Schicht k**
	* Ableitung von z^k^ bzgl W^k^ **von Schicht k**

⇒ ∂E/∂W^k^ = ∂E/∂a^k^ * ∂a^k^/∂z^k^ * ∂z^k^/∂W^k^
hat, egal, wie viele Schichten danach noch folgen.

### Dimension der Ableitung

* ∂E/∂W^k^: R^dim(W^k)^ → R^?^, Matrix mit partiellen Ableitungen
	* ∂E/∂W^k^ **ist kein Gradient sondern eine Matrix**, bzw. die beste Linearisierung von E in der Variablen W^k^ (nicht in einem Punkt, da in diesem Ausdruck noch Nichts eingesetzt wird)
* ∂E/∂W^k^|~W^{k*~}: R^?^ → R^dim(W^k)^, Matrix mit konkreten Werten
	* Dieser Ausdruck wird für **Gradientenabstieg** verwendet









