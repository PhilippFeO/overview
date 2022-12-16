# HTML
Angelegt Freitag 28 Oktober 2022

Man kann prinzipiell alles „HTML-kodiert“, dh. als [HTML-Entität − Wikipedia](https://de.wikipedia.org/wiki/HTML-Entit%C3%A4t), darstellen. Manche Zeichen haben auch einen eigenen HTML-Namen, bspw ``&amp;`` für ``&``. Im Allgemeinen gilt folgende Syntax (``;`` nicht vergessen!):

* ``&#nnn;`` Dezimalkodierung (ohne führende Nullen)
* ``&#xhhhh;`` Hexadezimalkodierung


* [To HTML Entity − CyberChef](https://gchq.github.io/CyberChef/#recipe=To_HTML_Entity(true,'Named%20entities')) hilft vllt bei der Kodierung

@itsicherheit

* Eventuell können Filter dadurch überlistet werden, indem führende Nullen in ausreichender Stückzahl angegeben werden
* Eventuell können Filter, bspw. ``alert(…)``-Filter, umgangen werden, indem ``alert("Hello")`` als  [HTML-Entität − Wikipedia](https://de.wikipedia.org/wiki/HTML-Entit%C3%A4t) kodiert wird, vgl.

	<img src=x onerror="&#97;&#108;&#101;&#114;&#116;&lpar;&apos;&#72;&#101;&#108;&#108;&#111;&apos;&rpar;">

oder [IMG onerror and JavaScript Alert Encode − OWASP Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XSS_Filter_Evasion_Cheat_Sheet.html#img-onerror-and-javascript-alert-encode)
	

