# tmp
Angelegt Dienstag 06 Dezember 2022

∫~A⊆Ω ~X(ω) dP(ω) kann mit Dichte ausgerechnet werden, **wenn** X reellwertig, dh. X: Ω→ℝ, und dann einfach per ∫~X(A)~ f dμ.

* Ich glaube, X(A) sollte sich als Schnitt endlich vieler (halboffener) Intervalle (a, b) darstellen lassen, da die Definition nur für

P((-∞, a)) = P(X≤a) = ∫~-∞~^a^ f(x) dx
gegeben ist.

* Oder anders gesagt: Wenn man die Mengen nicht als Schnitte solcher Intervalle darstellen kann, kann man nicht die Dichte zum ausrechnen verwenden.


* Ich schätze, in der Praxis kommen nur reellwertige Dichtefunktionen vor
* Kennt man die Dichte einer Zufallsvariablen nicht, dann kann man mit ihr nicht praxisrelevant arbeiten. Alles, was ohne Dichte formuliert wird/wurde, hat einen theoretischen Hintergrund.
	* Bspw. T := Lebensdauer von elektronischen Bauteilen, T ist exponentialverteilt
		* Als Zufallsvariabel schön und gut, man kann als hinschreiben
		* Aber erst mit der Dichte kann man rechnen und „konkrete“ Ergebnisse bestimmen
		* Die Bestimmung der Dichte, ist dabei eine Frage für sich und kann meistens „nur“ mit Messwerten approximiert werden. In diesem Fall sind die Ereignisse ω die Bauteile und das Bild, dh. T(ω), aus ℝ, die gemessene Lebenszeit. Unter diesen Bedingungen ist praktisch natürlich zu einer Dichte und einem Riemann-Integral (keinem mit dP(ω)) überzugehen.


