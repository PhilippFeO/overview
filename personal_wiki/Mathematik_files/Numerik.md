# Numerik
Angelegt Sonntag 13 November 2022


* [YouTube-Kanal mit Videos zu Finite Differenzen und Finite Elemente](https://www.youtube.com/channel/UCHZyhrpXXmbuyF8kzrsvgPg/videos)

Differenzenquotient
-------------------
@ableitung @differenzierbar @pdg @pde @gdl @ode
Es gibt drei Mögl. den *Differenzenquotienten* auszudrücken:

1. Vorwärts: ( f(x~0~ + h) − f(x~0~) ) / h
2. Rückwärts: ( f(x~0~) − f(x~0~ − h) ) / h
3. Zentral: ( f(x~0~ + h) − f(x~0~ − h) ) / 2h

Für ein kleines h, approximieren diese zudem die erste Ableitung f'(x~0~).
Mit dem dem vorwärts und rückwärts Differenzenquotienten, kann man die zweite Ableitung approximieren:
f''(x~0~) ≈ ( [ (f(x~0 ~+ h) − f(x~0~)) / h ] − [(f(x~0~) − f(x~0~ − h)) / h] ) \ h
= ( f(x~0~ + h) − 2f(x~0~) − f(x~0~ − h) ) / h²
Für die numerische Lösung von {partiellen, gewöhnlichen} Differenzialgleichungen sind beide Formeln unerlässlich.


